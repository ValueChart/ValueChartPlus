import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class UserDialog {
    public static void showMessage(String msg, String title, JFrame frame) {
        JOptionPane.showMessageDialog(frame,
                msg,
                (title == null ? "Warning" : title),
                JOptionPane.WARNING_MESSAGE);
    }
    
    public static void showError(String msg, String title, JFrame frame) {
        JOptionPane.showMessageDialog(frame,
                msg,
                (title == null ? "Error" : title),
                JOptionPane.ERROR_MESSAGE);
    }
}
