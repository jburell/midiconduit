package org.burre.MIDIConduit;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.Vector;

import javax.sound.midi.ShortMessage;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MIDIConduit{
	private TrayIcon m_trayIcon;
	private static CheckboxMenuItem[] m_inDeviceCBList;
	private static CheckboxMenuItem[] m_outDeviceCBList;
	public PopupMenu m_popup;
	public SystemTray m_tray;
	public MIDIRoute m_route;

	public static void main(String[] args){
		// Use an appropriate Look and Feel
		try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			// UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		}catch(UnsupportedLookAndFeelException ex){
			ex.printStackTrace();
		}catch(IllegalAccessException ex){
			ex.printStackTrace();
		}catch(InstantiationException ex){
			ex.printStackTrace();
		}catch(ClassNotFoundException ex){
			ex.printStackTrace();
		}

		// Turn off metal's use of bold fonts
		UIManager.put("swing.boldMetal", Boolean.FALSE);

		final MIDIConduit conduit = new MIDIConduit();

		// Schedule a job for the event-dispatching thread:
		// adding TrayIcon.
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				createAndShowGUI(conduit);
			}
		});
	}

	public MIDIConduit(){
		m_popup = new PopupMenu();
		m_tray = SystemTray.getSystemTray();
		m_trayIcon = new TrayIcon(createImage("/images/note.png", "tray icon"));
		m_route = new MIDIRoute(m_trayIcon);
	}

	public class StateChangedListener implements ItemListener{
		@Override
		public void itemStateChanged(ItemEvent e){
			int cb1Id = e.getStateChange();
			if(cb1Id == ItemEvent.SELECTED){
				Menu menu = ((Menu)((CheckboxMenuItem)e.getSource()).getParent());
				int itemCount = menu.getItemCount();
				for(int i = 0; i < itemCount; i++){
					if(menu.getItem(i).getLabel() != e.getItem().toString()){
						((CheckboxMenuItem)menu.getItem(i)).setState(false);
					}else{
						if(menu.getLabel() == "Output"){
							m_route.setSelectedOutDevice(i);
						}else if(menu.getLabel() == "Input"){
							m_route.setSelectedInDevice(i);
						}
						((CheckboxMenuItem)menu.getItem(i)).setState(true);
					}
				}
			}
		}
	}
	
	public class ExitActionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			m_tray.remove(m_trayIcon);
			System.exit(0);
		}
	}
	
	public class TestOperationActionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			MenuItem item = (MenuItem)e.getSource();
			if("Test MIDI output".equals(item.getLabel())){
				try{
					for(int j = 0; j < 128; j += 1){
						m_route.writeMIDIMessage(ShortMessage.CONTROL_CHANGE, (byte)0, (byte)1, (byte)j);
						Thread.sleep(10);
					}
				}catch(InterruptedException e1){
					m_trayIcon.displayMessage(
						"Error",
						e1.getMessage(),
						TrayIcon.MessageType.ERROR);
				}
			}
		}
	}

	public static void createAndShowGUI(MIDIConduit conduit){
		Vector<Info> inDevices = conduit.m_route.getInDevices();
		Vector<Info> outDevices = conduit.m_route.getOutDevices();

		// Check the SystemTray support
		if(!SystemTray.isSupported()){
			System.out.println("SystemTray is not supported");
			return;
		}

		// Init checkbox lists
		m_inDeviceCBList = new CheckboxMenuItem[inDevices.size()];
		m_outDeviceCBList = new CheckboxMenuItem[outDevices.size()];

		// Create a popup menu components
		Menu inMenu = new Menu("Input");
		Menu outMenu = new Menu("Output");
		MenuItem errorItem = new MenuItem("Error");
		MenuItem exitItem = new MenuItem("Exit");

		MenuItem midiItem = new MenuItem("Test MIDI output");

		// Add components to popup menu
		conduit.m_popup.add(inMenu);
		conduit.m_popup.add(outMenu);
		conduit.m_popup.add(midiItem);
		conduit.m_popup.addSeparator();
		conduit.m_popup.add(exitItem);

		// Populate input MIDI list
		for(int i = 0; i < inDevices.size(); i++){
			CheckboxMenuItem cb = new CheckboxMenuItem(inDevices.elementAt(i).getName());
			m_inDeviceCBList[i] = cb;
			cb.addItemListener(conduit.new StateChangedListener());

			inMenu.add(cb);
		}
		CheckboxMenuItem netCheckBox = new CheckboxMenuItem("Network (" + conduit.m_route.getMIDISocketMgr().getAdress().getHostAddress() + ")");
		netCheckBox.addItemListener(conduit.new StateChangedListener());
		inMenu.add(netCheckBox);

		// Populate output MIDI list
		for(int i = 0; i < outDevices.size(); i++){
			CheckboxMenuItem cb = new CheckboxMenuItem(outDevices.elementAt(i).getName());
			m_outDeviceCBList[i] = cb;
			cb.addItemListener(conduit.new StateChangedListener());

			outMenu.add(cb);
		}

		conduit.m_trayIcon.setPopupMenu(conduit.m_popup);

		try{
			conduit.m_tray.add(conduit.m_trayIcon);
		}catch(AWTException e){
			System.out.println("TrayIcon could not be added.");
			return;
		}

		conduit.m_trayIcon.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JOptionPane.showMessageDialog(
					null,
					"This dialog box is run from System Tray");
			}
		});

		ActionListener listener = conduit.new TestOperationActionListener();
		midiItem.addActionListener(listener);
		exitItem.addActionListener(conduit.new ExitActionListener());
	}

	// Obtain the image URL
	protected static Image createImage(String path, String description){
		URL imageURL = MIDIConduit.class.getResource(path);

		if(imageURL == null){
			System.err.println("Resource not found: " + path);
			return null;
		}else{
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}
}
