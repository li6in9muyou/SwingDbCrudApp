public class Patch {
    private final Object primaryKey;
    private final int modifiedColumn;
    private final Object newValue;
    private final TableMeta metaData;

    public Patch(TableMeta metaData, Object primaryKey, int modifiedColumn, Object newValue) {
        this.metaData = metaData;
        this.primaryKey = primaryKey;
        this.modifiedColumn = modifiedColumn;
        this.newValue = newValue;
    }

    public Object getPrimaryKeyValue() {
        return primaryKey;
    }

    public int getModifiedColumn() {
        return modifiedColumn;
    }

    public Object getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        String nameOfModifiedColumn = metaData.getColumnName(modifiedColumn);
        String nameOfPrimaryKeyColumn = metaData.getColumnName(metaData.getPrimaryKeyColumn());
        return "%s 是 %s 的 %s 改为 %s".formatted(
                nameOfPrimaryKeyColumn,
                primaryKey,
                nameOfModifiedColumn,
                newValue
        );
    }
}
