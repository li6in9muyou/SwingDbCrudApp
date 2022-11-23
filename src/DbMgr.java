import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Vector;
import java.util.stream.Collectors;

public class DbMgr {
    private final FetchDecorator fetch;
    private final Blackboard blackboard;
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
    private JTextField subQueryPredicate;
    private JButton fetchPreview;
    private boolean shouldAutoCommit;

    public DbMgr() {
        blackboard = new Blackboard(notifications);
        fetch = new FetchDecorator(blackboard, new Fetch("employee"));
        enableBetterColumnWidthAdjustment();
        CancelOperationButton.addActionListener(e -> SwingUtilities.getWindowAncestor((JComponent) e.getSource()).dispose());
        doSingleInsert.addActionListener(this::handleSingleInsert);
        doManyLineInsert.addActionListener(this::handleManyLineInsert);
        doSubQueryInsert.addActionListener(this::handleSubQueryInsert);
        LoadMoreIntoMemoryButton.addActionListener(this::handleFetchAllRows);
        StageSelectedRowsButton.addActionListener(this::handleStageSelectedRows);
        fetchPreview.addActionListener(this::handleFetchSubQueryPreview);
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

    private void handleFetchAllRows(ActionEvent actionEvent) {
        QueryResultTable.setModel(
                new DefaultTableModel(fetch.fetchAllRows().toArray(String[][]::new), fetch.getColumnHeaders())
        );
    }

    private void handleFetchSubQueryPreview(ActionEvent actionEvent) {
        String[][] rows = fetch.fetchPredicate(subQueryPredicate.getText());
        subQueryInsert.setText(
                Arrays.stream(rows)
                        .map(row -> String.join(",", row))
                        .collect(Collectors.joining("\n"))
        );
    }

    private void handleStageSelectedRows(ActionEvent actionEvent) {
        int[] selectedRows = QueryResultTable.getSelectedRows();
        blackboard.postTrace("SelectedRows = " + Arrays.toString(selectedRows));

        int columnCount = QueryResultTable.getColumnCount();
        String[] result = new String[columnCount];
        if (selectedRows.length == 1) {
            int row = selectedRows[0];
            for (int i = 0; i < columnCount; i++) {
                result[i] = (String) QueryResultTable.getModel().getValueAt(row, i);
            }
            singleLineInsert.setText(String.join(",", result));
        } else {
            Vector<String> rows = new Vector<>();
            for (int row : selectedRows) {
                for (int i = 0; i < columnCount; i++) {
                    result[i] = (String) QueryResultTable.getModel().getValueAt(row, i);
                }
                rows.add(String.join(",", result));
            }
            multiLineInsert.setText(String.join("\n", rows));
        }
    }

    private void enableBetterColumnWidthAdjustment() {
        TableColumnAdjuster adjuster = new TableColumnAdjuster(QueryResultTable);
        adjuster.setOnlyAdjustLarger(true);
        adjuster.setColumnDataIncluded(true);
        adjuster.adjustColumns();
    }

    private void handleSingleInsert(ActionEvent actionEvent) {
        String text = singleLineInsert.getText();
        String[] fields = text.split(",");
        fetch.createRows(new String[][]{fields});
        LoadMoreIntoMemoryButton.doClick();
    }

    private void handleManyLineInsert(ActionEvent actionEvent) {
        String text = multiLineInsert.getText();
        String[] lines = text.split("[\r\n]");
        String[][] rows = Arrays.stream(lines).map(line -> line.split(",")).toArray(String[][]::new);
        fetch.createRows(rows);
        LoadMoreIntoMemoryButton.doClick();
    }

    private void handleSubQueryInsert(ActionEvent actionEvent) {
    }
}
