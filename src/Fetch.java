import com.ibm.db2.jcc.DB2Diagnosable;
import com.ibm.db2.jcc.DB2Sqlca;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import org.sql2o.data.Column;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Fetch implements TableMeta {
    private static final Sql2o db;

    static {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            db = new Sql2o(
                    "jdbc:db2://192.168.245.128:50000/sample:" +
                            "retrieveMessagesFromServerOnGetMessage=true;",
                    "student",
                    "student"
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public final String tableName;
    private Table table;
    private boolean memIsStale;

    public Fetch(String tableName) {
        this.tableName = tableName;

    }

    @Override
    public String getColumnName(int col) {
        return getTable().columns().get(col).getName();
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    private Table getTable() {
        if (table == null || memIsStale) {
            try (Connection con = db.open()) {
                table = con
                        .createQuery("select * from %s order by 1".formatted(tableName))
                        .executeAndFetchTable();
                memIsStale = false;
                return table;
            }
        } else {
            return table;
        }
    }

    public ArrayList<String[]> fetchAllRows() {
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

    public ArrayList<Object[]> fetchAllRowsAsObjects() {
        List<Row> rows = getTable().rows();
        int colCnt = getTable().columns().size();
        ArrayList<Object[]> ans = new ArrayList<>(rows.size());
        for (Row row : rows) {
            Object[] objects = new Object[colCnt];
            for (int i = 0; i < colCnt; i++) {
                objects[i] = row.getObject(i);
            }
            ans.add(objects);
        }
        return ans;
    }

    public String[][] fetchPredicate(String predicate) {
        try (Connection con = db.open()) {
            List<Row> rows = con
                    .createQuery("select * from %s where %s".formatted(tableName, predicate))
                    .executeAndFetchTable().rows();
            int colCnt = getTable().columns().size();
            ArrayList<String[]> ans = new ArrayList<>(rows.size());
            for (Row row : rows) {
                String[] rowText = new String[colCnt];
                for (int i = 0; i < colCnt; i++) {
                    rowText[i] = row.getString(i);
                }
                ans.add(rowText);
            }
            return ans.toArray(String[][]::new);
        }
    }

    private String defaultParams(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append(":p1");
        for (int i = 2; i < count + 1; i++) {
            sb.append(", :p%d".formatted(i));
        }
        return sb.toString();
    }

    public Throwable createRows(String[][] rows) {
        try {
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
            return null;
        } catch (Exception exception) {
            return exception;
        }
    }

    public String fetchErrorMessage(Throwable error) {
        Throwable unwrapped = error.getCause();
        if (unwrapped instanceof DB2Diagnosable) {
            DB2Sqlca sqlca = ((DB2Diagnosable) unwrapped).getSqlca();
            try (Connection con = db.open()) {
                return con.createQueryWithParams(
                        "values (sysproc.SQLERRM(:p1, :p2, ';', 'zh_CN', 1))",
                        "SQL" + Math.abs(sqlca.getSqlCode()), sqlca.getSqlErrmc()
                ).executeScalar(String.class);
            }
        } else {
            return error.getMessage();
        }
    }

    public String[] getColumnHeaders() {
        return table.columns().stream().map(Column::toString).toArray(String[]::new);
    }

    public int getPrimaryKeyColumn() {
        return 0;
    }

    public Throwable deleteRows(Object[] victims) {
        try (Connection con = db.beginTransaction()) {
            con.setRollbackOnException(true);
            String pkColName = getTable().columns().get(getPrimaryKeyColumn()).getName();
            Query kill = con.createQuery(
                    "delete from %s where %s = :pk".formatted(tableName, pkColName)
            );
            for (Object victim : victims) {
                kill.addParameter("pk", victim).addToBatch();
            }
            try {
                kill.executeBatch();
                con.commit();
            } catch (Sql2oException e) {
                return ((SQLException) e.getCause()).getNextException();
            }
        }
        memIsStale = true;
        return null;
    }

    public Throwable updateRows(Patch[] patches) {
        return null;
    }
}
