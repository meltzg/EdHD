package org.meltzg.edhd.submission;

import org.meltzg.edhd.security.AbstractSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for retrieving information about EdHD submissions
 */
@RestController
public class SubmissionController {
    @Autowired
    private AbstractSubmissionService submissionService;
    @Autowired
    private AbstractSecurityService securityService;

    /**
     * Retrieves a list of submission IDs for the given assignment.  if the user is an admin, this wll
     * include the validator and all submission IDs.  If not, only the validator and user's submissions
     * will be returned
     *
     * @param principal - (Automatic)
     * @param assignmentId
     * @return
     */
    @RequestMapping("/submission/status/{assignmentId}")
    public ResponseEntity<List<UUID>> getSubmissionIds(Principal principal, @PathVariable UUID assignmentId) {
        List<UUID> pendingIds;
        try {
            pendingIds = submissionService.getSubmissionIds(assignmentId, principal.getName());
        } catch (ClassNotFoundException | SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.ok(pendingIds);
    }

    /**
     * Retrieves the status properties for the requested submission IDs
     * If the user is not an admin, only the statuses of their own submissions and the validator can
     * be retrieved
     *
     * @param principal
     * @param submissions
     * @return
     */
    @RequestMapping(value = "/submission/status", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<List<StatusProperties>> getStatuses(Principal principal, @RequestBody List<UUID> submissions) {
        List<StatusProperties> statuses = new ArrayList<StatusProperties>();
        boolean isAdmin = securityService.isAdmin(principal.getName());
        try {
            for (UUID submissionId : submissions) {
                StatusProperties statusProps = submissionService.getStatus(submissionId, isAdmin, principal.getName());
                if (statusProps != null) {
                    statuses.add(statusProps);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok(statuses);
    }
}
