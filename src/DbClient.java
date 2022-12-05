import java.util.ArrayList;

public interface DbClient extends TableMeta, HelpfulDbClient {
    ArrayList<String[]> fetchAllRows();

    ArrayList<Object[]> fetchAllRowsAsObjects();

    Object[][] fetchPredicate(String predicate);

    Exception createRows(String[][] rows);

    Exception deleteRows(Object[] victims);

    Exception updateRows(Patch[] patches);

    Exception initConnection();
}
