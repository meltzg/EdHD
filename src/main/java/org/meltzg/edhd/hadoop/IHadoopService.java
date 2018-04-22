package org.meltzg.edhd.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface for interacting with Hadoop and HDFS
 */
public interface IHadoopService {
    /**
     * @param path - location to retrieve info on
     * @return information on the children of the specified location in HDFS
     * @throws IOException
     */
    HDFSLocationInfo getChildren(String path) throws IOException;

    /**
     * @param path - location to retrieve info on
     * @return information on the requested file in HDFS
     * @throws IOException
     */
    HDFSEntry getHDFSFileInfo(String path) throws IOException;

    /**
     * Creates a new directory in HDFS
     * @param location - parent directory in HDFS to make a new directory under
     * @param newDir - name of the directory to create
     * @return true if successful
     * @throws IOException
     */
    boolean mkDir(String location, String newDir) throws IOException;

    /**
     * Recursively delete a location in HDFS
     *
     * @param path - path in HDFS to delete
     * @return true if successful
     * @throws IOException
     */
    boolean delete(String path) throws IOException;

    /**
     * Copys a file into HDFS
     * @param location - directory to put the file
     * @param file - file to transfer to HDFS
     * @return true if successful
     * @throws IOException
     */
    boolean put(String location, MultipartFile file) throws IOException;

    /**
     * @return a Hadoop Configuration object for this application's cluster
     */
    Configuration getConfiguration();

    /**
     * @return The contents of the Hadoop classpath from executing `hadoop classpath`
     */
    String getHadoopClasspath();

    /**
     * Determins if a job was successful given its output directory location
     *
     * @param outputPath - output path of job being checked
     * @return whether the job ran successfully
     * @throws IOException
     */
    boolean isJobSuccessful(String outputPath) throws IOException;

    /**
     * @param path - path to get a linecount for
     * @return Total line count for the file or directory content specified
     * @throws IOException
     */
    long lineCount(String path) throws IOException;

    /**
     * @param path - file oath to get a preview of
     * @return some portion of a file in HDFS as a String
     * @throws IOException
     */
    String getHDFSFilePreview(String path) throws IOException;

    /**
     * @param path - file to retrieve from HDFS
     * @return HDFS file as an input stream
     * @throws IOException
     */
    InputStream getHDFSFile(String path) throws IOException;
}
