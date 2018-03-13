package org.meltzg.edhd.status;

import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

@Service
public class StatusService extends AbstractStatusService {

	private static final String COMPILESTATUS = "compileStatus";
	private static final String COMPILEMSG = "compileMsg";
	private static final String RUNSTATUS = "runStatus";
	private static final String RUNMSG = "runMsg";
	private static final String VALIDATESTATUS = "validateStatus";
	private static final String VALIDATEMSG = "validateMsg";
	private static final String COMPLETESTATUS = "completeStatus";
	private static final String COMPLETEMSG = "completeMsg";

	@PostConstruct
	public void init() throws Exception {
		super.init();
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME() + " (" + ID + " UUID, " + COMPILESTATUS
				+ " INTEGER, " + RUNSTATUS + " INTEGER, " + VALIDATESTATUS + " INTEGER, " + COMPLETESTATUS
				+ " INTEGER, " + COMPILEMSG + " TEXT, " + RUNMSG + " TEXT, " + VALIDATEMSG + " TEXT, " + COMPLETEMSG
				+ " TEXT, " + "PRIMARY KEY(" + ID + "))";
		statement.executeUpdate(createUsers);
		conn.close();
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
