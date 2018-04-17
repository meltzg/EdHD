package org.meltzg.edhd.hadoop;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.springframework.web.multipart.MultipartFile;

public interface IHadoopService {
	public HDFSLocationInfo getChildren(String path) throws IOException;
	public HDFSEntry getHDFSFileInfo(String path) throws IOException;
	public boolean mkDir(String location, String newDir) throws IOException;
	public boolean delete(String path) throws IOException;
	public boolean put(String location, MultipartFile file) throws IOException;
	public String getDefaultFS();
	public Configuration getConfiguration();
	public String getHadoopClasspath();
	public boolean isJobSuccessful(String outputPath) throws IOException;
	public long lineCount(String path) throws IOException;
	public String getHDFSFilePreview(String path) throws IOException;
	public InputStream getHDFSFile(String path) throws IOException;
}
