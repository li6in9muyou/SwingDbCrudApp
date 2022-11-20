import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class DatabaseTableDataModel extends AbstractTableModel {
    private final String[] header;
    ArrayList<String[]> data;

    public DatabaseTableDataModel(ArrayList<String[]> data, String[] header) {
        this.data = data;
        this.header = header;
    }

    @Override
    public String getColumnName(int column) {
        return header[column];
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return data.get(0).length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex)[columnIndex];
    }
}
