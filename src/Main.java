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
	public static Chord chord = new Chord(new Chord.Note(Chord.Note.Letter.C, Chord.Note.Accidental.Natural, 2),
			Chord.Variant.Tonic, Chord.Modifier.None, true);
}

class MultiKeyPressListener implements KeyListener {
	static final Set<Integer> pressedKeys = new HashSet<>();

	@Override
	public synchronized void keyPressed(KeyEvent e) { // on key press
		pressedKeys.add(e.getKeyCode());
		System.err.println(pressedKeys);

		if (pressedKeys.contains(KeyEvent.VK_ESCAPE)) {
			System.out.println("Quitting...");
			System.exit(0);
		}

		if (pressedKeys.contains(KeyEvent.VK_A)) {
			Vars.chord.var = Chord.Variant.Tonic;
		} else if (pressedKeys.contains(KeyEvent.VK_W)) {
			Vars.chord.var = Chord.Variant.Supertonic;
		} else if (pressedKeys.contains(KeyEvent.VK_S)) {
			Vars.chord.var = Chord.Variant.Mediant;
		} else if (pressedKeys.contains(KeyEvent.VK_E)) {
			Vars.chord.var = Chord.Variant.Subdominant;
		} else if (pressedKeys.contains(KeyEvent.VK_D)) {
			Vars.chord.var = Chord.Variant.Dominant;
		} else if (pressedKeys.contains(KeyEvent.VK_R)) {
			Vars.chord.var = Chord.Variant.Submediant;
		} else if (pressedKeys.contains(KeyEvent.VK_F)) {
			Vars.chord.var = Chord.Variant.LeadingTone;
		} else {
			Vars.sound.flags = new Flags(SoundState.Paused);
		}

	}

	@Override
	public synchronized void keyReleased(KeyEvent e) {
		pressedKeys.remove(e.getKeyCode());
		System.err.println(pressedKeys);
		if (pressedKeys.isEmpty()) {
			Vars.sound.setFreqvols(Vars.chord, 0);
		}
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
				Make global vars atomic
				flags is null? what that means...
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

		Vars.sound.setFreqvols(Vars.chord, 0);
		Vars.sound.play();
	}
}
