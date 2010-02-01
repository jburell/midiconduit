//
//
// MidiPortMessage
//
//
package jmidi;

/**
 * A class for encapsulating MIDI input messages.
 *
 * <p>A MidiPortMessage contains data received from a MidiPort.  The data consists of a single message (or
 * as much as will fit in the MidiPortMessage's data instance variable) and the time the message was
 * received.  Time is measured as the number of seconds since MidiPort.open() was called, and should be
 * accurate to within one or two milliseconds.
 *
 * @version 2
 * @see     jmidi.MidiPort
 */

public class MidiPortMessage
{
	// instance variables
	public double timeStamp;
	public byte[] messageData;
	
	// constructor

/**
 * creates a MidiPortMessage with space for up to a set number of data bytes
 *
 */
	public MidiPortMessage(int len)
	{
		// allocate space for messageData
		messageData = new byte[len];
	}
}
