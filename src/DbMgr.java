import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Vector;

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
        CancelOperationButton.addActionListener(e -> SwingUtilities.getWindowAncestor((JComponent) e.getSource()).dispose());
        doSingleInsert.addActionListener(this::handleSingleInsert);
        doManyLineInsert.addActionListener(this::handleManyLineInsert);
        doSubQueryInsert.addActionListener(this::handleSubQueryInsert);
        LoadMoreIntoMemoryButton.addActionListener(e -> QueryResultTable.setModel(
                new DefaultTableModel(fetch.fetchAllRows().toArray(String[][]::new), fetch.getColumnHeaders())
        ));
        StageSelectedRowsButton.addActionListener(this::handleStageSelectedRows);
        multiLineInsert.setText("""
                000141,HEATHER,A,NICHOLLS,C01,1793,2006-12-15,ANALYST,18,F,1976-01-19,68420.00,600.00,2274.00
                000152,BRUCE,,ADAMSON,D11,4510,2002-02-12,DESIGNER,16,M,1977-05-17,55280.00,500.00,2022.00
                000163,ELIZABETH,R,PIANKA,D11,3782,2006-10-11,DESIGNER,17,F,1980-04-12,62250.00,400.00,1780.00"""
        );
        singleLineInsert.setText("""
                000141,HEATHER,A,NICHOLLS,C01,1793,2006-12-15,ANALYST,18,F,1976-01-19,68420.00,600.00,2274.00,000152,BRUCE,,ADAMSON,D11,4510,2002-02-12,DESIGNER,16,M,1977-05-17,55280.00,500.00,2022.00,000163,ELIZABETH,R,PIANKA,D11,3782,2006-10-11,DESIGNER,17,F,1980-04-12,62250.00,400.00,1780.00
                """
        );
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

    private void handleStageSelectedRows(ActionEvent actionEvent) {
        int[] selectedRows = QueryResultTable.getSelectedRows();
        System.out.println("SelectedRows = " + Arrays.toString(selectedRows));

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
