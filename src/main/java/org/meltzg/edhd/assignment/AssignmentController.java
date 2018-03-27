package org.meltzg.edhd.assignment;

import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.meltzg.edhd.security.AbstractSecurityService;
import org.meltzg.edhd.submission.AbstractSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AssignmentController {
	@Autowired
	private AbstractSecurityService securityService;

	@Autowired
	AbstractAssignmentService assignmentService;
	
	@Autowired
	AbstractSubmissionService submissionService;

	@RequestMapping(value = "/create-assignment", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<Map<String, String>> createAssignment(Principal principal,
			@RequestPart("properties") @Valid AssignmentDefinition props,
			@RequestPart(name = "primarySrc", required = false) MultipartFile primarySrc,
			@RequestPart(name = "secondarySrc", required = false) MultipartFile secondarySrc) {
		Map<String, String> returnBody = new HashMap<String, String>();

		if (securityService.isAdmin(principal.getName())) {
			UUID assignmentId;
			try {
				if (props.getId() == null) {
					assignmentId = assignmentService.createAssignment(props, primarySrc, secondarySrc);
				} else {
					assignmentId = assignmentService.updateAssignment(props, primarySrc, secondarySrc);
				}
				returnBody.put("assignment_id", assignmentId.toString());
			} catch (IOException e) {
				returnBody.put("message", "An error occured while creating the assignment");
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnBody);
			}

			return ResponseEntity.ok(returnBody);
		} else {
			returnBody.put("message", "Only admins can create assignments");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnBody);
		}
	}

	@RequestMapping("/get-assignments")
	public ResponseEntity<List<AssignmentDefinition>> getAllAssignments() {
		try {
			List<AssignmentDefinition> assignments = assignmentService.getAllAssignments();
			return ResponseEntity.ok(assignments);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@RequestMapping("/get-assignment/{id}")
	public ResponseEntity<AssignmentDefinition> getAssignment(Principal principal, @PathVariable UUID id) {
		try {
			AssignmentDefinition assignment = assignmentService.getAssignment(id,
					securityService.isAdmin(principal.getName()));
			return ResponseEntity.ok(assignment);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@RequestMapping(value = "/delete-assignment/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Map<String, String>> deleteAssignment(Principal principal, @PathVariable UUID id) {
		Map<String, String> returnBody = new HashMap<String, String>();
		if (securityService.isAdmin(principal.getName())) {
			boolean success = assignmentService.deleteAssignment(id);
			if (success) {
				return ResponseEntity.ok(returnBody);
			} else {
				returnBody.put("message", "An error occured while deleting the assignment");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnBody);
			}
		} else {
			returnBody.put("message", "Only admins can create assignments");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnBody);
		}
	}

	@RequestMapping(value = "/submit-assignment", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<Map<String, String>> createAssignment(Principal principal,
			@RequestPart("properties") @Valid AssignmentSubmission submission,
			@RequestPart(name = "src", required = true) MultipartFile src) {
		Map<String, String> returnBody = new HashMap<String, String>();

		UUID submissionId;
		try {
			AssignmentDefinition definition = assignmentService.getAssignment(submission.getId(), false);
			if (definition != null) {
				submissionId = submissionService.executeSubmission(definition, submission, src);
				returnBody.put("submission_id", submissionId.toString());
			} else {
				returnBody.put("message", "Could not find assignment with ID " + submission.getId().toString());
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnBody);
			}
		} catch (IOException | ClassNotFoundException | SQLException e) {
			returnBody.put("message", "An error occured while creating the assignment");
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnBody);
		}

		return ResponseEntity.ok(returnBody);
	}
}
