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
		// (Tremelo, Flanger, Chorus, Glide, ADSR, (Filter?))
		public class Tremelo { // change volume
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

		public class Flanger { // change frequency
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

		public class Glide { // change chord progressively over time
			public List<FreqVol> startChord, targetChord;
			public double totalTimeSeconds;
			private double elapsedTimeSeconds;
			private boolean isActive;

			public Glide(List<FreqVol> startChord, List<FreqVol> targetChord, double totalTimeSeconds) {
				this.startChord = new ArrayList<>(startChord);
				this.targetChord = new ArrayList<>(targetChord);
				this.totalTimeSeconds = totalTimeSeconds;
				this.elapsedTimeSeconds = 0.0;
				this.isActive = true;
			}

			public Glide() {
				this.startChord = new ArrayList<>();
				this.targetChord = new ArrayList<>();
				this.totalTimeSeconds = 0.0;
				this.elapsedTimeSeconds = 0.0;
				this.isActive = false;
			}

			public void startGlide(List<FreqVol> from, List<FreqVol> to, double durationSeconds) {
				this.startChord = new ArrayList<>(from);
				this.targetChord = new ArrayList<>(to);
				this.totalTimeSeconds = durationSeconds;
				this.elapsedTimeSeconds = 0.0;
				this.isActive = true;
			}

			public void stop() {
				this.isActive = false;
			}

			public boolean isActive() {
				return isActive && elapsedTimeSeconds < totalTimeSeconds;
			}

			public double getProgress() {
				if (!isActive || totalTimeSeconds <= 0)
					return 1.0;
				return Math.min(elapsedTimeSeconds / totalTimeSeconds, 1.0);
			}

			public List<FreqVol> apply(List<FreqVol> currentChord) {
				if (!isActive()) {
					return new ArrayList<>(currentChord);
				}
				elapsedTimeSeconds += 1.0 / sampleRate;
				double progress = getProgress();
				if (progress >= 1.0) {
					this.isActive = false;
					return new ArrayList<>(targetChord);
				}
				return interpolateChords(startChord, targetChord, progress);
			}

			private List<FreqVol> interpolateChords(List<FreqVol> from, List<FreqVol> to, double progress) {
				List<FreqVol> result = new ArrayList<>();
				if (from.size() == to.size()) {
					for (int i = 0; i < from.size(); i++) {
						FreqVol interpolated = from.get(i).lerp(to.get(i), progress);
						result.add(interpolated);
					}
					return result;
				}
				List<FreqVol> fromCopy = new ArrayList<>(from);
				List<FreqVol> toCopy = new ArrayList<>(to);
				while (!fromCopy.isEmpty() && !toCopy.isEmpty()) {
					FreqVol fromVoice = fromCopy.get(0);
					FreqVol closestToVoice = findClosestFrequency(fromVoice, toCopy);
					FreqVol interpolated = fromVoice.lerp(closestToVoice, progress);
					result.add(interpolated);
					fromCopy.remove(fromVoice);
					toCopy.remove(closestToVoice);
				}
				for (FreqVol remainingFrom : fromCopy) {
					double fadeOutVolume = remainingFrom.volume * (1.0 - progress);
					result.add(new FreqVol(remainingFrom.frequency, fadeOutVolume));
				}
				for (FreqVol remainingTo : toCopy) {
					double fadeInVolume = remainingTo.volume * progress;
					result.add(new FreqVol(remainingTo.frequency, fadeInVolume));
				}
				return result;
			}

			private FreqVol findClosestFrequency(FreqVol target, List<FreqVol> candidates) {
				if (candidates.isEmpty())
					return target;
				FreqVol closest = candidates.get(0);
				double minDistance = Math.abs(target.frequency - closest.frequency);
				for (FreqVol candidate : candidates) {
					double distance = Math.abs(target.frequency - candidate.frequency);
					if (distance < minDistance) {
						minDistance = distance;
						closest = candidate;
					}
				}
				return closest;
			}

			private double easeInOutCubic(double t) {
				return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
			}

			public List<FreqVol> applyWithEasing(List<FreqVol> currentChord) {
				if (!isActive()) {
					return new ArrayList<>(currentChord);
				}
				elapsedTimeSeconds += 1.0 / sampleRate;
				double linearProgress = getProgress();
				if (linearProgress >= 1.0) {
					this.isActive = false;
					return new ArrayList<>(targetChord);
				}
				double easedProgress = easeInOutCubic(linearProgress);
				return interpolateChords(startChord, targetChord, easedProgress);
			}
		}

		public class ADSR { // change volume fancily
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
					if (this.prog >= 1) {
						this.state = ADSRState.Release;
						this.prog = 0;
					}
					in.volume = sustainPower;
				} else {
					prog += releaseTime / sampleRate;
					in.volume = lerp(sustainPower, releasePower, prog);
				}
				return in;
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

		public Optional<Tremelo> trem;
		public Optional<Flanger> flang;
		public Optional<Glide> glide;
		public Optional<ADSR> adsr;
		public int voices;

		public List<FreqVol> apply(List<FreqVol> in) {
			List<FreqVol> out = new ArrayList<>();
			for (FreqVol freqvol : in) {
				if (this.trem.isPresent()) {
					out.add(this.trem.get().apply(freqvol));
				}
				if (this.flang.isPresent()) {
					out.add(this.flang.get().apply(freqvol));
				}
				if (this.adsr.isPresent()) {
					out.add(this.adsr.get().apply(freqvol));
				}
			}
			if (this.glide.isPresent()) {
				out = this.glide.get().apply(out);
			}
			return out;
		}

		public Effects(Optional<Tremelo> trem, Optional<Flanger> flang, Optional<Glide> glide, Optional<ADSR> adsr,
				int voices) {
			this.trem = trem;
			this.flang = flang;
			this.glide = glide;
			this.adsr = adsr;
			this.voices = voices;
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
	public Effects effects;

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
	}

	public float sample() {
		try {
			double time = this.currentsample / (double) sampleRate;
			double sample = 0;
			for (FreqVol i : this.freqvols) {
				for (int over = 1; over <= this.effects.voices; over++) {
					double pitch = i.frequency * Math.TAU * (over * 2);
					double vol = i.volume / (over * 2);
					sample += this.wave.apply(time * pitch) * vol;
				}
			}
			this.currentsample++;
			return (float) (sample / (this.effects.voices));
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
