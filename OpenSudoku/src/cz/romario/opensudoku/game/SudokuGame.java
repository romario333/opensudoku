package cz.romario.opensudoku.game;

import java.util.Date;


import android.os.Parcel;
import android.os.Parcelable;


public class SudokuGame implements Parcelable {
	
	public static final int GAME_STATE_NOT_STARTED = 0;
	public static final int GAME_STATE_PLAYING = 1;
	public static final int GAME_STATE_COMPLETED = 2;
	
	private long id;
	private String name = "";
	private Date created;
	private int state;
	private long time;
	private Date lastPlayed;
	
	private SudokuCellCollection cells;

	public SudokuGame() {
		
	}
	
	public void setName(String name) {
		this.name = name;
		
	}

	public String getName() {
		return name;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getCreated() {
		return created;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return state;
	}

	/**
	 * Sets time of play in seconds.
	 * @param time
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * Gets time of play in seconds.
	 * @return
	 */
	public long getTime() {
		return time;
	}

	public void setLastPlayed(Date lastPlayed) {
		this.lastPlayed = lastPlayed;
	}

	public Date getLastPlayed() {
		return lastPlayed;
	}

	public void setCells(SudokuCellCollection cells) {
		this.cells = cells;
		this.validate();
	}
	
	public SudokuCellCollection getCells() {
		return cells;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}
	
	public void validate() {
		cells.validate();
	}
	
	/**
	 * Returns true, if puzzle is solved. In order to know actual state, you have to
	 * call validate first. 
	 * @return
	 */
	public boolean isCompleted() {
		return cells.isCompleted();
	}
	
	// constructor for Parcelable
	private SudokuGame(Parcel in) {
		id = in.readLong();
		name = in.readString();
		created = new Date(in.readLong());
		state = in.readInt();
		time = in.readLong();
		lastPlayed = new Date(in.readLong());
		
		cells = (SudokuCellCollection) in.readParcelable(SudokuCellCollection.class.getClassLoader());
	}
	
	public static final Parcelable.Creator<SudokuGame> CREATOR = new Parcelable.Creator<SudokuGame>() {
		public SudokuGame createFromParcel(Parcel in) {
		    return new SudokuGame(in);
		}
		
		public SudokuGame[] newArray(int size) {
		    return new SudokuGame[size];
		}
	};
	
	@Override
	public int describeContents() {
		// TODO nevim k cemu je
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeLong(created.getTime());
		dest.writeInt(state);
		dest.writeLong(time);
		dest.writeLong(lastPlayed.getTime());
		// TODO: zas ty flags co nevim k cemu jsou
		dest.writeParcelable(cells, flags);
	}

}
