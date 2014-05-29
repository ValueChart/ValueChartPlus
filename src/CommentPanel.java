import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CommentPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private ValueChart chart;
    private JTextField userInputField;
    private JButton inputButton;
    
    public CommentPanel(ValueChart ch) {
        chart = ch;
        buildDisplay();
    }
    
    public void buildDisplay() {
        inputButton = new JButton("Add Comment");
        inputButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                logComment(sanitizeXmlChars(userInputField.getText()));
            }
        });
        
        userInputField = new JTextField(30);
        userInputField.setMaximumSize(new Dimension(380, inputButton.getPreferredSize().height));
        userInputField.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                logComment(sanitizeXmlChars(userInputField.getText()));
            }
        });

    
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(userInputField);
        this.add(inputButton);
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

}