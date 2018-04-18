package org.meltzg.edhd.security;

import org.meltzg.edhd.db.DBServiceBase;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.sql.SQLException;
import java.util.Map;

public abstract class AbstractSecurityService extends DBServiceBase implements UserDetailsService {
    public abstract void addUser(UserDTO user);

    public abstract boolean isAdmin(String user);

    public abstract boolean updateAdmin(String user, Map<String, String> options);

    public abstract boolean userExists(String user) throws ClassNotFoundException, SQLException;

    public abstract User getUser(String user) throws ClassNotFoundException, SQLException;
}
