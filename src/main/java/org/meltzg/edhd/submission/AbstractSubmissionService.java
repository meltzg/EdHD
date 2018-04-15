package org.meltzg.edhd.submission;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.meltzg.edhd.assignment.AssignmentDefinition;
import org.meltzg.edhd.assignment.AssignmentSubmission;
import org.meltzg.edhd.db.DBServiceBase;
import org.springframework.web.multipart.MultipartFile;

public abstract class AbstractSubmissionService extends DBServiceBase {

	public abstract UUID executeDefinition(AssignmentDefinition definition)
			throws IOException, ClassNotFoundException, SQLException;

	public abstract UUID executeSubmission(AssignmentDefinition definition, AssignmentSubmission submission,
			MultipartFile src) throws IOException, ClassNotFoundException, SQLException;

	public abstract StatusProperties getStatus(UUID submissionId, boolean isAdmin, String name) throws ClassNotFoundException, SQLException;

	public abstract List<UUID> getSubmissionIds(UUID assignmentId, String name) throws ClassNotFoundException, SQLException;

	public abstract void updateStatus(StatusProperties status) throws ClassNotFoundException, SQLException;

	public abstract void deleteByAssignment(UUID id);

	public abstract boolean deleteByUserAssignment(String user, UUID assignmentId, boolean isValidation, boolean deleteOldFiles)
			throws ClassNotFoundException, SQLException, IOException;

	public abstract StatusProperties getValidatorStatProps(UUID assignmentId);

	public abstract boolean validatorPending(UUID assignmentId);

	public abstract boolean submissionPending(UUID assignmentId, String user);

	public abstract AssignmentSubmission getByUserAssignment(String user, UUID assignmentId, boolean isValidation);

	public abstract void revalidateSubmissions(AssignmentDefinition definition);
}
