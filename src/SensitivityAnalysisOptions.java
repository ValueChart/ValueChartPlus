import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

class SensitivityAnalysisOptions extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	JRadioButton optInc;
	JRadioButton optDec;
	// add a drop-down box to choose a selection mode
	// pump: single click on criteria to increase/decrease
	// sort: double click on criteria to sort in descending order
	JComboBox<String> selectMode;
	String[] selectOpt = {"None","Pump","Sort"};
	JCheckBox chkAbs;
	ValueChart chart;
	JPanel pnlSA;
	
	SensitivityAnalysisOptions(ValueChart ch){
		chart = ch;
		Font font = new Font("Arial", Font.PLAIN, 11);
		
		// set up drop-down selection box
		// TODO JRE 6 or 7? getting rawtype warning
		selectMode = new JComboBox<String>(selectOpt);
		selectMode.setSelectedIndex(0);
		selectMode.setActionCommand("select");
		selectMode.addActionListener(this);
		selectMode.setFont(font);
		selectMode.setMaximumSize(selectMode.getPreferredSize());
		
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
        // add selection mode options to panel
        pnlSA.add(selectMode);
        pnlSA.add(optInc);
        pnlSA.add(optDec);
        
        setSelection();
        
        // TODO: rename Sensitivity Anaysis options?
        pnlSA.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), "Sensitivity Analysis", 0, 0, font));
        add(pnlSA);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        if (chart.displayType == ValueChart.DEFAULT_DISPLAY){
        	add(Box.createRigidArea(new Dimension(0, 10)));
        	add(chart.pnlDom); 
        }   
        add(pnlSA);     
    }    	
	
    private void setSelection() {
        if (!chart.pump_increase)
            optDec.setSelected(true);
        else
            optInc.setSelected(true);
        
        if (chart.pump)
            selectMode.setSelectedIndex(1);
        else if (chart.sort)
            selectMode.setSelectedIndex(2);
    }
	
	public void actionPerformed(ActionEvent ae) {
		// if action sourced from drop-down select box
		if ("select".equals(ae.getActionCommand())){
			String s = (String)(((JComboBox<String>) ae.getSource()).getSelectedItem());
			// pump selected
			if ("Pump".equals(s)){
				chart.pump = true;
				chart.sort = false;
				optInc.setEnabled(true);
				optDec.setEnabled(true);			
			}
			// none selected
			else if ("None".equals(s)){
				if (chart.pump)
				{
					chart.pump = false;
					optInc.setEnabled(false);
					optDec.setEnabled(false);			
				}
				chart.sort = false;
			}
			// sort selected
			else if ("Sort".equals(s)){
				if (chart.pump)
				{
					chart.pump = false;
					optInc.setEnabled(false);
					optDec.setEnabled(false);			
				}
				chart.sort = true;			
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
  