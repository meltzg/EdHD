package org.meltzg.edhd.hdfs;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface IHDFSService {
	public HDFSLocationInfo getChildren(String path) throws IOException;
	public boolean mkDir(String location, String newDir) throws IOException;
	public boolean delete(String path) throws IOException;
	public boolean put(String location, MultipartFile file) throws IOException;
}
