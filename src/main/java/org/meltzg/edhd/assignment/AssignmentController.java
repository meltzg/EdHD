package org.meltzg.edhd.assignment;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.meltzg.edhd.security.ISecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AssignmentController {
	@Autowired
	private ISecurityService securityService;
	
	@Autowired
	IAssignmentService assignmentService;

	@RequestMapping(value = "/create-assignment", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<Map<String, String>> createAssignment(Principal principal,
			@RequestPart("properties") @Valid AssignmentCreationProperties props,
			@RequestPart(name = "primarySrc", required = false) MultipartFile primarySrc,
			@RequestPart(name = "secondarySrc", required = false) MultipartFile secondarySrc) {
		Map<String, String> returnBody = new HashMap<String, String>();

		if (securityService.isAdmin(principal.getName())) {
			UUID assignmentId;
			try {
				assignmentId = assignmentService.createAssignment(props, primarySrc, secondarySrc);
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
}
