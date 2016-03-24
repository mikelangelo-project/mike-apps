import java.util.*;

public class Worker {
    public static void main(String[] args) throws Exception {
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

        System.out.println("Hello, I am Worker!");

        Random rnd = new Random();

        for (int i = 0; i < duration; i++) {
            Thread.sleep(1000);

            if (rnd.nextBoolean()) {
                System.out.println("stdout: " + getLine());
            } else {
                System.err.println("stderr: " + getLine());
            }
        }
    }

    public static void showUsage() {
        System.out.println("Usage: java Worker <duration-in-secconds>\n");
        System.out.println("The supervisor will start one subprocess that will");
        System.out.println("send output to stdout and stderr for the given duration.");
    }

    public static String getLine() {
        return "test=" + System.getProperty("test") + "; supervisor=" + System.getProperty("supervisor");
    }
}

