import org.sql2o.Sql2oException;

import java.util.ArrayList;
import java.util.function.Supplier;

public class FetchDecorator {
    final Blackboard blackboard;
    Fetch fetch;

    public FetchDecorator(Blackboard blackboard, Fetch fetch) {
        this.blackboard = blackboard;
        this.fetch = fetch;
    }

    public int getPrimaryKeyColumn() {
        return fetch.getPrimaryKeyColumn();
    }

    public ArrayList<String[]> fetchAllRows() {
        return decorateFetchQuery(
                () -> {
                    blackboard.postInfo("查询 %s 表的所有行……".formatted(fetch.tableName));
                    ArrayList<String[]> rows = new ArrayList<>(fetch.fetchAllRows());
                    blackboard.postInfo("查询到%d行".formatted(rows.size()));
                    return rows;
                },
                new ArrayList<>()
        );
    }

    private <T> T withFailureReporting(Supplier<T> fn, T defaultValue) {
        try {
            return fn.get();
        } catch (Sql2oException exception) {
            handleError(exception.getCause());
            return defaultValue;
        }
    }

    public Object[][] fetchAllRowsAsObjects() {
        return withFailureReporting(
                () -> {
                    blackboard.postInfo("查询 %s 表的所有行……".formatted(fetch.tableName));
                    ArrayList<Object[]> rows = new ArrayList<>(fetch.fetchAllRowsAsObjects());
                    blackboard.postInfo("查询到%d行".formatted(rows.size()));
                    return rows.toArray(Object[][]::new);
                },
                new Object[0][0]
        );
    }

    public String[][] fetchPredicate(String predicate) {
        return withFailureReporting(
                () -> {
                    String[][] rows = fetch.fetchPredicate(predicate);
                    blackboard.postInfo("查询到%d行".formatted(rows.length));
                    return rows;
                },
                new String[0][0]
        );
    }

    public void createRows(String[][] rows) {
        blackboard.postInfo("向数据库发送请求……");
        Throwable error = fetch.createRows(rows);
        handleError(error);
    }

    public String[] getColumnHeaders() {
        return fetch.getColumnHeaders();
    }

    private void handleError(Throwable error) {
        if (error != null) {
            System.out.println("operation failed");
            System.out.println("error.getMessage() = " + error.getMessage());
            String errorMessage = fetch.fetchErrorMessage(error);
            System.out.println("fetch.fetchErrorMessage(error) = " + errorMessage);
            blackboard.postError("失败");
            blackboard.postError(errorMessage);
        } else {
            blackboard.postInfo("成功");
            System.out.println("operation is successful");
        }
    }

    public void deleteRows(Object[] victims) {
        blackboard.postError("即将删除行");
        Throwable error = fetch.deleteRows(victims);
        handleError(error);
    }

    public Patch createPatch(Object pk, int modifiedCol, Object newVal) {
        return new Patch(fetch, pk, modifiedCol, newVal);
    }

    public void commitPatches(Patch[] patches) {
        blackboard.postInfo("提交暂存区中的更改");
        Throwable error = fetch.updateRows(patches);
        handleError(error);
    }
}
