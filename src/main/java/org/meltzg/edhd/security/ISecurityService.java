package org.meltzg.edhd.security;

public interface ISecurityService {
	public void addUser(String user);
	public boolean isAdmin(String user);
	public boolean setAdmin(String user, boolean isAdmin, String password);
}
