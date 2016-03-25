= About

Compile OSU Micro-Benchmarks. See http://mvapich.cse.ohio-state.edu/benchmarks/.

= Usage

```
run.py ... -e '/libexec/osu-micro-benchmarks/mpi/pt2pt/osu_latency -h'

mpirun -n 2 -H 127.0.0.2 -wd / --launch-agent "$HOME/devel/mikelangelo/osv_proxy/lin_proxy.sh --image ..." /libexec/osu-micro-benchmarks/mpi/pt2pt/osu_latency
```