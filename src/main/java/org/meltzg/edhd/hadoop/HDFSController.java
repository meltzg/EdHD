package org.meltzg.edhd.hadoop;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.meltzg.edhd.Utilities;
import org.meltzg.edhd.security.AbstractSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HDFSController {

	@Autowired
	private IHadoopService hadoopService;

	@Autowired
	AbstractSecurityService securityService;

	@RequestMapping("/hdfs/ls/{path}")
	public HDFSLocationInfo getChildren(@PathVariable String path) {
		try {
			path = Utilities.decodeBase64(path);
			return hadoopService.getChildren(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/hdfs/mkdir/{location}/{newDir}", method = RequestMethod.POST)
	public ResponseEntity<Boolean> mkDir(Principal principal, @PathVariable String location,
			@PathVariable String newDir) {
		if (securityService.isAdmin(principal.getName())) {
			boolean success;
			try {
				location = Utilities.decodeBase64(location);
				newDir = Utilities.decodeBase64(newDir);
				success = hadoopService.mkDir(location, newDir);
				return ResponseEntity.ok(success);
			} catch (IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
		}
	}

	@RequestMapping(value = "/hdfs/rm/{path}", method = RequestMethod.DELETE)
	public ResponseEntity<Boolean> delete(Principal principal, @PathVariable String path) {
		if (securityService.isAdmin(principal.getName())) {
			boolean success;
			try {
				path = Utilities.decodeBase64(path);
				success = hadoopService.delete(path);
				return ResponseEntity.ok(success);
			} catch (IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
		}
	}

	@RequestMapping(value = "/hdfs/put", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<Boolean> putFile(Principal principal,
			@RequestPart("location") @Valid HDFSLocationInfo location,
			@RequestPart("file") @Valid MultipartFile file) {
		if (securityService.isAdmin(principal.getName())) {
			boolean success;
			try {
				success = hadoopService.put(location.getLocation(), file);
				return ResponseEntity.ok(success);
			} catch (IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
		}
	}
	
	@RequestMapping(value = "/hdfs/preview/{path}", produces = "application/json")
	public ResponseEntity<Map<String, String>> getFilePreview(@PathVariable String path) {
		Map<String, String> responseBody = new HashMap<String, String>();
		try {
			path = Utilities.decodeBase64(path);
			String preview = hadoopService.getHDFSFilePreview(path);
			responseBody.put("preview", preview);
			return ResponseEntity.ok(responseBody);
		} catch (FileNotFoundException e) {
			responseBody.put("message", "File not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
		} catch (IOException e) {
			responseBody.put("message", "En error occured retrieveing the file preview");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
		}
	}
	
	@RequestMapping("/hdfs/get/{path}")
	public void getFile(HttpServletResponse response, @PathVariable String path) {
		try {
			path = Utilities.decodeBase64(path);
			HDFSEntry fileInfo = hadoopService.getHDFSFileInfo(path);
			if (!fileInfo.isDirectory()) {
                InputStream file = hadoopService.getHDFSFile(path);
                response.addHeader("Content-disposition", "attachment;filename=" + path);
                response.setContentType("txt/plain");
                IOUtils.copy(file, response.getOutputStream());
                response.flushBuffer();
            }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
