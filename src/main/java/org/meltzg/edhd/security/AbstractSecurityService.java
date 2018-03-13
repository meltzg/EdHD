package org.meltzg.edhd.security;

import java.util.Map;

import org.meltzg.edhd.db.DBServiceBase;

public abstract class AbstractSecurityService extends DBServiceBase {
	public abstract void addUser(String user);
	public abstract boolean isAdmin(String user);
	public abstract boolean updateAdmin(String user, Map<String, String> options);
	
	@Override
	public String TABLE_NAME() {
		return "users";
	}
}
