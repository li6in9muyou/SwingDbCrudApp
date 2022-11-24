import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ExplicitTableCellRenderer implements TableCellRenderer {
    private final TableCellRenderer defaultRenderer;

    public ExplicitTableCellRenderer(TableCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null || value.equals("")) {
            JLabel label = new JLabel();
            if (value == null) {
                label.setText("空");
                label.setBackground(Color.RED);
            } else {
                label.setText("空字符串");
                label.setForeground(Color.WHITE);
                label.setBackground(Color.GRAY);
            }
            label.setOpaque(true);
            return label;
        } else {
            return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
