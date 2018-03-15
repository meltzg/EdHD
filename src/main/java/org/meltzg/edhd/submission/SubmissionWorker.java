package org.meltzg.edhd.submission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.meltzg.edhd.assignment.AssignmentDefinition;
import org.meltzg.edhd.storage.AbstractStorageService;

public class SubmissionWorker implements Runnable {

	private UUID submissionId;
	private AssignmentDefinition definition;
	private StatusProperties statProps;
	private AbstractStorageService storageService;
	private AbstractSubmissionService submissionService;
	private String workerDir;

	public SubmissionWorker(UUID submissionId, AssignmentDefinition definition, StatusProperties statProps,
			AbstractStorageService storageService, AbstractSubmissionService submissionService) throws IOException {
		super();
		this.submissionId = submissionId;
		this.definition = definition;
		this.statProps = statProps;
		this.storageService = storageService;
		this.submissionService = submissionService;

		this.workerDir = storageService.getStorageDir() + "/worker/" + submissionId.toString();
		FileUtils.forceMkdir(new File(this.workerDir));
	}

	@Override
	public void run() {

		try {
			// unzip source archives to worker dir
			if (this.definition.getPrimarySrcLoc() != null) {
				unzipFile(this.storageService.getFile(this.definition.getPrimarySrcLoc()));
			}
			if (this.definition.getSrcLoc() != null) {
				unzipFile(this.storageService.getFile(this.definition.getSrcLoc()));
			}
			boolean compileSuccess = compileSrc();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			cleanup();

		}
	}

	private void unzipFile(File zipFile) throws IOException {
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry entry = zis.getNextEntry();
		while (entry != null) {
			File newFile = new File(this.workerDir + "/" + entry.getName());
			FileOutputStream fos = new FileOutputStream(newFile);
			int len = 0;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			entry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}

	private boolean compileSrc() {
		Collection<File> srcFiles = FileUtils.listFiles(new File(this.workerDir), new SuffixFileFilter(".java"),
				TrueFileFilter.INSTANCE);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
		CompilationTask task = compiler.getTask(null, manager, null, null, null,
				manager.getJavaFileObjectsFromFiles(srcFiles));
		
		task.call();

		return false;
	}

	private void cleanup() {
		try {
			FileUtils.forceDelete(new File(this.workerDir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
