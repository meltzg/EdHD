package org.meltzg.edhd.assignment;

import org.meltzg.edhd.db.DBServiceBase;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * An abstract database service for interacting with EdHD assignments
 */
public abstract class AbstractAssignmentService extends DBServiceBase {
    /**
     * Creates a new EdHD assignment
     *
     * @param props - A full assignment definition with a primary (assignment) and secondary (validation)
     *              configuration
     * @param primarySrc - a zip file containing the necessary source files for the primary configuration.
     *                   If no source files are needed for the primary configuration, this should be null
     * @param secondarySrc - a zip file containing the necessary source files for the secondary configuration.
     *                     If no source files are needed for the secondary configuration, this should be null
     * @return UUID of the newly created assignment or null if it could not be created
     * @throws IOException
     */
    public abstract UUID createAssignment(AssignmentDefinition props, MultipartFile primarySrc,
                                          MultipartFile secondarySrc) throws IOException;

    /**
     * Updates and existing EdHD assignment
     *
     * @param props - A full assignment definition with a primary (assignment) and secondary (validation)
     *              configuration
     * @param primarySrc - a zip file containing the necessary source files for the primary configuration.
     *                   If the configuration contains a primarySrcLoc, the existing file will be reused
     * @param secondarySrc - a zip file containing the necessary source files for the secondary configuration.
     *                     If the configuration contains a srcLoc, the existing file will be reused
     * @return UUID of the updated assignment or null if it could not be updated
     * @throws IOException
     */
    public abstract UUID updateAssignment(AssignmentDefinition props, MultipartFile primarySrc,
                                          MultipartFile secondarySrc) throws IOException;

    /**
     * Deletes an existing assignment and all submissions/files associated with the assignment
     *
     * @param id - ID of the assignment to delete
     * @return true if assignment is successfully deleted, false otherwise
     */
    public abstract boolean deleteAssignment(UUID id);

    /**
     * Retrieves a list of all the assignments currently registered in EdHD.  These definitions may not
     * contain the secondary (validator) configuration
     * @return List of AssignmentDefinitions
     * @throws IOException
     */
    public abstract List<AssignmentDefinition> getAllAssignments() throws IOException;

    /**
     * Retrieves a single AssignmentDefinition by ID
     *
     * @param id - ID of assignment to retrieve
     * @param includeSecondary - true if this should return the secondary (validation) configuration as
     *                         part of the AssignmentDefinition
     * @return the requested assignment definition or null if no assignment with the requested ID can be found
     * @throws IOException
     */
    public abstract AssignmentDefinition getAssignment(UUID id, boolean includeSecondary) throws IOException;

    /**
     * Creates and returns a Zip archive containing all of the submissions (configuration and source) for
     * a given assignment
     *
     * @param id - Assignment ID to retrieve submissions for
     * @return Zip archive with all submission artifacts for the assignment
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public abstract File getAssignmentSubmissionZip(UUID id) throws IOException, SQLException, ClassNotFoundException;
}
