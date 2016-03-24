import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class Supervisor {
    public static void main(String[] args) {
        // Make sure the Supervisor receives a single argument specifying the
        // duration the child worker should "process".
        if (args.length != 1) {
            Supervisor.showUsage();

            System.exit(1);
        }

        int duration = 0;
        try {
            duration = Integer.parseInt(args[0]);
        } catch (Exception e) {
            Supervisor.showUsage();

            System.exit(1);
        }

        System.out.println("Hello, I am the supervisor!");

        // Use Supervisor to run a single worker.
        Supervisor s = new Supervisor();
        s.runWorker(duration);
    }
    
    public static void showUsage() {
        System.out.println("Usage: java Supervisor <duration-in-secconds>\n");
        System.out.println("The supervisor will start one subprocess that will");
        System.out.println("send output to stdout and stderr for the given duration.");
    }

    public void runWorker(int duration) {
        String supervisorConfig = System.getProperty("supervisor");

        List<String> command = Arrays.asList(new String[] { 
            // Execute Java binary as a child process (set the classpath as well)
            "java", 
            // Set the classpath to be the same as that for the supervisor.
            "-cp", "stormy-java",
            // Use a property that was passed to the Supervisor and pass it to the child worker.
            "-Dsupervisor=" + supervisorConfig,
            // Make a new property for the worker.
            "-Dtest=worker",
            // Specify the main class, Worker, that is to be executed.
            "Worker", 
            // Provide an additional argument for the Worker.
            "" + duration });

        try {
            // This will be invoked in case of problems with the child.
            ExitCallback cb = new ExitCallback();

            // Simulate the behaviour of the Storm supervisor starting a worker
            // as a child process. Provide an exit callback that is to wait for
            // the child to complete it's operation.
            Process p = Utils.launchProcessImpl(command, null, "Worker: ", cb, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ExitCallback implements Utils.ExitCodeCallable<Integer> {
        @Override
        public Integer call() {
            return this.call(-1);
        }

        @Override
        public Integer call(int exitCode) {
            System.out.println("ExitCallback with code " + exitCode);

            return new Integer(exitCode);
        }
    }

}
