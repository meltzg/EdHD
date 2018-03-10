package org.meltzg.edhd.assignment;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.meltzg.edhd.db.DBServiceBase;
import org.meltzg.edhd.storage.IStorageService;
import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AssignmentService extends DBServiceBase implements IAssignmentService {

	@Autowired
	private IStorageService storageService;

	@PostConstruct
	public void init() throws Exception {
		super.init();
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		String createUsers = "CREATE TABLE IF NOT EXISTS assignments (" + "id UUID, " + "dueDate BIGINT, "
				+ "assignmentName TEXT, " + "assignmentDesc TEXT, " + "primaryConfigLoc UUID REFERENCES storage, "
				+ "secondaryConfigLoc UUID REFERENCES storage, " + "primarySrcLoc UUID REFERENCES storage, "
				+ "secondarySrcLoc UUID REFERENCES storage, " + "PRIMARY KEY(id))";
		statement.executeUpdate(createUsers);
		conn.close();
	}

	@Override
	public UUID createAssignment(AssignmentCreationProperties props, MultipartFile primarySrc,
			MultipartFile secondarySrc) throws IOException {

		UUID id = UUID.randomUUID();

		UUID primarySrcLoc = null;
		UUID secondarySrcLoc = null;
		UUID primaryConfigLoc = null;
		UUID secondaryConfigLoc = null;

		GenJobConfiguration primaryConfig = props.getPrimaryConfig();
		GenJobConfiguration secondaryConfig = props.getSecondaryConfig();

		if (primaryConfig != null) {
			primaryConfigLoc = storageService.putFile(primaryConfig);
		}
		if (secondaryConfig != null) {
			secondaryConfigLoc = storageService.putFile(secondaryConfig);
		}
		if (primarySrc != null) {
			primarySrcLoc = storageService.putFile(primarySrc);
		}
		if (secondarySrc != null) {
			secondarySrcLoc = storageService.putFile(secondarySrc);
		}

		String insertQuery = "INSERT INTO assignments (" + "id, " + "dueDate, " + "assignmentName, "
				+ "assignmentDesc, " + "primaryConfigLoc, " + "secondaryConfigLoc, " + "primarySrcLoc, "
				+ "secondarySrcLoc) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(id, DBType.UUID));
		params.add(new StatementParameter(props.getDueDate(), DBType.BIGINT));
		params.add(new StatementParameter(props.getName(), DBType.TEXT));
		params.add(new StatementParameter(props.getDesc(), DBType.TEXT));
		params.add(new StatementParameter(primaryConfigLoc, DBType.UUID));
		params.add(new StatementParameter(secondaryConfigLoc, DBType.UUID));
		params.add(new StatementParameter(primarySrcLoc, DBType.UUID));
		params.add(new StatementParameter(secondarySrcLoc, DBType.UUID));
		
		try {
			int inserted = executeUpdate(insertQuery, params);
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
