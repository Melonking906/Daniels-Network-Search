package net.danielsnet.indexer;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class DatabaseConnect {
    private static final String HOST = "jdbc:mysql://127.0.0.1:{PORT}/XXX";
    private static final String USER = "XXX";
    private static final String PASS = "XXX";
    private static final String ENCODE = "?useUnicode=true&character_set_server=utf8mb4";

    private int proxyPort;
    private Connection connection;

    public static HashMap<String, String> getFirstRow(String sqlQuery) {
        return getFirstRow(Main.getDatabase().query(sqlQuery, true));
    }

    public static HashMap<String, String> getFirstRow(HashSet<HashMap<String, String>> data) {
        Optional<HashMap<String, String>> row = data.stream().findFirst();
        return row.orElse(null);
    }

    public static String getOnlyValue(String sqlQuery, String column) {
        return getOnlyValue(Main.getDatabase().query(sqlQuery, true), column);
    }

    public static String getOnlyValue(HashSet<HashMap<String, String>> data, String column) {
        Optional<HashMap<String, String>> row = data.stream().findFirst();
        return row.map(innerRow -> innerRow.get(column)).orElse(null);
    }

    public static HashSet<String> getOnlyValueList(HashSet<HashMap<String, String>> data, String column) {
        if (data == null) {
            return null;
        }
        HashSet<String> list = new HashSet<>();
        for (HashMap<String, String> row : data) {
            String id = row.get(column);
            if (id == null || id.equals("")) {
                continue;
            }

            list.add(id);
        }
        return list;
    }

    public boolean test() {
        return query("SELECT VERSION()", true) != null;
    }

    public String getVersion() {
        return getOnlyValue("SELECT VERSION();", "VERSION()");
    }

    public void connect() {
        // Test if a new connection needs to be made.
        boolean needToConnect = false;
        try {
            if (connection == null) {
                needToConnect = true;
            } else if (connection.isClosed()) {
                needToConnect = true;
            }
        } catch (SQLException exception) {
            UI.log("Unexpected database connection test error.");
            return;
        }
        if (!needToConnect) {
            return;
        }

        // Connect to SSH proxy
        try {
            JSch jsch = new JSch();

            Session session = jsch.getSession("XXX", "XXX", 22);
            session.setPassword("XXX");

            // Additional SSH options.  See your ssh_config manual for
            // more options.  Set options according to your requirements.
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("Compression", "yes");
            config.put("ConnectionAttempts", "2");

            session.setConfig(config);

            // Connect
            session.connect();

            proxyPort = session.setPortForwardingL("0.0.0.0", 3308, "XXX", 3306);

        } catch (JSchException e) {
            e.printStackTrace();
            UI.log("Could not connect to proxy server");
        }

        // Connect to the SQL server
        try {
            String url = HOST.replace("{PORT}", proxyPort + "")+ENCODE;
            connection = DriverManager.getConnection(url, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            UI.log("Database connection issue");
        }
    }

    public HashSet<HashMap<String, String>> query(String sql, boolean hasReturn) {
        return query(sql, hasReturn, true);
    }

    public HashSet<HashMap<String, String>> query(String sql, boolean hasReturn, boolean showException) {
        connect(); // Connect function will test if a new connection is needed.

        PreparedStatement statement = null;
        ResultSet results;
        ResultSetMetaData metaData;

        HashSet<HashMap<String, String>> returnData = new HashSet<>();

        try {
            statement = connection.prepareStatement(sql);

            if (!hasReturn) {
                statement.execute(sql);
            } else {
                results = statement.executeQuery(sql);
                metaData = results.getMetaData();

                while (results.next()) {
                    HashMap<String, String> row = new HashMap<>();

                    for (int i = 1; i <= metaData.getColumnCount(); ++i) {
                        row.put(metaData.getColumnName(i), results.getObject(i).toString());
                    }

                    returnData.add(row);
                }

//                if (returnData.isEmpty()) {
//                    returnData = null;
//                }
            }
        } catch (SQLException ex) {
            if (showException) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    //connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return returnData;
    }
}
