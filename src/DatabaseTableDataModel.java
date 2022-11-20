import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class DatabaseTableDataModel extends AbstractTableModel {
    ArrayList<String[]> data;

    public DatabaseTableDataModel(ArrayList<String[]> data) {
        this.data = data;
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
