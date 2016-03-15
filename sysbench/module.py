from osv.modules import api

#default = api.run("/usr/bin/sysbench --test=cpu --cpu-max-prime=100000 --num-threads=4 run")
default = api.run("/usr/bin/sysbench --test=memory --num-threads=4 run")
