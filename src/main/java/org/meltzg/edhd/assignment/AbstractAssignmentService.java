package org.meltzg.edhd.assignment;

import org.meltzg.edhd.db.DBServiceBase;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public abstract class AbstractAssignmentService extends DBServiceBase {
    public abstract UUID createAssignment(AssignmentDefinition props, MultipartFile primarySrc,
                                          MultipartFile secondarySrc) throws IOException;

    public abstract UUID updateAssignment(AssignmentDefinition props, MultipartFile primarySrc,
                                          MultipartFile secondarySrc) throws IOException;

    public abstract boolean deleteAssignment(UUID id);

    public abstract List<AssignmentDefinition> getAllAssignments() throws IOException;

    public abstract AssignmentDefinition getAssignment(UUID id, boolean includeSecondary) throws IOException;

    public abstract File getAssignmentSubmissionZip(UUID id) throws IOException, SQLException, ClassNotFoundException;
}
