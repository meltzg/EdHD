package org.meltzg.edhd.submission;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.meltzg.edhd.assignment.AssignmentDefinition;
import org.meltzg.edhd.hadoop.IHadoopService;
import org.meltzg.edhd.storage.AbstractStorageService;
import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.meltzg.genmapred.runner.GenJobRunner;

public class SubmissionWorker implements Runnable {

	private UUID submissionId;
	private AssignmentDefinition definition;
	private StatusProperties statProps;
	private AbstractStorageService storageService;
	private AbstractSubmissionService submissionService;
	private IHadoopService hadoopService;
	private String workerDir;

	public SubmissionWorker(UUID submissionId, AssignmentDefinition definition, StatusProperties statProps,
			AbstractStorageService storageService, AbstractSubmissionService submissionService,
			IHadoopService hadoopService) throws IOException {
		super();
		this.submissionId = submissionId;
		this.definition = definition;
		this.statProps = statProps;
		this.storageService = storageService;
		this.submissionService = submissionService;
		this.hadoopService = hadoopService;

		this.workerDir = storageService.getStorageDir() + "/worker/" + submissionId.toString();
		FileUtils.forceMkdir(new File(this.workerDir));
	}

	@Override
	public void run() {
		boolean succeeded = true;

		try {
			// unzip source archives to worker dir
			if (this.definition.getPrimarySrcLoc() != null) {
				unzipFile(this.storageService.getFile(this.definition.getPrimarySrcLoc()));
			}
			if (this.definition.getSrcLoc() != null) {
				unzipFile(this.storageService.getFile(this.definition.getSrcLoc()));
			}
		} catch (IOException e) {
			statProps.setCompileInfo(StatusValue.FAIL, e.toString());
			updateStatus();
			succeeded = false;
		}

		String compiledJar = compileSrc();
		if (compiledJar == null) {
			succeeded = false;
		}

		if (succeeded) {
			// submit jar to GenMapred
			succeeded = submitJar(compiledJar);
		}

		if (succeeded) {
			statProps.setCompleteInfo(StatusValue.SUCCESS, "Submission completed successfully!");
			updateStatus();
		} else {
			statProps.setCompleteInfo(StatusValue.FAIL, "Submission failed.");
			updateStatus();
		}

		cleanup();
	}

	private void unzipFile(File srcFile) throws IOException {
		ZipFile zipFile = new ZipFile(srcFile);
		Enumeration<?> enu = zipFile.entries();
		while (enu.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) enu.nextElement();

			String name = this.workerDir + "/" + zipEntry.getName();
			long size = zipEntry.getSize();
			long compressedSize = zipEntry.getCompressedSize();
			System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n", name, size, compressedSize);

			// Do we need to create a directory ?
			File file = new File(name);
			if (name.endsWith("/")) {
				file.mkdirs();
				continue;
			}

			File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}

			// Extract the file
			InputStream is = zipFile.getInputStream(zipEntry);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = is.read(bytes)) >= 0) {
				fos.write(bytes, 0, length);
			}
			is.close();
			fos.close();

		}
		zipFile.close();
	}

	private String compileSrc() {
		String compiledJar = null;

		List<String> optionList = new ArrayList<String>();
		// set compiler's classpath to be same as the runtime's
		String classpath = hadoopService.getHadoopClasspath();
		if (classpath != null && classpath.length() > 0) {
			optionList.addAll(Arrays.asList("-cp", classpath));
		}

		Collection<File> srcFiles = FileUtils.listFiles(new File(this.workerDir), new SuffixFileFilter(".java"),
				TrueFileFilter.INSTANCE);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		CompilationTask task = compiler.getTask(null, manager, diagnostics, optionList, null,
				manager.getJavaFileObjectsFromFiles(srcFiles));

		boolean success = task.call();

		if (success) {
			// create jar
			Collection<File> classFiles = FileUtils.listFiles(new File(this.workerDir), new SuffixFileFilter(".class"),
					TrueFileFilter.INSTANCE);

			Manifest manifest = new Manifest();
			Attributes global = manifest.getMainAttributes();
			global.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
			global.put(new Attributes.Name("Created-By"), "submissionId: " + this.submissionId.toString());

			String jarName = this.workerDir + "/submission-" + this.submissionId.toString() + ".jar";

			File jarFile = new File(jarName);
			try {
				OutputStream os = new FileOutputStream(jarFile);
				JarOutputStream jos = new JarOutputStream(os, manifest);
				int len = 0;
				byte[] buffer = new byte[1024];
				for (File clazz : classFiles) {
					String jeName = clazz.getCanonicalPath().replace('\\', '/');
					jeName = jeName.replace(this.workerDir.replace('\\', '/'), "");
					if (jeName.indexOf('/') == 0) {
						jeName = jeName.substring(1);
					}
					JarEntry je = new JarEntry(jeName);
					jos.putNextEntry(je);
					InputStream is = new BufferedInputStream(new FileInputStream(clazz));
					while ((len = is.read(buffer, 0, buffer.length)) != -1) {
						jos.write(buffer, 0, len);
					}
					is.close();
					jos.closeEntry();
				}
				jos.close();
				compiledJar = jarFile.getAbsolutePath();
				statProps.setCompileInfo(StatusValue.SUCCESS, "Submission compiled!");
			} catch (IOException e) {
				success = false;
				statProps.setCompileInfo(StatusValue.FAIL, e.toString());
				updateStatus();
			}
		} else {
			StringBuilder errors = new StringBuilder();
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				errors.append(diagnostic.toString());
			}
			statProps.setCompileInfo(StatusValue.FAIL, errors.toString());
			updateStatus();
		}

		return compiledJar;
	}

	private boolean submitJar(String compiledJar) {
		// read configs, fill in submission specific info, save to worker dir
		File primaryConfigFile = storageService.getFile(definition.getPrimaryConfigLoc());
		File secondaryConfigFile = storageService.getFile(definition.getConfigLoc());
		GenJobConfiguration primaryConfig = null;
		GenJobConfiguration secondaryConfig = null;
		String primaryConfigWorkerPath = workerDir + "/primaryConfig.json";
		String secondaryConfigWorkerPath = workerDir + "/secondaryConfig.json";

		try {
			primaryConfig = new GenJobConfiguration(primaryConfigFile.getAbsolutePath());
			secondaryConfig = new GenJobConfiguration(secondaryConfigFile.getAbsolutePath());

			primaryConfig.setProp(GenJobConfiguration.JOB_NAME, "Submission " + submissionId.toString());
			primaryConfig.setProp(GenJobConfiguration.OUTPUT_PATH, "/submission/" + submissionId.toString());
			secondaryConfig.setProp(GenJobConfiguration.ARTIFACT_JAR_PATHS, compiledJar);

			primaryConfig.marshal(primaryConfigWorkerPath);
			secondaryConfig.marshal(secondaryConfigWorkerPath);
		} catch (IOException e) {
			statProps.setRunInfo(StatusValue.FAIL, e.toString());
			updateStatus();
			return false;
		}

		Configuration conf = hadoopService.getConfiguration();
		Tool runner = new GenJobRunner();
		 
//		// Create a stream to hold the output
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		PrintStream ps = new PrintStream(baos);
//		// IMPORTANT: Save the old System.out!
//		PrintStream old = System.out;
//		// Tell Java to use your special stream
//		System.setOut(ps);

		String[] args = { "--primary", primaryConfigWorkerPath, "--secondary", secondaryConfigWorkerPath };
		Boolean success = false;
		try {
			ToolRunner.run(conf, runner, args);
		} catch (Exception e) {
			// TODO Check HDFS for the SUCCESS file. sometimes the cluster barfs even when
			// things are good.
			success = false;
		}
		
//		System.out.flush();
//	    System.setOut(old);
//	    String output = baos.toString();
		
		if (success) {
			statProps.setRunInfo(StatusValue.SUCCESS, "Submission ran successfully!");
		} else {
			statProps.setRunInfo(StatusValue.FAIL, "Submission failed to run!");
		}
		
		updateStatus();
		
		return success;
	}

	private void cleanup() {
		try {
			FileUtils.forceDelete(new File(this.workerDir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateStatus() {
		try {
			submissionService.updateStatus(statProps);
		} catch (ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
