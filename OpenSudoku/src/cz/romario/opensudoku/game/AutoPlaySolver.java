/*
 * Copyright (C) 2009 Ralfoide at gmail
 *
 * This file is part of OpenSudoku.
 *
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cz.romario.opensudoku.game;

import java.util.Set;

import android.util.SparseIntArray;

public class AutoPlaySolver {

    private Cell mCell;
    private int mValue;

    AutoPlaySolver() {
    }

    public Cell getSolvedCell() {
        return mCell;
    }

    public int getSolvedValue() {
        return mValue;
    }

    public boolean solveNext(CellCollection cells) {

        Cell candidateCell = null;
        int newValue = 0;

        // First we make sure there is not a single cell with no notes
        // in it. This command must run after FillInNotesCommand, which sets
        // notes for all empty cells. If such a cell fails to have a note
        // that means there's conflict somewhere in the grid so we better
        // abort here.
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                Cell cell = cells.getCell(r, c);

                // Ignore cells with values
                if (cell.getValue() > 0) continue;

                CellNote note = cell.getNote();

                Set<Integer> numbers = note.getNotedNumbers();
                if (numbers.isEmpty()) {
                    // Argh. Abort!
                    return false;
                }
            }
        }

        // Now look for the easy case of a note that has only 1 value
        simple_loop:
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                Cell cell = cells.getCell(r, c);

                // Ignore cells with values
                if (cell.getValue() > 0) continue;

                CellNote note = cell.getNote();

                Set<Integer> numbers = note.getNotedNumbers();
                if (numbers.size() == 1) {
                    candidateCell = cell;
                    //noinspection UnnecessaryUnboxing
                    newValue = numbers.iterator().next().intValue();
                    break simple_loop;
                }
            }
        }

        if (candidateCell == null) {
            // Second look at the more advanced case where all the
            // notes have more than 1 number but in a given sector
            // there is one value that is unique, in which case we
            // select it.
            sector_loop:
            for (int s = 0; s < CellCollection.SUDOKU_SIZE; s++) {
                CellGroup sector = cells.getSector(s);

                SparseIntArray count = new SparseIntArray();
                Cell[] found = new Cell[CellCollection.SUDOKU_SIZE + 1];

                for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                    Cell cell = sector.getCell(c);

                    // Ignore cells with values
                    if (cell.getValue() > 0) continue;

                    CellNote note = cell.getNote();

                    for (Integer ii : note.getNotedNumbers()) {
                        //noinspection UnnecessaryUnboxing
                        int i = ii.intValue();
                        if (i >= 1 && i <= CellCollection.SUDOKU_SIZE) {
                            count.put(i, count.get(i) + 1);
                            found[i] = cell;
                        }
                    }
                }

                // Find the first value with is present only once
                int key = count.indexOfValue(1);
                if (key > 0) {
                    int c = count.keyAt(key);
                    if (c >= 1 && c <= CellCollection.SUDOKU_SIZE) {
                        candidateCell = found[c];
                        newValue = c;
                        break sector_loop;
                    }
                }
            }
        }

        if (candidateCell != null) {
            mCell = candidateCell;
            mValue = newValue;
        }
        return mCell != null;
    }
}
