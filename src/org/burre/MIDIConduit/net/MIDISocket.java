package org.burre.MIDIConduit.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

public class MIDISocket {
	MIDISocketManager m_parent;
	InputStream m_inputStream;
	OutputStream m_outputStream;
	Socket m_socket;
	Thread m_socketListener;
	protected boolean m_isListening = true;
	Vector<IMIDIListener> m_midiInputListeners = new Vector<IMIDIListener>();
	
	public Vector<IMIDIListener> getListeners(){
		return m_midiInputListeners;
	}
	
	public MIDISocket(/*MIDISocketManager manager,*/ Socket socket) throws IOException{
		//m_parent = manager;
		m_socket = socket;
		m_inputStream = m_socket.getInputStream();
		
		m_socketListener = new Thread(new Runnable(){
			@Override
			public void run(){
				byte[] msgBytes = new byte[3];
				int numRead = 0;
				while(numRead != -1){
					try{
						numRead = m_inputStream.read(msgBytes, 0, 3);
						if(numRead != 3){
							if(numRead == -1){
								continue;
							}else{
								throw new IOException("Error: problem with MIDI message, expected 3 bytes, recieved: " + numRead + ", data: " + msgBytes);
							}
						}
						
						ShortMessage msg = new ShortMessage();
						msg.setMessage(msgBytes[0], msgBytes[1], msgBytes[2]);
						for(IMIDIListener listener: m_midiInputListeners){
							listener.recieve(msg);
						}
					}catch(IOException e){
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch(InvalidMidiDataException e){
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}, "MIDISocketDataListener");
		m_socketListener.start();
	}
	
	public void close() throws IOException{
		m_socket.close();
	}
	
	@Override
	protected void finalize() throws Throwable{
		m_isListening = false;
		m_socketListener.join(1000);
		
		m_socketListener.interrupt();
	}
	
	public void register(IMIDIListener listener){
		if(m_midiInputListeners.contains(listener) == false){
			m_midiInputListeners.add(listener);
		}
	}
	
	public void unregister(IMIDIListener listener){
		m_midiInputListeners.remove(listener);
	}
	
	public void writeMessage(MidiMessage message){
		try{
			System.out.println("Writing MIDI message");
			m_outputStream.write(message.getMessage());
		}catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
