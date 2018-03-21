package org.meltzg.edhd.security;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityService extends AbstractSecurityService {

	private static final String NAME = "username";
	private static final String ISADMIN = "isAdmin";
	private static final String PASSWORD = "password";

	@Value("${edhd.adminPassword}")
	private String adminPassword;

	@PostConstruct
	public void init() throws Exception {
		super.init();
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME() + " (" + "" + NAME + " TEXT, " + PASSWORD
				+ " TEXT, " + ISADMIN + " BOOLEAN DEFAULT FALSE, " + "PRIMARY KEY(" + NAME + "))";
		statement.executeUpdate(createUsers);
		conn.close();
	}

	@Override
	public String TABLE_NAME() {
		return "users";
	}

	@Override
	public void addUser(UserDTO user) {
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(user.getUsername(), DBType.TEXT));
		params.add(new StatementParameter(user.getPassword(), DBType.TEXT));
		try {
			if (!userExists(user.getUsername())) {
				int inserted = executeUpdate(
						"INSERT INTO " + TABLE_NAME() + " (" + NAME + ", " + PASSWORD + ") VALUES (?, ?);", params);
				if (inserted == 0) {
					System.err.println("Could not add user " + user);
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isAdmin(String username) {
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(username, DBType.TEXT));
		try {
			ResultSet rs = executeQuery("SELECT isAdmin FROM " + TABLE_NAME() + " WHERE " + NAME + " = ?;", params);
			if (rs.next()) {
				return rs.getBoolean("isAdmin");
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean updateAdmin(String username, Map<String, String> options) {
		String password = options.get("password");
		if (!this.adminPassword.equals(password)) {
			return false;
		}
		if (options.containsKey("isAdmin")) {
			boolean isAdmin = Boolean.parseBoolean(options.get("isAdmin"));
			List<StatementParameter> params = new ArrayList<StatementParameter>();
			params.add(new StatementParameter(isAdmin, DBType.BOOLEAN));
			params.add(new StatementParameter(username, DBType.TEXT));

			try {
				executeUpdate("UPDATE " + TABLE_NAME() + " SET isAdmin = ? WHERE " + NAME + " = ?;", params);
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean userExists(String username) throws ClassNotFoundException, SQLException {
		return getUser(username) != null;
	}

	@Override
	public User getUser(String username) throws ClassNotFoundException, SQLException {
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(username, DBType.TEXT));
		ResultSet rs = executeQuery("SELECT * FROM " + TABLE_NAME() + " WHERE " + NAME + " = ?;", params);

		if (rs.next()) {
			String password = rs.getString(PASSWORD);
			UserBuilder builder = User.withUsername(username).password(password).roles("USER");
			return (User) builder.build();
		}

		return null;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			User user = getUser(username);
			if (user == null) {
				throw new UsernameNotFoundException("No user found with username: " + username);
			}
			boolean enabled = true;
			boolean accountNonExpired = true;
			boolean credentialsNonExpired = true;
			boolean accountNonLocked = true;
			return new org.springframework.security.core.userdetails.User(user.getUsername(),
					user.getPassword().toLowerCase(), enabled, accountNonExpired, credentialsNonExpired,
					accountNonLocked, user.getAuthorities());
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UsernameNotFoundException("No user found with username: " + username);
		}
	}
}
