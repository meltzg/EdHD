package org.meltzg.edhd.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

public class DBServiceBase {
	@Value("${spring.datasource.url}")
	private String dbUrl;

	@Value("${spring.datasource.username}")
	private String dbUser;

	@Value("${spring.datasource.password}")
	private String dbPassword;

	@Value("${edhd.dbName}")
	private String dbName;

	protected void init() throws Exception {
		Connection conn;
		try {
			conn = getConnection();
			conn.close();
		} catch (SQLException e) {
			System.err.println("Could not connect to database.  Attempting to create new database.");
			try {
				Connection defaultConn = getConnection("postgres");
				Statement s = defaultConn.createStatement();
				int result = s.executeUpdate(MessageFormat.format("CREATE DATABASE {0};", dbName));
				defaultConn.close();
			} catch (SQLException e1) {
				System.err.println("Could not connect to default postgres database.");
				throw e1;
			}
		}
	}

	protected Connection getConnection() throws ClassNotFoundException, SQLException {
		return getConnection(dbName);
	}

	protected Connection getConnection(String dbName) throws SQLException, ClassNotFoundException {
		Connection c = null;
		String fullUrl = dbUrl + "/" + dbName;
		Class.forName("org.postgresql.Driver");
		c = DriverManager.getConnection(fullUrl, dbUser, dbPassword);

		return c;
	}
	
	protected ResultSet executeQuery(String query, List<StatementParameter> params) throws SQLException, ClassNotFoundException {		
		return (ResultSet) executeQuery(query, params, false);
	}
	
	protected int executeUpdate(String query, List<StatementParameter> params) throws SQLException, ClassNotFoundException {		
		return (Integer) executeQuery(query, params, true);
	}
	
	private Object executeQuery(String query, List<StatementParameter> params, boolean isUpdate) throws SQLException, ClassNotFoundException {
		Object results = null;
		
		Connection conn = getConnection();
		PreparedStatement stmt = conn.prepareStatement(query);
		setStatementParams(stmt, params, conn);
		if (isUpdate) {
			results = stmt.executeUpdate();
		} else {
			results = stmt.executeQuery();
		}
		conn.close();
		
		return results;
	}

	private void setStatementParams(PreparedStatement stmt, List<StatementParameter> params, Connection conn) throws SQLException {
		if (params != null) {
			for (int i = 1; i <= params.size(); i++) {
				StatementParameter prm = params.get(i-1);
				switch (prm.getType()) {
				case ARRAY:
					Array tempArray = conn.createArrayOf(prm.getItemsType().toString(),
							((List) prm.getValue()).toArray());
					stmt.setArray(i, tempArray);
					break;
				case BIGINT:
					stmt.setLong(i, (Long) prm.getValue());
					break;
				case BOOLEAN:
					stmt.setBoolean(i, (Boolean) prm.getValue());
					break;
				case DOUBLE:
					stmt.setDouble(i, (Double) prm.getValue());
					break;
				case INT:
					stmt.setInt(i, (Integer) prm.getValue());
					break;
				case UUID:
					stmt.setObject(i, ((UUID) prm.getValue()));
					break;
				case TEXT:
					stmt.setString(i, (String) prm.getValue());
					break;
				default:
					System.err.println("Unsupported Parameter Type: " + prm.getType());
					break;
				}
			}
		}
	}

	protected enum DBType {
		ARRAY("array"), BIGINT("bigint"), BOOLEAN("boolean"), DOUBLE("double"), INT("integer"), UUID("uuid"), TEXT("text");

		private final String name;

		private DBType(String n) {
			name = n;
		}

		public boolean equalsName(String otherName) {
			return name.equals(otherName);
		}

		public String toString() {
			return this.name;
		}
	}

	protected class StatementParameter {
		private Object value;
		private DBType type;
		private DBType itemsType;

		public StatementParameter(Object value, DBType type) {
			this(value, type, null);
		}
		
		public StatementParameter(Object value, DBType type, DBType itemsType) {
			this.value = value;
			this.type = type;
			this.itemsType = itemsType;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public DBType getType() {
			return type;
		}

		public void setType(DBType type) {
			this.type = type;
		}

		public DBType getItemsType() {
			return itemsType;
		}

		public void setItemsType(DBType itemsType) {
			this.itemsType = itemsType;
		}
	}
}
