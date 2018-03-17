package org.meltzg.edhd.hdfs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HDFSService implements IHDFSService {

	@Value("${edhd.hadoop.hdfsProxy}")
	private String fsName;

	@Value("${edhd.hadoop.hduser}")
	private String hdUser;

	@Value("${edhd.storageDir}")
	private String storageDir;

	@PostConstruct
	public void init() {
		System.setProperty("HADOOP_USER_NAME", hdUser);
	}

	@Override
	public HDFSLocationInfo getChildren(String path) throws IOException {
		Configuration conf = getConfiguration();
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

	@Override
	public boolean mkDir(String location, String newDir) throws IOException {
		Configuration conf = getConfiguration();
		FileSystem fs = FileSystem.get(URI.create(fsName), conf);
		Path path = new Path(fsName + "/" + location + "/" + newDir);

		return fs.mkdirs(path);
	}

	@Override
	public boolean delete(String path) throws IOException {
		Configuration conf = getConfiguration();
		FileSystem fs = FileSystem.get(URI.create(fsName), conf);
		Path hdPath = new Path(fsName + "/" + path);

		return fs.delete(hdPath, true);
	}

	@Override
	public boolean put(String location, MultipartFile file) throws IOException {
		Configuration conf = getConfiguration();
		FileSystem fs = FileSystem.get(URI.create(fsName), conf);

		UUID id = UUID.randomUUID();
		boolean success = false;

		try {
			Files.createDirectories(Paths.get(storageDir + "/" + id.toString()));
			File convFile = new File(storageDir + "/" + id.toString() + "/" + file.getOriginalFilename());
			file.transferTo(convFile);
			Path srcPath = new Path("file:///" + convFile.getAbsolutePath());
			Path destPath = new Path("/" + location + "/" + convFile.getName());
			fs.copyFromLocalFile(srcPath, destPath);
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.forceDelete(new File(storageDir + "/" + id.toString()));
		}

		return success;
	}

	private String removeFSName(Path path) {
		return path.toString().replace(fsName, "");
	}
	
	private Configuration getConfiguration() {
		Configuration conf = new Configuration();
		conf.set("fs.webhdfs.impl", org.apache.hadoop.hdfs.web.WebHdfsFileSystem.class.getName());
		return conf;
	}
}
