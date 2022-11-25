import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class HighlightNullAndEmptyString extends DefaultTableCellRenderer implements TableCellRenderer {
    private final TableCellRenderer defaultRenderer;

    public HighlightNullAndEmptyString(TableCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null || value.equals("")) {
            if (value == null) {
                setText("空");
                setBackground(Color.RED);
            } else {
                setText("空字符串");
                setForeground(Color.WHITE);
                setBackground(Color.GRAY);
            }
            setOpaque(true);
            return this;
        } else {
            return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
