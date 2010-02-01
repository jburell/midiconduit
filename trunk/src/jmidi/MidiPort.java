//
//
// MidiPort
//
//
package jmidi;

import java.io.File;
import java.net.URL;
import java.util.Vector;

import javax.swing.JOptionPane;

/**
 * A class for platform-independent MIDI input and output.
 *
 * <p>MidiPort wraps the device-dependent system calls necessary for reading and writing MIDI
 * data to and from MIDI devices.  Output can take two forms: short and long messages.  Long
 * messages are used for most System Exclusive commands; short messages are used for everything
 * else.  Input is buffered with a circular buffer, which can be read one message at a time.
 * A method is provided indicating how many complete messages are in the buffer.
 *
 * <p>Before reading or writing, a MidiPort must be opened with the open() method.  Before it is
 * opened, the input and output device numbers must be specified, either in the MidiPort's constructor
 * or using the setDeviceNumber() accessor.  The number of valid devices and their names
 * can be retrieved using the getNumDevices() and getDeviceName() methods.  A Vector
 * of devices of a given type can be obtained through a call to the method enumerateDevices().
 *
 * <p>After use, a MidiPort should be closed with the close() method.
 * 
 * <p>MidiPort has an associated exception class, MidiPortException.  An instance of MidiPortException
 * is thrown when an error occurs.
 *
 * @version 1
 * @see     jmidi.MidiPortException
 */

public class MidiPort{
	// static variables
	/**
	 * indicates the MIDI device is of type input.
	 */
	static public final int MIDIPORT_INPUT = 0;
	/**
	 * indicates the MIDI device is of type output.
	 */
	static public final int MIDIPORT_OUTPUT = 1;

	// library loader
	static{
		try{
			System.loadLibrary("MidiPort");
		}catch(UnsatisfiedLinkError e){
			try{
			URL url = MidiPort.class.getResource("/exe/MidiPort.dll");
			String f = new File(url.getFile()).getAbsolutePath();
			System.load(f);
			}catch(UnsatisfiedLinkError err){
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error loading MidiPort.dll", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// private instance variables -----------------------------------------
	private int inputDeviceNumber = -1;
	private int outputDeviceNumber = -1;
	private boolean isOpen = false;
	//private double lastTimeStamp = 0;

	// constructors ------------------------------------------------------
	/**
	 * creates a MidiPort, but doesn't set the input and output devices.
	 *
	 * @see #setDeviceNumber
	 * @see #getDeviceNumber
	 */
	public MidiPort(){
	}

	/**
	 * creates a MidiPort and sets the input and output device numbers.
	 *
	 * @see #setDeviceNumber
	 * @see #getDeviceNumber
	 */
	public MidiPort(int inputDeviceNumber, int outputDeviceNumber)
		throws MidiPortException{
		setDeviceNumber(MIDIPORT_INPUT, inputDeviceNumber);
		setDeviceNumber(MIDIPORT_OUTPUT, outputDeviceNumber);
	}

	// accessors ---------------------------------------------------------
	/**
	 * sets either the inputDevice or outputDevice.
	 *
	 * A given system may have one or more MIDI devices accessible to the system.  Before reading or
	 * writing MIDI data, the MidiPort must be opened on a specific input and output device.  Devices
	 * are identified using an index; 0 is the lowest-numbered device.  The number of valid input or
	 * output devices in the system can be derived from a call to the method getNumDevices().  The
	 * name of a given device can be derived from a call to the method getDeviceName().  A Vector
	 * of devices of a given type can be obtained through a call to the method enumerateDevices().
	 *
	 * <p>If the specified device is open when setDeviceNumber() is called, the system closes both
	 * input and output devices, and then opens the new devices.
	 *
	 * <p>If the device numbers were passed as arguments to the MidiPort constructor, you only need to
	 * call setDeviceNumber() if you want to change the device(s) being used.
	 *
	 * @param     type    either MIDIPORT_INPUT or MIDIPORT_OUTPUT
	 * @param     n       the number of the device.
	 * @exception jmidi.MidiPortException
	 *                      if type is not MIDIPORT_INPUT or MIDIPORT_OUTPUT, or if the MidiPort is
	 *                      already open and closing and reopening it throws an exception.
	 * @see       #getDeviceNumber
	 * @see       #getNumDevices
	 * @see       #getDeviceName
	 */
	public void setDeviceNumber(int type, int n) throws MidiPortException{
		boolean wasOpen = isOpen;
		if(isOpen)
			close();

		switch(type){
		case MIDIPORT_INPUT:
			inputDeviceNumber = n;
		break;
		case MIDIPORT_OUTPUT:
			outputDeviceNumber = n;
		break;
		default:
			throw new MidiPortException(-1);
		}

		if(wasOpen)
			open();
	}

	/**
	 * gets either the current inputDevice or current outputDevice.
	 *
	 * A given system may have one or more MIDI devices accessible to the system.  Before reading or
	 * writing MIDI data, the MidiPort must be opened on a specific input and output device.  Devices
	 * are identified using an index; 0 is the lowest-numbered device.  The number of valid input or
	 * output devices in the system can be derived from a call to the method getNumDevices().  The
	 * name of a given device can be derived from a call to the method getDeviceName().  A Vector
	 * of devices of a given type can be obtained through a call to the method enumerateDevices().
	 *
	 * @param     type    either MIDIPORT_INPUT or MIDIPORT_OUTPUT.
	 * @return            the selected device number.
	 * @exception jmidi.MidiPortException
	 *                      if type is neither MIDIPORT_INPUT nor MIDIPORT_OUTPUT.
	 * @see       #setDeviceNumber
	 * @see       #getNumDevices
	 * @see       #getDeviceName
	 */
	public int getDeviceNumber(int type) throws MidiPortException{
		switch(type){
		case MIDIPORT_INPUT:
			return (inputDeviceNumber);
		case MIDIPORT_OUTPUT:
			return (outputDeviceNumber);
		default:
			throw new MidiPortException(-1);
		}
	}

	// native methods ----------------------------------------------------
	private native static int nGetNumDevices(int type);
	private native static String nGetDeviceName(int type, int num);
	private native int nOpen();
	private native int nClose();
	private native int nWriteShortMessage(byte status, byte data1, byte data2);
	private native int nWriteLongMessage(byte[] message, int len, int timeout);
	private native int nReadMessage(byte[] message, int maxLength);
	private native int nReadMessageToObject(MidiPortMessage msg, int maxLength);
	private native int nMessagesWaiting();
	private native int nResetInput();

	// wrapper methods ---------------------------------------------------
	/**
	 * returns the number of input or output devices known to the system.
	 * 
	 * A given system may have one or more accessible MIDI devices.  Before reading or
	 * writing MIDI data, the MidiPort must be opened on a specific input and output device.  Devices
	 * are identified using an index; 0 is the lowest-numbered device.  The number of valid input or
	 * output devices in the system can be derived from a call to this method.  The
	 * name of a given device can be derived from a call to the method getDeviceName().
	 *
	 * @param     type    either MIDIPORT_INPUT or MIDIPORT_OUTPUT.
	 * @return            the number of devices of the selected type known to the system.
	 * @exception jmidi.MidiPortException
	 *                      if the device-dependent library flags an error.  The message associated
	 *                      with the exception is derived from the system-dependent error.
	 * @see       #getDeviceName
	 */
	public static int getNumDevices(int type) throws MidiPortException{
		int err = nGetNumDevices(type);
		if(err < 0)
			throw new MidiPortException(err);
		return (err);
	}
	/**
	 * returns a String containing the system-defined name of a given MIDI device.
	 *
	 * @param     type    either MIDIPORT_INPUT or MIDIPORT_OUTPUT.
	 * @param     num     the index of the device.  0 is the first device; use getNumDevices() to find
	 *                    out how many devices of the given type are known.
	 * @return            the system-defined device name.
	 * @exception jmidi.MidiPortException
	 *                      if the device-dependent library flags an error.  The message associated
	 *                      with the exception is derived from the system-dependent error.
	 * @see       #getNumDevices
	 */
	public static String getDeviceName(int type, int num)
		throws MidiPortException{
		String name;

		name = nGetDeviceName(type, num);
		if(name == null)
			throw new MidiPortException();
		return (name);
	}

	/**
	 * returns a Vector of devices of the specified type.
	 * 
	 * @param     type    either MIDIPORT_INPUT or MIDIPORT_OUTPUT.
	 * @return            a Vector containing the names of all the known devices of the specified type.
	 * @exception jmidi.MidiPortException
	 *                      if the device-dependent library flags an error.  The message associated
	 *                      with the exception is derived from the system-dependent error.
	 * @see       #getNumDevices
	 * @see       #getDeviceName
	 */
	public static Vector<String> enumerateDevices(int type)
		throws MidiPortException{
		Vector<String> result = new Vector<String>();

		for(int i = 0; i < getNumDevices(type); i++){
			result.addElement(getDeviceName(type, i));
		}

		return (result);
	}

	/**
	 * opens the current input device for reading, and the current output device for writing.
	 *
	 * @exception jmidi.MidiPortException
	 *                      if the current input or output device has not been set, or if the
	 *                      device-dependent library flags an error.  In the latter case, the message
	 *                      associated with the exception is derived from the system-dependent error.
	 * @see       #setDeviceNumber
	 */
	public void open() throws MidiPortException{
		// check that devices have been set
		if((getDeviceNumber(MIDIPORT_INPUT) < 0)
			|| (getDeviceNumber(MIDIPORT_OUTPUT) < 0))
			throw new MidiPortException(-1);

		int err = nOpen();
		if(err != 0)
			throw new MidiPortException(err);
		else
			isOpen = true;
	}

	/**
	 * closes the current input and output devices.
	 *
	 * If the MidiPort has not been opened, closing it does nothing.
	 *
	 * @exception jmidi.MidiPortException
	 *                      if the device-dependent library flags an error.  The message
	 *                      associated with the exception is derived from the system-dependent error.
	 * @see       #open
	 */
	public void close() throws MidiPortException{
		int err = nClose();
		if(err != 0)
			throw new MidiPortException(err);
		else
			isOpen = false;
	}

	/**
	 * writes a 2-data-byte short message to the output device.
	 *
	 * 2-byte messages include, for example, NoteOn and NoteOff.  Note that the status byte must always
	 * be provided when writing messages; whether or not running status is used at lower levels is system-dependent.
	 * A one-byte version of writeShortMessage() is also defined.
	 *
	 * @param     status  the status byte containing the command and channel, as defined by the
	 *                    MIDI spec.
	 * @param     data1   the first data byte following the status byte, as defined by the MIDI spec.
	 *                    For example, a NoteOn message has the note number as the first data byte.
	 * @param     data2   the second data byte following the status byte, as defined by the MIDI spec.
	 *                    For example, a NoteOn message has the velocity as the second data byte.
	 * @exception jmidi.MidiPortException
	 *                      if the device-dependent library flags an error.  The message
	 *                      associated with the exception is derived from the system-dependent error.
	 */
	public void writeShortMessage(byte status, byte data1, byte data2)
		throws MidiPortException{
		int err = nWriteShortMessage(status, data1, data2);
		if(err != 0)
			throw new MidiPortException(err);
	}

	/**
	 * writes a 1-data-byte short message to the output device.
	 *
	 * 1-byte messages include, for example, Aftertouch.  Note that the status byte must always
	 * be provided when writing messages; whether or not running status is used at lower levels is system-dependent.
	 * A two-byte version of writeShortMessage() is also defined.
	 *
	 * @param     status  the status byte containing the command and channel, as defined by the
	 *                    MIDI spec.
	 * @param     data1   the data byte following the status byte, as defined by the MIDI spec.
	 *                    For example, an Aftertouch message has the pressure value as the data byte.
	 * @exception jmidi.MidiPortException
	 *                      if the device-dependent library flags an error.  The message
	 *                      associated with the exception is derived from the system-dependent error.
	 */
	public void writeShortMessage(byte status, byte data1)
		throws MidiPortException{
		int err = nWriteShortMessage(status, data1, (byte)0);
		if(err != 0)
			throw new MidiPortException(err);
	}

	/**
	 * writes a complete, or a portion of, a System Exclusive message to the output device.
	 *
	 * This method blocks until the system has handled the message buffer array.  On systems that support it,
	 * the timeout parameter can be used to abort a blocked transaction after a set period of time.
	 * @param     message a byte buffer containing the message data.  Both the sysex message start
	 *                    and message end bytes should be included.  Other bytes should have their top
	 *                    bits clear, as per the MIDI spec.
	 * @param     len     the number of bytes in the message, including start and end bytes.  If the buffer
	 *                    represents only part of a message, then len should be the length of the byte array.
	 * @param     timeout the number of milliseconds to wait for the write to complete.  On systems that support
	 *                    it, the call will abort after this time and throw an exception.
	 * @exception jmidi.MidiPortException
	 *                      if the device-dependent library flags an error.  The message
	 *                      associated with the exception is derived from the system-dependent error.
	 */
	public void writeLongMessage(byte[] message, int len, int timeout)
		throws MidiPortException{
		int err = nWriteLongMessage(message, len, timeout);
		if(err != 0)
			throw new MidiPortException(err);
	}

	/**
	 * reads a complete message, or as much will fit, into a user-specified buffer, if a message is
	 * waiting.
	 *
	 * Use messagesWaiting() to determine if there is a message to be read.  If the message is successfully
	 * read, the number of messages waiting returned by messagesWaiting() will be decremented.  If the
	 * message won't fit completely in the message buffer you provide, the number of messages waiting
	 * won't be decremented.
	 *
	 * <p>If an input overrun has been detected (see messagesWaiting()), there may be spurious bytes in the
	 * input buffer.  You should call resetInput() to restart MIDI input.
	 *
	 * <p>If there are no messages waiting, readMessage() does nothing.
	 *
	 * @param     message   a byte buffer into which the message is read by the system.
	 * @param     maxLength the maximum number of bytes to write into the message buffer.  This is normally
	 *                      just the length of the byte array.
	 * @return              the number of bytes read into the message array.
	 * @exception jmidi.MidiPortException
	 *                      if the device-dependent library flags an error.  The message
	 *                      associated with the exception is derived from the system-dependent error.
	 * @see       #messagesWaiting
	 * @see       #resetInput
	 */
	public int readMessage(byte[] message, int maxLength)
		throws MidiPortException{
		int err = nReadMessage(message, maxLength);
		if(err <= 0)
			throw new MidiPortException(err);
		else
			return (err);
	}

	/**
	 * reads a complete message, or as much will fit, and its timestamp into a MidiPortMsg object, if a message is
	 * waiting.
	 *
	 * Use messagesWaiting() to determine if there is a message to be read.  If the message is successfully
	 * read, the number of messages waiting returned by messagesWaiting() will be decremented.  If the
	 * message won't fit completely in the message buffer you provide, the number of messages waiting
	 * won't be decremented.
	 *
	 * <p>If an input overrun has been detected (see messagesWaiting()), there may be spurious bytes in the
	 * input buffer.  You should call resetInput() to restart MIDI input.
	 *
	 * <p>If there are no messages waiting, readMessage() does nothing.
	 *
	 * @param     message   a MidiPortMsg object.
	 * @return              the number of bytes read into the message array.
	 * @exception jmidi.MidiPortException
	 *                      if the device-dependent library flags an error.  The message
	 *                      associated with the exception is derived from the system-dependent error.
	 * @see       #readMessage 
	 * @see       #messagesWaiting
	 * @see       #resetInput
	 */
	public int readMessage(MidiPortMessage msg) throws MidiPortException{
		int err;
		int len = msg.messageData.length;

		err = nReadMessageToObject(msg, len);
		if(err <= 0)
			throw new MidiPortException(err);
		else
			return (err);
	}

	/**
	 * returns the number of complete messages received and not yet read with readMessage().
	 *
	 * Note that messagesWaiting() will not reflect that a long message has been partially received, or
	 * that a message has been partially read using readMessage().
	 *
	 * <p>This method is also used to check for input overrun.  Overrun occurs when more data has been
	 * received but not read with readMessage() than can be stored in the system's circular buffer.
	 * When this happens, further input is not written to the circular buffer, and the next call to
	 * messagesWaiting() throws a "Buffer overrun" MidiPortException.  Your program should check for
	 * this condition, and call resetInput() to re-enable MIDI input.
	 *
	 * @exception jmidi.MidiPortException
	 *                      if an input overrun condition has occurred, or if the device-dependent library
	 *                      flags an error.  In the latter case, the message
	 *                      associated with the exception is derived from the system-dependent error.
	 * @see       #resetInput
	 * @see       #readMessage
	 */
	public int messagesWaiting() throws MidiPortException{
		int err = nMessagesWaiting();
		if(err < 0)
			throw new MidiPortException("Buffer overrun");
		else
			return (err);
	}

	/**
	 * Re-enables MIDI input after an overrun has been detected.
	 *
	 * Overrun occurs when more data has been
	 * received but not read with readMessage() than can be stored in the system's circular buffer.
	 * When this happens, further input is not written to the circular buffer, and the next call to
	 * messagesWaiting() throws a "Buffer overrun" MidiPortException.  Your program should check for
	 * this condition, and call resetInput() to re-enable MIDI input.
	 *
	 * @exception jmidi.MidiPortException
	 *                      If the device-dependent library flags an error.  The message
	 *                      associated with the exception is derived from the system-dependent error.
	 * @see       #messagesWaiting
	 * @see       #readMessage
	 */
	public void resetInput() throws MidiPortException{
		int err = nResetInput();
		if(err < 0)
			throw new MidiPortException(err);
	}
}