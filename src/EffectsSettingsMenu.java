import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Optional;

public class EffectsSettingsMenu extends JDialog {
	private Sound currentSound;
	private JCheckBox tremoloEnabled;
	private JSlider tremoloStrength;
	private JSlider tremoloFrequency;
	private JLabel tremoloStrengthLabel;
	private JLabel tremoloFrequencyLabel;
	private JCheckBox flangerEnabled;
	private JSlider flangerStrength;
	private JSlider flangerFrequency;
	private JLabel flangerStrengthLabel;
	private JLabel flangerFrequencyLabel;
	private JCheckBox adsrEnabled;
	private JSlider adsrPeak;
	private JSlider adsrTimeToPeak;
	private JSlider adsrSustain;
	private JSlider adsrTimeToRelease;
	private JLabel adsrPeakLabel;
	private JLabel adsrTimeToPeakLabel;
	private JLabel adsrSustainLabel;
	private JLabel adsrTimeToReleaseLabel;
	private JCheckBox glideEnabled;
	private JSlider glideDuration;
	private JLabel glideDurationLabel;
	private JButton startGlideButton;
	private JSlider voicesSlider;
	private JLabel voicesLabel;
	private JButton presetClean;
	private JButton presetVibrato;
	private JButton presetChorus;
	private JButton presetOrgan;

	public EffectsSettingsMenu(JFrame parent, Sound sound) {
		super(parent, "Effects Settings", true);
		this.currentSound = sound;
		initializeComponents();
		layoutComponents();
		addListeners();
		loadCurrentSettings();
		setSize(500, 700);
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void initializeComponents() {
		tremoloEnabled = new JCheckBox("Enable Tremolo");
		tremoloStrength = new JSlider(0, 100, 50);
		tremoloFrequency = new JSlider(1, 10, 5);
		tremoloStrengthLabel = new JLabel("Strength: 0.5");
		tremoloFrequencyLabel = new JLabel("Frequency: 5.0 Hz");
		flangerEnabled = new JCheckBox("Enable Flanger");
		flangerStrength = new JSlider(0, 10, 3);
		flangerFrequency = new JSlider(1, 10, 2);
		flangerStrengthLabel = new JLabel("Strength: 0.3");
		flangerFrequencyLabel = new JLabel("Frequency: 2.0 Hz");
		adsrEnabled = new JCheckBox("Enable ADSR Envelope");
		adsrPeak = new JSlider(0, 100, 80);
		adsrTimeToPeak = new JSlider(1, 50, 10);
		adsrSustain = new JSlider(0, 100, 60);
		adsrTimeToRelease = new JSlider(1, 100, 20);
		adsrPeakLabel = new JLabel("Peak: 0.8");
		adsrTimeToPeakLabel = new JLabel("Time to Peak: 1.0s");
		adsrSustainLabel = new JLabel("Sustain: 0.6");
		adsrTimeToReleaseLabel = new JLabel("Release Time: 2.0s");
		glideEnabled = new JCheckBox("Enable Glide");
		glideDuration = new JSlider(1, 100, 30);
		glideDurationLabel = new JLabel("Duration: 3.0s");
		startGlideButton = new JButton("Start Glide Test");
		voicesSlider = new JSlider(1, 10, 5);
		voicesLabel = new JLabel("Voices: 5");
		presetClean = new JButton("Clean");
		presetVibrato = new JButton("Vibrato");
		presetChorus = new JButton("Chorus");
		presetOrgan = new JButton("Organ");
		configureSlider(tremoloStrength);
		configureSlider(tremoloFrequency);
		configureSlider(flangerStrength);
		configureSlider(flangerFrequency);
		configureSlider(adsrPeak);
		configureSlider(adsrTimeToPeak);
		configureSlider(adsrSustain);
		configureSlider(adsrTimeToRelease);
		configureSlider(glideDuration);
		configureSlider(voicesSlider);
		loadPreset("clean");
	}

	private void configureSlider(JSlider slider) {
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setPaintLabels(false);
	}

	private void layoutComponents() {
		setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel tremoloPanel = createEffectPanel("Tremolo", new Component[] {
				tremoloEnabled,
				tremoloStrengthLabel, tremoloStrength,
				tremoloFrequencyLabel, tremoloFrequency
		});
		JPanel flangerPanel = createEffectPanel("Flanger", new Component[] {
				flangerEnabled,
				flangerStrengthLabel, flangerStrength,
				flangerFrequencyLabel, flangerFrequency
		});
		JPanel adsrPanel = createEffectPanel("ADSR Envelope", new Component[] {
				adsrEnabled,
				adsrPeakLabel, adsrPeak,
				adsrTimeToPeakLabel, adsrTimeToPeak,
				adsrSustainLabel, adsrSustain,
				adsrTimeToReleaseLabel, adsrTimeToRelease
		});
		JPanel glidePanel = createEffectPanel("Glide", new Component[] {
				glideEnabled,
				glideDurationLabel, glideDuration,
				startGlideButton
		});
		JPanel voicesPanel = createEffectPanel("Harmonics", new Component[] {
				voicesLabel, voicesSlider
		});
		JPanel presetsPanel = new JPanel(new GridLayout(1, 4, 5, 5));
		presetsPanel.setBorder(new TitledBorder("Presets"));
		presetsPanel.add(presetClean);
		presetsPanel.add(presetVibrato);
		presetsPanel.add(presetChorus);
		presetsPanel.add(presetOrgan);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton cancelButton = new JButton("Cancel");
		JButton okButton = new JButton("OK");
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		mainPanel.add(tremoloPanel);
		mainPanel.add(flangerPanel);
		mainPanel.add(adsrPanel);
		mainPanel.add(glidePanel);
		mainPanel.add(voicesPanel);
		mainPanel.add(presetsPanel);
		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		okButton.addActionListener(e -> {
			applySettings();
			dispose();
		});
		cancelButton.addActionListener(e -> dispose());
	}

	private JPanel createEffectPanel(String title, Component[] components) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(new TitledBorder(title));
		for (Component comp : components) {
			JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
			wrapper.add(comp);
			panel.add(wrapper);
		}
		return panel;
	}

	private void addListeners() {
		tremoloStrength.addChangeListener(e -> {
			double value = tremoloStrength.getValue() / 100.0;
			tremoloStrengthLabel.setText("Strength: " + String.format("%.2f", value));
		});
		tremoloFrequency.addChangeListener(e -> {
			double value = tremoloFrequency.getValue();
			tremoloFrequencyLabel.setText("Frequency: " + String.format("%.1f Hz", value));
		});
		flangerStrength.addChangeListener(e -> {
			double value = flangerStrength.getValue() / 100.0;
			flangerStrengthLabel.setText("Strength: " + String.format("%.2f", value));
		});
		flangerFrequency.addChangeListener(e -> {
			double value = flangerFrequency.getValue();
			flangerFrequencyLabel.setText("Frequency: " + String.format("%.1f Hz", value));
		});
		adsrPeak.addChangeListener(e -> {
			double value = adsrPeak.getValue() / 100.0;
			adsrPeakLabel.setText("Peak: " + String.format("%.2f", value));
		});
		adsrTimeToPeak.addChangeListener(e -> {
			double value = adsrTimeToPeak.getValue() / 10.0;
			adsrTimeToPeakLabel.setText("Time to Peak: " + String.format("%.1fs", value));
		});
		adsrSustain.addChangeListener(e -> {
			double value = adsrSustain.getValue() / 100.0;
			adsrSustainLabel.setText("Sustain: " + String.format("%.2f", value));
		});
		adsrTimeToRelease.addChangeListener(e -> {
			double value = adsrTimeToRelease.getValue() / 10.0;
			adsrTimeToReleaseLabel.setText("Release Time: " + String.format("%.1fs", value));
		});
		glideDuration.addChangeListener(e -> {
			double value = glideDuration.getValue() / 10.0;
			glideDurationLabel.setText("Duration: " + String.format("%.1fs", value));
		});
		voicesSlider.addChangeListener(e -> {
			int value = voicesSlider.getValue();
			voicesLabel.setText("Voices: " + value);
		});
		presetClean.addActionListener(e -> loadPreset("clean"));
		presetVibrato.addActionListener(e -> loadPreset("vibrato"));
		presetChorus.addActionListener(e -> loadPreset("chorus"));
		presetOrgan.addActionListener(e -> loadPreset("organ"));
	}

	private void loadCurrentSettings() {
		if (currentSound.effects != null) {
			if (currentSound.effects.trem.isPresent()) {
				tremoloEnabled.setSelected(true);
				Sound.Effects.Tremelo trem = currentSound.effects.trem.get();
				tremoloStrength.setValue((int) (trem.stren * 100));
				tremoloFrequency.setValue((int) trem.freq);
			}
			if (currentSound.effects.flang.isPresent()) {
				flangerEnabled.setSelected(true);
				Sound.Effects.Flanger flang = currentSound.effects.flang.get();
				flangerStrength.setValue((int) (flang.stren * 100));
				flangerFrequency.setValue((int) flang.freq);
			}
			if (currentSound.effects.adsr.isPresent()) {
				adsrEnabled.setSelected(true);
				Sound.Effects.ADSR adsr = currentSound.effects.adsr.get();
				adsrPeak.setValue((int) (adsr.decayPower * 100));
				adsrTimeToPeak.setValue((int) (adsr.attackTime * 10));
				adsrSustain.setValue((int) (adsr.sustainPower * 100));
				adsrTimeToRelease.setValue((int) (adsr.releaseTime * 10));
			}
			if (currentSound.effects.glide.isPresent()) {
				glideEnabled.setSelected(true);
				Sound.Effects.Glide glide = currentSound.effects.glide.get();
				glideDuration.setValue((int) (glide.totalTimeSeconds * 10));
			}
			voicesSlider.setValue(currentSound.effects.voices);
		} else {
			voicesSlider.setValue(5);
		}
	}

	private void applySettings() {
		Optional<Sound.Effects.Tremelo> trem = Optional.empty();
		Optional<Sound.Effects.Flanger> flang = Optional.empty();
		Optional<Sound.Effects.Glide> glide = Optional.empty();
		Optional<Sound.Effects.ADSR> adsr = Optional.empty();
		if (tremoloEnabled.isSelected()) {
			double strength = tremoloStrength.getValue() / 100.0;
			double frequency = tremoloFrequency.getValue();
			trem = Optional.of(currentSound.new Effects().new Tremelo(strength, frequency));
		}
		if (flangerEnabled.isSelected()) {
			double strength = flangerStrength.getValue() / 100.0;
			double frequency = flangerFrequency.getValue();
			flang = Optional.of(currentSound.new Effects().new Flanger(strength, frequency));
		}
		if (adsrEnabled.isSelected()) {
			double peak = adsrPeak.getValue() / 100.0;
			double timeToPeak = adsrTimeToPeak.getValue() / 10.0;
			double sustain = adsrSustain.getValue() / 100.0;
			double timeToRelease = adsrTimeToRelease.getValue() / 10.0;
			adsr = Optional.of(currentSound.new Effects().new ADSR(peak, timeToPeak, sustain, timeToRelease));
		}
		if (glideEnabled.isSelected()) {
			glide = Optional.of(currentSound.new Effects().new Glide());
		}
		int voices = voicesSlider.getValue();
		currentSound.effects = currentSound.new Effects(trem, flang, glide, adsr, voices);
		JOptionPane.showMessageDialog(this, "Effects applied successfully!", "Success",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void loadPreset(String presetName) {
		switch (presetName.toLowerCase()) {
			case "clean":
				tremoloEnabled.setSelected(false);
				flangerEnabled.setSelected(false);
				adsrEnabled.setSelected(false);
				glideEnabled.setSelected(false);
				voicesSlider.setValue(5);
				break;
			case "vibrato":
				tremoloEnabled.setSelected(true);
				tremoloStrength.setValue(30);
				tremoloFrequency.setValue(6);
				flangerEnabled.setSelected(false);
				adsrEnabled.setSelected(false);
				glideEnabled.setSelected(false);
				voicesSlider.setValue(5);
				break;
			case "chorus":
				tremoloEnabled.setSelected(false);
				flangerEnabled.setSelected(true);
				flangerStrength.setValue(20);
				flangerFrequency.setValue(3);
				adsrEnabled.setSelected(false);
				glideEnabled.setSelected(true);
				glideDuration.setValue(20);
				voicesSlider.setValue(10);
				break;
			case "organ":
				tremoloEnabled.setSelected(true);
				tremoloStrength.setValue(15);
				tremoloFrequency.setValue(4);
				flangerEnabled.setSelected(false);
				adsrEnabled.setSelected(true);
				adsrPeak.setValue(90);
				adsrTimeToPeak.setValue(5);
				adsrSustain.setValue(70);
				adsrTimeToRelease.setValue(30);
				glideEnabled.setSelected(false);
				voicesSlider.setValue(8);
				break;
		}
		updateAllLabels();
	}

	private void updateAllLabels() {
		tremoloStrengthLabel.setText("Strength: " + String.format("%.2f", tremoloStrength.getValue() / 100.0));
		tremoloFrequencyLabel.setText("Frequency: " + String.format("%.1f Hz", (double) tremoloFrequency.getValue()));
		flangerStrengthLabel.setText("Strength: " + String.format("%.2f", flangerStrength.getValue() / 100.0));
		flangerFrequencyLabel.setText("Frequency: " + String.format("%.1f Hz", (double) flangerFrequency.getValue()));
		adsrPeakLabel.setText("Peak: " + String.format("%.2f", adsrPeak.getValue() / 100.0));
		adsrTimeToPeakLabel.setText("Time to Peak: " + String.format("%.1fs", adsrTimeToPeak.getValue() / 10.0));
		adsrSustainLabel.setText("Sustain: " + String.format("%.2f", adsrSustain.getValue() / 100.0));
		adsrTimeToReleaseLabel.setText("Release Time: " + String.format("%.1fs", adsrTimeToRelease.getValue() / 10.0));
		glideDurationLabel.setText("Duration: " + String.format("%.1fs", glideDuration.getValue() / 10.0));
		voicesLabel.setText("Voices: " + voicesSlider.getValue());
	}
}
