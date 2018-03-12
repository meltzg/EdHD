package org.meltzg.edhd.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.meltzg.edhd.db.DBServiceBase;
import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService extends AbstractStorageService {

	private static final String PATH = "path";

	@Value("${edhd.storageDir}")
	private String storageDir;

	@PostConstruct
	public void init() throws Exception {
		super.init();
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME() +" (" + ID + " UUID, " + PATH + " TEXT, " + "PRIMARY KEY(id))";
		statement.executeUpdate(createUsers);
		conn.close();

		File sDir = new File(storageDir);
		if (!sDir.exists() && !sDir.mkdirs()) {
			throw new FileNotFoundException();
		}
	}

	@Override
	public UUID putFile(MultipartFile file) throws IllegalStateException, IOException {
		// The UUID is added to prevent name collision
		UUID id = UUID.randomUUID();
		File convFile = new File(storageDir + "/" + id.toString() + "-" + file.getOriginalFilename());
		file.transferTo(convFile);
		return putFile(convFile, id);
	}

	@Override
	public UUID putFile(GenJobConfiguration file) throws IOException {
		// The UUID is added to prevent name collision
		UUID id = UUID.randomUUID();
		String fileName = storageDir + "/" + id.toString() + "-config.json";
		file.marshal(fileName);
		return putFile(new File(fileName), id);
	}

	@Override
	public UUID putFile(File file) {
		UUID id = UUID.randomUUID();
		return putFile(file, id);
	}

	@Override
	public File getFile(UUID id) {
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(id, DBType.UUID));
		File file = null;
		try {
			ResultSet rs = executeQuery("SELECT path FROM " + TABLE_NAME() +" WHERE " + ID + " = ?;", params);
			if (rs.next()) {
				file = new File(rs.getString(1));
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	@Override
	public boolean deleteFile(UUID id) throws ClassNotFoundException, SQLException {
		if (id == null) {
			return true;
		}
		File file = getFile(id);
		boolean success = false;
		if (file != null) {
			success = file.delete();
			if (success) {
				deleteById(id);
			}
		}
		return success;
	}
	
	private UUID putFile(File file, UUID id) {
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		String path = file.getAbsolutePath();

		params.add(new StatementParameter(id, DBType.UUID));
		params.add(new StatementParameter(path, DBType.TEXT));
		try {
			int inserted = executeUpdate("INSERT INTO " + TABLE_NAME() +" (" + ID + ", " + PATH + ") VALUES (?, ?);", params);
			if (inserted > 0) {
				return id;
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
