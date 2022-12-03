import org.sql2o.Sql2oException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.Supplier;

public class FetchDecorator {
    final Blackboard blackboard;
    DbClient fetch;

    public FetchDecorator(Blackboard blackboard, DbClient fetch) {
        this.blackboard = blackboard;
        this.fetch = fetch;
    }

    private <T> T decorateFetchQuery(Supplier<T> fn, T defaultValue) {
        try {
            return fn.get();
        } catch (Sql2oException exception) {
            handleError(exception.getCause());
            return defaultValue;
        }
    }

    private <T extends Throwable> void decorateUpsert(Supplier<T> fn) {
        Throwable error = fn.get();
        handleError(error);
    }

    public String[] getColumnHeaders() {
        return fetch.getColumnHeaders();
    }

    public int getPrimaryKeyColumn() {
        return fetch.getPrimaryKeyColumn();
    }

    public ArrayList<String[]> fetchAllRows() {
        return decorateFetchQuery(
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
        return decorateFetchQuery(
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
        return decorateFetchQuery(
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
        decorateUpsert(() -> fetch.createRows(rows));
    }

    private void handleError(Throwable error) {
        if (error != null) {
            System.out.println("operation failed");
            System.out.println("error.getMessage() = " + error.getMessage());
            String errorMessage = fetch.fetchErrorMessage((SQLException) error);
            if (!errorMessage.isEmpty()) {
                blackboard.postError("失败，数据库报告错误");
                System.out.println("fetch.fetchErrorMessage(error) = " + errorMessage);
                blackboard.postError(errorMessage);
            } else {
                errorMessage = error.toString();
                blackboard.postError("失败，本地程序错误");
                System.out.println("fetch.fetchErrorMessage(error) = " + errorMessage);
                blackboard.postError(errorMessage);
            }
        } else {
            blackboard.postInfo("成功");
            System.out.println("operation is successful");
        }
    }

    public void deleteRows(Object[] victims) {
        blackboard.postError("即将删除行");
        decorateUpsert(() -> fetch.deleteRows(victims));
    }

    public Patch createPatch(Object pk, int modifiedCol, Object newVal) {
        return new Patch(fetch, pk, modifiedCol, newVal);
    }

    public void commitPatches(Patch[] patches) {
        blackboard.postInfo("提交暂存区中的更改");
        decorateUpsert(() -> fetch.updateRows(patches));
    }

    public boolean initConnection() {
        blackboard.postInfo("尝试连接到数据库");
        Throwable error = fetch.initConnection();
        handleError(error);
        return error != null;
    }
}
