package org.meltzg.edhd.security;

import java.util.Map;

public interface ISecurityService {
	public void addUser(String user);
	public boolean isAdmin(String user);
	public boolean updateAdmin(String user, Map<String, String> options);
}
