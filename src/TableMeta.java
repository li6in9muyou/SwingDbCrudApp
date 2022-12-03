public interface TableMeta {
    String getColumnName(int col);

    int getPrimaryKeyColumn();

    String[] getColumnHeaders();

    String getCurrentTableName();
}
