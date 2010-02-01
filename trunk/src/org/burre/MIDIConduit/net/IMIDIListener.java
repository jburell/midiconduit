package org.burre.MIDIConduit.net;

import javax.sound.midi.ShortMessage;

public interface IMIDIListener{
	public void recieve(ShortMessage message);
}
