package org.meltzg.edhd.submission;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.meltzg.edhd.security.AbstractSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubmissionController {
	@Autowired
	private AbstractSecurityService securityService;
	
	@Autowired
	AbstractSubmissionService submissionService;
	
	
//	@RequestMapping("/submission/pending/{assignmentId}")
//	public ResponseEntity<List<UUID>> getPendingSubmissionIds
	
	@RequestMapping(value="/get-statuses", method=RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<List<StatusProperties>> getStatuses(Principal principal, List<UUID> submissions) {
		List<StatusProperties> statuses = new ArrayList<StatusProperties>();
		boolean isAdmin = securityService.isAdmin(principal.getName());
		for (UUID submissionId : submissions) {
			StatusProperties statusProps = submissionService.getStatus(submissionId, isAdmin, principal.getName());
			statuses.add(statusProps);
		}
		return ResponseEntity.ok(statuses);
	}
}
