package org.meltzg.edhd.db;

import org.springframework.beans.factory.annotation.Value;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract utility class for creating Postgres SQL database services
 */
public abstract class DBServiceBase {

    /**
     * For all of the DBServiceBase methods to work, the table must have a UUID id column
     */
    protected static final String ID = "id";

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${edhd.dbName}")
    private String dbName;

    /**
     * @return The table name for this database service
     */
    public abstract String TABLE_NAME();

    /**
     * Initializes the database.  If a connection can't be made to the dbName database,
     * this will attempt to create the database.
     *
     * Derivative classes should override init (make sure to call super.init()) and initialize
     * their table
     *
     * @throws Exception
     */
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

    /**
     * @return a connection to this service's database
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    protected Connection getConnection() throws ClassNotFoundException, SQLException {
        return getConnection(dbName);
    }

    /**
     * @param dbName - name of the database to connect to at dbUrl
     * @return a connection to the requested database
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    protected Connection getConnection(String dbName) throws SQLException, ClassNotFoundException {
        Connection c = null;
        String fullUrl = dbUrl + "/" + dbName;
        Class.forName("org.postgresql.Driver");
        c = DriverManager.getConnection(fullUrl, dbUser, dbPassword);

        return c;
    }

    /**
     * Executes a query (SELECT) on this service's table
     *
     * @param query - string query to use for creating a PreparedStatement
     * @param params - query parameters to be inserted into the query
     *               The parameters are inserted in the query in order
     * @return Query's ResultSet
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    protected ResultSet executeQuery(String query, List<StatementParameter> params)
            throws SQLException, ClassNotFoundException {
        return (ResultSet) executeQuery(query, params, false);
    }

    /**
     * Executes an update query (INSERT, UPDATE, DELETE) on this service's table
     * @param query
     * @param params
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    protected int executeUpdate(String query, List<StatementParameter> params)
            throws SQLException, ClassNotFoundException {
        return (Integer) executeQuery(query, params, true);
    }

    /**
     * Deletes a row by its ID
     *
     * @param id - ID of the row to delete
     * @return number of affected rows (should be 1)
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    protected int deleteById(UUID id) throws ClassNotFoundException, SQLException {
        List<StatementParameter> params = new ArrayList<StatementParameter>();
        params.add(new StatementParameter(id, DBType.UUID));
        return executeUpdate("DELETE FROM " + TABLE_NAME() + " WHERE " + ID + " = ?;", params);
    }

    /**
     * Executes a query on this service's table
     *
     * @param query - query to execute
     * @param params - parameters to insert into query
     * @param isUpdate - whether this is a selection or an update
     * @return Integer if isUpdate else ResultSet
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private Object executeQuery(String query, List<StatementParameter> params, boolean isUpdate)
            throws SQLException, ClassNotFoundException {
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

    /**
     * Sets the PreParedStatement stmt's fields to the values in params
     *
     * @param stmt - PreparedStatement without its fields replaced with values
     * @param params - values to insert into the PreparedStatement
     * @param conn - database connection
     * @throws SQLException
     */
    private void setStatementParams(PreparedStatement stmt, List<StatementParameter> params, Connection conn)
            throws SQLException {
        if (params != null) {
            for (int i = 1; i <= params.size(); i++) {
                StatementParameter prm = params.get(i - 1);
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

    /**
     * Enum represents the SQL data types supported by the DBServiceBase
     */
    protected enum DBType {
        ARRAY("array"), BIGINT("bigint"), BOOLEAN("boolean"), DOUBLE("double"), INT("integer"), UUID("uuid"), TEXT(
                "text");

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

    /**
     * Represents a value to use in a PreparedStatement
     */
    protected class StatementParameter {
        private Object value;
        private DBType type;
        /**
         * The type of elements if this's type is DBType.ARRAY
         */
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
