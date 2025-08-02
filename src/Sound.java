import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.Objects;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Optional;

class FreqVol {
	public double frequency, volume;

	public FreqVol(double freq, double vol) {
		this.frequency = freq;
		this.volume = vol;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FreqVol))
			return false;
		FreqVol other = (FreqVol) o;
		return Double.compare(frequency, other.frequency) == 0 &&
				Double.compare(volume, other.volume) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(frequency, volume);
	}

	public FreqVol lerp(FreqVol to, double time) {
		FreqVol out = new FreqVol(
				this.frequency + (time * (to.frequency - this.frequency)),
				this.volume + (time * (to.volume - this.volume)));
		return out;
	}
}

public class Sound {
	public double lerp(double a, double b, double t) {
		return a + (t * (b - a));
	}

	public class Effects {
		public class Tremelo {
			public double stren, freq;
			private double phase = 0;

			public FreqVol apply(FreqVol in) {
				double time = currentsample / (double) sampleRate;
				double lfo = Math.sin(2 * Math.PI * freq * time + phase);
				double volumeModulation = 1.0 + (stren * lfo * 0.5);
				FreqVol out = new FreqVol(in.frequency, in.volume * volumeModulation);
				return out;
			}

			public Tremelo(double strength, double frequency) {
				this.stren = strength;
				this.freq = frequency;
			}
		}

		public class Flanger {
			public double stren, freq;
			private double phase = 0;

			public FreqVol apply(FreqVol in) {
				double time = currentsample / (double) sampleRate;
				double lfo = Math.sin(2 * Math.PI * freq * time + phase);
				double frequencyModulation = 1.0 + (stren * lfo * 0.1);
				FreqVol out = new FreqVol(in.frequency * frequencyModulation, in.volume);
				return out;
			}

			public Flanger(double strength, double frequency) {
				this.stren = strength;
				this.freq = frequency;
			}
		}

		public class Glide {
			public List<FreqVol> previous;
			public double totalTimeSeconds;
			private double elapsedTimeSeconds;

			public Glide(List<FreqVol> previous, double totalTimeSeconds) {
				this.previous = new ArrayList<>(previous);
				this.totalTimeSeconds = totalTimeSeconds;
				this.elapsedTimeSeconds = 0.0;
			}

			public Glide() {
				this.previous = new ArrayList<>();
				this.totalTimeSeconds = 0.0;
				this.elapsedTimeSeconds = 0.0;
			}

			public List<FreqVol> apply(List<FreqVol> target) {
				List<FreqVol> out = target; // will be midpoint between previous and target
				previous = target;
				elapsedTimeSeconds += 1.0 / sampleRate;
				return out;
			}
		}

		public class ADSR {
			enum ADSRState {
				Attack,
				Decay,
				Sustain,
				Release,
			}

			public double attackPower, decayPower, sustainPower, releasePower;
			public double attackTime, decayTime, releaseTime;
			public ADSRState state;
			public double prog;

			public FreqVol apply(FreqVol in) {
				if (this.state == ADSRState.Attack) {
					prog += attackTime / sampleRate;
					if (this.prog >= 1) {
						this.state = ADSRState.Decay;
						this.prog = 0;
					}
					in.volume = lerp(attackPower, decayPower, prog);
				} else if (this.state == ADSRState.Decay) {
					prog += decayTime / sampleRate;
					if (this.prog >= 1) {
						this.state = ADSRState.Sustain;
						this.prog = 0;
					}
					in.volume = lerp(decayPower, sustainPower, prog);
				} else if (this.state == ADSRState.Sustain) {
					in.volume = sustainPower;
				} else {
					prog += releaseTime / sampleRate;
					in.volume = lerp(sustainPower, releasePower, prog);
				}
				return in;
			}

			public void release() {
				this.prog = 0;
				this.state = ADSRState.Release;
			}

			public ADSR(double peak, double timetopeak, double sustain, double timetorelease) {
				this.attackPower = 0;
				this.decayPower = peak;
				this.sustainPower = sustain;
				this.releasePower = 0;
				this.attackTime = timetopeak / 2;
				this.decayTime = timetopeak / 2;
				this.releaseTime = timetorelease;
				this.state = ADSRState.Attack;
				this.prog = 0;
			}
		}

		public class Chorus {
			public int voices;
			public double detune;
			public double depth;
			public double rate;
			private double phase = 0;

			public Chorus(int voices, double detune, double depth, double rate) {
				this.voices = voices;
				this.detune = detune;
				this.depth = depth;
				this.rate = rate;
			}

			public Chorus(int voices, double detune) {
				this(voices, detune, 0.5, 1.0);
			}

			public List<FreqVol> apply(FreqVol in) {
				List<FreqVol> out = new ArrayList<>();
				out.add(new FreqVol(in.frequency, in.volume / voices));
				for (int i = 1; i < voices; i++) {
					double voiceDetune = detune * (i - (voices - 1) / 2.0) / (voices - 1);
					double time = currentsample / (double) sampleRate;
					double lfo = Math.sin(2 * Math.PI * rate * time + phase + (i * Math.PI / voices));
					double modulatedDetune = voiceDetune + (depth * lfo * 10.0);
					double frequencyRatio = Math.pow(2.0, modulatedDetune / 1200.0);
					double chorusFreq = in.frequency * frequencyRatio;
					double chorusVol = in.volume / voices * (0.9 + 0.1 * Math.cos(lfo));
					out.add(new FreqVol(chorusFreq, chorusVol));
				}
				return out;
			}
		}

		public Optional<Tremelo> trem;
		public Optional<Flanger> flang;
		public Optional<Glide> glide;
		public Optional<ADSR> adsr;
		public Optional<Chorus> chorus;

		public Effects(Optional<Tremelo> trem, Optional<Flanger> flang, Optional<Glide> glide,
				Optional<ADSR> adsr, Optional<Chorus> chorus) {
			this.trem = trem;
			this.flang = flang;
			this.glide = glide;
			this.adsr = adsr;
			this.chorus = chorus;
		}

		public List<FreqVol> apply(List<FreqVol> in) {
			List<FreqVol> out = new ArrayList<>();
			for (FreqVol freqvol : in) {
				List<FreqVol> processedVoices = new ArrayList<>();
				FreqVol currentVoice = new FreqVol(freqvol.frequency, freqvol.volume);
				if (this.trem.isPresent()) {
					currentVoice = this.trem.get().apply(currentVoice);
				}
				if (this.flang.isPresent()) {
					currentVoice = this.flang.get().apply(currentVoice);
				}
				if (this.adsr.isPresent()) {
					currentVoice = this.adsr.get().apply(currentVoice);
				}
				if (this.chorus.isPresent()) {
					processedVoices.addAll(this.chorus.get().apply(currentVoice));
				} else {
					processedVoices.add(currentVoice);
				}
				out.addAll(processedVoices);
			}
			if (this.glide.isPresent()) {
				out = this.glide.get().apply(out);
			}
			if (out.isEmpty()) {
				out = new ArrayList<>(in);
			}
			return out;
		}

		public Effects() {
			this.trem = Optional.empty();
			this.flang = Optional.empty();
			this.glide = Optional.empty();
			this.adsr = Optional.empty();
			this.chorus = Optional.empty();
		}
	}

	enum SoundState {
		Running,
		Paused,
	}

	public class Flags {
		SoundState state;

		Flags(SoundState state) {
			this.state = state;
		}
	}

	int sampleRate = 44800;
	int bufferSize = 1024;
	public int currentsample;
	public List<FreqVol> freqvols = new CopyOnWriteArrayList<>();
	public Function<Double, Double> wave;
	public Flags flags;
	public Effects effects = new Effects();

	public void setFreqvols(Chord chord, double vol) {
		this.freqvols.clear();
		for (Chord.Note note : chord.notes()) {
			this.freqvols.add(new FreqVol(note.frequency(), vol));
		}
	}

	public void addFreqvols(Chord chord, double vol) {
		for (Chord.Note note : chord.notes()) {
			FreqVol freqvol = new FreqVol(note.frequency(), vol);
			if (!this.freqvols.contains(freqvol)) {
				this.freqvols.add(freqvol);
			}
		}
	}

	public void subFreqvols(Chord chord, double vol) {
		for (Chord.Note note : chord.notes()) {
			this.freqvols.remove(new FreqVol(note.frequency(), vol));
		}
	}

	public Sound(ArrayList<FreqVol> freqvols, Function<Double, Double> wave) {
		this.currentsample = 0;
		this.freqvols = new CopyOnWriteArrayList<FreqVol>(freqvols);
		this.wave = wave;
		this.flags = new Flags(SoundState.Running);
		this.effects = new Effects();
	}

	public float sample() {
		try {
			double time = this.currentsample / (double) sampleRate;
			double sample = 0;
			List<FreqVol> effected = this.effects.apply(this.freqvols);
			for (FreqVol i : effected) {
				for (int over = 1; over <= 5; over++) {
					double pitch = i.frequency * Math.TAU * (over * 2);
					double vol = i.volume / (over * 2);
					sample += this.wave.apply(time * pitch) * vol;
				}
			}
			this.currentsample++;
			int totalVoices = effected.size();
			if (totalVoices > 0) {
				sample = sample / Math.sqrt(totalVoices);
			}
			if (sample < -1.0) {
				sample = -1.0;
			} else if (sample > 1.0) {
				sample = 1.0;
			}
			return (float) sample;
		} catch (NullPointerException e) {
			System.err.println(e);
		}
		return 0.0f;
	}

	public void play() {
		AudioFormat format = new AudioFormat(sampleRate, 32, 1, true, false);
		SourceDataLine line;
		try {
			line = AudioSystem.getSourceDataLine(format);
			line.open(format, bufferSize);
		} catch (LineUnavailableException exception) {
			System.err.println(exception);
			return;
		}
		line.start();
		while (this.flags.state == SoundState.Running) {
			float floatSample = sample();
			byte[] buffer = new byte[4];
			int intSample = (int) (floatSample * Integer.MAX_VALUE);
			buffer[0] = (byte) ((intSample >> 8 * 0) & 0xFF);
			buffer[1] = (byte) ((intSample >> 8 * 1) & 0xFF);
			buffer[2] = (byte) ((intSample >> 8 * 2) & 0xFF);
			buffer[3] = (byte) ((intSample >> 8 * 3) & 0xFF);
			line.write(buffer, 0, 4);
		}
	}
}
