import java.util.ArrayList;

public interface DbClient extends TableMeta, HelpfulDbClient {
    ArrayList<String[]> fetchAllRows();

    ArrayList<Object[]> fetchAllRowsAsObjects();

    String[][] fetchPredicate(String predicate);

    Throwable createRows(String[][] rows);

    Throwable deleteRows(Object[] victims);

    Throwable updateRows(Patch[] patches);

    Throwable initConnection();
}
