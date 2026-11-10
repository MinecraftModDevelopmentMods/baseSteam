package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;

public class NativeDamageTypeContractTest {
    @BeforeClass public static void bootstrap() { NativeTestBootstrap.start(); }

    @Test public void nativeDamageTypesRetainLegacyMessagesScalingAndExhaustion() throws Exception {
        for (String id : new String[] { "steam_machine", "musket" }) {
            Path path = Path.of("src/main/resources/data/basesteam/damage_type", id + ".json");
            try (Reader reader = Files.newBufferedReader(path)) {
                DamageType type = DamageType.DIRECT_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .result().orElseThrow(() -> new AssertionError("Invalid native damage type " + id));
                assertEquals(id, type.msgId());
                assertEquals(DamageScaling.NEVER, type.scaling());
                assertEquals(0.1F, type.exhaustion(), 0.0F);
            }
        }
    }
}
