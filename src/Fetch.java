import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

public class Fetch {
    static {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final String tableName;
    private final Connection db;

    public Fetch(String tableName) {
        this.tableName = tableName;
        try {
            db = DriverManager.getConnection(
                    "jdbc:db2://192.168.245.128:50000/sample",
                    "student",
                    "student"
            );
            db.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getColumnHeaders() {
        Vector<String> columnsNames = new Vector<>();
        try {
            ResultSet columnMetaDate = db.getMetaData().getColumns(
                    null,
                    null,
                    tableName.toUpperCase(),
                    null
            );
            while (columnMetaDate.next()) {
                columnsNames.add("%s (%s)".formatted(
                        columnMetaDate.getString("COLUMN_NAME"),
                        columnMetaDate.getString("TYPE_NAME")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return columnsNames.toArray(String[]::new);
    }

    public ArrayList<String[]> FetchAllRows() {
        try {
            PreparedStatement query = db.prepareStatement("select * from " + tableName);
            ResultSet rs = query.executeQuery();
            ArrayList<String[]> rows = new ArrayList<>(rs.getFetchSize());
            int column_count = rs.getMetaData().getColumnCount();
            for (; rs.next(); rs.next()) {
                String[] row = new String[column_count];
                for (int i = 1; i <= column_count; i++) {
                    Object obj = rs.getObject(i);
                    row[i - 1] = (obj == null) ? "<null>" : obj.toString();
                }
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createRows(String[][] rows) {
        if (rows.length == 0) {
            return;
        }
        int columnCount = rows[0].length;
        String questionMarks = "?" + ",?".repeat(columnCount - 1);
        String sql = "insert into %s values ( %s )".formatted(tableName, questionMarks);
        try {
            PreparedStatement insertQuery = db.prepareStatement(sql);
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    insertQuery.setString(i + 1, row[i]);
                }
                insertQuery.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
