import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DbMgr {
    JPanel Show;
    private JTable QueryResultTable;
    private JButton InsertRowButton;
    private JButton DeleteRowButton;
    private JButton HelpButton;
    private JButton CancelOperationButton;
    private JButton CommitChangeButton;
    private JButton RollBackButton;
    private JButton LoadMoreIntoMemoryButton;
    private JButton FilterButton;
    private JCheckBox ToggleAutoCommitCheckBox;
    private JLabel RowCountLabel;
    private JComboBox<String> SelectedTable;
    private boolean shouldAutoCommit;

    public DbMgr(String[] table_names) {
        for (String name : table_names) {
            SelectedTable.addItem(name);
        }
        InsertRowButton.addActionListener(e -> {
            CreateRowDialog dialog = new CreateRowDialog();
            dialog.pack();
            dialog.setVisible(true);
        });
        CancelOperationButton.addActionListener(e -> {
            SwingUtilities.getWindowAncestor((JComponent) e.getSource()).dispose();
        });
    }
}
