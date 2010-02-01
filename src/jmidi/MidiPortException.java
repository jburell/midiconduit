/*	MidiPortException.java
 *
 */

package jmidi;

/**
 * Thrown by MidiPort methods.
 */
public class MidiPortException extends Exception {

/**
	 * 
	 */
	private static final long serialVersionUID = -7811782855129849090L;

/**
* constructs a MidiPortException with no detail message.
*/
    public MidiPortException() {
		super();
    }

/**
* constructs a MidiPortException with a message reflecting a system error number.
*
* Error numbers are device-dependent.  They may be useful during development, if the underlying
* system error documentation is available.
*/
    public MidiPortException(int rc) {
		super("MIDI error return code: " + rc);
    }

/**
* constructs a MidiPortException with the specified detail message.
*/
    public MidiPortException(String string) {
		super("MIDI error: " + string);
    }
}

