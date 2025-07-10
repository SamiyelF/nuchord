import javax.sound.sampled.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

class FreqVol {
	public double frequency, volume;

	public FreqVol(double freq, double vol) {
		this.frequency = freq;
		this.volume = vol;
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

	public Sound(FreqVol[] freqvols, Function<Double, Double> wave) {
		this.currentsample = 0;
		this.freqvols = new ArrayList<>(Arrays.asList(freqvols));
		this.wave = wave;
	}

	public void setFreqvols(Chord chord) {
		this.freqvols.clear();
		for (Chord.Note note : chord.notes()) {
			System.out.println("F: " + note.frequency());
			this.freqvols.add(new FreqVol(note.frequency(), 1));
		}
		System.out.println(freqvols.size());
	}

	public Sound(ArrayList<FreqVol> freqvols, Function<Double, Double> wave) {
		this.currentsample = 0;
		this.freqvols = freqvols;
		this.wave = wave;
	}

	public short sample() {
		double time = this.currentsample / (double) sampleRate;
		double bigsample = 0;
		for (FreqVol i : this.freqvols) {
			bigsample += this.wave.apply(time * i.frequency * Math.TAU) * i.volume;
		}
		bigsample /= freqvols.size();
		bigsample *= Short.MAX_VALUE / 4;
		short sample = (short) bigsample;
		return sample;
	}

	public void play(Flags flags) {
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
		while (flags.state == SoundState.Running) {
			short sample = sample();
			buffer[0] = (byte) ((sample >> 8) & 0xFF);
			buffer[1] = (byte) (sample & 0xFF);
			line.write(buffer, 0, 2);
			this.currentsample++;
		}
	}
}
