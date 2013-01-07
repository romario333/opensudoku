package cz.romario.opensudoku.game;

import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Scott Liu
 * Date: 12/25/12
 * Time: 9:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class CellHint {
    private Cell mCell;
    private boolean[] mIsValueAvailable = new boolean[CellCollection.SUDOKU_SIZE];

    public CellHint(Cell cell) {
        mCell = cell;
    }

    public void CalcValueAvailable() {
        for (int value = 1; value <= CellCollection.SUDOKU_SIZE; value++) {
            mIsValueAvailable[value - 1] = isAvailableInternal(value);
        }
    }

    public boolean isAvailable(int value) {
        if (0 == value)
            return true;
        return mIsValueAvailable[value - 1];
    }

    private boolean isAvailableInternal(int value) {
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }

        if (mCell.getValue() > 0)
            return false;

        if (mCell.getRow().contains(value))
            return false;

        if (mCell.getColumn().contains(value))
            return false;

        if (mCell.getSector().contains(value))
            return false;

        return true;
    }
}
