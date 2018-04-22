package org.meltzg.edhd.assignment;

import org.apache.commons.io.IOUtils;
import org.meltzg.edhd.security.AbstractSecurityService;
import org.meltzg.edhd.submission.AbstractSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.*;

/**
 * REST Controller for EdHD assignment interactions
 */
@RestController
public class AssignmentController {
    @Autowired
    private AbstractAssignmentService assignmentService;
    @Autowired
    private AbstractSubmissionService submissionService;
    @Autowired
    private AbstractSecurityService securityService;


    /**
     * Assignment creation/update route.  If the AssignmentDefinition's ID is not null, it will attempt to update
     * an existing assignment with the same ID.
     *
     * @param principal - (Automatic)
     * @param props - (form-data.properties)
     * @param primarySrc - (Optional) (form-data.primarySrc)
     * @param secondarySrc - (Optional) (form-data.secondarySrc)
     * @return A JSON object with the assignmnet_id or an error message
     */
    @RequestMapping(value = "/assignment/create", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, String>> createAssignment(Principal principal,
                                                                @RequestPart("properties") @Valid AssignmentDefinition props,
                                                                @RequestPart(name = "primarySrc", required = false) MultipartFile primarySrc,
                                                                @RequestPart(name = "secondarySrc", required = false) MultipartFile secondarySrc) {
        Map<String, String> returnBody = new HashMap<String, String>();

        // only admins can create assignments
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

    /**
     * Assignment retrieval route.  Since this route does not require admin privileges, the AssignmentDefinitions
     * should not contain the secondary config
     * @return A list of all registered AssignmentDefinitions
     */
    @RequestMapping("/assignment/get")
    public ResponseEntity<List<AssignmentDefinition>> getAllAssignments() {
        try {
            List<AssignmentDefinition> assignments = assignmentService.getAllAssignments();
            return ResponseEntity.ok(assignments);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Individual assignment definition retrieval route.  If the user is an admin, the secondary config info
     * @param principal - (Automatic)
     * @param id - ID of the assignment to retrieve
     * @return
     */
    @RequestMapping("/assignment/get/{id}")
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

    @RequestMapping(value = "/assignment/delete/{id}", method = RequestMethod.DELETE)
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

    @RequestMapping(value = "/assignment/submit", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, String>> submitAssignment(Principal principal,
                                                                @RequestPart("properties") @Valid AssignmentSubmission submission,
                                                                @RequestPart(name = "src", required = true) MultipartFile src) {
        Map<String, String> returnBody = new HashMap<String, String>();

        UUID submissionId;
        try {
            AssignmentDefinition definition = assignmentService.getAssignment(submission.getId(), false);
            if (definition != null) {
                if (definition.getDueDate() < (new Date()).getTime() / 1000) {
                    returnBody.put("message", "Assignment is no longer accepting submissions!");
                }
                submission.setUser(principal.getName());
                if (submissionService.validatorPending(definition.getId())) {
                    returnBody.put("message", "Cannot submit assignment while validator is pending!");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(returnBody);
                }
                if (submissionService.submissionPending(definition.getId(), principal.getName())) {
                    returnBody.put("message", "Cannot submit assignment while previous submission is pending!");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(returnBody);
                }
                submissionId = submissionService.executeSubmission(definition, submission, src);
                returnBody.put("submission_id", submissionId.toString());
            } else {
                returnBody.put("message", "Could not find assignment with ID " + submission.getId().toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnBody);
            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            returnBody.put("message", "An error occurred while creating the assignment");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnBody);
        }

        return ResponseEntity.ok(returnBody);
    }

    @RequestMapping("/assignment/artifacts/{id}")
    public void getAssignmentSubmissions(Principal principal, HttpServletResponse response, @PathVariable UUID id) throws IOException {
        try {
            if (securityService.isAdmin(principal.getName())) {
                File archive = assignmentService.getAssignmentSubmissionZip(id);
                response.addHeader("Content-disposition", "attachment;filename=" + archive.getName());
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                IOUtils.copy(new FileInputStream(archive), response.getOutputStream());
                response.flushBuffer();
                archive.delete();
            } else {
                response.sendError(HttpStatus.UNAUTHORIZED.value());
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }
}
