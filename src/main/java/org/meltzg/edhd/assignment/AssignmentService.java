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
import org.meltzg.edhd.submission.AbstractSubmissionService;
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

	@Autowired
	AbstractSubmissionService submissionService;

	@PostConstruct
	public void init() throws Exception {
		super.init();
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME() + " (" + ID + " UUID, " + DUEDATE
				+ " BIGINT, " + ASSIGNMENTNAME + " TEXT, " + ASSIGNMENTDESC + " TEXT, " + PRIMARYCONFIGLOC
				+ " UUID REFERENCES " + storageService.TABLE_NAME() + ", " + CONFIGLOC + " UUID REFERENCES "
				+ storageService.TABLE_NAME() + ", " + PRIMARYSRCLOC + " UUID REFERENCES " + storageService.TABLE_NAME()
				+ ", " + SRCLOC + " UUID REFERENCES " + storageService.TABLE_NAME() + ", " + "PRIMARY KEY(" + ID + "))";
		statement.executeUpdate(createUsers);
		conn.close();
	}

	@Override
	public String TABLE_NAME() {
		return "assignments";
	}

	@Override
	public UUID createAssignment(AssignmentDefinition props, MultipartFile primarySrc, MultipartFile secondarySrc)
			throws IOException {

		UUID id = UUID.randomUUID();
		if (props.getId() != null) {
			id = props.getId();
		}

		return commitDefinition(props, primarySrc, secondarySrc, id, false);
	}

	@Override
	public UUID updateAssignment(AssignmentDefinition props, MultipartFile primarySrc, MultipartFile secondarySrc)
			throws IOException {
		AssignmentDefinition current = getAssignment(props.getId(), true);
		
		try {
			submissionService.deleteByUserAssignment("admin", current.getId(), true);
		} catch (ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<StatementParameter> params;

		// delete old src files if necessary
		if (current.getPrimarySrcLoc() != null) {
			if (props.getPrimarySrcName() == null) {
				params = new ArrayList<StatementParameter>();
				params.add(new StatementParameter(null, DBType.UUID));
				params.add(new StatementParameter(props.getId(), DBType.UUID));
				try {
					executeUpdate("UPDATE " + TABLE_NAME() + " SET " + PRIMARYSRCLOC + "=? WHERE " + ID + "=?;",
							params);
					storageService.deleteFile(current.getPrimarySrcLoc());
				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				props.setPrimarySrcLoc(current.getPrimarySrcLoc());
			}
		}
		if (current.getSrcLoc() != null) {
			if (props.getSrcName() == null) {
				params = new ArrayList<StatementParameter>();
				params.add(new StatementParameter(null, DBType.UUID));
				params.add(new StatementParameter(props.getId(), DBType.UUID));
				try {
					executeUpdate("UPDATE " + TABLE_NAME() + " SET " + SRCLOC + "=? WHERE " + ID + "=?;", params);
					storageService.deleteFile(current.getSrcLoc());
				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				props.setSrcLoc(current.getSrcLoc());
			}
		}
		// delete old configs
		params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(null, DBType.UUID));
		params.add(new StatementParameter(props.getId(), DBType.UUID));
		try {
			executeUpdate("UPDATE " + TABLE_NAME() + " SET " + PRIMARYCONFIGLOC + "=? WHERE " + ID + "=?;", params);
			executeUpdate("UPDATE " + TABLE_NAME() + " SET " + CONFIGLOC + "=? WHERE " + ID + "=?;", params);
			storageService.deleteFile(current.getPrimaryConfigLoc());
			storageService.deleteFile(current.getConfigLoc());
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return commitDefinition(props, primarySrc, secondarySrc, props.getId(), true);
	}

	@Override
	public boolean deleteAssignment(UUID id) {
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(id, DBType.UUID));
		try {
			ResultSet rs = executeQuery("SELECT " + PRIMARYCONFIGLOC + ", " + CONFIGLOC + ", " + PRIMARYSRCLOC + ", "
					+ SRCLOC + " FROM " + TABLE_NAME() + " WHERE " + ID + " = ?;", params);
			if (rs.next()) {
				submissionService.deleteByAssignment(id);
				deleteById(TABLE_NAME(), id);
				// delete associated files
				storageService.deleteFile((UUID) rs.getObject(PRIMARYCONFIGLOC));
				storageService.deleteFile((UUID) rs.getObject(PRIMARYSRCLOC));
				return true;
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<AssignmentDefinition> getAllAssignments() throws IOException {
		List<AssignmentDefinition> assignments = null;
		try {
			ResultSet rs = executeQuery("SELECT * FROM " + TABLE_NAME() + ";", null);
			assignments = new ArrayList<AssignmentDefinition>();

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
	public AssignmentDefinition getAssignment(UUID id, boolean includeSecondary) throws IOException {
		AssignmentDefinition assignment = null;
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

	private UUID commitDefinition(AssignmentDefinition props, MultipartFile primarySrc, MultipartFile secondarySrc,
			UUID id, boolean isUpdate) throws IOException {
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
		} else {
			primarySrcLoc = props.getPrimarySrcLoc();
		}
		if (secondarySrc != null) {
			secondarySrcLoc = storageService.putFile(secondarySrc);
		} else {
			secondarySrcLoc = props.getSrcLoc();
		}

		String insertQuery = "INSERT INTO " + TABLE_NAME() + " (" + ID + ", " + DUEDATE + ", " + ASSIGNMENTNAME + ", "
				+ ASSIGNMENTDESC + ", " + PRIMARYCONFIGLOC + ", " + CONFIGLOC + ", " + PRIMARYSRCLOC + ", " + SRCLOC
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

		String updateQuery = "UPDATE " + TABLE_NAME() + " SET " + DUEDATE + "=?, " + ASSIGNMENTNAME + "=?, "
				+ ASSIGNMENTDESC + "=?, " + PRIMARYCONFIGLOC + "=?, " + CONFIGLOC + "=?, " + PRIMARYSRCLOC + "=?, "
				+ SRCLOC + "=? " + "WHERE " + ID + "=?;";

		List<StatementParameter> params = new ArrayList<StatementParameter>();
		// params.add(new StatementParameter(id, DBType.UUID));
		params.add(new StatementParameter(props.getDueDate(), DBType.BIGINT));
		params.add(new StatementParameter(props.getName(), DBType.TEXT));
		params.add(new StatementParameter(props.getDesc(), DBType.TEXT));
		params.add(new StatementParameter(primaryConfigLoc, DBType.UUID));
		params.add(new StatementParameter(secondaryConfigLoc, DBType.UUID));
		params.add(new StatementParameter(primarySrcLoc, DBType.UUID));
		params.add(new StatementParameter(secondarySrcLoc, DBType.UUID));

		String queryString;
		if (isUpdate) {
			queryString = updateQuery;
			params.add(new StatementParameter(id, DBType.UUID));
		} else {
			queryString = insertQuery;
			params.add(0, new StatementParameter(id, DBType.UUID));
		}

		try {
			int inserted = executeUpdate(queryString, params);
			AssignmentDefinition def = getAssignment(id, true);
			def.setUser("admin");
			submissionService.executeDefinition(def);
			if (inserted > 0) {
				return id;
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private AssignmentDefinition extractAssignmentProps(ResultSet rs, boolean includeSecondary)
			throws SQLException, IOException {
		UUID id = (UUID) rs.getObject(ID);
		Long dueDate = rs.getLong(DUEDATE);
		String name = rs.getString(ASSIGNMENTNAME);
		String desc = rs.getString(ASSIGNMENTDESC);
		Map<String, PropValue> primaryConfig = new HashMap<String, PropValue>();
		UUID primaryConfigLoc = null;
		Map<String, PropValue> config = new HashMap<String, PropValue>();
		UUID configLoc = null;
		String primarySrcName = null;
		UUID primarySrcLoc = null;
		String srcName = null;
		UUID srcLoc = null;

		primaryConfigLoc = (UUID) rs.getObject(PRIMARYCONFIGLOC);
		primarySrcLoc = (UUID) rs.getObject(PRIMARYSRCLOC);
		if (primaryConfigLoc != null) {
			GenJobConfiguration gConfig = new GenJobConfiguration(
					storageService.getFile(primaryConfigLoc).getAbsolutePath());
			primaryConfig = gConfig.getconfigProps();
		}
		if (primarySrcLoc != null) {
			primarySrcName = storageService.getFile(primarySrcLoc).getName();
		}
		if (includeSecondary) {
			configLoc = (UUID) rs.getObject(CONFIGLOC);
			srcLoc = (UUID) rs.getObject(SRCLOC);
			if (configLoc != null) {
				GenJobConfiguration gConfig = new GenJobConfiguration(
						storageService.getFile(configLoc).getAbsolutePath());
				config = gConfig.getconfigProps();
			}
			if (srcLoc != null) {
				srcName = storageService.getFile(srcLoc).getName();
			}
		}

		AssignmentDefinition assignment = new AssignmentDefinition(id, null, dueDate, name, desc, primaryConfig,
				primaryConfigLoc, config, configLoc, primarySrcName, primarySrcLoc, srcName, srcLoc);

		return assignment;
	}
}
