package org.meltzg.edhd.submission;

import org.meltzg.edhd.assignment.AssignmentDefinition;
import org.meltzg.edhd.assignment.AssignmentSubmission;
import org.meltzg.edhd.db.DBServiceBase;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Abstract database service for interacting with EdHD submissions
 */
public abstract class AbstractSubmissionService extends DBServiceBase {

    /**
     * Executes an assignment definition on the Hadoop cluster via a SubmissionWorker
     *
     * @param definition - Assignment definition to run
     * @return ID of the submission if successfully submitted
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public abstract UUID executeDefinition(AssignmentDefinition definition)
            throws IOException, ClassNotFoundException, SQLException;

    /**
     * Merges the AssignmentSubmission into the definition and executes
     * it on the Hadoop cluster via a SubmissionWorker
     *
     * @param definition - submission primary configuration
     * @param submission - secondary (submission) configuration
     * @param src - submission source archive
     * @return ID of the submission if successfully submitted
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public abstract UUID executeSubmission(AssignmentDefinition definition, AssignmentSubmission submission,
                                           MultipartFile src) throws IOException, ClassNotFoundException, SQLException;

    /**
     * Retrieves the StatusProperties for the requested submission
     * @param submissionId - submission to retrieve status info for
     * @param isAdmin - whether the caller is an admin
     * @param user - caller's username
     * @return if user isAdmin or submissionId is user's, the submission's status properties, otherwise null
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public abstract StatusProperties getStatus(UUID submissionId, boolean isAdmin, String user) throws ClassNotFoundException, SQLException;

    /**
     * Retrieves the submission IDs for all submissions for the given assignment ID.
     * if user is an admin, submission IDs will be returned regardless of the submission's user
     * if user is not an admin, only their submission IDs will be returned
     *
     * @param assignmentId
     * @param user
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public abstract List<UUID> getSubmissionIds(UUID assignmentId, String user) throws ClassNotFoundException, SQLException;

    /**
     * Updates the status properties for a submission
     * @param status - properties to use
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public abstract void updateStatus(StatusProperties status) throws ClassNotFoundException, SQLException;

    /**
     * Deletes all submissions associated with the given assignment ID
     * @param id
     */
    public abstract void deleteByAssignment(UUID id);

    /**
     * Deletes all submissions by the given user for a given assignment
     *
     * @param user - associated username
     * @param assignmentId
     * @param isValidation - whether the submission being deleted is a validator submission
     * @param deleteOldFiles - whether to delete files associated with the configuration (configuration,
     *                       source archives)
     * @return true if successfull
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public abstract boolean deleteByUserAssignment(String user, UUID assignmentId, boolean isValidation, boolean deleteOldFiles)
            throws ClassNotFoundException, SQLException, IOException;

    /**
     * @param assignmentId
     * @return the status properties of the submission validator for the given assignment
     */
    public abstract StatusProperties getValidatorStatProps(UUID assignmentId);

    /**
     * @param assignmentId
     * @return whether or not the validator is still being initialized for a given assignment
     */
    public abstract boolean validatorPending(UUID assignmentId);

    /**
     * @param assignmentId
     * @param user
     * @return whether or not the user's submission for a given assignment is running
     */
    public abstract boolean submissionPending(UUID assignmentId, String user);

    /**
     * @param user - submission's username
     * @param assignmentId - submission's assignemt ID
     * @param isValidation - whether the submission is a validator
     * @return Retrieves the AssignmentSubmission
     */
    public abstract AssignmentSubmission getByUserAssignment(String user, UUID assignmentId, boolean isValidation);

    /**
     * Reruns validation jobs for all submissions for the specified assignment
     * @param definition
     */
    public abstract void revalidateSubmissions(AssignmentDefinition definition);

    /**
     * @param assignmentId
     * @param includeValidator - whether or not the validator submission should be included
     * @return AssignmentSubmissions for a given assignment
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public abstract List<AssignmentSubmission> getSubmissions(UUID assignmentId, boolean includeValidator) throws SQLException, ClassNotFoundException, IOException;
}
