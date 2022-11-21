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
            db = new Sql2o("jdbc:db2://192.168.245.128:50000/sample", "student", "student");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final String tableName;
    private Table table;
    private boolean memIsStale;

    public Fetch(String tableName) {
        this.tableName = tableName;

    }

    private Table getTable() {
        if (table == null || memIsStale) {
            try (Connection con = db.open()) {
                table = con
                        .createQuery("select * from %s".formatted(tableName))
                        .executeAndFetchTable();
                memIsStale = false;
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

    private String defaultParams(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append(":p1");
        for (int i = 2; i < count + 1; i++) {
            sb.append(", :p%d".formatted(i));
        }
        return sb.toString();
    }

    public void createRows(String[][] rows) {
        try (Connection con = db.beginTransaction()) {
            con.setRollbackOnException(true);
            for (String[] row : rows) {
                con.createQueryWithParams(
                        "insert into %s values ( %s )".formatted(tableName, defaultParams(getTable().columns().size())),
                        row[0],
                        row[1],
                        row[2],
                        row[3],
                        row[4],
                        row[5],
                        row[6],
                        row[7],
                        Integer.parseInt(row[8]),
                        row[9],
                        row[10],
                        Double.parseDouble(row[11]),
                        Double.parseDouble(row[12]),
                        Double.parseDouble(row[13])
                ).executeUpdate();
                con.commit();
                memIsStale = true;
            }
        }
    }

    public String[] getColumnHeaders() {
        return table.columns().stream().map(Column::toString).toArray(String[]::new);
    }
}
