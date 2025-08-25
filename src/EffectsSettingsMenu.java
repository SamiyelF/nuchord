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
	private JCheckBox vibratoEnabled;
	private JSlider vibratoStrength;
	private JSlider vibratoFrequency;
	private JLabel vibratoStrengthLabel;
	private JLabel vibratoFrequencyLabel;
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
	private JCheckBox chorusEnabled;
	private JSlider chorusVoices;
	private JSlider chorusDetune;
	private JLabel chorusVoicesLabel;
	private JLabel chorusDetuneLabel;
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
		vibratoEnabled = new JCheckBox("Enable Vibrato");
		vibratoStrength = new JSlider(0, 10, 3);
		vibratoFrequency = new JSlider(1, 10, 2);
		vibratoStrengthLabel = new JLabel("Strength: 0.3");
		vibratoFrequencyLabel = new JLabel("Frequency: 2.0 Hz");
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
		chorusEnabled = new JCheckBox("Enable Chorus");
		chorusVoices = new JSlider(2, 8, 4);
		chorusDetune = new JSlider(0, 10, 5);
		chorusVoicesLabel = new JLabel("Voices: 4");
		chorusDetuneLabel = new JLabel("Detune: 20 cents");
		presetClean = new JButton("Clean");
		presetVibrato = new JButton("Vibrato");
		presetChorus = new JButton("Chorus");
		presetOrgan = new JButton("Organ");
		configureSlider(tremoloStrength);
		configureSlider(tremoloFrequency);
		configureSlider(vibratoStrength);
		configureSlider(vibratoFrequency);
		configureSlider(adsrPeak);
		configureSlider(adsrTimeToPeak);
		configureSlider(adsrSustain);
		configureSlider(adsrTimeToRelease);
		configureSlider(glideDuration);
		configureSlider(chorusVoices);
		configureSlider(chorusDetune);
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
		JPanel vibratoPanel = createEffectPanel("Vibrato", new Component[] {
				vibratoEnabled,
				vibratoStrengthLabel, vibratoStrength,
				vibratoFrequencyLabel, vibratoFrequency
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
		JPanel chorusPanel = createEffectPanel("Chorus", new Component[] {
				chorusEnabled,
				chorusVoicesLabel, chorusVoices,
				chorusDetuneLabel, chorusDetune,
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
		mainPanel.add(vibratoPanel);
		mainPanel.add(adsrPanel);
		mainPanel.add(glidePanel);
		mainPanel.add(chorusPanel);
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
		vibratoStrength.addChangeListener(e -> {
			double value = vibratoStrength.getValue() / 100.0;
			vibratoStrengthLabel.setText("Strength: " + String.format("%.2f", value));
		});
		vibratoFrequency.addChangeListener(e -> {
			double value = vibratoFrequency.getValue();
			vibratoFrequencyLabel.setText("Frequency: " + String.format("%.1f Hz", value));
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
		chorusVoices.addChangeListener(e -> {
			int value = chorusVoices.getValue();
			chorusVoicesLabel.setText("Voices: " + value);
		});
		chorusDetune.addChangeListener(e -> {
			int value = chorusDetune.getValue();
			chorusDetuneLabel.setText("Detune: " + value + " cents");
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
				tremoloStrength.setValue((int) (trem.stren * 100.0));
				tremoloFrequency.setValue((int) trem.freq);
			}
			if (currentSound.effects.vib.isPresent()) {
				vibratoEnabled.setSelected(true);
				Sound.Effects.Vibrato vib = currentSound.effects.vib.get();
				vibratoStrength.setValue((int) (vib.stren * 100.0));
				vibratoFrequency.setValue((int) vib.freq);
			}
			if (currentSound.effects.adsr.isPresent()) {
				adsrEnabled.setSelected(true);
				Sound.Effects.ADSR adsr = currentSound.effects.adsr.get();
				adsrPeak.setValue((int) (adsr.decayPower * 100.0));
				adsrTimeToPeak.setValue((int) (adsr.attackTime * 10.0));
				adsrSustain.setValue((int) (adsr.sustainPower * 100.0));
				adsrTimeToRelease.setValue((int) (adsr.releaseTime * 10.0));
			}
			if (currentSound.effects.glide.isPresent()) {
				glideEnabled.setSelected(true);
				Sound.Effects.Glide glide = currentSound.effects.glide.get();
				glideDuration.setValue((int) (glide.totalTimeSeconds * 10.0));
			}
			if (currentSound.effects.chorus.isPresent()) {
				chorusEnabled.setSelected(true);
				Sound.Effects.Chorus chorus = currentSound.effects.chorus.get();
				chorusVoices.setValue(chorus.voices);
				chorusDetune.setValue((int) (chorus.detune * 10.0));
			}
		}
		updateAllLabels();
	}

	private void applySettings() {
		Optional<Sound.Effects.Tremelo> trem = Optional.empty();
		Optional<Sound.Effects.Vibrato> vib = Optional.empty();
		Optional<Sound.Effects.Glide> glide = Optional.empty();
		Optional<Sound.Effects.ADSR> adsr = Optional.empty();
		if (tremoloEnabled.isSelected()) {
			double strength = tremoloStrength.getValue() / 100.0;
			double frequency = tremoloFrequency.getValue();
			trem = Optional.of(currentSound.new Effects().new Tremelo(strength, frequency));
		}
		if (vibratoEnabled.isSelected()) {
			double strength = vibratoStrength.getValue() / 100.0;
			double frequency = vibratoFrequency.getValue();
			vib = Optional.of(currentSound.new Effects().new Vibrato(strength, frequency));
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
		Optional<Sound.Effects.Chorus> chorus = Optional.empty();
		if (chorusEnabled.isSelected()) {
			int voices = chorusVoices.getValue();
			double detune = chorusDetune.getValue() / 10.0;
			chorus = Optional.of(currentSound.new Effects().new Chorus(voices, detune));
		}
		currentSound.effects = currentSound.new Effects(trem, vib, glide, adsr, chorus);
		JOptionPane.showMessageDialog(this, "Effects applied successfully!", "Success",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void loadPreset(String presetName) {
		switch (presetName.toLowerCase()) {
			case "clean":
				tremoloEnabled.setSelected(false);
				vibratoEnabled.setSelected(false);
				adsrEnabled.setSelected(false);
				glideEnabled.setSelected(false);
				break;
			case "vibrato":
				tremoloEnabled.setSelected(true);
				tremoloStrength.setValue(30);
				tremoloFrequency.setValue(6);
				vibratoEnabled.setSelected(false);
				adsrEnabled.setSelected(false);
				glideEnabled.setSelected(false);
				break;
			case "chorus":
				tremoloEnabled.setSelected(false);
				vibratoEnabled.setSelected(true);
				vibratoStrength.setValue(20);
				vibratoFrequency.setValue(3);
				adsrEnabled.setSelected(false);
				glideEnabled.setSelected(true);
				glideDuration.setValue(20);
				chorusEnabled.setSelected(true);
				chorusVoices.setValue(6);
				chorusDetune.setValue(25);
				break;
			case "organ":
				tremoloEnabled.setSelected(true);
				tremoloStrength.setValue(15);
				tremoloFrequency.setValue(4);
				vibratoEnabled.setSelected(false);
				adsrEnabled.setSelected(true);
				adsrPeak.setValue(90);
				adsrTimeToPeak.setValue(5);
				adsrSustain.setValue(70);
				adsrTimeToRelease.setValue(30);
				glideEnabled.setSelected(false);
				break;
		}
		updateAllLabels();
	}

	private void updateAllLabels() {
		tremoloStrengthLabel.setText("Strength: " + String.format("%.2f", tremoloStrength.getValue() / 100.0));
		tremoloFrequencyLabel.setText("Frequency: " + String.format("%.1f Hz", (double) tremoloFrequency.getValue()));
		vibratoStrengthLabel.setText("Strength: " + String.format("%.2f", vibratoStrength.getValue() / 100.0));
		vibratoFrequencyLabel.setText("Frequency: " + String.format("%.1f Hz", (double) vibratoFrequency.getValue()));
		adsrPeakLabel.setText("Peak: " + String.format("%.2f", adsrPeak.getValue() / 100.0));
		adsrTimeToPeakLabel.setText("Time to Peak: " + String.format("%.1fs", adsrTimeToPeak.getValue() / 10.0));
		adsrSustainLabel.setText("Sustain: " + String.format("%.2f", adsrSustain.getValue() / 100.0));
		adsrTimeToReleaseLabel.setText("Release Time: " + String.format("%.1fs", adsrTimeToRelease.getValue() / 10.0));
		glideDurationLabel.setText("Duration: " + String.format("%.1fs", glideDuration.getValue() / 10.0));
		chorusVoicesLabel.setText("Voices: " + chorusVoices.getValue());
		chorusDetuneLabel.setText("Detune: " + chorusDetune.getValue() + " cents");
	}
}
