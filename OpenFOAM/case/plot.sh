#!/bin/bash
#scp -pr matej@192.168.1.41:/home/matej/openfoam/model22/log /home/matej/data/motorarcus/model22/
foamLog log
sed 1d forces/0/forceCoeffs.dat > forces/0/forceCoeffs1.dat
# sed 's/(//g'  forces/5/forces.dat > forces/5/forces1.dat  # ni treba brisat oklepajev.
# sed 's/)//g'  forces/5/forces1.dat > logs/forces2.dat
gnuplot AllToPlot
