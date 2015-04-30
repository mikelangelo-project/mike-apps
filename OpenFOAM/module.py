from osv.modules import api

default = api.run("--env=WM_PROJECT_DIR=/openfoam /usr/bin/simpleFoam -case /openfoam/case")
