import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.Objects;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Optional;
import java.util.Random;

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

			public FreqVol apply(FreqVol in) {
				double t = (double) currentsample / (double) sampleRate;
				double lfo = Math.sin(Math.TAU * freq * t);
				double modulation = (1 - stren) + (stren * (lfo + 1) / 2);
				FreqVol out = new FreqVol(in.frequency, in.volume * modulation);
				return out;
			}

			public Tremelo(double strength, double frequency) {
				this.stren = strength;
				this.freq = frequency;
			}
		}

		public class Vibrato {
			public double stren;
			public double freq;

			public FreqVol apply(FreqVol in) {
				return in;
			}

			public Vibrato(double strength, double frequency) {
				this.stren = strength;
				this.freq = frequency;
			}
		}

		public class Glide {
			public List<FreqVol> start, stop, cur;
			public double totalTimeSeconds;
			private double elapsedTimeSeconds;

			public Glide() {
				this.start = new ArrayList<>();
				this.cur = new ArrayList<>();
				this.stop = new ArrayList<>();
				this.totalTimeSeconds = 0.0;
				this.elapsedTimeSeconds = 0.0;
			}

			public List<FreqVol> apply(List<FreqVol> target) {
				return target;
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
			public List<Double> detunes = new ArrayList<>();

			public Chorus(int voices, double detune) {
				this.voices = voices;
				this.detune = detune;
				for (int v = 0; v < this.voices; v++) {
					double min = 0.0, max = this.detune;
					double random = min + Math.random() * (max - min);
					detunes.add(random);
				}
			}

			public List<FreqVol> apply(FreqVol in) {
				List<FreqVol> out = new ArrayList<FreqVol>();
				for (int v = 0; v < this.voices; v++) {
					out.add(new FreqVol(in.frequency * detunes.get(v) * Math.pow(2, v), in.volume
							/ (Math.pow(2, v))));
				}
				out.add(in);
				return out;
			}
		}

		public Optional<Tremelo> trem;
		public Optional<Vibrato> vib;
		public Optional<Glide> glide;
		public Optional<ADSR> adsr;
		public Optional<Chorus> chorus;

		public Effects(Optional<Tremelo> trem, Optional<Vibrato> vib, Optional<Glide> glide,
				Optional<ADSR> adsr, Optional<Chorus> chorus) {
			this.trem = trem;
			this.vib = vib;
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
				if (this.vib.isPresent()) {
					currentVoice = this.vib.get().apply(currentVoice);
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
			this.vib = Optional.empty();
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
				double pitch = i.frequency * Math.TAU;
				double vol = i.volume;
				sample += this.wave.apply(time * pitch) * vol;
			}
			this.currentsample++;
			int totalVoices = effected.size();
			if (totalVoices > 0) {
				sample = sample / totalVoices;
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
