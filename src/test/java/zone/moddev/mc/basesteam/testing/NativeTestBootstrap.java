package zone.moddev.mc.basesteam.testing;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.registries.GameData;

public final class NativeTestBootstrap {
    private NativeTestBootstrap() {}

    public static void start() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Standalone fixtures construct blocks and items outside Forge's mod registration phase.
        GameData.unfreezeData();
    }
}
