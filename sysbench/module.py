from osv.modules import api

default = api.run("/usr/bin/sysbench --test=cpu --cpu-max-prime=100000 run")
