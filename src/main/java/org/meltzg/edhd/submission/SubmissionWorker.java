package org.meltzg.edhd.submission;

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
import org.meltzg.genmapred.conf.GenJobConfiguration.PropValue;
import org.meltzg.genmapred.runner.GenJobRunner;
import org.meltzg.genmapred.validator.GenJobValidatorMapper;
import org.meltzg.genmapred.validator.GenJobValidatorReducer;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.*;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Executes an EdHD submission job and updates the submission's status
 */
public class SubmissionWorker implements Runnable {

    public static final String SUBMISSION_DIR = "/submission/";
    public static final String VALIDATION_DIR = "/validation/";

    private UUID submissionId;
    private AssignmentDefinition definition;
    private StatusProperties statProps;
    private AbstractStorageService storageService;
    private AbstractSubmissionService submissionService;
    private IHadoopService hadoopService;
    private String workerDir;
    private String compiledJar;

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

        compiledJar = compileSrc();
        if (compiledJar == null) {
            succeeded = false;
        }

        if (succeeded) {
            // submit jar to GenMapred
            succeeded = submitJar();
        }

        if (succeeded && !statProps.isValidation()) {
            StatusProperties validatorProps = submissionService.getValidatorStatProps(definition.getId());
            if (validatorProps != null && !submissionService.validatorPending(definition.getId())) {
                succeeded = validateSubmission(validatorProps);
            }
        }

        if (succeeded) {
            statProps.setCompleteInfo(StatusValue.SUCCESS, "Submission completed successfully!");
            updateStatus();
            if (statProps.isValidation()) {
                submissionService.revalidateSubmissions(definition);
            }
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
            try {
                compiledJar = createJarFromClassFiles("submission");
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

    private boolean submitJar() {
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
            primaryConfig.setProp(GenJobConfiguration.OUTPUT_PATH, SUBMISSION_DIR + submissionId.toString());
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

        // // Create a stream to hold the output
        // ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // PrintStream ps = new PrintStream(baos);
        // // IMPORTANT: Save the old System.out!
        // PrintStream old = System.out;
        // // Tell Java to use your special stream
        // System.setOut(ps);

        String[] args = {"--primary", primaryConfigWorkerPath, "--secondary", secondaryConfigWorkerPath};
        try {
            ToolRunner.run(conf, runner, args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean success;
        try {
            success = hadoopService.isJobSuccessful(primaryConfig.getProp(GenJobConfiguration.OUTPUT_PATH));
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }

        // System.out.flush();
        // System.setOut(old);
        // String output = baos.toString();

        if (success) {
            statProps.setRunInfo(StatusValue.SUCCESS, "Submission ran successfully!");
        } else {
            statProps.setRunInfo(StatusValue.FAIL, "Submission failed to run!");
        }

        updateStatus();

        return success;
    }

    private boolean validateSubmission(StatusProperties validatorProps) {
        GenJobConfiguration validator = new GenJobConfiguration();
        String outputPath = VALIDATION_DIR + submissionId.toString();
        validator.setProp(GenJobConfiguration.MAP_CLASS, GenJobValidatorMapper.class.getCanonicalName());
        validator.setProp(GenJobConfiguration.REDUCER_CLASS, GenJobValidatorReducer.class.getCanonicalName());
        validator.setProp(GenJobConfiguration.MAP_OUTPUT_KEY_CLASS, GenJobValidatorMapper.getOutputKeyClassName());
        validator.setProp(GenJobConfiguration.MAP_OUTPUT_VALUE_CLASS, GenJobValidatorMapper.getOutputValueClassName());
        validator.setProp(GenJobConfiguration.OUTPUT_KEY_CLASS, GenJobValidatorReducer.getOutputKeyClassName());
        validator.setProp(GenJobConfiguration.OUTPUT_VALUE_CLASS, GenJobValidatorReducer.getOutputValueClassName());
        validator.setProp(GenJobConfiguration.JOB_NAME, "Validation " + submissionId.toString());
        validator.setProp(GenJobConfiguration.OUTPUT_PATH, outputPath);

        PropValue inputPaths = new PropValue(SUBMISSION_DIR + validatorProps.getId().toString() + "/part*", true);
        inputPaths.append(SUBMISSION_DIR + submissionId.toString() + "/part*");

        validator.getconfigProps().put(GenJobConfiguration.INPUT_PATH, inputPaths);

        String validatorConfigPath = workerDir + "/validatorConfig.json";

        try {
            extractClassFile(GenJobValidatorMapper.class);
            extractClassFile(GenJobValidatorReducer.class);
            String validatorJar = createJarFromClassFiles("validation");
            validator.setProp(GenJobConfiguration.ARTIFACT_JAR_PATHS, validatorJar);
            validator.marshal(validatorConfigPath);
        } catch (IOException e) {
            statProps.setValidateInfo(StatusValue.FAIL, e.toString());
            updateStatus();
            return false;
        }

        Configuration conf = hadoopService.getConfiguration();
        Tool runner = new GenJobRunner();

        String[] args = {"--primary", validatorConfigPath};
        try {
            ToolRunner.run(conf, runner, args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean success;
        try {
            success = hadoopService.isJobSuccessful(outputPath);
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }

        if (!success) {
            success = false;
            statProps.setValidateInfo(StatusValue.FAIL, "Validation job failed!");
        } else {
            try {
                long differences = hadoopService.lineCount(outputPath);
                if (differences == 0) {
                    statProps.setValidateInfo(StatusValue.SUCCESS, "Validation complete!");
                } else {
                    success = false;
                    statProps.setValidateInfo(StatusValue.FAIL,
                            "Differences were found during validation.  Check your ouptput (" + SUBMISSION_DIR
                                    + submissionId.toString() + ") against the validator (" + SUBMISSION_DIR
                                    + validatorProps.getId().toString() + ")");
                }
            } catch (IOException e) {
                success = false;
                statProps.setValidateInfo(StatusValue.FAIL, "Validation job failed! " + e.getMessage());
            }
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

    private String createJarFromClassFiles(String jarPrefix) throws IOException {
        Collection<File> classFiles = FileUtils.listFiles(new File(this.workerDir), new SuffixFileFilter(".class"),
                TrueFileFilter.INSTANCE);

        Manifest manifest = new Manifest();
        Attributes global = manifest.getMainAttributes();
        global.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
        global.put(new Attributes.Name("Created-By"), "submissionId: " + this.submissionId.toString());

        String jarName = this.workerDir + "/" + jarPrefix + "-" + this.submissionId.toString() + ".jar";

        File jarFile = new File(jarName);
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
        return jarFile.getAbsolutePath();
    }

    private void extractClassFile(Class clazz) throws IOException {
        InputStream is = clazz.getResourceAsStream('/' + clazz.getName().replace('.', '/') + ".class");
        String classFilePath = clazz.getCanonicalName().replace('.', '/') + ".class";
        File classFile = new File(this.workerDir + "/" + classFilePath);
        FileUtils.forceMkdirParent(classFile);
        java.nio.file.Files.copy(is, classFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        is.close();
    }
}
