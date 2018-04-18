package org.meltzg.edhd.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface IHadoopService {
    HDFSLocationInfo getChildren(String path) throws IOException;

    HDFSEntry getHDFSFileInfo(String path) throws IOException;

    boolean mkDir(String location, String newDir) throws IOException;

    boolean delete(String path) throws IOException;

    boolean put(String location, MultipartFile file) throws IOException;

    String getDefaultFS();

    Configuration getConfiguration();

    String getHadoopClasspath();

    boolean isJobSuccessful(String outputPath) throws IOException;

    long lineCount(String path) throws IOException;

    String getHDFSFilePreview(String path) throws IOException;

    InputStream getHDFSFile(String path) throws IOException;
}
