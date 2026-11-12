package zone.moddev.mc.basesteam.resources;

import cpw.mods.jarhandling.SecureJar;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileParser;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.loading.targets.ForgeDevLocator;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeRequiredDependencyContractTest {
    @Test public void processedMetadataRequiresBaseMetalsThroughActualForgeModInfo() throws Exception {
        requireBaseMetals(parse(Files.readString(Path.of("build/resources/main/META-INF/mods.toml"))));
    }

    @Test public void nativeParserDistinguishesRequiredAndOptionalDependencyFlags() throws Exception {
        assertFalse(baseMetals(parse(fixture(dependency(false)))).isMandatory());
        assertTrue(baseMetals(parse(fixture(dependency(true)))).isMandatory());
    }

    @Test public void requiredContractRejectsOptionalMissingAndDuplicateDependencies() throws Exception {
        for (String declarations : new String[] {dependency(false), "", dependency(true) + dependency(true)}) {
            ModInfo parsed = parse(fixture(declarations));
            assertThrows("Missing, optional or duplicated Base Metals cannot satisfy the contract",
                    AssertionError.class, () -> requireBaseMetals(parsed));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected the actual candidate jar path");
        requireBaseMetals(parseJar(Path.of(args[0])));
        System.out.println("NATIVE_REQUIRED_BASEMETALS_ARTIFACT_PASS");
    }

    private static void requireBaseMetals(ModInfo info) {
        IModInfo.ModVersion dependency = baseMetals(info);
        assertTrue("Base Metals is REQUIRED, not an optional installation scope", dependency.isMandatory());
        assertEquals(IModInfo.Ordering.AFTER, dependency.getOrdering());
        assertEquals(IModInfo.DependencySide.BOTH, dependency.getSide());
        assertEquals("[3.1.2.2603001,4.0.0)", dependency.getVersionRange().toString());
        assertTrue(dependency.getVersionRange().containsVersion(new DefaultArtifactVersion("3.1.2.2603001")));
        assertFalse(dependency.getVersionRange().containsVersion(new DefaultArtifactVersion("3.1.1.2603001")));
        assertFalse(dependency.getVersionRange().containsVersion(new DefaultArtifactVersion("4.0.0")));
    }

    private static IModInfo.ModVersion baseMetals(ModInfo info) {
        var dependencies = info.getDependencies().stream().filter(item -> item.getModId().equals("basemetals")).toList();
        assertEquals("Declare exactly one Base Metals dependency", 1, dependencies.size());
        return dependencies.getFirst();
    }

    private static ModInfo parse(String metadata) throws Exception {
        Path root = Path.of("build/native-required-dependency-metadata");
        Files.createDirectories(root);
        Path jar = Files.createTempDirectory(root, "forge64-").resolve("metadata.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Automatic-Module-Name", "basesteam");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
            output.putNextEntry(new JarEntry("META-INF/mods.toml"));
            output.write(metadata.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return parseJar(jar);
    }

    private static ModInfo parseJar(Path jar) {
        ModFile mod = new ModFile(SecureJar.from(jar), new ForgeDevLocator(), ModFileParser::modsTomlParser);
        assertEquals(1, mod.getModInfos().size());
        assertEquals("basesteam", mod.getModInfos().getFirst().getModId());
        return (ModInfo)mod.getModInfos().getFirst();
    }

    private static String fixture(String dependency) {
        return """
                modLoader="javafml"
                loaderVersion="[66,67)"
                license="GPL-3.0-only"
                [[mods]]
                modId="basesteam"
                version="2.2.19.2603001"
                displayName="baseSteam"
                """ + dependency;
    }

    private static String dependency(boolean required) {
        return """
                [[dependencies.basesteam]]
                modId="basemetals"
                mandatory=%s
                versionRange="[3.1.2.2603001,4.0.0)"
                ordering="AFTER"
                side="BOTH"
                """.formatted(required);
    }
}
