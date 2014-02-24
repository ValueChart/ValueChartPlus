import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.*;

/* ColorChooserDemo.java is a 1.4 application that requires no other files. */
public class ColorSelection extends JPanel
                              implements ChangeListener, ActionListener{
	private static final long serialVersionUID = 1L;
	JFrame frame;
    protected JColorChooser cc;
    JComponent lblObj;
    JLabel lblPreview;
    JButton btnOK;
    ValueChart chart = null;
    JLabel lblBase;
    
    public ColorSelection(JComponent obj, ValueChart ch, JLabel lbl){    	
    	this (obj);
    	chart = ch;
    	lblBase = lbl;
    }
    
    public ColorSelection(JComponent obj){
        super(new BorderLayout());

        lblObj=obj;
       
        cc = new JColorChooser(lblObj.getBackground());
        cc.getSelectionModel().addChangeListener(this);
        cc.setBorder(BorderFactory.createTitledBorder("Choose Objective Color"));
        cc.setPreviewPanel(new JPanel());
        
        lblPreview = new JLabel(obj.getName());
        lblPreview.setBorder(BorderFactory.createEtchedBorder());
        lblPreview.setPreferredSize(new Dimension(150, 30));
        lblPreview.setMaximumSize(new Dimension(150, 30));
        lblPreview.setHorizontalAlignment(JLabel.CENTER);
        lblPreview.setOpaque(true);
        lblPreview.setBackground(cc.getColor());
        
        JPanel pnlPrev = new JPanel(); 
        pnlPrev.add(lblPreview);
        pnlPrev.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), "Preview"));
        
        
        btnOK = new JButton("OK");
        btnOK.addActionListener(this);
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.LINE_AXIS));
        pnlButtons.add(Box.createHorizontalGlue());
        pnlButtons.add(btnOK);
        pnlButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(cc, BorderLayout.NORTH);
        add(pnlPrev, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
        showDisplay();        
    }
    
    public void stateChanged(ChangeEvent e) {
        Color newColor = cc.getColor();
        lblPreview.setBackground(newColor);
    }

    public void showDisplay() {    	
        //Create and set up the window.
        frame = new JFrame("Set Objective Color");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.pack();
       	frame.setVisible(true);        
    }

	public void actionPerformed(ActionEvent arg0) {
		if (lblObj instanceof JObjective)
			((JObjective)lblObj).setColor(cc.getColor());
		else{ 
			((AttributeCell)lblObj).setColor(cc.getColor());
			lblBase.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(cc.getColor()), BorderFactory.createBevelBorder(BevelBorder.RAISED)));
			chart.updateAll();
		}
		frame.dispose();
	}
}
   