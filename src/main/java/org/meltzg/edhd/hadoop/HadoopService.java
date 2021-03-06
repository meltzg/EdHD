package org.meltzg.edhd.hadoop;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class HadoopService implements IHadoopService {

    @Value("${edhd.hadoop.defaultFS}")
    private String defaultFS;

    @Value("${edhd.hadoop.hduser}")
    private String hdUser;

    @Value("${edhd.hadoop.hdfsFilePreview}")
    private int previewLength;

    @Value("${edhd.storageDir}")
    private String storageDir;

    @PostConstruct
    public void init() {
        System.setProperty("HADOOP_USER_NAME", hdUser);
    }

    @Override
    public HDFSLocationInfo getChildren(String path) throws IOException {
        Configuration conf = getConfiguration();
        FileSystem fs = FileSystem.get(URI.create(defaultFS), conf);

        Path[] paths = new Path[1];
        paths[0] = new Path(path);

        FileStatus[] status = fs.listStatus(paths);
        List<HDFSEntry> children = new ArrayList<HDFSEntry>();
        if (status.length == 1 && removeFSName(status[0].getPath()).equals(path)) {
            // The requested path is a file
            return getChildren(removeFSName(status[0].getPath().getParent()));
        }
        for (FileStatus fstat : status) {
            children.add(getHDFSFileInfo(fstat));
        }

        HDFSLocationInfo locInfo = new HDFSLocationInfo(path, children);
        return locInfo;
    }

    @Override
    public HDFSEntry getHDFSFileInfo(String path) throws IOException {
        Configuration conf = getConfiguration();
        FileSystem fs = FileSystem.get(URI.create(defaultFS), conf);
        Path fsPath = new Path(defaultFS + "/" + path);
        return getHDFSFileInfo(fs.getFileStatus(fsPath));
    }

    @Override
    public boolean mkDir(String location, String newDir) throws IOException {
        Configuration conf = getConfiguration();
        FileSystem fs = FileSystem.get(URI.create(defaultFS), conf);
        Path path = new Path(defaultFS + "/" + location + "/" + newDir);

        return fs.mkdirs(path);
    }

    @Override
    public boolean delete(String path) throws IOException {
        Configuration conf = getConfiguration();
        FileSystem fs = FileSystem.get(URI.create(defaultFS), conf);
        Path hdPath = new Path(defaultFS + "/" + path);

        return fs.delete(hdPath, true);
    }

    @Override
    public boolean put(String location, MultipartFile file) throws IOException {
        Configuration conf = getConfiguration();
        FileSystem fs = FileSystem.get(URI.create(defaultFS), conf);

        UUID id = UUID.randomUUID();
        boolean success = false;

        try {
            Files.createDirectories(Paths.get(storageDir + "/" + id.toString()));
            File convFile = new File(storageDir + "/" + id.toString() + "/" + file.getOriginalFilename());
            file.transferTo(convFile);
            Path srcPath = new Path("file:///" + convFile.getAbsolutePath());
            Path destPath = new Path(defaultFS + "/" + location + "/" + convFile.getName());
            fs.copyFromLocalFile(srcPath, destPath);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.forceDelete(new File(storageDir + "/" + id.toString()));
        }

        return success;
    }

    @Override
    public Configuration getConfiguration() {
        Configuration conf = new Configuration();
        String hadoopConfDir = System.getenv().get("HADOOP_CONF_DIR");
        conf.addResource(new Path("file://" + hadoopConfDir + "/core-site.xml"));
        conf.addResource(new Path("file://" + hadoopConfDir + "/hdfs-site.xml"));
        conf.addResource(new Path("file://" + hadoopConfDir + "/yarn-site.xml"));
        conf.addResource(new Path("file://" + hadoopConfDir + "/mapred-site.xml"));

        return conf;
    }

    @Override
    public String getHadoopClasspath() {
        try {
            Process proc = Runtime.getRuntime().exec("hadoop classpath");
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String classpath = "";
            String tmp = null;
            while ((tmp = reader.readLine()) != null) {
                classpath += tmp;
            }

            String[] subPaths = classpath.split(":");
            Set<String> jarPaths = new HashSet<String>();
            for (String subPath : subPaths) {
                if (subPath.charAt(subPath.length() - 1) == '*') {
                    subPath = subPath.substring(0, subPath.length() - 1);
                }
                Collection<File> jars = org.apache.commons.io.FileUtils.listFiles(new File(subPath),
                        new SuffixFileFilter(".jar"), TrueFileFilter.INSTANCE);
                for (File jar : jars) {
                    jarPaths.add(jar.getAbsolutePath());
                }
            }
            classpath = String.join(":", jarPaths);
            return classpath;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isJobSuccessful(String outputPath) throws IOException {
        Configuration conf = getConfiguration();
        FileSystem fs = FileSystem.get(URI.create(defaultFS), conf);
        // job success is determined by the existance of the _SUCCESS file in the given outputPath
        Path successPath = new Path(defaultFS + "/" + outputPath + "/_SUCCESS");

        return fs.exists(successPath);
    }

    @Override
    public long lineCount(String path) throws IOException {
        Configuration conf = getConfiguration();
        FileSystem fs = FileSystem.get(URI.create(defaultFS), conf);
        Path fsPath = new Path(defaultFS + "/" + path);
        FileStatus[] status = fs.listStatus(fsPath);

        long count = 0;
        for (FileStatus stat : status) {
            if (stat.isFile()) {
                FSDataInputStream is = fs.open(stat.getPath());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while (reader.readLine() != null) {
                    count++;
                }
                reader.close();
            }
        }

        return count;
    }

    @Override
    public String getHDFSFilePreview(String path) throws IOException {
        StringBuilder preview = new StringBuilder();
        InputStream fileStream = getHDFSFile(path);

        if (fileStream != null) {
            BufferedReader in = new BufferedReader(new InputStreamReader(fileStream));

            String tmp = null;
            for (int i = 0; i < previewLength && (tmp = in.readLine()) != null; i++) {
                preview.append(tmp + "\n");
            }
        }
        return preview.toString();
    }

    @Override
    public InputStream getHDFSFile(String path) throws IOException {
        Configuration conf = getConfiguration();
        FileSystem fs = FileSystem.get(URI.create(defaultFS), conf);
        Path fsPath = new Path(defaultFS + "/" + path);

        FileStatus stat = fs.getFileLinkStatus(fsPath);
        if (stat.isFile()) {
            return fs.open(fsPath);
        }

        return null;
    }

    private String removeFSName(Path path) {
        return path.toString().replace(defaultFS, "");
    }

    private HDFSEntry getHDFSFileInfo(FileStatus stat) {
        String cPath = removeFSName(stat.getPath());
        String permissions = stat.getPermission().toString();
        String group = stat.getGroup();
        String owner = stat.getOwner();
        long size = stat.getLen();
        boolean isDirectory = stat.isDirectory();
        return new HDFSEntry(group, owner, permissions, cPath, size, isDirectory);
    }
}
