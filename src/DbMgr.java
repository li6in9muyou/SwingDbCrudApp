import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class DbMgr {
    private final Fetch fetch;
    JPanel Show;
    private JTable QueryResultTable;
    private JButton StageSelectedRowsButton;
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
    private JTextArea singleLineInsert;
    private JTextArea multiLineInsert;
    private JTextArea subQueryInsert;
    private JButton doSingleInsert;
    private JButton doManyLineInsert;
    private JButton doSubQueryInsert;
    private JTextPane notifications;
    private boolean shouldAutoCommit;

    public DbMgr() {
        fetch = new Fetch("employee");
        enableBetterColumnWidthAdjustment();
        CancelOperationButton.addActionListener(e -> {
            SwingUtilities.getWindowAncestor((JComponent) e.getSource()).dispose();
        });
        doSingleInsert.addActionListener(this::handleSingleInsert);
        doManyLineInsert.addActionListener(this::handleManyLineInsert);
        doSubQueryInsert.addActionListener(this::handleSubQueryInsert);
        LoadMoreIntoMemoryButton.addActionListener(e -> QueryResultTable.setModel(
                new DefaultTableModel(fetch.FetchAllRows().toArray(String[][]::new), fetch.getColumnHeaders())
        ));
    }

    public static void main(String[] args) {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().equals("Nimbus")) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (Exception ignored) {
                }
                break;
            }
        }
        JFrame frame = new JFrame("数据表管理器");
        frame.setContentPane(new DbMgr().Show);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void enableBetterColumnWidthAdjustment() {
        TableColumnAdjuster adjuster = new TableColumnAdjuster(QueryResultTable);
        adjuster.setOnlyAdjustLarger(true);
        adjuster.setColumnDataIncluded(true);
        adjuster.adjustColumns();
    }

    private void handleError(Throwable error) {
        if (error != null) {
            System.out.println("operation failed");
            System.out.println("error.getMessage() = " + error.getMessage());
            System.out.println("fetch.fetchErrorMessage(error) = " + fetch.fetchErrorMessage(error));
        } else {
            System.out.println("operation is successful");
        }
    }

    private void handleSingleInsert(ActionEvent actionEvent) {
        String text = singleLineInsert.getText();
        String[] fields = text.split(",");
        Throwable error = fetch.createRows(new String[][]{fields});
        handleError(error);
    }

    private void handleManyLineInsert(ActionEvent actionEvent) {
        String text = multiLineInsert.getText();
        String[] lines = text.split("[\r\n]");
        String[][] rows = Arrays.stream(lines).map(line -> line.split(",")).toArray(String[][]::new);
        Throwable error = fetch.createRows(rows);
        handleError(error);
    }

    private void handleSubQueryInsert(ActionEvent actionEvent) {
    }

    private void postNotification(String message) {
        String text = notifications.getText();
        if (!text.isEmpty()) {
            text += "\n";
        }
        notifications.setText(text + message);
    }
}
