import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


public class DescriptionView extends JFrame implements HyperlinkListener {
    

    private static final long serialVersionUID = 1L;
    private String htmlText;
    private int width = 500;
    private int height = 400;
    
    public DescriptionView(String text){
        htmlText = text;
        showFrame();
    }
    
    public void showFrame() {
        setPreferredSize(new Dimension(width,height));
        
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText(htmlText);
        textPane.setBackground(Color.white);
        textPane.setOpaque(true);
        textPane.setPreferredSize(new Dimension(width-50, height-50));
        textPane.addHyperlinkListener(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.white);
        panel.setOpaque(true);
        panel.add(textPane);
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBackground(Color.white);
        scrollPane.setOpaque(true);
        setContentPane(scrollPane);
        pack();
        setVisible(true);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (!(e.getInputEvent() instanceof MouseEvent)) return;
        MouseEvent me = (MouseEvent) e.getInputEvent();
        if (SwingUtilities.isLeftMouseButton(me)) {
            try {
                URI uri = e.getURL().toURI();
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                }
            } catch (Exception ex) {
                    ex.printStackTrace();
            }
        }
    }

}
