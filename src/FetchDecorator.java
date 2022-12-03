import java.util.ArrayList;

public class FetchDecorator {
    private final ErrorReporter errorReporter;
    private final Blackboard blackboard;
    private final DbClient fetch;

    public FetchDecorator(Blackboard blackboard, DbClient fetch) {
        this.errorReporter = new ErrorReporter(blackboard, fetch);
        this.blackboard = blackboard;
        this.fetch = fetch;
    }

    public String[] getColumnHeaders() {
        return fetch.getColumnHeaders();
    }

    public int getPrimaryKeyColumn() {
        return fetch.getPrimaryKeyColumn();
    }

    public ArrayList<String[]> fetchAllRows() {
        return errorReporter.catchQuery(
                () -> {
                    blackboard.postInfo("查询 %s 表的所有行……".formatted(fetch.getCurrentTableName()));
                    ArrayList<String[]> rows = new ArrayList<>(fetch.fetchAllRows());
                    blackboard.postInfo("查询到%d行".formatted(rows.size()));
                    return rows;
                },
                new ArrayList<>()
        );
    }

    public Object[][] fetchAllRowsAsObjects() {
        return errorReporter.catchQuery(
                () -> {
                    blackboard.postInfo("查询 %s 表的所有行……".formatted(fetch.getCurrentTableName()));
                    ArrayList<Object[]> rows = new ArrayList<>(fetch.fetchAllRowsAsObjects());
                    blackboard.postInfo("查询到%d行".formatted(rows.size()));
                    return rows.toArray(Object[][]::new);
                },
                new Object[0][0]
        );
    }

    public String[][] fetchPredicate(String predicate) {
        return errorReporter.catchQuery(
                () -> {
                    String[][] rows = fetch.fetchPredicate(predicate);
                    blackboard.postInfo("查询到%d行".formatted(rows.length));
                    return rows;
                },
                new String[0][0]
        );
    }

    public void createRows(String[][] rows) {
        errorReporter.reportError(fetch.createRows(rows));
    }

    public void deleteRows(Object[] victims) {
        errorReporter.reportError(fetch.deleteRows(victims));
    }

    public Patch createPatch(Object pk, int modifiedCol, Object newVal) {
        return new Patch(fetch, pk, modifiedCol, newVal);
    }

    public void commitPatches(Patch[] patches) {
        errorReporter.reportError(fetch.updateRows(patches));
    }

    public boolean initConnection() {
        Throwable error = fetch.initConnection();
        errorReporter.reportError(error);
        return error != null;
    }
}
