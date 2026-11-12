package zone.moddev.mc.basesteam.testing;

import java.nio.file.Path;
import net.minecraft.server.packs.PackType;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativePackResourceProofTest {
    @Test public void childResourceReaderDisablesJLineInPipedDumbTerminal() throws Exception {
        var proof = NativePackResourceProof.read();
        var child = proof.getAsJsonObject("childProcess");
        assertEquals("terminal.jline", net.minecrell.terminalconsole.TerminalConsoleAppender.JLINE_OVERRIDE_PROPERTY);
        assertEquals("false", child.get("terminalJline").isJsonNull()
                ? null : child.get("terminalJline").getAsString());
        assertEquals("dumb", child.get("term").getAsString());
        assertFalse("The child must not attach a console", child.get("consoleAttached").getAsBoolean());
        assertFalse("JLine must not construct a terminal", child.get("terminalCreated").getAsBoolean());
        assertEquals(0, proof.getAsJsonArray("warningsAndErrors").size());
        for (PackType type : PackType.values()) {
            assertTrue(type.toString(), proof.getAsJsonObject(type.name()).get("resourcesVerified").getAsInt() > 0);
        }
    }

    @Test public void terminalWarningsAndResourceErrorsRemainFatal() {
        for (String severity : new String[] {"WARN", "ERROR", "FATAL"}) {
            String message = severity.equals("WARN")
                    ? "Advanced terminal features are not available in this environment" : "Resource diagnostic";
            for (String output : new String[] {
                    "2026-11-12 00:00:00 main " + severity + " " + message,
                    "[00:00:00] [main/" + severity + "] [NativeProof]: " + message}) {
                assertThrows(output, AssertionError.class,
                        () -> NativePackResourceProof.requireCleanOutput(output, Path.of("negative-control.log")));
            }
        }
        NativePackResourceProof.requireCleanOutput("[00:00:00] [main/INFO] Native resources loaded",
                Path.of("positive-control.log"));
    }
}
