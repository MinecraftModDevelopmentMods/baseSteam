package zone.moddev.mc.basesteam.migration;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import zone.moddev.mc.basesteam.SteamAdvantage;

public final class SteamConfigMigration {
    private SteamConfigMigration() {}

    public static boolean copyLegacyCommonConfig(Path configDirectory) throws IOException {
        Path target = configDirectory.resolve(SteamAdvantage.MODID + "-common.toml");
        Path source = configDirectory.resolve(SteamLegacyMigrations.LEGACY_MODID + "-common.toml");
        if (Files.exists(target) || !Files.isRegularFile(source)) return false;
        try {
            Files.copy(source, target);
            return true;
        } catch (FileAlreadyExistsException alreadyMigrated) {
            return false;
        }
    }
}
