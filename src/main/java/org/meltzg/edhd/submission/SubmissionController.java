package org.meltzg.edhd.submission;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.meltzg.edhd.security.AbstractSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubmissionController {
	@Autowired
	private AbstractSecurityService securityService;

	@Autowired
	AbstractSubmissionService submissionService;

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
				statuses.add(statusProps);
			}
		} catch (ClassNotFoundException | SQLException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
		return ResponseEntity.ok(statuses);
	}
}
