import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pieces of Time class taken from Storm that are relevant for this mockup.
 *
 * This class is only provided so that the other logic can be used unmodified.
 */
public class Time {
    private static AtomicBoolean simulating = new AtomicBoolean(false);
    private static final Object sleepTimesLock = new Object();
    private static volatile Map<Thread, AtomicLong> threadSleepTimes;

    private static AtomicLong simulatedCurrTimeMs; //should this be a thread local that's allowed to keep advancing?

    public static void sleepUntil(long targetTimeMs) throws InterruptedException {
        if(simulating.get()) {
            try {
                synchronized(sleepTimesLock) {
                    if (threadSleepTimes == null) {
                        //LOG.debug("{} is still sleeping after simulated time disabled.", Thread.currentThread(), new RuntimeException("STACK TRACE"));
                        throw new InterruptedException();
                    }
                    threadSleepTimes.put(Thread.currentThread(), new AtomicLong(targetTimeMs));
                }
                while(simulatedCurrTimeMs.get() < targetTimeMs) {
                    synchronized(sleepTimesLock) {
                        if (threadSleepTimes == null) {
                            //LOG.debug("{} is still sleeping after simulated time disabled.", Thread.currentThread(), new RuntimeException("STACK TRACE"));
                            throw new InterruptedException();
                        }
                    }
                    Thread.sleep(10);
                }
            } finally {
                synchronized(sleepTimesLock) {
                    if (simulating.get() && threadSleepTimes != null) {
                        threadSleepTimes.remove(Thread.currentThread());
                    }
                }
            }
        } else {
            long sleepTime = targetTimeMs-currentTimeMillis();
            if(sleepTime>0) 
                Thread.sleep(sleepTime);
        }
    }

    public static void sleep(long ms) throws InterruptedException {
        sleepUntil(currentTimeMillis()+ms);
    }

    public static void sleepSecs (long secs) throws InterruptedException {
        if (secs > 0) {
            sleep(secs * 1000);
        }
    }

    public static long currentTimeMillis() {
        if(simulating.get()) {
            return simulatedCurrTimeMs.get();
        } else {
            return System.currentTimeMillis();
        }
    }

    public static boolean isThreadWaiting(Thread t) {
        if(!simulating.get()) throw new IllegalStateException("Must be in simulation mode");
        AtomicLong time;
        synchronized(sleepTimesLock) {
            time = threadSleepTimes.get(t);
        }
        return !t.isAlive() || time!=null && currentTimeMillis() < time.longValue();
    }  
}
