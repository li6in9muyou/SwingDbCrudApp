import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.function.Supplier;

public class ErrorReporter {
    final Blackboard blackboard;
    private final HelpfulDbClient helpful;

    public ErrorReporter(Blackboard blackboard, HelpfulDbClient helpful) {
        this.blackboard = blackboard;
        this.helpful = helpful;
    }

    <T> T catchQuery(Supplier<T> fn, T defaultValue) {
        try {
            return fn.get();
        } catch (Exception exception) {
            reportError(exception);
            return defaultValue;
        }
    }

    private void reportBatchException(BatchUpdateException ignoredBatchWrapper) {
        SQLException e = ignoredBatchWrapper;
        while (true) {
            e = e.getNextException();
            if (e == null) {
                break;
            }
            blackboard.postError(e.getMessage());
            reportError(e);
        }
    }

    private void reportJavaException(Exception error) {
        String errorMessage = error.toString();
        blackboard.postError("失败，本地程序错误");
        blackboard.postError(errorMessage);
    }

    private void reportSQLException(SQLException error) {
        String errorMessage = helpful.fetchErrorMessage(error);
        blackboard.postError("失败，数据库报告错误");
        blackboard.postError(errorMessage);
    }

    void reportError(Exception error) {
        if (error == null) {
            blackboard.postInfo("成功");
        } else {
            if (error instanceof BatchUpdateException e) {
                reportBatchException(e);
            } else if (error instanceof SQLException e) {
                reportSQLException(e);
            } else {
                reportJavaException(error);
            }
        }
    }
}