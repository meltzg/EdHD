package org.meltzg.edhd.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SecurityController {

    @Autowired
    private AbstractSecurityService securityService;

    @RequestMapping("/user")
    public Principal user(Principal principal) {
        return principal;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody @Valid UserDTO accountInfo) {
        Map<String, Object> responseBody = new HashMap<String, Object>();
        responseBody.put("success", false);

        try {
            if (securityService.userExists(accountInfo.getUsername())) {
                responseBody.put("message", "A user " + accountInfo.getUsername() + " already exists");
            } else {
                securityService.addUser(accountInfo);
                responseBody.put("success", true);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }

        return ResponseEntity.ok().body(responseBody);
    }

    @RequestMapping("/admin/is/{user:.+}")
    public Map<String, Boolean> isAdmin(@PathVariable String user) {
        return Collections.singletonMap("isAdmin", securityService.isAdmin(user));
    }

    @RequestMapping(value = "/admin/update", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Map<String, Object>> updateUser(Principal principal, @RequestBody Map<String, String> body) {
        boolean success = securityService.updateAdmin(principal.getName(), body);
        Map<String, Object> returnBody = new HashMap<String, Object>();
        returnBody.put("success", success);

        if (success) {
            return ResponseEntity.ok(returnBody);
        } else {
            returnBody.put("message", "Unable to authorize admin setting update");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnBody);
        }
    }
}
