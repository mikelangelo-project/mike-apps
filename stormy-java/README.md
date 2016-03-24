# Java Subprocess Mockup

This application is a demo for a simple java application using ProcessBuilder to start
a child process. The sole intention of this application is to have a test case that
we can use during the implementation of the OSv Java API (and accompanying JNI) that
will interact with OSv to launch several instances of the JVM.

The reason for this mockup is that Apache Storm (and others) use subprocesses to invoke
worker processes doing the work. With the modified logic we are going to investigate if
it makes sense to use OSv for Storm applications (spouts and bolts) that would run more
efficiently in a virtual environment.

## Running

First, compile the entire code using attached Makefile:
```bash
$ make
```

After that, start the Supervisor that will in turn start a single Worker. The single
command argument is the duration, i.e. the number of seconds the Worker will run.

```bash
$ java -cp stormy-java Supervisor 5
Hello, I am the supervisor!
[INFO] Worker: : Hello, I am Worker!
[INFO] Worker: : stderr: test=worker; supervisor=null
[INFO] Worker: : stdout: test=worker; supervisor=null
[INFO] Worker: : stdout: test=worker; supervisor=null
[INFO] Worker: : stdout: test=worker; supervisor=null
[INFO] Worker: : stderr: test=worker; supervisor=null
```

As is seen in the previous command log, the worker outputs text to stdout and
stderr randomly. Each time, the worker outputs the value of two system props:
"test" and "supervisor". The "test" property is hardcoded within the Supervisor
to test whether the property is properly passed to the child. The "supervisor"
property can be passed on the command line when invoking the Supervisor, e.g.:

```bash
$ java -cp stormy-java -Dsupervisor=john Supervisor 5
Hello, I am the supervisor!
[INFO] Worker: : Hello, I am Worker!
[INFO] Worker: : stdout: test=worker; supervisor=john
[INFO] Worker: : stderr: test=worker; supervisor=john
[INFO] Worker: : stderr: test=worker; supervisor=john
```

## Running in OSv

Use standard OSv way to build and OSv VM with this example application. First, make sure
mike-apps repository is part of OSv's configuration file (refer to top-level mike-apps
README for details).

Then execute the following commend:

```bash
$ ./scripts/build image=stormy-java
```

from OSV_HOME (root of the OSv source tree).

To run the VM with stormy-java app, simply use run.py script, for example:

```bash
$ ./scripts/run.py -m512m
```

This should faile with the following error report until we have support for running
child workers using OSv.

```
OSv v0.24-78-g69bd35e
eth0: 192.168.122.15
Hello, I am the supervisor!
vfork() stubbed
java.io.IOException: Cannot run program "java": error=2, No such file or directory
        at java.lang.ProcessBuilder.start(ProcessBuilder.java:1041)
        at Utils.launchProcessImpl(Utils.java:46)
        at Supervisor.runWorker(Supervisor.java:66)
        at Supervisor.main(Supervisor.java:33)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:606)
        at io.osv.ContextIsolator.runMain(ContextIsolator.java:233)
        at io.osv.ContextIsolator.access$400(ContextIsolator.java:32)
        at io.osv.ContextIsolator$3.run(ContextIsolator.java:118)
Caused by: java.io.IOException: error=2, No such file or directory
        at java.lang.UNIXProcess.forkAndExec(Native Method)
        at java.lang.UNIXProcess.<init>(UNIXProcess.java:135)
        at java.lang.ProcessImpl.start(ProcessImpl.java:130)
        at java.lang.ProcessBuilder.start(ProcessBuilder.java:1022)
        ... 10 more
```
