import java.sql.*;
import java.util.ArrayList;

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    }
}
