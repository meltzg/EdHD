package org.meltzg.edhd.submission;

import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.meltzg.edhd.assignment.AssignmentDefinition;
import org.meltzg.edhd.assignment.AssignmentSubmission;
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
	
	@Value("${edhd.threads}")
	private Integer nThreads;

	@Autowired
	private AbstractStorageService storageService;

	@Autowired
	AbstractSecurityService securityService;
	
	private ExecutorService threadpool;

	@PostConstruct
	public void init() throws Exception {
		super.init();
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME() + " (" + ASSIGNMENTID + " UUID, " + USER
				+ " TEXT REFERENCES " + securityService.TABLE_NAME() + ", " + CONFIGLOC + " UUID REFERENCES "
				+ storageService.TABLE_NAME() + ", " + SRCLOC + " UUID REFERENCES " + storageService.TABLE_NAME() + ", "
				+ ISVALIDATION + " BOOLEAN, PRIMARY KEY(" + ASSIGNMENTID + ", " + USER + "))";
		statement.executeUpdate(createUsers);
		conn.close();
		
		threadpool = Executors.newFixedThreadPool(nThreads);
	}

	@Override
	public UUID executeDefinition(AssignmentDefinition definition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID executeSubmission(AssignmentDefinition definition, AssignmentSubmission submission) {
		// TODO Auto-generated method stub
		return null;
	}

}
