package org.meltzg.edhd.submission;

import java.util.UUID;

import org.meltzg.edhd.assignment.AssignmentDefinition;
import org.meltzg.edhd.assignment.AssignmentSubmission;
import org.meltzg.edhd.db.DBServiceBase;

public abstract class AbstractSubmissionService extends DBServiceBase {

	public abstract UUID executeDefinition(AssignmentDefinition definition);
	public abstract UUID executeSubmission(AssignmentDefinition definition, AssignmentSubmission submission);
	
	protected static String TABLE_NAME() {
		return "submissions";
	}
}
