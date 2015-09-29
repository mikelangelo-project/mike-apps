.PHONY: module
module: omp_hello

CFLAGS = -std=gnu99 -fpie -rdynamic -fopenmp

omp_hello: omp_hello.c
	$(CC) -pie -o $@ $(CFLAGS) $(LDFLAGS) omp_hello.c

clean:
	rm -f omp_hello
