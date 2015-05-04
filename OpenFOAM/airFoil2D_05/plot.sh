#!/bin/bash
foamLog log


tail -n+5 postProcessing/forces/0/forceCoeffs.dat > postProcessing/forces/0/forceCoeffs1.dat
tail -n+4 postProcessing/forces/0/forceCoeffs.dat > postProcessing/forces/0/forceCoeffs2.dat
head -n -1 postProcessing/forces/0/forceCoeffs2.dat  > postProcessing/forces/0/forceCoeffs3.dat
# displays all lines from the 9th line forward.

sed -i 's/[(,)]//g' postProcessing/forces/0/forceCoeffs1.dat
sed -i 's/[(,)]//g' postProcessing/forces/0/forceCoeffs3.dat

gnuplot AllToPlot
