import java.util.ArrayList;

public class Chord {
	public static class Note {
		public enum Letter {
			A, B, C, D, E, F, G
		}

		double letterasnumber(Letter in) {
			double scale = Math.pow(2, 8);
			switch (in) {
				default:
				case A:
					return 7040.00f / scale;
				case B:
					return 7902.13f / scale;
				case C:
					return 4186.00f / scale;
				case D:
					return 4698.63f / scale;
				case E:
					return 5274.00f / scale;
				case F:
					return 5587.65f / scale;
				case G:
					return 6271.93f / scale;
			}
		}

		public enum Accidental {
			Sharp, Natural
		}

		public Letter let;
		public Accidental acc;
		public int oct;

		public Note(Letter letter, Accidental accidental, int octave) {
			this.let = letter;
			this.acc = accidental;
			this.oct = octave;
		}

		public double frequency() {
			double base = letterasnumber(this.let) * Math.pow(2, this.oct);
			double acc = 0;
			if (this.acc == Accidental.Sharp) {
				acc = 1;
			}
			acc = Math.pow(2, acc / 12);
			return base * acc;
		}

		public int semitone_value() {
			int basesemi = 0;
			switch (this.let) {
				case Letter.C:
					basesemi = 0;
					break;
				case Letter.D:
					basesemi = 2;
					break;
				case Letter.E:
					basesemi = 4;
					break;
				case Letter.F:
					basesemi = 5;
					break;
				case Letter.G:
					basesemi = 7;
					break;
				case Letter.A:
					basesemi = 9;
					break;
				case Letter.B:
					basesemi = 11;
					break;
			}
			int accsemi = 0;
			if (this.acc == Accidental.Sharp) {
				accsemi = 1;
			}
			return (basesemi + accsemi) % 12;
		}

		public Note offset_semitones(int deltasemis) {
			int currentsemis = this.semitone_value();
			int totalsemis = currentsemis + deltasemis;
			int targetsemis, octaveoffset;
			if (totalsemis >= 0) {
				targetsemis = totalsemis % 12;
				octaveoffset = totalsemis / 12;
			} else {
				int abstotal = Math.abs(totalsemis);
				int octdown = (abstotal + 11) / 12;
				int semup = (12 - (abstotal % 12)) % 12;
				targetsemis = semup;
				octaveoffset = -octdown;
			}
			Letter letter = Letter.A;
			Accidental accidental = Accidental.Natural;
			switch (targetsemis) {
				case 0:
					letter = Letter.C;
					accidental = Accidental.Natural;
					break;
				case 1:
					letter = Letter.C;
					accidental = Accidental.Sharp;
					break;
				case 2:
					letter = Letter.D;
					accidental = Accidental.Natural;
					break;
				case 3:
					letter = Letter.D;
					accidental = Accidental.Sharp;
					break;
				case 4:
					letter = Letter.E;
					accidental = Accidental.Natural;
					break;
				case 5:
					letter = Letter.F;
					accidental = Accidental.Natural;
					break;
				case 6:
					letter = Letter.F;
					accidental = Accidental.Sharp;
					break;
				case 7:
					letter = Letter.G;
					accidental = Accidental.Natural;
					break;
				case 8:
					letter = Letter.G;
					accidental = Accidental.Sharp;
					break;
				case 9:
					letter = Letter.A;
					accidental = Accidental.Natural;
					break;
				case 10:
					letter = Letter.A;
					accidental = Accidental.Sharp;
					break;
				case 11:
					letter = Letter.B;
					accidental = Accidental.Natural;
					break;
			}
			return new Note(letter, accidental, this.oct + octaveoffset);
		}
	}

	public enum Variant {
		Tonic, // (,)',',',
		Supertonic, // ,('),',',
		Mediant, // ,'(,)',',
		Subdominant, // ,',('),',
		Dominant, // ,','(,)',
		Submediant, // ,',',('),
		LeadingTone, // ,',','(,)
		None, // ,',',',
	}

	enum Modifier {
		None, // o
		MajMin, // ⇑ (Maj OR Min)
		Seven, // ⇗
		MajMinSeven, // ⇒ (Maj OR Min) Seven
		MajMinNine, // ⇘ (Maj OR Min) Nine
		SusFour, // ⇓
		SusTwoMajSix, // ⇙ SusTwo OR MajSix
		Dim, // ⇐
		Aug, // ⇖
	};

	public Note key;
	public Variant var;
	public Modifier mod;
	public boolean maj;

	public Chord(Note p_key, Variant p_var, Modifier p_mod, boolean p_maj) {
		this.key = p_key;
		this.var = p_var;
		this.mod = p_mod;
		this.maj = p_maj;
	}

	public ArrayList<Note> notes() {
		int rootoffset = 0;
		switch (this.var) {
			case Tonic:
				rootoffset = 0;
				break;
			case Supertonic:
				rootoffset = 2;
				break;
			case Mediant:
				rootoffset = 4;
				break;
			case Subdominant:
				rootoffset = 5;
				break;
			case Dominant:
				rootoffset = 7;
				break;
			case Submediant:
				rootoffset = 9;
				break;
			case LeadingTone:
				rootoffset = 11;
				break;
			case None:
				return new ArrayList<Note>();
		}
		Note root = this.key.offset_semitones(rootoffset);
		ArrayList<Note> noteslist = new ArrayList<Note>();
		noteslist.add(root);
		switch (this.mod) {
			case None:
				if (this.maj) {
					noteslist.add(root.offset_semitones(3));
					noteslist.add(root.offset_semitones(7));
				} else {
					noteslist.add(root.offset_semitones(4));
					noteslist.add(root.offset_semitones(7));
				}
				break;
			case MajMin:
				if (this.maj) {
					noteslist.add(root.offset_semitones(4));
					noteslist.add(root.offset_semitones(7));
				} else {
					noteslist.add(root.offset_semitones(3));
					noteslist.add(root.offset_semitones(7));
				}
				break;
			case Seven:
				noteslist.add(root.offset_semitones(4));
				noteslist.add(root.offset_semitones(7));
				noteslist.add(root.offset_semitones(10));
				break;
			case MajMinSeven:
				if (this.maj) {
					noteslist.add(root.offset_semitones(4));
					noteslist.add(root.offset_semitones(7));
					noteslist.add(root.offset_semitones(11));
				} else {
					noteslist.add(root.offset_semitones(4));
					noteslist.add(root.offset_semitones(7));
					noteslist.add(root.offset_semitones(10));
				}
				break;
			case MajMinNine:
				if (this.maj) {
					noteslist.add(root.offset_semitones(4));
					noteslist.add(root.offset_semitones(7));
					noteslist.add(root.offset_semitones(11));
					noteslist.add(root.offset_semitones(14));
				} else {
					noteslist.add(root.offset_semitones(3));
					noteslist.add(root.offset_semitones(7));
					noteslist.add(root.offset_semitones(10));
					noteslist.add(root.offset_semitones(14));
				}
				break;
			case SusFour:
				noteslist.add(root.offset_semitones(5));
				noteslist.add(root.offset_semitones(7));
				break;
			case SusTwoMajSix:
				if (this.maj) {
					noteslist.add(root.offset_semitones(4));
					noteslist.add(root.offset_semitones(7));
					noteslist.add(root.offset_semitones(9));
				} else {
					noteslist.add(root.offset_semitones(2));
					noteslist.add(root.offset_semitones(7));
				}
				break;
			case Dim:
				noteslist.add(root.offset_semitones(3));
				noteslist.add(root.offset_semitones(6));
				break;
			case Aug:
				noteslist.add(root.offset_semitones(4));
				noteslist.add(root.offset_semitones(8));
				break;
		}
		return noteslist;
	}
}
