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

@RestController
public class SubmissionController {
    @Autowired
    private AbstractSubmissionService submissionService;
    @Autowired
    private AbstractSecurityService securityService;

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
