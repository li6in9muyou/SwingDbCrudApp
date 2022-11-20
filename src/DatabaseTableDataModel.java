import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class DatabaseTableDataModel extends AbstractTableModel {
    ArrayList<String[]> data;

    public DatabaseTableDataModel(String table_name) {
        data = Fetch.FetchAllRows(table_name);
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

    public void createRows(String[][] rows) {
    }
}
