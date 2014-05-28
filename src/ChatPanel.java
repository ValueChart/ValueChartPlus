import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

//http://asimdlv.com/java-swing-auto-scrolling-jscrollpane-i-e-chat-window/
public class ChatPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private ValueChart chart;
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private JTextField userInputField;
    
    public ChatPanel(ValueChart ch) {
        chart = ch;
        buildDisplay();
    }
    
    public void buildDisplay() {
        // We create a TextArea object
        textArea = new JTextArea(5, 30);
        // We put the TextArea object in a Scrollable Pane
        scrollPane = new JScrollPane(textArea);
    
        // In order to ensure the scroll Pane object appears in your window, 
        // set a preferred size to it!
        //textArea.setPreferredSize(new Dimension(380, 100));
    
        // Lines will be wrapped if they are too long to fit within the 
        // allocated width
        textArea.setLineWrap(true);
    
        // Lines will be wrapped at word boundaries (whitespace) if they are 
        // too long to fit within the allocated width
        textArea.setWrapStyleWord(true);
    
        // Assuming this is the chat client's window where we read text sent out 
        // and received, we don't want our Text Area to be editable!
        textArea.setEditable(false);
    
        // We also want a vertical scroll bar on our pane, as text is added to it
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    
        // Now let's just add a Text Field for user input, and make sure our 
        // text area stays on the last line as subsequent lines are 
        // added and auto-scrolls
        userInputField = new JTextField(30);
        userInputField.setMaximumSize(new Dimension(380, 100));
        userInputField.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
            //We get the text from the textfield
            String fromUser = userInputField.getText();
    
            if (fromUser != null) {
                //We append the text from the user
                textArea.append("User: " + fromUser + "\n");
    
                //The pane auto-scrolls with each new response added
                textArea.setCaretPosition(textArea.getDocument().getLength());
                //We reset our text field to "" each time the user presses Enter
                userInputField.setText("");
            }
            }
        });
    
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //adds and centers the text field to the frame
        this.add(userInputField, SwingConstants.CENTER);
        //adds and centers the scroll pane to the frame
        this.add(scrollPane, SwingConstants.CENTER);
    }

}