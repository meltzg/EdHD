package org.meltzg.edhd.submission;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.meltzg.edhd.assignment.AssignmentDefinition;
import org.meltzg.edhd.assignment.AssignmentSubmission;
import org.meltzg.edhd.hadoop.IHadoopService;
import org.meltzg.edhd.security.AbstractSecurityService;
import org.meltzg.edhd.storage.AbstractStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
	public UUID executeDefinition(AssignmentDefinition definition) throws IOException {
		UUID submissionId = UUID.randomUUID();
		SubmissionWorker worker = new SubmissionWorker(submissionId, definition, new StatusProperties(), storageService,
				this, hadoopService);
		threadpool.execute(worker);
		return submissionId;
	}

	@Override
	public UUID executeSubmission(AssignmentDefinition definition, AssignmentSubmission submission) {
		// TODO Auto-generated method stub
		return null;
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
}
