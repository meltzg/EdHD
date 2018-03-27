package org.meltzg.edhd.submission;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.meltzg.edhd.assignment.AssignmentDefinition;
import org.meltzg.edhd.assignment.AssignmentSubmission;
import org.meltzg.edhd.hadoop.IHadoopService;
import org.meltzg.edhd.security.AbstractSecurityService;
import org.meltzg.edhd.storage.AbstractStorageService;
import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SubmissionService extends AbstractSubmissionService {

	private static final String ASSIGNMENTID = "assignmentId";
	private static final String USER = "name";
	private static final String CONFIGLOC = "configLoc";
	private static final String SRCLOC = "srcLoc";
	private static final String ISVALIDATION = "isValidation";

	// Status Info
	private static final String COMPILESTATUS = "compileStatus";
	private static final String COMPILEMSG = "compileMsg";
	private static final String RUNSTATUS = "runStatus";
	private static final String RUNMSG = "runMsg";
	private static final String VALIDATESTATUS = "validateStatus";
	private static final String VALIDATEMSG = "validateMsg";
	private static final String COMPLETESTATUS = "completeStatus";
	private static final String COMPLETEMSG = "completeMsg";

	@Value("${edhd.threads}")
	private Integer nThreads;

	@Autowired
	private AbstractStorageService storageService;

	@Autowired
	private AbstractSecurityService securityService;

	@Autowired
	IHadoopService hadoopService;

	private ExecutorService threadpool;

	@PostConstruct
	public void init() throws Exception {
		super.init();
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME() + " (" + ID + " UUID, " + ASSIGNMENTID
				+ " UUID, " + USER + " TEXT REFERENCES " + securityService.TABLE_NAME() + ", " + CONFIGLOC
				+ " UUID REFERENCES " + storageService.TABLE_NAME() + ", " + SRCLOC + " UUID REFERENCES "
				+ storageService.TABLE_NAME() + ", " + ISVALIDATION + " BOOLEAN, " + COMPILESTATUS + " INTEGER, "
				+ RUNSTATUS + " INTEGER, " + VALIDATESTATUS + " INTEGER, " + COMPLETESTATUS + " INTEGER, " + COMPILEMSG
				+ " TEXT, " + RUNMSG + " TEXT, " + VALIDATEMSG + " TEXT, " + COMPLETEMSG + " TEXT, PRIMARY KEY(" + ID
				+ "))";
		statement.executeUpdate(createUsers);
		conn.close();

		threadpool = Executors.newFixedThreadPool(nThreads);
	}

	@Override
	public String TABLE_NAME() {
		return "submissions";
	}

	@Override
	public UUID executeDefinition(AssignmentDefinition definition) throws IOException, ClassNotFoundException, SQLException {
		return executeSubmission(definition, true);
	}

	@Override
	public UUID executeSubmission(AssignmentDefinition definition, AssignmentSubmission submission, MultipartFile src)
			throws IOException, ClassNotFoundException, SQLException {
		UUID srcLoc = storageService.putFile(src);
		UUID configLoc = storageService.putFile(new GenJobConfiguration(submission.getConfig()));

		definition.setConfig(submission.getConfig());
		definition.setConfigLoc(configLoc);
		definition.setSrcName(submission.getSrcName());
		definition.setSrcLoc(srcLoc);
		definition.setUser(submission.getUser());
		return executeSubmission(definition, false);
	}

	@Override
	public StatusProperties getStatus(UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateStatus(StatusProperties status) {
		// TODO Auto-generated method stub

	}

	private UUID executeSubmission(AssignmentDefinition definition, boolean isValidation) throws IOException, ClassNotFoundException, SQLException {
		UUID submissionId = UUID.randomUUID();
		StatusProperties statProps = new StatusProperties(submissionId);
		
		deleteByUserAssignment(definition.getUser(), definition.getId(), isValidation);
		
		
		SubmissionWorker worker = new SubmissionWorker(submissionId, definition, statProps, storageService, this,
				hadoopService);
		threadpool.execute(worker);
		return submissionId;
	}

	private AssignmentSubmission getByUserAssignment(String user, UUID assignmentId, boolean isValidation) {
		AssignmentSubmission submission = null;
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(user, DBType.TEXT));
		params.add(new StatementParameter(assignmentId, DBType.UUID));
		params.add(new StatementParameter(isValidation, DBType.BOOLEAN));

		try {
			ResultSet rs = executeQuery("SELECT * FROM " + TABLE_NAME() + " WHERE " + USER + "=? AND " + ASSIGNMENTID
					+ "=? AND " + ISVALIDATION + "=?;", params);
			if (rs.next()) {
				submission = extractSubmissionProps(rs);
			}
		} catch (ClassNotFoundException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return submission;
	}

	

	private boolean deleteByUserAssignment(String user, UUID assignmentId, boolean isValidation) throws ClassNotFoundException, SQLException, IOException {
		AssignmentSubmission existingSubmission = getByUserAssignment(user, assignmentId, isValidation);
		if (existingSubmission != null) {
			UUID srcLoc = existingSubmission.getSrcLoc();
			UUID configLoc = existingSubmission.getConfigLoc();
			deleteById(TABLE_NAME(), existingSubmission.getId());
			
			if (!isValidation) {
				storageService.deleteFile(configLoc);
				storageService.deleteFile(srcLoc);
				hadoopService.delete("/submission/" + existingSubmission.getId());
			}
			
			return true;
		}
		
		return false;
	}
	
	private AssignmentSubmission extractSubmissionProps(ResultSet rs) throws SQLException, IOException {
		UUID id = (UUID) rs.getObject(ID);
		String user = rs.getString(USER);
		UUID configLoc = (UUID) rs.getObject(CONFIGLOC);
		UUID srcLoc = (UUID) rs.getObject(SRCLOC);
		
		AssignmentSubmission submission = new AssignmentSubmission();
		submission.setId(id);
		submission.setUser(user);
		submission.setConfigLoc(configLoc);
		submission.setSrcLoc(srcLoc);
		
		if (configLoc != null) {
			GenJobConfiguration gConfig = new GenJobConfiguration(
					storageService.getFile(configLoc).getAbsolutePath());
			submission.setConfig(gConfig.getconfigProps());
		}
		if (srcLoc != null) {
			submission.setSrcName(storageService.getFile(srcLoc).getName());
		}
		
		return submission;
	}
}
