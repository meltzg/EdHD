package org.meltzg.edhd.assignment;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.meltzg.edhd.storage.AbstractStorageService;
import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.meltzg.genmapred.conf.GenJobConfiguration.PropValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AssignmentService extends AbstractAssignmentService {

	private static final String DUEDATE = "dueDate";
	private static final String ASSIGNMENTNAME = "assignmentName";
	private static final String ASSIGNMENTDESC = "assignmentDesc";
	private static final String PRIMARYCONFIGLOC = "primaryConfigLoc";
	private static final String CONFIGLOC = "configLoc";
	private static final String PRIMARYSRCLOC = "primarySrcLoc";
	private static final String SRCLOC = "srcLoc";

	@Autowired
	private AbstractStorageService storageService;

	@PostConstruct
	public void init() throws Exception {
		super.init();
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME() + " (" + "id UUID, " + DUEDATE + " BIGINT, "
				+ ASSIGNMENTNAME + " TEXT, " + ASSIGNMENTDESC + " TEXT, " + PRIMARYCONFIGLOC + " UUID REFERENCES "
				+ storageService.TABLE_NAME() + ", " + CONFIGLOC + " UUID REFERENCES " + storageService.TABLE_NAME()
				+ ", " + PRIMARYSRCLOC + " UUID REFERENCES " + storageService.TABLE_NAME() + ", " + SRCLOC
				+ " UUID REFERENCES " + storageService.TABLE_NAME() + ", " + "PRIMARY KEY(id))";
		statement.executeUpdate(createUsers);
		conn.close();
	}

	@Override
	public UUID createAssignment(AssignmentSubmissionProperties props, MultipartFile primarySrc,
			MultipartFile secondarySrc) throws IOException {

		UUID id = UUID.randomUUID();

		UUID primarySrcLoc = null;
		UUID secondarySrcLoc = null;
		UUID primaryConfigLoc = null;
		UUID secondaryConfigLoc = null;

		Map<String, PropValue> primaryConfig = props.getPrimaryConfig();
		Map<String, PropValue> secondaryConfig = props.getConfig();

		if (primaryConfig != null) {
			primaryConfigLoc = storageService.putFile(new GenJobConfiguration(primaryConfig));
		}
		if (secondaryConfig != null) {
			secondaryConfigLoc = storageService.putFile(new GenJobConfiguration(secondaryConfig));
		}
		if (primarySrc != null) {
			primarySrcLoc = storageService.putFile(primarySrc);
		}
		if (secondarySrc != null) {
			secondarySrcLoc = storageService.putFile(secondarySrc);
		}

		String insertQuery = "INSERT INTO " + TABLE_NAME() + " (" + "id, " + DUEDATE + ", " + ASSIGNMENTNAME + ", "
				+ ASSIGNMENTDESC + ", " + PRIMARYCONFIGLOC + ", " + CONFIGLOC + ", " + PRIMARYSRCLOC + ", " + SRCLOC
				+ ") " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

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
	public boolean deleteAssignment(UUID id) {
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(id, DBType.UUID));
		try {
			ResultSet rs = executeQuery("SELECT " + PRIMARYCONFIGLOC + ", " + CONFIGLOC + ", " + PRIMARYSRCLOC + ", "
					+ SRCLOC + " FROM " + TABLE_NAME() + " WHERE " + ID + " = ?;", params);
			if (rs.next()) {
				deleteById(id);
				// delete associated files
				storageService.deleteFile((UUID) rs.getObject(PRIMARYCONFIGLOC));
				storageService.deleteFile((UUID) rs.getObject(PRIMARYSRCLOC));
				storageService.deleteFile((UUID) rs.getObject(CONFIGLOC));
				storageService.deleteFile((UUID) rs.getObject(SRCLOC));
				// TODO delete submissions
				// TODO delete HDFS content
				return true;
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<AssignmentSubmissionProperties> getAllAssignments() throws IOException {
		List<AssignmentSubmissionProperties> assignments = null;
		try {
			ResultSet rs = executeQuery("SELECT * FROM " + TABLE_NAME() + ";", null);
			assignments = new ArrayList<AssignmentSubmissionProperties>();

			while (rs.next()) {
				assignments.add(extractAssignmentProps(rs, false));
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return assignments;
	}

	@Override
	public AssignmentSubmissionProperties getAssignment(UUID id, boolean includeSecondary) throws IOException {
		AssignmentSubmissionProperties assignment = null;
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(id, DBType.UUID));
		try {
			ResultSet rs = executeQuery("SELECT * FROM " + TABLE_NAME() + " WHERE " + ID + " = ?;", params);
			if (rs.next()) {
				assignment = extractAssignmentProps(rs, includeSecondary);
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return assignment;
	}

	private AssignmentSubmissionProperties extractAssignmentProps(ResultSet rs, boolean includeSecondary)
			throws SQLException, IOException {
		UUID id = (UUID) rs.getObject(ID);
		Long dueDate = rs.getLong(DUEDATE);
		String name = rs.getString(ASSIGNMENTNAME);
		String desc = rs.getString(ASSIGNMENTDESC);
		Map<String, PropValue> primaryConfig = new HashMap<String, PropValue>();
		Map<String, PropValue> config = new HashMap<String, PropValue>();
		String primarySrcName = null;
		String srcName = null;

		UUID primaryConfigLoc = (UUID) rs.getObject(PRIMARYCONFIGLOC);
		UUID primarySrcLoc = (UUID) rs.getObject(PRIMARYSRCLOC);
		if (primaryConfigLoc != null) {
			GenJobConfiguration gConfig = new GenJobConfiguration(
					storageService.getFile(primaryConfigLoc).getAbsolutePath());
			primaryConfig = gConfig.getconfigProps();
		}
		if (primarySrcLoc != null) {
			primarySrcName = storageService.getFile(primarySrcLoc).getName();
		}
		if (includeSecondary) {
			UUID configLoc = (UUID) rs.getObject(CONFIGLOC);
			UUID srcLoc = (UUID) rs.getObject(SRCLOC);
			if (configLoc != null) {
				GenJobConfiguration gConfig = new GenJobConfiguration(
						storageService.getFile(configLoc).getAbsolutePath());
				config = gConfig.getconfigProps();
			}
			if (srcLoc != null) {
				srcName = storageService.getFile(srcLoc).getName();
			}
		}

		AssignmentSubmissionProperties assignment = new AssignmentSubmissionProperties(id, dueDate, name, desc,
				primaryConfig, config, primarySrcName, srcName);

		return assignment;
	}
}
