import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.*;
import java.util.Collections;
import java.util.Optional;

class Vars {
	public static Function<Double, Double> sine = x -> Math.sin(x);
	public static Function<Double, Double> square = x -> {
		double sinValue = Math.sin(x);
		return sinValue >= 0 ? 1.0 : -1.0;
	};
	public static Function<Double, Double> triangle = x -> {
		double normalized = x % (2 * Math.PI);
		if (normalized < 0)
			normalized += 2 * Math.PI;
		if (normalized <= Math.PI / 2) {
			return 2 * normalized / Math.PI;
		} else if (normalized <= 3 * Math.PI / 2) {
			return 2 - 2 * normalized / Math.PI;
		} else {
			return 2 * normalized / Math.PI - 4;
		}
	};
	public static Function<Double, Double> saw = x -> {
		double normalized = x % (2 * Math.PI);
		if (normalized < 0)
			normalized += 2 * Math.PI;
		return 2 * normalized / (2 * Math.PI) - 1;
	};
	public static AtomicReference<Sound> sound = new AtomicReference<Sound>(
			new Sound(new ArrayList<FreqVol>(), saw));
	public static AtomicReference<Chord> chord = new AtomicReference<>(
			new Chord(new Chord.Note(Chord.Note.Letter.G, Chord.Note.Accidental.Sharp, 3),
					Chord.Variant.Tonic, Chord.Modifier.None, true));
	public static AtomicReference<Double> volume = new AtomicReference<Double>(0.5d);
}

class MultiKeyPressListener implements KeyListener {
	final Set<Integer> keys = Collections.synchronizedSet(new HashSet<>());
	private JFrame parentFrame;

	public MultiKeyPressListener(JFrame frame) {
		this.parentFrame = frame;
	}

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

	Function<Double, Double> handleWave() {
		if (keys.contains(KeyEvent.VK_1)) {
			return Vars.sine;
		}
		if (keys.contains(KeyEvent.VK_2)) {
			return Vars.saw;
		}
		if (keys.contains(KeyEvent.VK_3)) {
			return Vars.square;
		}
		if (keys.contains(KeyEvent.VK_4)) {
			return Vars.triangle;
		}
		return Vars.sound.get().wave;
	}

	double handleVolume() {
		double step = 0.01;
		if (keys.contains(KeyEvent.VK_T)) {
			return Vars.volume.get() + step;
		}
		if (keys.contains(KeyEvent.VK_G)) {
			return Vars.volume.get() - step;
		}
		return Vars.volume.get();
	}

	boolean shouldOpenEffectsMenu() {
		return keys.contains(KeyEvent.VK_B);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		keys.add(e.getKeyCode());
		if (e.getKeyCode() == KeyEvent.VK_B) {
			SwingUtilities.invokeLater(() -> {
				EffectsSettingsMenu effectsMenu = new EffectsSettingsMenu(parentFrame, Vars.sound.get());
				effectsMenu.setVisible(true);
			});
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keys.remove(e.getKeyCode());
		Vars.sound.get().effects.adsr.ifPresent(a -> {
			a.release();
		});

	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}

public class Main {
	public static void main(String[] args) throws InterruptedException, IOException {
		JFrame frame = new JFrame("NUCHORD - (c) Samiyel Frazier 2025");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 600);
		frame.setResizable(true);
		frame.setFocusable(true);
		frame.requestFocusInWindow();
		frame.setLocation((1920 - 500) / 2, (1080 - 600) / 2);
		MultiKeyPressListener listener = new MultiKeyPressListener(frame);
		frame.addKeyListener(listener);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		String controlstext = new String(Files.readAllBytes(Paths.get("src/controls.html")), StandardCharsets.UTF_8);
		JLabel info = new JLabel(controlstext);
		info.setVerticalAlignment(JLabel.TOP);
		panel.add(info);
		JLabel debuginfo = new JLabel("");
		debuginfo.setVerticalAlignment(JLabel.BOTTOM);
		panel.add(debuginfo);
		JMenuBar menuBar = new JMenuBar();
		JMenu settingsMenu = new JMenu("Settings");
		JMenuItem effectsMenuItem = new JMenuItem("Effects Settings");
		effectsMenuItem.addActionListener(e -> {
			EffectsSettingsMenu effectsMenu = new EffectsSettingsMenu(frame, Vars.sound.get());
			effectsMenu.setVisible(true);
		});
		settingsMenu.add(effectsMenuItem);
		menuBar.add(settingsMenu);
		frame.setJMenuBar(menuBar);
		frame.setContentPane(panel);
		frame.setVisible(true);
		Thread gui = new Thread() {
			public void run() {
				while (true) {
					StringBuilder text = new StringBuilder("<html>");
					text.append("Keys: ").append(listener.keys).append("<br><br>");
					text.append("Current Frequencies:<br>");
					for (FreqVol freqvol : Vars.sound.get().freqvols) {
						text.append(String.format("%.2f Hz<br>", freqvol.frequency));
					}
					text.append("<br>Volume: ").append(String.format("%.2f", Vars.volume.get())).append("<br>");
					if (Vars.sound.get().effects != null) {
						text.append("<br>Active Effects:<br>");
						if (Vars.sound.get().effects.trem.isPresent()) {
							Sound.Effects.Tremelo trem = Vars.sound.get().effects.trem.get();
							text.append("- Tremolo (Strength: ").append(String.format("%.2f", trem.stren))
									.append(", Freq: ").append(String.format("%.1f Hz", trem.freq)).append(")<br>");
						}
						if (Vars.sound.get().effects.vib.isPresent()) {
							Sound.Effects.Vibrato vib = Vars.sound.get().effects.vib.get();
							text.append("- Vibrato (Strength: ").append(String.format("%.2f", vib.stren))
									.append(", Freq: ").append(String.format("%.1f Hz", vib.freq)).append(")<br>");
						}
						if (Vars.sound.get().effects.adsr.isPresent()) {
							text.append("- ADSR Envelope<br>");
						}
						if (Vars.sound.get().effects.glide.isPresent()) {
							Sound.Effects.Glide glide = Vars.sound.get().effects.glide.get();
							text.append("- Glide (Duration: ").append(String.format("%.1fs", glide.totalTimeSeconds))
									.append(")<br>");
						}
						if (Vars.sound.get().effects.chorus.isPresent()) {
							Sound.Effects.Chorus chorus = Vars.sound.get().effects.chorus.get();
							text.append("- Chorus (Voices: ").append(chorus.voices)
									.append(", Detune: ").append(String.format("%.1f cents", chorus.detune))
									.append("<br>");
						}
						int totalVoices = Vars.sound.get().freqvols.size();
						if (Vars.sound.get().effects.chorus.isPresent()) {
							totalVoices *= Vars.sound.get().effects.chorus.get().voices;
						}
						text.append("<br>Total Active Voices: ").append(totalVoices).append("<br>");
					}
					text.append("</html>");
					SwingUtilities.invokeLater(() -> {
						debuginfo.setText(text.toString());
					});
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						break;
					}
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
					Sound newsound = Vars.sound.get();
					Chord.Variant var = listener.handleChordVar();
					Chord.Modifier mod = listener.handleChordMod();
					Chord currentChord = Vars.chord.get();
					if (!(currentChord.mod == mod && currentChord.var == var)
							|| Vars.volume.get() != listener.handleVolume()) {
						currentChord.mod = mod;
						currentChord.var = var;
						Vars.chord.set(currentChord);
						newsound.setFreqvols(Vars.chord.get(), Vars.volume.get());
					}
					Function<Double, Double> wav = listener.handleWave();
					if (!(newsound.wave == wav)) {
						newsound.wave = wav;
					}
					Vars.volume.set(listener.handleVolume());
					Vars.sound.set(newsound);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		gui.start();
		input.start();
		Sound temp = Vars.sound.get();
		temp.setFreqvols(Vars.chord.get(), 0);
		temp.effects = temp.new Effects(
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		Vars.sound.set(temp);
		Vars.sound.get().play();
		System.err.println("Sound playback stopped unexpectedly");
		System.exit(1);
	}
}
