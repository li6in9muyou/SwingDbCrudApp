import java.sql.SQLException;
import java.util.ArrayList;

public interface DbClient extends TableMeta {
    ArrayList<String[]> fetchAllRows();

    ArrayList<Object[]> fetchAllRowsAsObjects();

    String[][] fetchPredicate(String predicate);

    String fetchErrorMessage(SQLException error);

    Throwable createRows(String[][] rows);

    Throwable deleteRows(Object[] victims);

    Throwable updateRows(Patch[] patches);

    Throwable initConnection();
}
