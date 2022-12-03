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

    void reportError(Exception error) {
        if (error != null) {
            System.out.println("operation failed");
            System.out.println("error.getMessage() = " + error.getMessage());
            String errorMessage = helpful.fetchErrorMessage((SQLException) error);
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
}