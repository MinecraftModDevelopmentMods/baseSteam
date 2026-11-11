package zone.moddev.mc.basesteam.testing;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/** Emits exact JUnit counts for the separately compiled Eclipse test scope. */
public final class EclipseUnitLauncher {
    private EclipseUnitLauncher() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length < 2) throw new IllegalArgumentException("Receipt path and test classes required");
        Class<?>[] tests = new Class<?>[arguments.length - 1];
        for (int index = 1; index < arguments.length; index++) tests[index - 1] = Class.forName(arguments[index]);
        AtomicInteger assumptions = new AtomicInteger();
        JUnitCore core = new JUnitCore();
        core.addListener(new RunListener() {
            @Override public void testAssumptionFailure(Failure failure) { assumptions.incrementAndGet(); }
            @Override public void testFailure(Failure failure) { System.err.println(failure.getTrace()); }
        });
        var result = core.run(tests);
        JsonObject receipt = new JsonObject();
        receipt.add("classes", new com.google.gson.Gson().toJsonTree(Arrays.copyOfRange(arguments, 1, arguments.length)));
        receipt.addProperty("tests", result.getRunCount());
        receipt.addProperty("failures", result.getFailureCount());
        receipt.addProperty("ignored", result.getIgnoreCount());
        receipt.addProperty("assumptions", assumptions.get());
        receipt.addProperty("runtimeMilliseconds", result.getRunTime());
        boolean passed = result.wasSuccessful() && result.getRunCount() > 0
                && result.getIgnoreCount() == 0 && assumptions.get() == 0;
        receipt.addProperty("passed", passed);
        Files.writeString(Path.of(arguments[0]), new GsonBuilder().setPrettyPrinting().create().toJson(receipt) + "\n");
        System.out.println("ECJ_JUNIT tests=" + result.getRunCount() + " failures=" + result.getFailureCount()
                + " ignored=" + result.getIgnoreCount() + " assumptions=" + assumptions.get());
        if (!passed) System.exit(1);
    }
}
