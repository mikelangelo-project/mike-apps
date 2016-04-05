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

## Benchmarking with test cases

We will assume that all code repositories are inside `$DEVDIR` directory.
E.g. there are subdirectories:
```bash
osv
osv_proxy
mike-apps
openfoam-cases
```

### Install osv_proxy

osv_proxy.py is just a tiny wrapper for OpenMPI, used to start OSv VM with OpenMPI orted.so instead of only orted (as opposed to Linux).
Code and install instructions are at https://gitlab.xlab.si/mikelangelo/osv_proxy.

At the moment, branch `jc-temp1` should be used.

Also note that osv_proxy has to be installed on each compute host 
(e.g. machines with IP1, IP2 in command `mpirun -H IP1,IP2 ...`)

### Add openfoam-cases to OSv modules

Datafiles for test casses are located in a separate repository at https://gitlab.xlab.si/mikelangelo/openfoam-cases.
Clone code, and add path to it to config.json.

```
git clone https://gitlab.xlab.si/mikelangelo/openfoam-cases.git

cat osv/config.json
...
    "repositories": [
        "${OSV_BASE}/apps",
        "${OSV_BASE}/modules"
        , "${OSV_BASE}/../mike-apps"
        , "${OSV_BASE}/../openfoam-cases"
    ]
...
```

### Compile OpenFOAM and OpenMPI

To perform benchmark comparison between host and OSv VM, we build OpenFOAM and OpenMPI with same compiler flags.
To make this easier, are both OpenFOAM and OpenMPI from https://gitlab.xlab.si/mikelangelo/mike-apps modified to 
build regular executable for Linux and shared object file "executable" for OSv at the same time. 

#### OSv image

To build OSv image:

```bash
cd $DEVDIR/osv/
scripts/build mode=release image=OpenFOAM,OpenMPI,mik3d_1h-4cpu,mik3d_1h-1cpu,openmpi-hello,cli -j8
# could also include also openmpi-hello,sysbench,osu-micro-benchmarks
```

The OSv image is now prepared. We still have to install the same OpenMPI on the host.
```bash
(cd $DEVDIR/mike-apps/OpenMPI/ompi-release/build-osv/ && make install)
```

#### OpenMPI for host

Running `make install` on host will install compiled binaries to `$HOME/openmpi-bin/` directory.
This makes using binaries a bit painful. 
The reason for not installing into usual place (`/usr/local`) is to avoid requiring root access on host.
OpenMPI should (during compiling for OSv, while its GET executes) add to your .bashrc:

```bash
cat $HOME/.bashrc
...
# MAGIC LINE mike-apps OpenMPI included : OSv mike-apps openmpi bin/libs 
export PATH=/home/xlab/openmpi-bin/bin:$PATH
export LD_LIBRARY_PATH=/home/xlab/openmpi-bin/lib:$LD_LIBRARY_PATH
```

#### OpenFOAM for host

We don't need to install OpenFOAM.
Instead, environment variables are set and/or binaries are called with absolute path.

### Minimal openmpi-hello on OSv

Just to make sure all (osv_proxy, UNIX user is able to start libvirt VM, network bridges etc) is setup correctly.

```bash
mpirun -n 3 -H 127.0.0.2 --launch-agent "$DEVDIR/osv_proxy/lin_proxy.sh --cpus 1 --memory 2024 --image $DEVDIR/osv/build/release/usr.img --net-ip 192.168.122.200/24 --net-gw 192.168.122.1 " -x TERM=xterm -x MPI_BUFFER_SIZE=20100100 -x WM_PROJECT_DIR=/fff/openfoam -wd / /usr/lib/mpi_hello.so 192.168.122.1 8080 0
...
SIZE = 3 RANK = 0
SIZE = 3 RANK = 2
rank from=1 me=2 to=0
SIZE = 3 RANK = 1Recv 1 -> 2
rank from=0 me=1 to=2
Recv 0 -> 1
rank from=2 me=0 to=1
Send 0 -> 1
(rank= 0) 1000 0000 0000 0000 0000 0000 0000 0000 
Recv 2 -> 0
(rank= 1) 1000 0000 0000 0000 0000 0000 0000 0000 
Send 1 -> 2
(rank= 2) 1000 1001 0000 0000 0000 0000 0000 0000 
...
```

The --net-ip, --net-gw might be needed, or might be not.

### OpenFOAM mik3d_1h-4cpu case on OSv

```bash
mpirun -n 4 -H 127.0.0.2 -wd / --launch-agent "$DEVDIR/osv_proxy/lin_proxy.sh  -m2048 -c4 --bridge=virbr0 --image $DEVDIR/osv/build/release/usr.img --gdb" -x TERM=xterm -x MPI_BUFFER_SIZE=20100100 -x WM_PROJECT_DIR=/openfoam /usr/bin/simpleFoam.so -case /openfoam/mik3d_1h-4cpu -parallel
```

### OpenFOAM mik3d_1h-4cpu case on host

(I hope cmd below works)

```bash
$HOME/openmpi-bin/bin/mpirun --report-bindings -n 4 -H 127.0.0.2 --launch-agent $HOME/openmpi-bin/bin/orted -x LD_LIBRARY_PATH=$DEVDIR/mike-apps/OpenFOAM/OpenFOAM-2.4.0/platforms/linux64GccDPOpt/bin:$LD_LIBRARY_PATH -wd $DEVDIR/foam-cases/etc/ $DEVDIR/mike-apps/OpenFOAM/OpenFOAM-2.4.0/bin/foamExec $DEVDIR/mike-apps/OpenFOAM/OpenFOAM-2.4.0/platforms/linux64GccDPOpt/bin/simpleFoam -case $DEVDIR/foam-cases/mik3d_1h-4cpu -parallel
```
