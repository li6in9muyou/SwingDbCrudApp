import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.data.Column;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

import java.util.ArrayList;
import java.util.List;

public class Fetch {
    private static final Sql2o db;

    static {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            db = new Sql2o(
                    "jdbc:db2://192.168.245.128:50000/sample",
                    "student",
                    "student"
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final String tableName;
    private Table table;

    public Fetch(String tableName) {
        this.tableName = tableName;

    }

    private Table getTable() {
        if (table == null) {
            try (Connection con = db.open()) {
                table = con
                        .createQuery("select * from %s".formatted(tableName))
                        .executeAndFetchTable();
                return table;
            }
        } else {
            return table;
        }
    }

    public ArrayList<String[]> FetchAllRows() {
        List<Row> rows = getTable().rows();
        int colCnt = getTable().columns().size();

        ArrayList<String[]> ans = new ArrayList<>(rows.size());
        for (Row row : rows) {
            String[] rowText = new String[colCnt];
            for (int i = 0; i < colCnt; i++) {
                rowText[i] = row.getString(i);
            }
            ans.add(rowText);
        }
        return ans;
    }

    public void createRows(String[][] rows) {

    }
}
