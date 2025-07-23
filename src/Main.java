import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.*;
import java.util.Collections;
import java.util.List;

class Vars {
	public static AtomicReference<Function<Double, Double>> sineWave = new AtomicReference<>(x -> Math.sin(x));
	public static AtomicReference<Sound> sound = new AtomicReference<Sound>(
			new Sound(new ArrayList<FreqVol>(), sineWave.get()));
	public static AtomicReference<Chord> chord = new AtomicReference<>(
			new Chord(new Chord.Note(Chord.Note.Letter.C, Chord.Note.Accidental.Natural, 2),
					Chord.Variant.Tonic, Chord.Modifier.None, true));
	public static AtomicReference<Float> volume = new AtomicReference<Float>(1.0f);
}

class MultiKeyPressListener implements KeyListener {
	final Set<Integer> keys = Collections.synchronizedSet(new HashSet<>());

	Chord.Variant handleChordVar() {
		if (keys.contains(KeyEvent.VK_A)) {
			return Chord.Variant.Tonic;
		}
		if (keys.contains(KeyEvent.VK_W)) {
			return Chord.Variant.Supertonic;
		}
		if (keys.contains(KeyEvent.VK_S)) {
			return Chord.Variant.Mediant;
		}
		if (keys.contains(KeyEvent.VK_E)) {
			return Chord.Variant.Subdominant;
		}
		if (keys.contains(KeyEvent.VK_D)) {
			return Chord.Variant.Dominant;
		}
		if (keys.contains(KeyEvent.VK_R)) {
			return Chord.Variant.Submediant;
		}
		if (keys.contains(KeyEvent.VK_F)) {
			return Chord.Variant.LeadingTone;
		}
		return Chord.Variant.None;
	}

	Chord.Modifier handleChordMod() {
		int v, h;
		if (keys.contains(KeyEvent.VK_UP)) {
			v = 1;
		} else if (keys.contains(KeyEvent.VK_DOWN)) {
			v = -1;
		} else {
			v = 0;
		}
		if (keys.contains(KeyEvent.VK_LEFT)) {
			h = 1;
		} else if (keys.contains(KeyEvent.VK_RIGHT)) {
			h = -1;
		} else {
			h = 0;
		}
		if (v == 1 && h == 0)
			return Chord.Modifier.MajMin;
		else if (v == 1 && h == 1)
			return Chord.Modifier.Seven;
		else if (v == 0 && h == 1)
			return Chord.Modifier.MajMinSeven;
		else if (v == -1 && h == 1)
			return Chord.Modifier.MajMinNine;
		else if (v == -1 && h == 0)
			return Chord.Modifier.SusFour;
		else if (v == -1 && h == -1)
			return Chord.Modifier.SusTwoMajSix;
		else if (v == 0 && h == -1)
			return Chord.Modifier.Dim;
		else if (v == 1 && h == -1)
			return Chord.Modifier.Aug;
		else
			return Chord.Modifier.None;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		keys.add(e.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keys.remove(e.getKeyCode());
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
				Add settings (volume, effect manager, waveform switcher, key switcher)
				Add effects (Tremelo, Flanger, Chorus, Glide, ADSR, (Filter?))
				Add waveforms (Saw, Triangle, Square, (sinsin?))
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
		JLabel label = new JLabel("");
		frame.add(label);

		Thread gui = new Thread() {
			public void run() {
				while (true) {
					StringBuilder text = new StringBuilder("<html>");
					text.append(listener.keys).append("<br><br>");

					for (FreqVol freqvol : Vars.sound.get().freqvols) {
						text.append(freqvol.frequency).append("<br>");
					}
					text.append("</html>");

					SwingUtilities.invokeLater(() -> {
						label.setText(text.toString());
					});
				}
			}
		};
		Thread input = new Thread() {
			public void run() {
				while (true) {
					HashSet<Integer> keys;
					synchronized (listener.keys) {
						keys = new HashSet<>(listener.keys);
					}
					if (keys.contains(KeyEvent.VK_ESCAPE)) {
						System.out.println("Quitting...");
						System.exit(0);
					}

					Chord.Variant var = listener.handleChordVar();
					Chord.Modifier mod = listener.handleChordMod();
					Chord current = Vars.chord.get();
					if (current.mod == mod && current.var == var) {
						continue;
					}
					current.mod = mod;
					current.var = var;
					Vars.chord.set(current);

					Sound newsound = Vars.sound.get();
					newsound.setFreqvols(Vars.chord.get(), Vars.volume.get());
					Vars.sound.set(newsound);
				}
			}
		};

		gui.start();
		input.start();
		Sound temp = Vars.sound.get();
		temp.setFreqvols(Vars.chord.get(), 0);
		Vars.sound.set(temp);
		Vars.sound.get().play();
	}
}
