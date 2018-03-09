package org.meltzg.edhd.security;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.meltzg.edhd.db.DBServiceBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecurityService extends DBServiceBase implements ISecurityService {

	@Value("${edhd.adminPassword}")
	private String adminPassword;

	@PostConstruct
	public void init() throws SQLException, ClassNotFoundException {
		super.init();
		Connection conn = getConnection();
		Statement statement = conn.createStatement();
		String createUsers = "CREATE TABLE IF NOT EXISTS users (" + "name TEXT, " + "isAdmin BOOLEAN DEFAULT FALSE, "
				+ "PRIMARY KEY(name))";
		statement.executeUpdate(createUsers);
		conn.close();
	}

	@Override
	public void addUser(String user) {
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(user, DBType.TEXT));
		try {
			ResultSet rs = executeQuery("SELECT * FROM users WHERE name = ?;", params);
			if (!rs.next()) {
				int inserted = executeUpdate("INSERT INTO users (name) VALUES (?);", params);
				if (inserted == 0) {
					System.err.println("Could not add user " + user);
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isAdmin(String user) {
		List<StatementParameter> params = new ArrayList<StatementParameter>();
		params.add(new StatementParameter(user, DBType.TEXT));
		try {
			ResultSet rs = executeQuery("SELECT isAdmin FROM users WHERE name = ?;", params);
			if (rs.next()) {
				return rs.getBoolean("isAdmin");
			}
		}  catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean updateAdmin(String user, Map<String, String> options) {
		String password = options.get("password");
		if (!this.adminPassword.equals(password)) {
			return false;
		}
		if (options.containsKey("isAdmin")) {
			boolean isAdmin = Boolean.parseBoolean(options.get("isAdmin"));
			List<StatementParameter> params = new ArrayList<StatementParameter>();
			params.add(new StatementParameter(isAdmin, DBType.BOOLEAN));
			params.add(new StatementParameter(user, DBType.TEXT));

			try {
				executeUpdate("UPDATE users SET isAdmin = ? WHERE name = ?;", params);
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}
}