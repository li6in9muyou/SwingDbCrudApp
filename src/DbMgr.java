import javax.swing.*;
import java.awt.event.ActionEvent;

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

    public DbMgr() {
        QueryResultTable.setModel(new DatabaseTableDataModel("employee"));
        InsertRowButton.addActionListener(DbMgr::HandleInsertRow);
        CancelOperationButton.addActionListener(e -> {
            SwingUtilities.getWindowAncestor((JComponent) e.getSource()).dispose();
        });
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

    private static void HandleInsertRow(ActionEvent e) {
        JTextField firstName = new JTextField();
        JTextField lastName = new JTextField();
        JPasswordField password = new JPasswordField();
        final JComponent[] inputs = new JComponent[]{
                new JLabel("First"),
                firstName,
                new JLabel("Last"),
                lastName,
                new JLabel("Password"),
                password
        };
        int result = JOptionPane.showConfirmDialog(
                null,
                inputs,
                "添加一行",
                JOptionPane.DEFAULT_OPTION
        );
        if (result == JOptionPane.OK_OPTION) {
            System.out.println("You entered " +
                    firstName.getText() + ", " +
                    lastName.getText() + ", " +
                    password.getText());
        } else {
            System.out.println("User canceled / closed the dialog, result = " + result);
        }
    }
}
