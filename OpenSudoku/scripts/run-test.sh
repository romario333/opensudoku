#!/bin/bash

adb shell am instrument -w cz.romario.opensudoku/android.test.InstrumentationTestRunner

# run single test:
# adb shell am instrument -w -e class cz.romario.opensudoku.PerformanceTest cz.romario.opensudoku/android.test.InstrumentationTestRunner

exit 0

