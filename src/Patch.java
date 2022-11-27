public class Patch {
    private final Object pk;
    private final int modifiedColumn;
    private final Object newValue;
    private final TableMeta meta;

    public Patch(TableMeta meta, Object pk, int modifiedColumn, Object newValue) {
        this.meta = meta;
        this.pk = pk;
        this.modifiedColumn = modifiedColumn;
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        String colName = meta.getColumnName(modifiedColumn);
        String pkName = meta.getColumnName(meta.getPrimaryKeyColumn());
        return "%s 是 %s 的 %s 改为 %s".formatted(
                pkName,
                pk,
                colName,
                newValue
        );
    }
}
