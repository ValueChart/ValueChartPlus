import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Comment box for users to log feedback.
 * <p><img src="doc-files/CommentPanel.png"/></p>
 *
 */
public class CommentPanel extends JPanel implements FocusListener{

    private static final long serialVersionUID = 1L;
    
    private ValueChart chart;
    private JTextField userInputField;
    //private JButton inputButton;
    private String defaultText = "Type comment, then press Enter to log";
    private Font defaultFont = new Font("Arial", Font.ITALIC, 12);
    private Font typeFont = new Font("Arial", Font.PLAIN, 12);
    
    public CommentPanel(ValueChart ch) {
        chart = ch;
        buildDisplay();
    }
    
    public void buildDisplay() {
        /*inputButton = new JButton("Log Comment");
        inputButton.setFont(new Font("Verdana", Font.PLAIN, 10));
        inputButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                logComment(sanitizeXmlChars(userInputField.getText()));
            }
        });*/
        
        userInputField = new JTextField(30);
        userInputField.addFocusListener(this);
        userInputField.setToolTipText("press Enter to log comment");
        userInputField.setText(defaultText);
        userInputField.setFont(defaultFont);
        userInputField.setForeground(Color.gray);
        userInputField.setMaximumSize(new Dimension(380, 30));
        userInputField.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                logComment(sanitizeXmlChars(userInputField.getText()));
            }
        });

    
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(userInputField);
        //this.add(inputButton);
        this.setMaximumSize(new Dimension(380, EntryNamePanel.ANG_HT));
        this.setAlignmentY(BOTTOM_ALIGNMENT);
    }
    
    public void logComment(String str) {
        if (str != null) {
            chart.logString("", str);
            userInputField.setText("");
        }
    }
    
    //http://www.rgagnon.com/javadetails/java-sanitize-xml-string.html
    public static String sanitizeXmlChars(String xml) {
        if (xml == null || ("".equals(xml))) return "";
        // ref : http://www.w3.org/TR/REC-xml/#charsets
        // jdk 7
        Pattern xmlInvalidChars =
          Pattern.compile(
             "[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\\x{10000}-\\x{10FFFF}]"
            );
        
        return xmlInvalidChars.matcher(xml).replaceAll("").replaceAll("\\<|\\>", "");
      }

    @Override
    public void focusGained(FocusEvent fe) {
        if (fe.getSource() instanceof JTextField) {
            if (userInputField.getText().equals(defaultText)) {
                userInputField.setText("");
                userInputField.setFont(typeFont);
                userInputField.setForeground(Color.black);
            }
        }
    }

    @Override
    public void focusLost(FocusEvent fe) {
        if (fe.getSource() instanceof JTextField) {
            if (userInputField.getText().trim().isEmpty()) {
                userInputField.setText(defaultText);
                userInputField.setFont(defaultFont);
                userInputField.setForeground(Color.gray);
            }
        }
    }

}