import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;

public class HighlightNullAndEmptyString extends DefaultTableCellRenderer implements TableCellRenderer {
    private final TableCellRenderer defaultRenderer;
    private final HashMap<Integer, ImageIcon> cachedImage = new HashMap<>(10);

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
        } else if (value instanceof byte[]) {
            JLabel base = (JLabel) defaultRenderer.getTableCellRendererComponent(
                    table, "", isSelected, hasFocus, row, column
            );
            int key = value.hashCode();
            if (!cachedImage.containsKey(key)) {
                Image image = new ImageIcon((byte[]) value).getImage();
                float ar = (float) image.getWidth(null) / (float) image.getHeight(null);
                cachedImage.put(
                        key,
                        new ImageIcon(image.getScaledInstance(
                                (int) (300 * ar), 300, Image.SCALE_DEFAULT
                        ))
                );
            }
            base.setIcon(cachedImage.get(key));
            return base;
        } else {
            return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
