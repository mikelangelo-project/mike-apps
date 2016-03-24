import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * This class takes pieces from Storm's Utils class that are relevant for this mockup.
 *
 * The logic is the same, but we have removed some parts that are not relevant. In particular
 * the logging is done using System.out instead of a separate Logger which would complicate
 * the mockup.
 *
 * Please refer to https://github.com/apache/storm/blob/762ca287a7c77988d3ecdebbd6df331950a41ef2/storm-core/src/jvm/org/apache/storm/utils/Utils.java#L2270-L2317 for the exact version of the launchProcess function from Storm
 * source code.
 */
public class Utils {

    public interface ExitCodeCallable<V> extends Callable<V> {
        V call(int exitCode);
    }

    public static Process launchProcessImpl(
            List<String> command,
            Map<String,String> cmdEnv,
            final String logPrefix,
            final ExitCodeCallable exitCodeCallback,
            File dir) 
            throws IOException {

        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String,String> procEnv = builder.environment();
        if (dir != null) {
            builder.directory(dir);
        }
        builder.redirectErrorStream(true);
        if (cmdEnv != null) {
            procEnv.putAll(cmdEnv);
        }
        final Process process = builder.start();
        if (logPrefix != null || exitCodeCallback != null) {
            Utils.asyncLoop(new Callable() {
                public Object call() {
                    if (logPrefix != null ) {
                        Utils.readAndLogStream(logPrefix,
                            process.getInputStream());
                    }
                    if (exitCodeCallback != null) {
                        try {
                            process.waitFor();
                        } catch (InterruptedException ie) {
                            System.out.println("[ERROR] interrupted");
                            exitCodeCallback.call(process.exitValue());
                        }
                    }
                    return null; // Run only once.
                }
            });
        }
        return process;
    }

    /**
     * A thread that can answer if it is sleeping in the case of simulated time.
     * This class is not useful when simulated time is not being used.
     */
    public static class SmartThread extends Thread {
        public boolean isSleeping() {
            return Time.isThreadWaiting(this);
        }
        public SmartThread(Runnable r) {
            super(r);
        }
    }

    /**
     * Convenience method used when only the function is given.
     * @param afn the code to call on each iteration
     * @return the newly created thread
     */
    public static SmartThread asyncLoop(final Callable afn) {
        return asyncLoop(afn, false, null, Thread.NORM_PRIORITY, false, true,
                null);
    }

    /**
     * Creates a thread that calls the given code repeatedly, sleeping for an
     * interval of seconds equal to the return value of the previous call.
     *
     * The given afn may be a callable that returns the number of seconds to
     * sleep, or it may be a Callable that returns another Callable that in turn
     * returns the number of seconds to sleep. In the latter case isFactory.
     *
     * @param afn the code to call on each iteration
     * @param isDaemon whether the new thread should be a daemon thread
     * @param eh code to call when afn throws an exception
     * @param priority the new thread's priority
     * @param isFactory whether afn returns a callable instead of sleep seconds
     * @param startImmediately whether to start the thread before returning
     * @param threadName a suffix to be appended to the thread name
     * @return the newly created thread
     * @see java.lang.Thread
     */
    public static SmartThread asyncLoop(final Callable afn,
            boolean isDaemon, final Thread.UncaughtExceptionHandler eh,
            int priority, final boolean isFactory, boolean startImmediately,
            String threadName) {
        SmartThread thread = new SmartThread(new Runnable() {
            public void run() {
                Object s;
                try {
                    Callable fn = isFactory ? (Callable) afn.call() : afn;
                    while ((s = fn.call()) instanceof Long) {
                        Time.sleepSecs((Long) s);
                    }
                } catch (Throwable t) {
                    if (Utils.exceptionCauseIsInstanceOf(
                            InterruptedException.class, t)) {
                        System.out.println("[INFO] Async loop interrupted!");
                        return;
                            }
                    System.out.println("[ERROR] Async loop died!");
                    throw new RuntimeException(t);
                }
            }
        });
        if (eh != null) {
            thread.setUncaughtExceptionHandler(eh);
        } else {
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    System.out.println("[ERROR] Async loop died!");
                    e.printStackTrace();
                    Utils.exitProcess(1, "Async loop died!");
                }
            });
        }
        thread.setDaemon(isDaemon);
        thread.setPriority(priority);
        if (threadName != null && !threadName.isEmpty()) {
            thread.setName(thread.getName() +"-"+ threadName);
        }
        if (startImmediately) {
            thread.start();
        }
        return thread;
    }

    /**
     * Checks if a throwable is an instance of a particular class
     * @param klass The class you're expecting
     * @param throwable The throwable you expect to be an instance of klass
     * @return true if throwable is instance of klass, false otherwise.
     */
    public static boolean exceptionCauseIsInstanceOf(Class klass, Throwable throwable) {
        Throwable t = throwable;
        while (t != null) {
            if (klass.isInstance(t)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    public static void exitProcess (int val, String msg) {
        String combinedErrorMessage = "Halting process: " + msg;
        System.out.println(combinedErrorMessage);
        (new RuntimeException(combinedErrorMessage)).printStackTrace();
        Runtime.getRuntime().exit(val);
    }

    public static void readAndLogStream(String prefix, InputStream in) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = r.readLine()) != null) {
                System.out.println("[INFO] " + prefix + ": " + line);
            }
        } catch (IOException e) {
            System.out.println("[WARN] Error while trying to log stream");
            e.printStackTrace();
        }
    }
}
