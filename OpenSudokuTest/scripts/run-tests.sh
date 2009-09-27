#!/bin/bash

adb shell am instrument -w cz.romario.opensudoku.test/android.test.InstrumentationTestRunner

# run single test:
# adb shell am instrument -w -e class cz.romario.opensudoku.game.CellNoteTest cz.romario.opensudoku/android.test.InstrumentationTestRunner

exit 0
