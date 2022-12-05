import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

public class DbMgr {
    private static final int featSingleInsert = 0;
    private static final int featManyInsert = 1;
    private static final int featSubQueryInsert = 2;
    private static final int featAnyCellEdit = 3;
    private final FetchDecorator fetch;
    private final Blackboard blackboard;
    private final TableColumnAdjuster adjuster;
    private final DefaultTableModel dataModel;
    private final Vector<Patch> StagedPatches = new Vector<>();
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
    private JTextPane notifications;
    private JTextField subQueryPredicate;
    private JButton fetchPreview;
    private JTabbedPane featureTabs;
    private JTextArea oneCellUpdate;
    private boolean shouldAutoCommit;

    public DbMgr() {
        blackboard = new Blackboard(notifications);
        fetch = new FetchDecorator(blackboard, new Fetch("emp_photo"));
        adjuster = getColumnWidthAdjuster();
        dataModel = new DefaultTableModel();
        QueryResultTable.setModel(dataModel);
        QueryResultTable.setDefaultRenderer(
                Object.class,
                new HighlightNullAndEmptyString(QueryResultTable.getDefaultRenderer(Objects.class))
        );
        RowCountLabel.setText("还没有载入数据");
        dataModel.addTableModelListener(this::updateRowCountLabel);
        dataModel.addTableModelListener(this::handleStageOneCell);
        CancelOperationButton.addActionListener(e -> SwingUtilities.getWindowAncestor((JComponent) e.getSource()).dispose());
        CommitChangeButton.addActionListener(this::handleCommitChange);
        LoadMoreIntoMemoryButton.addActionListener(this::handleFetchAllRows);
        StageSelectedRowsButton.addActionListener(this::handleStageSelectedRows);
        fetchPreview.addActionListener(this::handleFetchSubQueryPreview);
        DeleteRowButton.addActionListener(this::handleDeleteRow);
        FilterButton.addActionListener(this::handleFetchFilteredRows);

        fetch.initConnection();
        subQueryPredicate.setText("photo_format='gif'");
        FilterButton.doClick();
        adjuster.adjustColumns();
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
        DbMgr dbMgr = new DbMgr();
        frame.setContentPane(dbMgr.Show);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        dbMgr.setUpConnectionOrExit();
    }

    private void setUpConnectionOrExit() {
        while (true) {
            boolean failed = fetch.initConnection();
            if (!failed) {
                return;
            } else {
                int c = JOptionPane.showConfirmDialog(
                        Show,
                        "连接数据库失败，要不要重试？",
                        "灾难性错误",
                        JOptionPane.YES_NO_OPTION
                );
                boolean userGiveUp = c == JOptionPane.NO_OPTION || c == JOptionPane.CLOSED_OPTION;
                if (userGiveUp) {
                    CancelOperationButton.doClick();
                    return;
                }
            }
        }
    }

    private void handleFetchFilteredRows(ActionEvent actionEvent) {
        featureTabs.setSelectedIndex(featSubQueryInsert);
        if (subQueryPredicate.getText().isEmpty()) {
            blackboard.postInfo("请先输入谓词再进行过滤");
        } else {
            updateMainTable(fetch.fetchPredicate(subQueryPredicate.getText()));
        }
    }

    private void handleCommitChange(ActionEvent actionEvent) {
        int which = featureTabs.getSelectedIndex();
        switch (which) {
            case featSingleInsert -> handleSingleInsert();
            case featManyInsert -> handleManyLineInsert();
            case featSubQueryInsert -> handleSubQueryInsert();
            case featAnyCellEdit -> handleAnyCellEditOnCommit();
            default -> throw new RuntimeException();
        }
    }

    private void handleAnyCellEditOnCommit() {
        fetch.commitPatches(StagedPatches.toArray(Patch[]::new));
        StagedPatches.clear();
        oneCellUpdate.setText("");
    }

    private void handleStageOneCell(TableModelEvent e) {
        if (e.getType() != TableModelEvent.UPDATE) {
            return;
        }
        int row = e.getFirstRow();
        int column = e.getColumn();
        boolean isTableInitEvent = row < 0 || column < 0;
        if (isTableInitEvent) {
            return;
        }
        Object data = dataModel.getValueAt(row, column);
        Object pk = dataModel.getValueAt(row, fetch.getPrimaryKeyColumn());
        StagedPatches.add(
                fetch.createPatch(pk, column, data)
        );
        oneCellUpdate.setText(
                StagedPatches.stream()
                        .map(Patch::toString)
                        .collect(Collectors.joining("\n"))
        );
        featureTabs.setSelectedIndex(featAnyCellEdit);
    }

    private void handleDeleteRow(ActionEvent actionEvent) {
        int[] selectedRows = QueryResultTable.getSelectedRows();
        int pkCol = fetch.getPrimaryKeyColumn();
        Vector<Object> victims = new Vector<>();
        for (int row : selectedRows) {
            victims.add(
                    QueryResultTable.getModel().getValueAt(row, pkCol)
            );
        }
        fetch.deleteRows(victims.toArray());
    }

    private void handleFetchAllRows(ActionEvent actionEvent) {
        updateMainTable(fetch.fetchAllRowsAsObjects());
    }

    private void updateRowCountLabel(TableModelEvent event) {
        int count = ((TableModel) event.getSource()).getRowCount();
        RowCountLabel.setText("内存中的%d行".formatted(count));
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
        JTextArea stage;
        if (selectedRows.length == 1) {
            stage = singleLineInsert;
            featureTabs.setSelectedIndex(featSingleInsert);
        } else {
            stage = multiLineInsert;
            featureTabs.setSelectedIndex(featManyInsert);
        }

        int columnCount = QueryResultTable.getColumnCount();
        Vector<String> rows = new Vector<>();
        for (int row : selectedRows) {
            String[] result = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                Object value = QueryResultTable.getModel().getValueAt(row, i);
                if (value == null) {
                    result[i] = "null";
                } else {
                    result[i] = value.toString().stripTrailing();
                }
            }
            rows.add(String.join(",", result));
        }
        stage.setText(String.join("\n", rows));
    }

    private TableColumnAdjuster getColumnWidthAdjuster() {
        TableColumnAdjuster adj = new TableColumnAdjuster(QueryResultTable);
        adj.setOnlyAdjustLarger(true);
        adj.setColumnDataIncluded(true);
        return adj;
    }

    private void handleSingleInsert() {
        String text = singleLineInsert.getText();
        String[] fields = text.split(",");
        fetch.createRows(new String[][]{fields});
    }

    private void handleManyLineInsert() {
        String text = multiLineInsert.getText();
        String[] lines = text.split("[\r\n]");
        String[][] rows = Arrays.stream(lines).map(line -> line.split(",")).toArray(String[][]::new);
        fetch.createRows(rows);
    }

    private void handleSubQueryInsert() {
        String text = subQueryInsert.getText();
        String[] lines = text.split("[\r\n]");
        String[][] rows = Arrays.stream(lines).map(line -> line.split(",")).toArray(String[][]::new);
        fetch.createRows(rows);
    }

    private void updateMainTable(Object[][] data) {
        dataModel.setDataVector(data, fetch.getColumnHeaders());
        adjuster.adjustColumns();
    }
}
