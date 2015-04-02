# OpenFOAM

## System requirements

Prior to building an OSv image, you have to install some mandatory system packages.

### Ubuntu

```bash
sudo apt-get install build-essential flex bison cmake zlib1g-dev libopenmpi-dev openmpi-bin qt4-dev-tools libqt4-dev libqt4-opengl-dev freeglut3-dev libqtwebkit-dev gnuplot libreadline-dev libncurses-dev libxt-dev
sudo apt-get install libscotch-dev libcgal-dev
```

### Other distributions

Refer to [OpenFOAM's manual](http://www.openfoam.org/download/source.php) for instructions on how to install required packages.

## Building the OpenFOAM OSv

The simplest way to build an OSv image is to invoke the following command from OSv home directory.

```bash
cd $OSV_HOME
make image=OpenFOAM
```
