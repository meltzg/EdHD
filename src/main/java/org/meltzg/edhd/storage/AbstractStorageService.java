package org.meltzg.edhd.storage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import org.meltzg.edhd.db.DBServiceBase;
import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.springframework.web.multipart.MultipartFile;

public abstract class AbstractStorageService extends DBServiceBase {
	public abstract UUID putFile(MultipartFile file) throws IllegalStateException, IOException;
	public abstract UUID putFile(GenJobConfiguration file) throws IOException;
	public abstract UUID putFile(File file);
	public abstract File getFile(UUID id);
	public abstract boolean deleteFile(UUID id) throws ClassNotFoundException, SQLException;
	
	public static String TABLE_NAME() {
		return "storage";
	}
}
