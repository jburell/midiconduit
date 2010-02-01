package org.burre.MIDIConduit.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.sound.midi.MidiMessage;

public class MIDISocketManager{
	int m_port = 6666;
	ServerSocket m_listenerSocket;
	
	Thread m_listenerThread;
	protected boolean m_stopListener = false;
	protected Vector<MIDISocket> m_openSockets = new Vector<MIDISocket>();
	protected Vector<IMIDIListener> m_midiListers = new Vector<IMIDIListener>();
	
	//protected HashMap<IMIDIListener, Vector<MIDISocket>> connections = new HashMap<IMIDIListener, Vector<MIDISocket>>();
	//protected HashMap<MIDISocket, Vector<IMIDIListener>> socketListeners = new HashMap<MIDISocket, Vector<IMIDIListener>>();
	
	/*public Vector<MIDISocket> getOpenSockets(){
		return m_openSockets;
	}*/
	
	public int getListenerPort(){
		return m_port;
	}
	
	public InetAddress getAdress(){
		return m_listenerSocket.getInetAddress();
	}
	
	public MIDISocketManager(int port) throws IOException{
		m_port = port;
	}
	
	public void startServer() throws IOException{
		m_listenerSocket = new ServerSocket(m_port);
		m_listenerThread = new Thread(new Runnable(){
			@Override public void run(){
				while(!m_stopListener){
					try{
						Socket tmpSocket = m_listenerSocket.accept();
						System.out.println("Socket connected: " + tmpSocket.getInetAddress() + ":" + tmpSocket.getPort());
						m_openSockets.add(new MIDISocket(tmpSocket));
						
						// Re-register the listeners to this new socket
						for(IMIDIListener listener: m_midiListers){
							for(MIDISocket midiSocket: m_openSockets){
								midiSocket.register(listener);
							}
						}
						
						Thread.sleep(10);
					}catch(IOException e){
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch(InterruptedException e){
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}, "MIDISocketManagerListener");
		
		m_stopListener = false;
		m_listenerThread.start();
	}
	
	public void stopServer(){
		for(MIDISocket sock: m_openSockets){
			try{
				sock.close();
				
			}catch(IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		m_openSockets.clear();
		
		try{
			m_stopListener = true;
			m_listenerSocket.close();
			m_listenerThread.join(1000);
			
			// TODO: Needed?
			// Interrupting the thread if still alive
			m_listenerThread.interrupt();
		}catch (Exception e) {
			// TODO: handle exception
		}		
	}
	
	public void register(IMIDIListener listener/*, MIDISocket socket*/){
		if(m_midiListers.contains(listener) == false){
			m_midiListers.add(listener);
		}
		
		for(MIDISocket socket: m_openSockets){
			socket.register(listener);
		}
		
//		if(m_openSockets.contains(socket) == false){
//			return false;
//		}
//		
//		// Register the socket to the listener
//		if(connections.containsKey(listener)){
//			Vector<MIDISocket> sockets = connections.get(listener);
//			if(sockets.contains(socket) == false){
//				
//				sockets.add(socket);
//			}
//		}else{
//			Vector<MIDISocket> sockets = new Vector<MIDISocket>();
//			sockets.add(socket);
//			connections.put(listener, sockets);
//		}
		
		//----------------------------
		
		// The reverse collection
		// Used for checking what listeners that listens to what socket
//		if(socketListeners.containsKey(socket)){
//			Vector<IMIDIListener> listeners = socketListeners.get(socket);
//			if(listeners.contains(listener) == false){
//				listeners.add(listener);
//			}
//		}else{
//			Vector<IMIDIListener> listeners = new Vector<IMIDIListener>();
//			listeners.add(listener);
//			socketListeners.put(socket, listeners);
//		}
//		return true;
	}
	
	public void unregister(IMIDIListener listener){
		m_midiListers.remove(listener);
		for(MIDISocket socket: m_openSockets){
			socket.unregister(listener);
		}
//		Vector<MIDISocket> sockets = connections.remove(listener);
//		
//		for(MIDISocket socket: sockets){
//		 //Vector<IMIDIListener> listeners = socketListeners.remove(socket);
//		 //for(IMIDIListener socketListener: listeners){
//			 socket.unregister(listener /*socketListener*/);
//		 //}
//		 //listeners.clear();
//		}
//		sockets.clear();
	}
}
