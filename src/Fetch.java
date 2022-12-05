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

public class Fetch implements DbClient, HelpfulDbClient {
    public final String tableName;
    private Sql2o sql2o;
    private Table table;
    private boolean memIsStale;

    public Fetch(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getCurrentTableName() {
        return tableName;
    }

    public Exception initConnection() {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            sql2o = new Sql2o(
                    "jdbc:db2://192.168.245.128:50000/sample:"
                            + "retrieveMessagesFromServerOnGetMessage=true;"
                            + "progressiveStreaming=2;", // 2 means NO
                    "student",
                    "student"
            );
        } catch (ClassNotFoundException e) {
            return e;
        }
        return null;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return getTable().columns().get(columnIndex).getName();
    }

    private int getColumnCount() {
        return getTable().columns().size();
    }

    private Table getTable() {
        if (table == null || memIsStale) {
            try (Connection connection = sql2o.open()) {
                table = connection
                        .createQuery("select * from %s order by 1".formatted(tableName))
                        .executeAndFetchTable();
                memIsStale = false;
                return table;
            }
        } else {
            return table;
        }
    }

    @Override
    public ArrayList<String[]> fetchAllRows() {
        List<Row> rows = getTable().rows();
        int colCnt = getColumnCount();

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

    @Override
    public ArrayList<Object[]> fetchAllRowsAsObjects() {
        List<Row> rows = getTable().rows();
        ArrayList<Object[]> ans = new ArrayList<>(rows.size());
        for (Row row : rows) {
            int c = getColumnCount();
            Object[] objects = new Object[c];
            for (int i = 0; i < c; i++) {
                objects[i] = row.getObject(i);
            }
            ans.add(objects);
        }
        return ans;
    }

    @Override
    public Object[][] fetchPredicate(String predicate) {
        try (Connection connection = sql2o.open()) {
            List<Row> rows = connection
                    .createQuery("select * from %s where %s".formatted(tableName, predicate))
                    .executeAndFetchTable().rows();
            int colCnt = getColumnCount();
            ArrayList<Object[]> ans = new ArrayList<>(rows.size());
            for (Row row : rows) {
                Object[] thisRow = new Object[colCnt];
                thisRow[0] = row.getObject(0);
                thisRow[1] = row.getObject(1);
                thisRow[2] = row.getObject(2, byte[].class);
                ans.add(thisRow);
            }
            return ans.toArray(Object[][]::new);
        }
    }

    private String makeParamMarkers(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append(":p1");
        for (int i = 2; i < count + 1; i++) {
            sb.append(", :p%d".formatted(i));
        }
        return sb.toString();
    }

    @Override
    public Exception createRows(Object[][] rows) {
        try {
            try (Connection connection = sql2o.beginTransaction()) {
                connection.setRollbackOnException(true);
                Query insert = connection.createQueryWithParams(
                        "insert into %s values ( %s )".formatted(
                                tableName, makeParamMarkers(getColumnCount())
                        )
                );
                for (Object[] row : rows) {
                    insert.withParams(
                            row[0],
                            row[1],
                            row[2]
                    ).addToBatch();
                }
                insert.executeBatch();
                connection.commit();
                memIsStale = true;
            }
            return null;
        } catch (Sql2oException e) {
            return (Exception) e.getCause();
        } catch (Exception exception) {
            return exception;
        }
    }

    private boolean isDB2Error(Throwable error) {
        return error.getCause() instanceof DB2Diagnosable || error instanceof DB2Diagnosable;
    }

    @Override
    public String fetchErrorMessage(SQLException error) {
        if (!isDB2Error(error)) {
            return "";
        }
        DB2Sqlca sqlca = ((DB2Diagnosable) error).getSqlca();
        try (Connection con = sql2o.open()) {
            return con.createQueryWithParams(
                    "values (sysproc.SQLERRM(:p1, :p2, ';', 'zh_CN', 1))",
                    "SQL" + Math.abs(sqlca.getSqlCode()), sqlca.getSqlErrmc()
            ).executeScalar(String.class);
        }
    }

    public String[] getColumnHeaders() {
        return getTable().columns().stream().map(Column::toString).toArray(String[]::new);
    }

    @Override
    public int getPrimaryKeyColumn() {
        return 0;
    }

    @Override
    public Exception deleteRows(Object[] victims) {
        try (Connection connection = sql2o.beginTransaction()) {
            connection.setRollbackOnException(true);
            String pkColName = getColumnName(getPrimaryKeyColumn());
            Query kill = connection.createQuery(
                    "delete from %s where %s = :pk".formatted(tableName, pkColName)
            );
            for (Object victim : victims) {
                kill.addParameter("pk", victim).addToBatch();
            }
            try {
                kill.executeBatch();
                connection.commit();
            } catch (Sql2oException e) {
                return (Exception) e.getCause();
            } catch (Exception e) {
                return e;
            }
        }
        memIsStale = true;
        return null;
    }

    @Override
    public Exception updateRows(Patch[] patches) {
        try (Connection connection = sql2o.beginTransaction()) {
            connection.setRollbackOnException(true);
            for (Patch patch : patches) {
                String withModifiedCol = "UPDATE %s t SET t.%s = :newVal WHERE t.%s LIKE :pk"
                        .formatted(
                                tableName,
                                getColumnName(patch.getModifiedColumn()),
                                getColumnName(getPrimaryKeyColumn())
                        );
                try {
                    connection.createQuery(withModifiedCol)
                            .addParameter("pk", patch.getPrimaryKeyValue())
                            .addParameter("newVal", patch.getNewValue())
                            .executeUpdate();
                } catch (Sql2oException e) {
                    return (Exception) e.getCause();
                } catch (Exception exception) {
                    return exception;
                }
            }
            connection.commit();
        }
        memIsStale = true;
        return null;
    }
}
