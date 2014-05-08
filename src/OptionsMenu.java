import java.awt.AWTException;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

class OptionsMenu extends JMenuBar implements ActionListener{
	private static final long serialVersionUID = 1L;

	   public static final int SMALL = 30,
            MEDIUM = 40,
            LARGE = 50,
            EXTRALARGE = 60;

    CheckBoxMenuEntry scoreMenuItem; 
    CheckBoxMenuEntry measureMenuItem; 
    CheckBoxMenuEntry absMenuItem; 
    CheckBoxMenuEntry graphMenuItem;
    CheckBoxMenuEntry domMenuItem;
    CheckBoxMenuEntry sepMenuItem;
    CheckBoxMenuEntry sideMenuItem;
    CheckBoxMenuEntry defMenuItem;
    CheckBoxMenuEntry smMenuItem;
    CheckBoxMenuEntry medMenuItem;
    CheckBoxMenuEntry lgMenuItem;
    CheckBoxMenuEntry xlgMenuItem;
    MenuEntry menuItem;  
    MenuEntry menuUndo;
    MenuEntry menuRedo;
    ValueChart chart;
    Font font; 
    
    OptionsMenu(ValueChart ch){
    	chart = ch;    	
    }
    
    void createMenu(){
    	font = new Font("Verdana", Font.PLAIN, 13);	

    	MenuTitle menu;
        menu = new MenuTitle("File");   
        menuItem = new MenuEntry("New");
        menu.add(menuItem);
        menuItem = new MenuEntry("Open");
        menu.add(menuItem);
        menu.addSeparator();
        menuItem = new MenuEntry("Save"); 
        //Added to email the individual VC
        menu.add(menuItem);
        menuItem = new MenuEntry("Send"); 
        menu.add(menuItem);
        menuItem = new MenuEntry("Snapshot"); 
        menu.add(menuItem);
        //menuItem = new MenuEntry("Print"); 
        //menu.add(menuItem);
        menu.addSeparator();
        menuItem = new MenuEntry("Close");
        menu.add(menuItem);
        add(menu);
        
        menu = new MenuTitle("Edit");   
        menuUndo = new MenuEntry("Undo");
        menuUndo.setEnabled(false);
		menu.add(menuUndo);		
        menuRedo = new MenuEntry("Redo");
        menuRedo.setEnabled(false);
		menu.add(menuRedo);
		menu.addSeparator();
        menuItem = new MenuEntry("Objectives");  
        menu.add(menuItem);
        menuItem = new MenuEntry("Alternatives"); 
        menu.add(menuItem);
        menuItem = new MenuEntry("Value Function");  
        menu.add(menuItem);
        menuItem = new MenuEntry("Initial Weighting");  
        menu.add(menuItem);  
        add(menu);
        
        MenuTitle submenu;        
        
        menu = new MenuTitle("View");         
        submenu = new MenuTitle("Detail Display");
        
        graphMenuItem = new CheckBoxMenuEntry("Utility Graphs"); 
        submenu.add(graphMenuItem);
        
        absMenuItem = new CheckBoxMenuEntry("Absolute Ratios", true); 
        submenu.add(absMenuItem);
        
        domMenuItem = new CheckBoxMenuEntry("Domain Values");        
        submenu.add(domMenuItem);
        
        scoreMenuItem = new CheckBoxMenuEntry("Total Score");  
        scoreMenuItem.setSelected(true);
        submenu.add(scoreMenuItem);         
        measureMenuItem = new CheckBoxMenuEntry("Score Measure");  
        submenu.add(measureMenuItem);    
        menu.add(submenu);
        
        submenu = new MenuTitle("Chart Display");
        defMenuItem = new CheckBoxMenuEntry("Vertical");
        submenu.add(defMenuItem);
        sideMenuItem = new CheckBoxMenuEntry("Horizontal");
        submenu.add(sideMenuItem);
        sepMenuItem = new CheckBoxMenuEntry("Separate");
        submenu.add(sepMenuItem);
        menu.add(submenu);
        
        submenu = new MenuTitle("View Size"); 
        smMenuItem = new CheckBoxMenuEntry("Small");
        submenu.add(smMenuItem);
        medMenuItem = new CheckBoxMenuEntry("Medium");
        submenu.add(medMenuItem);
        lgMenuItem = new CheckBoxMenuEntry("Large");
        submenu.add(lgMenuItem);
        xlgMenuItem = new CheckBoxMenuEntry("Extra Large");
        submenu.add(xlgMenuItem);
        menu.add(submenu);       
        
        menuItem = new MenuEntry("Set colors...");  
        menu.add(menuItem);
        
        add(menu);
        
        menu = new MenuTitle("Window");         
        menuItem = new MenuEntry("Open comparison view");
        menu.add(menuItem);

        add(menu);
        setOpaque(true);

    }    
    
    public void setSelectedItems(){
    	if (chart.showAbsoluteRatios)
    		absMenuItem.setSelected(true);
    	if (chart.show_graph)
    		graphMenuItem.setSelected(true);
    	switch (chart.displayType){
    		case ValueChart.SEPARATE_DISPLAY:{
    			sepMenuItem.setSelected(true); break;} 
    		case ValueChart.SIDE_DISPLAY:{
    			sideMenuItem.setSelected(true); break;}  
    		case ValueChart.DEFAULT_DISPLAY:{
    			defMenuItem.setSelected(true); break;} 
    	}
    	switch (chart.colWidth){
			case SMALL:{
				smMenuItem.setSelected(true); break;}  
			case MEDIUM:{
				medMenuItem.setSelected(true); break;}   
			case LARGE:{
				lgMenuItem.setSelected(true); break;}  
                        case EXTRALARGE:{
                                xlgMenuItem.setSelected(true); break;} 
    	}
    }
    
	public void actionPerformed(ActionEvent ae) {
		//menubar commands		
		if ("Snapshot".equals(ae.getActionCommand())){
	        String image_name = (String)JOptionPane.showInputDialog(this, "Image name: ", "Save Snapshot", 
	        		JOptionPane.PLAIN_MESSAGE, null, null, ".jpg");			
			try {
				createSnapshot(image_name);
			} catch (AWTException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if ("Open".equals(ae.getActionCommand())){
			ValueChartsPlus.chart = chart;
			ValueChartsPlus.showStartView();
		}
		
		if ("Save".equals(ae.getActionCommand())){
			saveFile();
		}	
		
		if ("Send".equals(ae.getActionCommand())){
			sendFile();
		}
		
		if ("Close".equals(ae.getActionCommand())){
			System.exit(0);
		}		
			
		if ("Undo".equals(ae.getActionCommand())){
			chart.last_int.setRedo(chart.next_int);
			chart.last_int.undo();
		}
		
		else if ("Redo".equals(ae.getActionCommand())){
			chart.next_int.setRedo(chart.last_int);
			chart.next_int.undo();
		}	
		
		else if ("Objectives".equals(ae.getActionCommand())){
			chart.showEditView(0);		}
		else if ("Alternatives".equals(ae.getActionCommand())){
			chart.showEditView(1);
		}
		else if ("Value Function".equals(ae.getActionCommand())){
			chart.showEditView(2);
		}
		else if ("Initial Weighting".equals(ae.getActionCommand())){
			chart.showEditView(3);
		}		
		else if ("Vertical".equals(ae.getActionCommand())){
			chart.resetDisplay(1, chart.getColWidth(), true, chart.show_graph);		
		}		
		else if ("Horizontal".equals(ae.getActionCommand())){
			chart.resetDisplay(2, chart.getColWidth(), true, chart.show_graph);	
		}
		
		else if ("Separate".equals(ae.getActionCommand())){
			chart.resetDisplay(3, chart.getColWidth(), true, chart.show_graph);	
		}
		
		else if ("Utility Graphs".equals(ae.getActionCommand())){
			if (graphMenuItem.isSelected()){
				chart.show_graph = true;
			}
			else
				chart.show_graph = false;
			chart.resetDisplay(chart.displayType, chart.colWidth, true, chart.show_graph);
		}
		
		else if ("Absolute Ratios".equals(ae.getActionCommand())){
			if (absMenuItem.isSelected())
				chart.showAbsoluteRatios = true;
			else
				chart.showAbsoluteRatios = false;
			changeHeaders(chart.mainPane);
		}		
		else if ("Domain Values".equals(ae.getActionCommand())){
			if (domMenuItem.isSelected())
				for(int j=0; j<chart.entryList.size(); j++)
					((ChartEntry)chart.entryList.get(j)).setShowFlag(true);
			else
				for(int j=0; j<chart.entryList.size(); j++)
					((ChartEntry)chart.entryList.get(j)).setShowFlag(false);
			chart.updateAll();	
		}				
		else if ("Total Score".equals(ae.getActionCommand())){
			if (scoreMenuItem.isSelected())
				chart.getDisplayPanel().setScore(true);
			else
				chart.getDisplayPanel().setScore(false);
			chart.updateDisplay();
		}		
		else if ("Score Measure".equals(ae.getActionCommand())){
			if (measureMenuItem.isSelected())
				chart.getDisplayPanel().setRuler(true);
			else
				chart.getDisplayPanel().setRuler(false);
			chart.updateDisplay();
		}			
		else if ("Small".equals(ae.getActionCommand())){
			chart.resetDisplay(chart.displayType, 30, true, chart.show_graph);
		}	
		else if ("Medium".equals(ae.getActionCommand())){
			chart.resetDisplay(chart.displayType, 40, true, chart.show_graph);			
		}		
		else if ("Large".equals(ae.getActionCommand())){
			chart.resetDisplay(chart.displayType, 50, true, chart.show_graph);			
		}	
		else if ("Extra Large".equals(ae.getActionCommand())){
			chart.resetDisplay(chart.displayType, 60, true, chart.show_graph);			
		}	
                
		else if ("Open comparison view".equals(ae.getActionCommand())){
			chart.setConnectingFields();
			chart.compareDisplay(chart.displayType, chart.colWidth);
		}


		
	}
	
	//Method to email the individual ValueChart
	void sendFile() {
		//Getting the list of vc files
        File f = new File(".");
        String files[] = f.list(); 
        Vector tempFiles = new Vector();
        for(int i = 0; i<files.length;i++){
        	if(files[i].endsWith(".vc")){
        		tempFiles.add(files[i]);
        	}        	
        }
        Object vcFiles[] = new Object[tempFiles.size()];
        for(int i =0;i<tempFiles.size();i++){
        	vcFiles[i] = tempFiles.get(i);
        }        
        String selectedVC = (String)JOptionPane.showInputDialog(
                this,
                "Select your ValueChart:",
                "Email ValueChart",
                JOptionPane.PLAIN_MESSAGE,
                null,
                vcFiles,
                vcFiles[0]);
        
        //Send mail on OK
		if ((selectedVC!= null) && (selectedVC.length() > 0)){
			
//			sendEmail(selectedVC);
			// Recipient's email ID needs to be mentioned.
		      String to = "sanzana05@gmail.com";

		      // Sender's email ID needs to be mentioned
		      final String from = "valuechartsplus@gmail.com";
		      
		      //Sender's password
		      final String pwd = "charts@v";

		      // Assuming you are sending email from localhost
		      String host = "smtp.gmail.com";

		      // Get system properties
//		      Properties properties = System.getProperties();
		      Properties props = new Properties();

		      // Setup mail server
//		      properties.setProperty("mail.smtp.host", host);
		      props.setProperty("mail.transport.protocol", "smtp");     
		      props.setProperty("mail.host", "smtp.gmail.com");  
		      props.put("mail.smtp.auth", "true");  
		      props.put("mail.smtp.port", "465");  
		      props.put("mail.debug", "true");  
		      props.put("mail.smtp.socketFactory.port", "465");  
		      props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");  
		      props.put("mail.smtp.socketFactory.fallback", "false"); 

		      // Get the default Session object.
//		      Session session = Session.getDefaultInstance(props);
		      Session session = Session.getDefaultInstance(props,  
		    		    new javax.mail.Authenticator() {
		    		       protected PasswordAuthentication getPasswordAuthentication() {  
		    		       return new PasswordAuthentication(from,pwd);  
		    		   }  
		    		   });

		      try{
		         // Create a default MimeMessage object.
		         MimeMessage message = new MimeMessage(session);

		         // Set From: header field of the header.
		         message.setFrom(new InternetAddress(from));

		         // Set To: header field of the header.
		         message.addRecipient(Message.RecipientType.TO,
		                                  new InternetAddress(to));

		         // Set Subject: header field
		         message.setSubject("Sending ValueChart - "+selectedVC);

		         // Create the message part 
		         BodyPart messageBodyPart = new MimeBodyPart();

		         // Fill the message
		         messageBodyPart.setText("Please find ValueChart attached.");
		         
		         // Create a multipart message
		         Multipart multipart = new MimeMultipart();

		         // Set text message part
		         multipart.addBodyPart(messageBodyPart);

		         // Part two is attachment
		         messageBodyPart = new MimeBodyPart();
		         String filename = selectedVC;
		         DataSource source = new FileDataSource(filename);
		         messageBodyPart.setDataHandler(new DataHandler(source));
		         messageBodyPart.setFileName(filename);
		         multipart.addBodyPart(messageBodyPart);

		         // Send the complete message parts
		         message.setContent(multipart );

		         // Send message
		         Transport.send(message);
		         System.out.println("Sent message successfully....");
		         JOptionPane.showMessageDialog(this,"ValueChart sent successfully.","ValueChart Sent",JOptionPane.INFORMATION_MESSAGE);
		      }catch (MessagingException mex) {
		         mex.printStackTrace();
		      }
		}
	}

	void saveFile(){
		File file;
		int ans = JOptionPane.YES_OPTION;
		chart.setConnectingFields();
	    String filename = (String)JOptionPane.showInputDialog(this, "ValueChart name: ", "Save ValueChart", 
	    		JOptionPane.PLAIN_MESSAGE, null, null, ".vc");
	    if ((filename != null) && (filename.length() > 0)) {
	    	file = new File(filename);
	    	if (file.exists()){
	    		ans = JOptionPane.showConfirmDialog(
	                    this, "Replace existing file??",
	                    "File overwrite",
	                    JOptionPane.YES_NO_OPTION);	        			     	    		        
	    	}
	    	if (ans == JOptionPane.YES_OPTION){
		        chart.con.createDataFile(filename);
		        }
	    	else 
	    		saveFile();
	    }
	}
	
	void changeHeaders(TablePane pane){
		Iterator<BaseTableContainer> it;    	
		for (it = pane.getRows(); it.hasNext();){
			BaseTableContainer btc = it.next();
			btc.updateHeader();
			if (btc.table instanceof TablePane) 
				changeHeaders((TablePane)btc.table); 
		}
	}
	
	void createSnapshot(String name) throws AWTException, IOException {
		try{
			JFrame frame = (JFrame)chart.getFrame();
//			BufferedImage image = new Robot().createScreenCapture( 
//					   new Rectangle(frame.getX() + 4, frame.getY() + 149, 
//					   		frame.getWidth() - 7, frame.getHeight() - 152));
			BufferedImage image = new Robot().createScreenCapture( 
					   new Rectangle(frame.getX() + 4, frame.getY(), 
					   		frame.getWidth() - 7, frame.getHeight()));

			File file = new File(name);
			ImageIO.write(image, "jpg", file);
		}catch (Exception e){}
	}

	class MenuEntry extends JMenuItem{
		private static final long serialVersionUID = 1L;
		MenuEntry(String str){
			this.setFont(font);
			this.setActionCommand(str);
			this.setText(str);
			this.addActionListener(OptionsMenu.this);
		}		
	}
	
	class CheckBoxMenuEntry extends JCheckBoxMenuItem{	
		private static final long serialVersionUID = 1L;
		CheckBoxMenuEntry(String str, boolean b){
			this (str);
			setSelected(b);			
		}
		CheckBoxMenuEntry(String str){
			setFont(font);
			setActionCommand(str);
			setText(str);
			addActionListener(OptionsMenu.this);
		}
	}
	
	class MenuTitle extends JMenu{
		private static final long serialVersionUID = 1L;

		MenuTitle(String str){
			this.setFont(font);
			this.setText(str);
		}
	}
	
}