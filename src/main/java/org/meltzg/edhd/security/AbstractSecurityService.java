package org.meltzg.edhd.security;

import org.meltzg.edhd.db.DBServiceBase;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.sql.SQLException;
import java.util.Map;

/**
 * An abstract database service for interacting with EdHD security
 */
public abstract class AbstractSecurityService extends DBServiceBase implements UserDetailsService {
    /**
     * Adds a user to the database
     * @param user - user to add
     */
    public abstract void addUser(UserDTO user);

    /**
     * @param user - username to check admin privileges
     * @return  whether or not the user is an admin
     */
    public abstract boolean isAdmin(String user);

    /**
     * Used to update admin settings
     * @param user - username to update information
     * @param options - key/value pairs for updating the user.
     *                Must include password: ${edhd.adminPassword}
     * @return true if the user was successfully updated
     */
    public abstract boolean updateAdmin(String user, Map<String, String> options);

    /**
     *
     * @param user - user to check
     * @return whether the specified user exists
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public abstract boolean userExists(String user) throws ClassNotFoundException, SQLException;

    /**
     * Returns a user by username
     * @param user
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public abstract User getUser(String user) throws ClassNotFoundException, SQLException;
}
