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
import org.meltzg.edhd.storage.AbstractStorageService;
import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AssignmentService extends AbstractAssignmentService {

	private static final String DUEDATE = "dueDate";
	private static final String ASSIGNMENTNAME = "assignmentName";
	private static final String ASSIGNMENTDESC = "assignmentDesc";
	private static final String PRIMARYCONFIGLOC = "primaryConfigLoc";
	private static final String SECONDARYCONFIGLOC = "secondaryConfigLoc";
	private static final String PRIMARYSRCLOC = "primarySrcLoc";
	private static final String SECONDARYSRCLOC = "secondarySrcLoc";

	@Autowired
	private AbstractStorageService storageService;

	@PostConstruct
	public void init() throws Exception {
		super.init();
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE + " (" + "id UUID, " + DUEDATE + " BIGINT, "
				+ ASSIGNMENTNAME + " TEXT, " + ASSIGNMENTDESC + " TEXT, "
				+ PRIMARYCONFIGLOC + " UUID REFERENCES " + AbstractStorageService.TABLE + ", "
				+ SECONDARYCONFIGLOC + " UUID REFERENCES " + AbstractStorageService.TABLE + ", "
				+ PRIMARYSRCLOC + " UUID REFERENCES " + AbstractStorageService.TABLE + ", "
				+ SECONDARYSRCLOC + " UUID REFERENCES " + AbstractStorageService.TABLE + ", "
				+ "PRIMARY KEY(id))";
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
		GenJobConfiguration secondaryConfig = props.getConfig();

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

		String insertQuery = "INSERT INTO " + TABLE + " (" + "id, " + DUEDATE + ", " + ASSIGNMENTNAME + ", "
				+ ASSIGNMENTDESC + ", " + PRIMARYCONFIGLOC + ", " + SECONDARYCONFIGLOC + ", " + PRIMARYSRCLOC + ", "
				+ SECONDARYSRCLOC + ") " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

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

	@Override
	public List<AssignmentProperties> getAllAssignments() {
		return null;
	}

}
