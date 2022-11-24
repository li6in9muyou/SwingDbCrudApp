import javax.swing.text.JTextComponent;

public class Blackboard {
    JTextComponent blackboard;

    public Blackboard(JTextComponent jTextComponent) {
        blackboard = jTextComponent;
    }

    void postNotification(String message) {
        String text = blackboard.getText();
        if (!text.isEmpty()) {
            text += "\n";
        }
        blackboard.setText(text + message);
    }

    void postError(String errorMessage) {
        postNotification("出错了：" + errorMessage);
    }

    void postInfo(String message) {
        postNotification("重要信息：" + message);
    }

    void postTrace(String message) {
        postNotification(message);
    }
}