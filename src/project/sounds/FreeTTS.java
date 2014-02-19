/*
 * Creates a thread in order to play text to speech.
 */
package project.sounds;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Riz
 */
public class FreeTTS extends Thread {

	private final String VOICE_KEVIN16 = "kevin16";
	private final String VOICE_ALAN = "alan";
	private final String VOICE_KEVIN = "kevin";
	private final Voice helloVoice;
	private String speech;
	private boolean isPlaying;
	private long TOT_TIME_SLEEP = 2000;
	private String newSpeech;
	private String oldSpeech;
	private Thread threadActive;
	private Thread thisThread;

	public FreeTTS() {
		String voiceName = VOICE_KEVIN16;
		VoiceManager voiceManager = VoiceManager.getInstance();
		helloVoice = voiceManager.getVoice(voiceName);
	}

	/**
	 *
	 * @param speechString The String to be converted to human speech
	 */
	public void startSpeech(String newSpeech) {
		this.newSpeech = newSpeech;
		isPlaying = true;
	}

	@Override
	public synchronized void run() {
		helloVoice.allocate();
		thisThread = FreeTTS.currentThread();
		threadActive = thisThread;
		while (thisThread == threadActive) {
			if (isPlaying) {
				oldSpeech = newSpeech;
				// synthesize speech.
				helloVoice.speak(oldSpeech);
				isPlaying = false;
			}
			try {
				Thread.sleep(TOT_TIME_SLEEP);
			} catch (InterruptedException ex) {
				Logger.getLogger(FreeTTS.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		// clean up and leave
	}

	public void closeFreeTTS() {
		thisThread = null;
		helloVoice.deallocate();
	}

}
