package org.meltzg.edhd.assignment;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.meltzg.edhd.db.DBServiceBase;
import org.springframework.web.multipart.MultipartFile;

public abstract class AbstractAssignmentService extends DBServiceBase {
	public abstract UUID createAssignment(AssignmentCreationProperties props, MultipartFile primarySrc,
			MultipartFile secondarySrc) throws IOException;
	
	public abstract List<AssignmentProperties> getAllAssignments();
	
	@Override
	protected String TABLE_NAME() {
		return "assignments";
	}
}
