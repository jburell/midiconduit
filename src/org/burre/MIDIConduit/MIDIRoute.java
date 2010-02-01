package org.burre.MIDIConduit;

import java.awt.TrayIcon;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MidiDevice.Info;

import org.burre.MIDIConduit.net.IMIDIListener;
import org.burre.MIDIConduit.net.MIDISocketManager;

public class MIDIRoute{
	private Vector<MidiDevice> m_currOpenOutDevices = new Vector<MidiDevice>();
	private Vector<MidiDevice> m_currOpenInDevices = new Vector<MidiDevice>();
	private Vector<Info> m_inDevices = new Vector<Info>();
	private Vector<Info> m_outDevices = new Vector<Info>();

	public final int MIDI_SOCKET_DEVICE_NR = -1;
	private MIDISocketManager m_midiSocketMgr;
	private IMIDIListener m_socketListener = new IMIDIListener(){
		@Override
		public void recieve(ShortMessage message){
			System.out.println("Message recieved: " + "Command: "
				+ message.getCommand() + ", Channel: " + message.getChannel()
				+ ", Data 1 and 2: " + message.getData1() + ", " + message.getData2());
			writeMIDIMessage(
				message.getCommand(),
				(byte)message.getChannel(),
				(byte)message.getData1(),
				(byte)message.getData2());
		}
	};

	private HashMap<Info, MidiDevice> m_infoDeviceMap = new HashMap<Info, MidiDevice>();

	private TrayIcon m_trayIcon;

	private int m_selectedInDevice = 0;
	private int m_selectedOutDevice = 0;

	public static enum MessageType{
		NOTE_ON(0x90), NOTE_OFF(0x80), CC(0xB0);

		private int code;

		private MessageType(int code){
			this.code = code;
		}

		public int getCode(){
			return code;
		}
	}

	public MIDISocketManager getMIDISocketMgr(){
		return m_midiSocketMgr;
	}

	public void setSelectedOutDevice(int devNr){
		closeMIDIOutDevice(m_selectedOutDevice);
		m_selectedOutDevice = devNr;
		openMIDIOutput(m_selectedOutDevice);
	}

	public int getSelectedOutDevice(){
		return m_selectedOutDevice;
	}

	public void setSelectedInDevice(int devNr){
		closeMIDIInDevice(m_selectedInDevice);
		m_selectedInDevice = devNr;
		openMIDIInput(m_selectedInDevice);
	}

	public int getSelectedInDevice(){
		return m_selectedInDevice;
	}

	public Vector<Info> getOutDevices(){
		return m_outDevices;
	}

	public Vector<Info> getInDevices(){
		// TODO Auto-generated method stub
		return m_inDevices;
	}

	public MIDIRoute(TrayIcon parent){
		m_trayIcon = parent;
		enumerateDevices();
		try{
			m_midiSocketMgr = new MIDISocketManager(6666);
			m_midiSocketMgr.startServer();
		}catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void enumerateDevices(){
		Info[] deviceInfoArr = MidiSystem.getMidiDeviceInfo();

		for(int i = 0; i < deviceInfoArr.length; i++){
			try{
				if(MidiSystem.getMidiDevice(deviceInfoArr[i]).getMaxTransmitters() != 0 /*name.contains("MidiInDeviceProvider")*/){
					m_inDevices.add(deviceInfoArr[i]);
				}else if(MidiSystem.getMidiDevice(deviceInfoArr[i]).getMaxReceivers() != 0 /*name.contains("MidiOutDeviceProvider")*/){
					m_outDevices.add(deviceInfoArr[i]);
				}else{
					System.out.println("Unknown device type: " + deviceInfoArr[i]);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public void openMIDIInput(int deviceNr){
		if(deviceNr == m_inDevices.size()/* MIDI_SOCKET_DEVICE_NR*/){
			System.out.println("Opening MIDI Socket at port: "
				+ m_midiSocketMgr.getListenerPort());
			// Connect to a MIDISocket instead of a device
			// FIXME: later on we should have multiple sockets, for now just pick the first one
			m_midiSocketMgr.register(m_socketListener);
		}else{
			try{
				System.out.println("Fetching input device #" + deviceNr + ": "
					+ m_inDevices.elementAt(deviceNr).getName());
				MidiDevice device = MidiSystem.getMidiDevice(m_inDevices.elementAt(deviceNr));
				System.out.println("Opening port");
				device.open();

				m_currOpenInDevices.add(device);
				m_infoDeviceMap.put(m_inDevices.elementAt(deviceNr), device);

				System.out.println("Done!");
			}catch(MidiUnavailableException e){
				showErrDialog(m_trayIcon, e.getMessage());
			}
		}
	}

	public void openMIDIOutput(int deviceNr){
		try{
			System.out.println("Fetching output device #" + deviceNr + ": "
				+ m_outDevices.elementAt(deviceNr).getName());
			MidiDevice device = MidiSystem.getMidiDevice(m_outDevices.elementAt(deviceNr));
			System.out.println("Opening port");
			device.open();

			m_currOpenOutDevices.add(device);
			m_infoDeviceMap.put(m_outDevices.elementAt(deviceNr), device);

			System.out.println("Done!");
		}catch(MidiUnavailableException e){
			showErrDialog(m_trayIcon, e.getMessage());
		}
	}

	public void writeMIDIMessage(int command, byte channel, byte data1,
		byte data2){
		try{
			for(int i = 0; i < m_currOpenOutDevices.size(); i++){
				Receiver recv = m_currOpenOutDevices.elementAt(i).getReceiver();

				ShortMessage msg = new ShortMessage();

				msg.setMessage(command, channel, data1, data2);
				recv.send(msg, 1);
				recv.close();

				System.out.println("Sending: " + command + ", data1: " + data1
					+ ", data2: " + data2);
			}
		}catch(InvalidMidiDataException e){
			showErrDialog(m_trayIcon, e.getMessage());
		}catch(MidiUnavailableException e){
			showErrDialog(m_trayIcon, e.getMessage());
		}
	}

	public void closeMIDIInDevice(int deviceNr){
		if(deviceNr == MIDI_SOCKET_DEVICE_NR){
			System.out.println("Closing MIDI Socket at port: "
				+ m_midiSocketMgr.getListenerPort());
			// Connect to a MIDISocket instead of a device
			// FIXME: later on we should have multiple sockets, for now just pick the first one
			m_midiSocketMgr.unregister(m_socketListener);
		}else{
			System.out.println("Closing port");
			Info devInfo = m_inDevices.elementAt(deviceNr);

			MidiDevice device = m_infoDeviceMap.get(devInfo);
			//infoDeviceMap.remove(devInfo);

			if(m_currOpenInDevices.remove(device)){
				device.close();
			}
		}

		//System.out.println("Done!");
	}

	public void closeMIDIOutDevice(int deviceNr){
		System.out.println("Closing port");
		Info devInfo = m_outDevices.elementAt(deviceNr);

		MidiDevice device = m_infoDeviceMap.get(devInfo);
		//infoDeviceMap.remove(devInfo);

		if(m_currOpenOutDevices.remove(device)){
			device.close();
		}
		//System.out.println("Done!");
	}

	public static void showErrDialog(TrayIcon trayIcon, String message){
		trayIcon.displayMessage(
			"MIDIConduit",
			"Error: " + message,
			TrayIcon.MessageType.ERROR);
	}
}
