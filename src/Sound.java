import javax.sound.sampled.*;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.Objects;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
}

enum SoundState {
	Running,
	Paused,
}

class Flags {
	SoundState state;

	Flags(SoundState state) {
		this.state = state;
	}
}

public class Sound {
	int sampleRate = 44800;
	int bufferSize = 1024;

	public int currentsample;
	public List<FreqVol> freqvols = new CopyOnWriteArrayList<>();
	public Function<Double, Double> wave;
	public Flags flags;

	public void setFreqvols(Chord chord, float vol) {
		this.freqvols.clear();
		for (Chord.Note note : chord.notes()) {
			this.freqvols.add(new FreqVol(note.frequency(), vol));
		}
	}

	public void addFreqvols(Chord chord, float vol) {
		for (Chord.Note note : chord.notes()) {
			FreqVol freqvol = new FreqVol(note.frequency(), vol);
			if (!this.freqvols.contains(freqvol)) {
				this.freqvols.add(freqvol);
			}
		}
	}

	public void subFreqvols(Chord chord, float vol) {
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
			int overtonecount = 5;
			double totalvolume = 0;
			for (FreqVol i : this.freqvols) {
				for (int over = 1; over <= overtonecount; over++) {
					double pitch = i.frequency * Math.TAU * (over * 2);
					double vol = i.volume / (over * 2);
					sample += this.wave.apply(time * pitch) * vol;
					totalvolume += Math.abs(vol);
				}
			}
			sample /= totalvolume * 1.5;
			this.currentsample++;
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
