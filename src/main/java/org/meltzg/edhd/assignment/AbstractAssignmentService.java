package org.meltzg.edhd.assignment;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.meltzg.edhd.db.DBServiceBase;
import org.springframework.web.multipart.MultipartFile;

public abstract class AbstractAssignmentService extends DBServiceBase {
	public abstract UUID createAssignment(AssignmentDefinition props, MultipartFile primarySrc,
			MultipartFile secondarySrc) throws IOException;

	public abstract UUID updateAssignment(AssignmentDefinition props, MultipartFile primarySrc,
			MultipartFile secondarySrc) throws IOException;

	public abstract boolean deleteAssignment(UUID id);

	public abstract List<AssignmentDefinition> getAllAssignments() throws IOException;

	public abstract AssignmentDefinition getAssignment(UUID id, boolean includeSecondary) throws IOException;

	public static String TABLE_NAME() {
		return "assignments";
	}
}
