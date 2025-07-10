import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

class Vars {
	public static Function<Double, Double> sineWave = x -> Math.sin(x);
	public static Sound sound = new Sound(new ArrayList<FreqVol>(), sineWave);
}

class MultiKeyPressListener implements KeyListener {
	static final Set<Integer> pressedKeys = new HashSet<>();

	@Override
	public synchronized void keyPressed(KeyEvent e) { // on key press
		pressedKeys.add(e.getKeyCode());
		if (!pressedKeys.isEmpty()) {
			for (Iterator<Integer> it = pressedKeys.iterator(); it.hasNext();) {
				switch (it.next()) {
					case KeyEvent.VK_ESCAPE:
						System.out.println("exiting...");
						System.exit(0);
						continue;
					case KeyEvent.VK_Q:
						Chord chord = new Chord(new Chord.Note(Chord.Note.Letter.C, Chord.Note.Accidental.Natural, 3),
								Chord.Variant.Tonic, Chord.Modifier.None, true);
						Vars.sound.setFreqvols(chord);
						continue;
				}
			}
		}
	}

	@Override
	public synchronized void keyReleased(KeyEvent e) {
		pressedKeys.remove(e.getKeyCode());
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// unused
	}
}

public class Main {
	public static void main(String[] args) throws InterruptedException, IOException {
		/* TODO 
			To Add:
				Add instructions (e.g., press this to play that)
				Add effects (Tremelo, Flanger, Chorus, Glide, ADSR, (Filter?))
				Add waveforms (Saw, Triangle, Square, (sinsin?))
			To Fix:
				Pressing 'Q' (switching chord) results in no sound. no idea why
		 */
		JFrame frame = new JFrame("Window Title");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);
		frame.setResizable(false);
		frame.setFocusable(true);
		frame.requestFocusInWindow();
		MultiKeyPressListener listener = new MultiKeyPressListener();
		frame.addKeyListener(listener);
		frame.setVisible(true);

		Chord chord = new Chord(new Chord.Note(Chord.Note.Letter.A, Chord.Note.Accidental.Natural, 3),
				Chord.Variant.Tonic, Chord.Modifier.None, true);
		Vars.sound.setFreqvols(chord);
		Flags flag = new Flags(SoundState.Running);
		Vars.sound.play(flag);
	}
}
