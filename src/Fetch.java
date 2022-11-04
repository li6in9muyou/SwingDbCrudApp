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

    static ArrayList<String[]> FetchAllRows(String table_name) {
        try {
            Connection db = DriverManager.getConnection(
                    "jdbc:db2://192.168.245.128:50000/sample",
                    "student",
                    "student"
            );

            PreparedStatement query = db.prepareStatement("select * from " + table_name);
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
}
