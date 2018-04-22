package org.meltzg.edhd.hadoop;

import org.apache.commons.io.IOUtils;
import org.meltzg.edhd.Utilities;
import org.meltzg.edhd.security.AbstractSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for HDFS interaction
 */
@RestController
public class HDFSController {

    @Autowired
    private AbstractSecurityService securityService;
    @Autowired
    private IHadoopService hadoopService;

    /**
     * Route to retrieve information about a location in HDFS (equivalent to hdfs dfs -ls {path}
     *
     * @param path
     * @return
     */
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

    /**
     * Route to create a folder in HDFS
     *
     * @param principal - (Automatic) (Requires Admin)
     * @param location - (Base64 encoded) path to put new folder in
     * @param newDir - (Base64 encoded) name of folder to create
     * @return true if folder successfully created
     */
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

    /**
     * Route to recursively delete file in HDFS
     *
     * @param principal - (Automatic) (Requires Admin)
     * @param path - (Base64 encoded) path to delete
     * @return true if location successfully deleted
     */
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

    /**
     * Route to create a folder in HDFS
     *
     * @param principal - (Automatic) (Requires Admin)
     * @param location - (form-data.location) path to put file in
     * @param file - (form-data.file) file to put into HDFS
     * @return true if folder successfully created
     */
    @RequestMapping(value = "/hdfs/put", method = RequestMethod.POST, consumes = {"multipart/form-data"})
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

    /**
     * Route to preview a file in HDFS
     *
     * @param path - (Base64 encoded) path to preview
     * @return the first ${edhd.hadoop.hdfsFilePreview} lines of the requested file
     */
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

    /**
     * Route to retrieve a file from HDFS
     * @param response - (Automatic) HTTP response to attach the file to
     * @param path - (Base64 encoded) file to retrieve
     */
    @RequestMapping("/hdfs/get/{path}")
    public void getFile(HttpServletResponse response, @PathVariable String path) {
        try {
            path = Utilities.decodeBase64(path);
            HDFSEntry fileInfo = hadoopService.getHDFSFileInfo(path);
            if (!fileInfo.isDirectory()) {
                InputStream file = hadoopService.getHDFSFile(path);
                response.addHeader("Content-disposition", "attachment;filename=" + path);
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
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
