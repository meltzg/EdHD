package org.meltzg.edhd.submission;

import java.io.IOException;
import java.util.UUID;

import org.meltzg.edhd.assignment.AssignmentDefinition;
import org.meltzg.edhd.assignment.AssignmentSubmission;
import org.meltzg.edhd.db.DBServiceBase;

public abstract class AbstractSubmissionService extends DBServiceBase {

	public abstract UUID executeDefinition(AssignmentDefinition definition) throws IOException;
	public abstract UUID executeSubmission(AssignmentDefinition definition, AssignmentSubmission submission);
	public abstract StatusProperties getStatus(UUID id);
	public abstract void updateStatus(StatusProperties status);
	
	public static String TABLE_NAME() {
		return "submissions";
	}
}
