package org.meltzg.edhd.security;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityController {

	@Autowired
	ISecurityService securityService;

	@RequestMapping("/user")
	public Principal user(Principal principal) {
		securityService.addUser(principal.getName());
		return principal;
	}

	@RequestMapping("/is-admin/{user}")
	public Map<String, Boolean> isAdmin(@PathVariable("user") String user) {
		return Collections.singletonMap("isAdmin", securityService.isAdmin(user));
	}

	@RequestMapping(value = "/make-admin/{user}/{is-admin}", method = RequestMethod.POST, consumes = "application/json")
	public Map<String, Boolean> makeAdmin(@PathVariable("user") String user,
			@PathVariable("is-admin") Boolean isAdmin,
			@RequestBody Map<String, Boolean> body) {
		boolean success = false;
		return Collections.singletonMap("success", success);
	}
}
