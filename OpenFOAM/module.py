from osv.modules import api

api.require('open-mpi')
#default = api.run("--env=WM_PROJECT_DIR=/openfoam --redirect=/log /usr/bin/simpleFoam -case /openfoam/case")
#default = api.run("--env=WM_PROJECT_DIR=/openfoam /usr/bin/simpleFoam.so -case /openfoam/case")
default = api.run("--env=WM_PROJECT_DIR=/openfoam /usr/bin/simpleFoam.so -help")
