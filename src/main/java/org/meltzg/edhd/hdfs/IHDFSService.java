package org.meltzg.edhd.hdfs;

import java.io.IOException;

public interface IHDFSService {
	public HDFSLocationInfo getChildren(String path) throws IOException;
	public boolean mkDir(String location, String newDir) throws IOException;
	public boolean delete(String path) throws IOException;
}
