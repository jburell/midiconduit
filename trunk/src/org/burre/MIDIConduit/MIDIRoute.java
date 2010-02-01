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
	private Vector<MidiDevice> currOpenOutDevices = new Vector<MidiDevice>();
	private Vector<MidiDevice> currOpenInDevices = new Vector<MidiDevice>();
	private Vector<Info> inDevices = new Vector<Info>();
	private Vector<Info> outDevices = new Vector<Info>();

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

	private HashMap<Info, MidiDevice> infoDeviceMap = new HashMap<Info, MidiDevice>();

	private TrayIcon trayIcon;

	private int selectedInDevice = 0;
	private int selectedOutDevice = 0;

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
		closeMIDIOutDevice(selectedOutDevice);
		selectedOutDevice = devNr;
		openMIDIOutput(selectedOutDevice);
	}

	public int getSelectedOutDevice(){
		return selectedOutDevice;
	}

	public void setSelectedInDevice(int devNr){
		closeMIDIInDevice(selectedInDevice);
		selectedInDevice = devNr;
		openMIDIInput(selectedInDevice);
	}

	public int getSelectedInDevice(){
		return selectedInDevice;
	}

	public Vector<Info> getOutDevices(){
		return outDevices;
	}

	public Vector<Info> getInDevices(){
		// TODO Auto-generated method stub
		return inDevices;
	}

	public MIDIRoute(TrayIcon parent){
		trayIcon = parent;
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
					inDevices.add(deviceInfoArr[i]);
				}else if(MidiSystem.getMidiDevice(deviceInfoArr[i]).getMaxReceivers() != 0 /*name.contains("MidiOutDeviceProvider")*/){
					outDevices.add(deviceInfoArr[i]);
				}else{
					System.out.println("Unknown device type: " + deviceInfoArr[i]);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public void openMIDIInput(int deviceNr){
		if(deviceNr == inDevices.size()/* MIDI_SOCKET_DEVICE_NR*/){
			System.out.println("Opening MIDI Socket at port: "
				+ m_midiSocketMgr.getListenerPort());
			// Connect to a MIDISocket instead of a device
			// FIXME: later on we should have multiple sockets, for now just pick the first one
			m_midiSocketMgr.register(m_socketListener);
		}else{
			try{
				System.out.println("Fetching input device #" + deviceNr + ": "
					+ inDevices.elementAt(deviceNr).getName());
				MidiDevice device = MidiSystem.getMidiDevice(inDevices.elementAt(deviceNr));
				System.out.println("Opening port");
				device.open();

				currOpenInDevices.add(device);
				infoDeviceMap.put(inDevices.elementAt(deviceNr), device);

				System.out.println("Done!");
			}catch(MidiUnavailableException e){
				showErrDialog(trayIcon, e.getMessage());
			}
		}
	}

	public void openMIDIOutput(int deviceNr){
		try{
			System.out.println("Fetching output device #" + deviceNr + ": "
				+ outDevices.elementAt(deviceNr).getName());
			MidiDevice device = MidiSystem.getMidiDevice(outDevices.elementAt(deviceNr));
			System.out.println("Opening port");
			device.open();

			currOpenOutDevices.add(device);
			infoDeviceMap.put(outDevices.elementAt(deviceNr), device);

			System.out.println("Done!");
		}catch(MidiUnavailableException e){
			showErrDialog(trayIcon, e.getMessage());
		}
	}

	public void writeMIDIMessage(int command, byte channel, byte data1,
		byte data2){
		try{
			for(int i = 0; i < currOpenOutDevices.size(); i++){
				Receiver recv = currOpenOutDevices.elementAt(i).getReceiver();

				ShortMessage msg = new ShortMessage();

				msg.setMessage(command, channel, data1, data2);
				recv.send(msg, 1);
				recv.close();

				System.out.println("Sending: " + command + ", data1: " + data1
					+ ", data2: " + data2);
			}
		}catch(InvalidMidiDataException e){
			showErrDialog(trayIcon, e.getMessage());
		}catch(MidiUnavailableException e){
			showErrDialog(trayIcon, e.getMessage());
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
			Info devInfo = inDevices.elementAt(deviceNr);

			MidiDevice device = infoDeviceMap.get(devInfo);
			//infoDeviceMap.remove(devInfo);

			if(currOpenInDevices.remove(device)){
				device.close();
			}
		}

		//System.out.println("Done!");
	}

	public void closeMIDIOutDevice(int deviceNr){
		System.out.println("Closing port");
		Info devInfo = outDevices.elementAt(deviceNr);

		MidiDevice device = infoDeviceMap.get(devInfo);
		//infoDeviceMap.remove(devInfo);

		if(currOpenOutDevices.remove(device)){
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
