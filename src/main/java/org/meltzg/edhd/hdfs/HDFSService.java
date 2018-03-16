package org.meltzg.edhd.hdfs;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HDFSService implements IHDFSService {
	
	@Value("${edhd.hadoop.fsname}")
	private String fsName;
	
	@Value("${edhd.hadoop.hduser}")
	private String hdUser;

	@Override
	public HDFSLocationInfo getChildren(String path) throws IOException {
		Configuration conf = new Configuration();
	    FileSystem fs = FileSystem.get(URI.create(fsName), conf);
	    
	    Path[] paths = new Path[1];
	    paths[0] = new Path(path);
	    
	    FileStatus[] status = fs.listStatus(paths);
	    List<HDFSEntry> children = new ArrayList<HDFSEntry>();
	    if (status.length == 1 && removeFSName(status[0].getPath()).equals(path)) {
	    	// The requested path is a file
	    	return getChildren(removeFSName(status[0].getPath().getParent()));
	    }
	    for (FileStatus fstat : status) {
	    	String cPath = removeFSName(fstat.getPath());
	    	String permissions = fstat.getPermission().toString();
	    	String group = fstat.getGroup();
	    	String owner = fstat.getOwner();
	    	boolean isDirectory = fstat.isDirectory();
	    	children.add(new HDFSEntry(group, owner, permissions, cPath, isDirectory));
	    }
	    
	    HDFSLocationInfo locInfo = new HDFSLocationInfo(path, children);
		return locInfo;
	}
	
	private String removeFSName(Path path) {
		return path.toString().replace(fsName, "");
	}
}
