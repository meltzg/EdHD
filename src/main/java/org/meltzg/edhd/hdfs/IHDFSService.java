package org.meltzg.edhd.hdfs;

import java.io.IOException;

public interface IHDFSService {
	public HDFSLocationInfo getChildren(String path) throws IOException;
}
