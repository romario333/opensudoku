package cz.romario.opensudoku.game;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

// TODO: rename all SudokuCell* classes to Cell*

public class CellNote {
	private Set<Integer> mNotedNumbers;
	
	public CellNote() {
		mNotedNumbers = new HashSet<Integer>();
		
	}
	
	private CellNote(Set<Integer> notedNumbers) {
		mNotedNumbers = notedNumbers;
	}
	
	/**
	 * TODO: find out how big performance impact would be to create new collection here
	 * 
	 * Returns reference to the data (for performance reasons, it's called from
	 * SudokuBoardView's onDraw). Do not change
	 * anything through this reference, use CellNote's methods !
	 * 
	 * @return
	 */
	public Collection<Integer> getNotedNumbers() {
		return mNotedNumbers;		
	}
	

	public static CellNote deserialize(String note) {
		Set<Integer> notedNumbers = new HashSet<Integer>();
		if (note != null && !note.equals("")) {
			StringTokenizer tokenizer = new StringTokenizer(note, ",");
	        while (tokenizer.hasMoreTokens()) {
	        	notedNumbers.add(Integer.parseInt(tokenizer.nextToken()));
	        }
		}
		
		return new CellNote(notedNumbers);
	}
	
	public static CellNote deserialize(Integer[] notedNums) {
		Set<Integer> notedNumbers = new HashSet<Integer>();
		
		for (Integer n : notedNums) {
			notedNumbers.add(n);
		}

		return new CellNote(notedNumbers);
	}
	
	public String serialize() {
		if (mNotedNumbers.size() == 0) {
			// TODO:
			return "-";
		} else {
			StringBuffer sb = new StringBuffer();
			for (Integer num : mNotedNumbers) {
				sb.append(num).append(",");
			}
			return sb.toString();
		}
	}

	public void clear() {
		mNotedNumbers.clear();
	}
	
	public CellNote clone() {
		Set<Integer> copy = new HashSet<Integer>();
		for (Integer n : mNotedNumbers) {
			copy.add(n);
		}
		return new CellNote(copy);
	}
	
	public void toggleNumber(int number) {
		Integer n = new Integer(number);
		if (mNotedNumbers.contains(n)) {
			mNotedNumbers.remove(n);
		} else {
			mNotedNumbers.add(n);
		}
	}
	
	/**
	 * Returns true, if the note is empty.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return mNotedNumbers.size() == 0;
	}

//	public void setNoteNumber(int number, boolean isSet) {
//		if (isSet) {
//			mNoteNumbers.add(number);
//		} else {
//			mNoteNumbers.remove(new Integer(number));
//		}
//
////		mNoteNumbers[number] = isSet ? 1 : 0;
//	}
//	
//	// TODO: ugly quick fix
//	public Collection<Integer> toggleNoteNumber(int number) {
//		Set<Integer> nums = new HashSet<Integer>();
//		
//		for (Integer n : mNoteNumbers) {
//			nums.add(n);
//		}
//		
//		Integer n = new Integer(number);
//		if (nums.contains(n)) {
//			nums.remove(n);
//		} else {
//			nums.add(n);
//		}
//		return nums;
//			
//		
////		if (mNoteNumbers[number] == 1) {
////			mNoteNumbers[number] = 0;
////		} else {
////			mNoteNumbers[number] = 1;
////		}
//	}




}
