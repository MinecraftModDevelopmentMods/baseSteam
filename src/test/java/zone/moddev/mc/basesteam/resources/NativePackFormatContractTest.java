package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.junit.Test;
import zone.moddev.mc.basesteam.testing.NativePackResourceProof;
import static org.junit.Assert.*;

public class NativePackFormatContractTest {
    @org.junit.BeforeClass public static void bootstrap() {
        zone.moddev.mc.basesteam.testing.NativeTestBootstrap.start();
    }

    @Test public void nativeClientAndServerMetadataAcceptTheSameCombinedPack() throws Exception {
        var proof = NativePackResourceProof.read();
        assertEquals(0, proof.getAsJsonArray("warningsAndErrors").size());
        var clientProof = proof.getAsJsonObject("CLIENT_RESOURCES");
        var serverProof = proof.getAsJsonObject("SERVER_DATA");
        assertEquals("mod_resources", clientProof.get("id").getAsString());
        assertEquals("net.minecraftforge.resource.DelegatingPackResources", clientProof.get("resourcesClass").getAsString());
        assertEquals("mod:basesteam", serverProof.get("id").getAsString());
        assertEquals("net.minecraft.server.packs.PathPackResources", serverProof.get("resourcesClass").getAsString());
        assertEquals("COMPATIBLE", clientProof.get("currentCompatibility").getAsString());
        assertEquals("COMPATIBLE", serverProof.get("currentCompatibility").getAsString());
        assertTrue(clientProof.get("resourcesVerified").getAsInt() > 0);
        assertTrue(serverProof.get("resourcesVerified").getAsInt() > 0);
        var pack = JsonParser.parseString(Files.readString(Path.of("src/main/resources/pack.mcmeta")))
                .getAsJsonObject().getAsJsonObject("pack");
        assertFalse(pack.has("pack_format"));
        assertFalse(pack.has("supported_formats"));
        var client = PackMetadataSection.forPackType(PackType.CLIENT_RESOURCES).codec()
                .parse(JsonOps.INSTANCE, clientProof.get("nativeMetadata")).getOrThrow();
        var server = PackMetadataSection.forPackType(PackType.SERVER_DATA).codec()
                .parse(JsonOps.INSTANCE, serverProof.get("nativeMetadata")).getOrThrow();
        assertEquals(server, PackMetadataSection.forPackType(PackType.SERVER_DATA).codec()
                .parse(JsonOps.INSTANCE, pack).getOrThrow());
        var version = SharedConstants.getCurrentVersion();
        assertEquals(PackFormat.of(88, 0), version.packVersion(PackType.CLIENT_RESOURCES));
        assertEquals(PackFormat.of(107, 1), version.packVersion(PackType.SERVER_DATA));
        assertTrue(client.supportedFormats().isValueInRange(version.packVersion(PackType.CLIENT_RESOURCES)));
        assertTrue(server.supportedFormats().isValueInRange(version.packVersion(PackType.SERVER_DATA)));
        assertFalse(client.supportedFormats().isValueInRange(PackFormat.of(83)));
        assertFalse(server.supportedFormats().isValueInRange(PackFormat.of(102)));
        assertFalse(client.supportedFormats().isValueInRange(PackFormat.of(107, 1)));
        assertFalse(server.supportedFormats().isValueInRange(PackFormat.of(88, 0)));
    }
}
