package org.burre.MIDIConduit.net.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import junit.framework.TestCase;

public class SocketInputTest extends TestCase{
	public void testSocketInput(){
		try{
			Socket connSocket = new Socket("127.0.0.1", 6666);
			OutputStream out = connSocket.getOutputStream();
			ShortMessage msg = new ShortMessage();
			msg.setMessage(ShortMessage.CONTROL_CHANGE, 0, 3, 127);
			out.write(msg.getMessage());
			connSocket.close();
			System.out.println("Data successfully written");
		}catch(InvalidMidiDataException e){
			fail("Invalid MIDI data: " + e.getMessage());
		}catch(UnknownHostException e){
			fail("Unknown host: " + e.getMessage());
		}catch(IOException e){
			fail("IOException: " + e.getMessage());
		}catch(Exception e){
			fail("Unknown error: " + e.getMessage());
		}
	}
}
