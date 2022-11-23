import java.util.ArrayList;

public class FetchDecorator {
    final Blackboard blackboard;
    Fetch fetch;

    public FetchDecorator(Blackboard blackboard, Fetch fetch) {
        this.blackboard = blackboard;
        this.fetch = fetch;
    }

    public ArrayList<String[]> fetchAllRows() {
        blackboard.postInfo("查询 %s 表的所有行……".formatted(fetch.tableName));
        ArrayList<String[]> rows = fetch.fetchAllRows();
        blackboard.postInfo("查询到%d行".formatted(rows.size()));
        return rows;
    }

    public String[][] fetchPredicate(String predicate) {
        blackboard.postInfo("使用谓词查询");
        String[][] rows = fetch.fetchPredicate(predicate);
        blackboard.postInfo("查询到%d行".formatted(rows.length));
        return rows;
    }

    public void createRows(String[][] rows) {
        blackboard.postInfo("向数据库发送请求……");
        Throwable error = fetch.createRows(rows);
        handleError(error);
    }

    public String fetchErrorMessage(Throwable error) {
        return fetch.fetchErrorMessage(error);
    }

    public String[] getColumnHeaders() {
        return fetch.getColumnHeaders();
    }

    private void handleError(Throwable error) {
        if (error != null) {
            System.out.println("operation failed");
            System.out.println("error.getMessage() = " + error.getMessage());
            String errorMessage = fetchErrorMessage(error);
            System.out.println("fetch.fetchErrorMessage(error) = " + errorMessage);
            blackboard.postError("失败");
            blackboard.postError(errorMessage);
        } else {
            blackboard.postInfo("成功");
            System.out.println("operation is successful");
        }
    }
}
