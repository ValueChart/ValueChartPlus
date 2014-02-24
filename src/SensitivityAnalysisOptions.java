import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

class SensitivityAnalysisOptions extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	JRadioButton optInc;
	JRadioButton optDec;
	JCheckBox chkAbs;
	ValueChart chart;
	JPanel pnlSA;
	
	SensitivityAnalysisOptions(ValueChart ch){
		chart = ch;
		Font font = new Font("Arial", Font.PLAIN, 11);			
		JCheckBox chkPump = new JCheckBox("pump");
		chkPump.addActionListener(this);
		chkPump.setBorder(BorderFactory.createEmptyBorder());    		
		chkPump.setFont(font);
		
		ButtonGroup grpPump = new ButtonGroup();
        optInc = new JRadioButton("increase");
        optInc.setActionCommand("inc");
        optInc.setSelected(true);	
        optInc.setEnabled(false);
        optInc.addActionListener(this);
        optInc.setFont(font);
        grpPump.add(optInc);
        
        optDec = new JRadioButton("decrease");
        optDec.setActionCommand("dec");
        optDec.setEnabled(false);
        optDec.addActionListener(this);
        optDec.setFont(font);
        grpPump.add(optDec);            
        
        JLabel lblDir = new JLabel("Direction:");
        lblDir.setFont(font);
		ButtonGroup grpDir = new ButtonGroup();
		JRadioButton optUp;
        optUp = new JRadioButton("roll up");
        optUp.setActionCommand("up");
        optUp.setSelected(true);	
        optUp.addActionListener(this);
        optUp.setFont(font);
        grpDir.add(optUp);
        
        JRadioButton optDown;
        optDown = new JRadioButton("drill down");
        optDown.setActionCommand("down");
        optDown.addActionListener(this);
        optDown.setFont(font);
        grpDir.add(optDown);  
        
        pnlSA = new JPanel();
        pnlSA.setLayout(new BoxLayout(pnlSA, BoxLayout.LINE_AXIS));
        //pnlSA.add(lblDir);
        //pnlSA.add(optUp);
        //pnlSA.add(optDown);
        pnlSA.add(chkPump);  
        pnlSA.add(optInc);
        pnlSA.add(optDec);
        pnlSA.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), "Sensitivity Analysis", 0, 0, font));
        add(pnlSA);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        if (chart.displayType == ValueChart.DEFAULT_DISPLAY){
        	add(Box.createRigidArea(new Dimension(0, 10)));
        	add(chart.pnlDom); 
        }   
        add(pnlSA);     
    	}    	
	
	public void actionPerformed(ActionEvent ae) {
		if ("pump".equals(ae.getActionCommand())){
				chart.pump = ((JCheckBox)ae.getSource()).isSelected();
				if (chart.pump){
					optInc.setEnabled(true);
					optDec.setEnabled(true);
				}
				else{
					optInc.setEnabled(false);
					optDec.setEnabled(false);
				}					
		}
		else if ("inc".equals(ae.getActionCommand()))
			chart.pump_increase = true;
		else if ("dec".equals(ae.getActionCommand()))
			chart.pump_increase = false;
		else if ("up".equals(ae.getActionCommand()))
			chart.sa_dir = BaseTableContainer.UP;
		else if ("down".equals(ae.getActionCommand()))
			chart.sa_dir = BaseTableContainer.DOWN;
	} 

    }
  