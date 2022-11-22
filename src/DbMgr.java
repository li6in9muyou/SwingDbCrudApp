import javax.swing.*;
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
    private boolean shouldAutoCommit;

    public DbMgr() {
        fetch = new Fetch("employee");
        DatabaseTableDataModel dbTable = new DatabaseTableDataModel(
                fetch.FetchAllRows(), fetch.getColumnHeaders()
        );
        QueryResultTable.setModel(dbTable);
        enableBetterColumnWidthAdjustment();
        CancelOperationButton.addActionListener(e -> {
            SwingUtilities.getWindowAncestor((JComponent) e.getSource()).dispose();
        });
        doSingleInsert.addActionListener(this::HandleSingleInsert);
        doManyLineInsert.addActionListener(this::HandleManyLineInsert);
        doSubQueryInsert.addActionListener(this::HandleSubQueryInsert);
        LoadMoreIntoMemoryButton.addActionListener(e -> QueryResultTable.setModel(
                new DatabaseTableDataModel(fetch.FetchAllRows(), fetch.getColumnHeaders())
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
        } else {
            System.out.println("operation is successful");
        }
    }

    private void HandleSingleInsert(ActionEvent actionEvent) {
        String text = singleLineInsert.getText();
        String[] fields = text.split(",");
        Throwable error = fetch.createRows(new String[][]{fields});
        handleError(error);
    }

    private void HandleManyLineInsert(ActionEvent actionEvent) {
        String text = multiLineInsert.getText();
        String[] lines = text.split("[\r\n]");
        String[][] rows = Arrays.stream(lines).map(line -> line.split(",")).toArray(String[][]::new);
        Throwable error = fetch.createRows(rows);
        handleError(error);
    }

    private void HandleSubQueryInsert(ActionEvent actionEvent) {
    }
}
