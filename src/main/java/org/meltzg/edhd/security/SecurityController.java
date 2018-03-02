package org.meltzg.edhd.security;

import java.security.Principal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityController {
	@RequestMapping("/user")
	public Principal user(Principal principal) {
		return principal;
	}
}
