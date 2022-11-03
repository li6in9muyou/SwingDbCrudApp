import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        String s = JOptionPane.showInputDialog(null,
                "请输入一个部门的编号",
                "");
        JOptionPane.showMessageDialog(null, "user input: " + s);
    }
}