import javax.sound.sampled.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.Objects;

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
	public ArrayList<FreqVol> freqvols;
	public Function<Double, Double> wave;
	public Flags flags;

	public Sound(FreqVol[] freqvols, Function<Double, Double> wave) {
		this.currentsample = 0;
		this.freqvols = new ArrayList<>(Arrays.asList(freqvols));
		this.wave = wave;
		this.flags = new Flags(SoundState.Paused);
	}

	public void setFreqvols(Chord chord, float vol) {
		ArrayList<FreqVol> newfreqvols = new ArrayList<FreqVol>();
		for (Chord.Note note : chord.notes()) {
			System.out.println("F: " + note.frequency());
			newfreqvols.add(new FreqVol(note.frequency(), vol));
		}
		if (newfreqvols.equals(this.freqvols)) {
			return;
		}
		this.freqvols.clear();
		this.freqvols = newfreqvols;
		System.out.println(freqvols.size());
	}

	public void setFreqvols(Chord chord) {
		setFreqvols(chord, 1);
	}

	public Sound(ArrayList<FreqVol> freqvols, Function<Double, Double> wave) {
		this.currentsample = 0;
		this.freqvols = freqvols;
		this.wave = wave;
	}

	public short sample() {
		double time = this.currentsample / (double) sampleRate;
		double bigsample = 0;
		int overtonecount = 4;
		for (FreqVol i : this.freqvols) {
			for (int over = 1; over < overtonecount; over++) {
				try {
					bigsample += this.wave.apply(time * i.frequency * Math.TAU * Math.pow(2, over)) * i.volume
							/ over;
				} catch (NullPointerException e) {
					System.err.println(e);
				}
			}
		}
		bigsample /= freqvols.size();
		bigsample *= Short.MAX_VALUE / 4;
		short sample = (short) bigsample;
		return sample;
	}

	public void play() {
		AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, true);
		SourceDataLine line;
		try {
			line = AudioSystem.getSourceDataLine(format);
			line.open(format, bufferSize);
		} catch (LineUnavailableException exception) {
			System.err.println(exception);
			return;
		}
		line.start();

		byte[] buffer = new byte[2];
		while (this.flags.state == SoundState.Running) {
			short sample = sample();
			buffer[0] = (byte) ((sample >> 8) & 0xFF);
			buffer[1] = (byte) (sample & 0xFF);
			line.write(buffer, 0, 2);
			this.currentsample++;
		}
	}
}
