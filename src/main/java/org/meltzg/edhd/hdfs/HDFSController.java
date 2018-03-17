package org.meltzg.edhd.hdfs;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.Principal;

import javax.validation.Valid;

import org.meltzg.edhd.security.AbstractSecurityService;
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
public class HDFSController {

	@Autowired
	private IHDFSService hdfsService;

	@Autowired
	AbstractSecurityService securityService;

	@RequestMapping("/hdfs-ls/{path}")
	public HDFSLocationInfo getChildren(@PathVariable String path) {
		try {
			path = URLDecoder.decode(path, "UTF-8");
			return hdfsService.getChildren(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/hdfs-mkdir/{location}/{newDir}", method = RequestMethod.POST)
	public ResponseEntity<Boolean> mkDir(Principal principal, @PathVariable String location,
			@PathVariable String newDir) {
		if (securityService.isAdmin(principal.getName())) {
			boolean success;
			try {
				location = URLDecoder.decode(location, "UTF-8");
				newDir = URLDecoder.decode(newDir, "UTF-8");
				success = hdfsService.mkDir(location, newDir);
				return ResponseEntity.ok(success);
			} catch (IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
		}
	}

	@RequestMapping(value = "/hdfs-rm/{path}", method = RequestMethod.DELETE)
	public ResponseEntity<Boolean> delete(Principal principal, @PathVariable String path) {
		if (securityService.isAdmin(principal.getName())) {
			boolean success;
			try {
				path = URLDecoder.decode(path, "UTF-8");
				success = hdfsService.delete(path);
				return ResponseEntity.ok(success);
			} catch (IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
		}
	}

	@RequestMapping(value = "/hdfs-put/{path}", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<Boolean> putFile(Principal principal,
			@RequestPart("location") @Valid HDFSLocationInfo location,
			@RequestPart("file") @Valid MultipartFile file) {
		if (securityService.isAdmin(principal.getName())) {
			boolean success;
			try {
				success = hdfsService.put(location.getLocation(), file);
				return ResponseEntity.ok(success);
			} catch (IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
		}
	}
}
