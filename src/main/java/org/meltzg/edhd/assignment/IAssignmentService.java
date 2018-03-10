package org.meltzg.edhd.assignment;

import java.io.IOException;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface IAssignmentService {
	public UUID createAssignment(AssignmentCreationProperties props, MultipartFile primarySrc,
			MultipartFile secondarySrc) throws IOException;
}
