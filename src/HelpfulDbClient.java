import java.sql.SQLException;

public interface HelpfulDbClient {
    String fetchErrorMessage(SQLException error);
}
