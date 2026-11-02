package zone.moddev.mc.basesteam.init;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.util.Constants.NBT;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.blocks.DrillBitTileEntity;
import zone.moddev.mc.basesteam.items.MusketItem;
import zone.moddev.mc.basesteam.machines.*;

import static org.junit.Assert.*;

public class SteamIdentityMigrationTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void allTwelveOriginalTileIdsLoadAndResaveCanonically() throws Exception {
        Map<String, Class<? extends TileEntity>> tiles = new LinkedHashMap<>();
        tiles.put("steam_boiler_coal", CoalBoilerTileEntity.class);
        tiles.put("steam_boiler_electric", ElectricBoilerTileEntity.class);
        tiles.put("steam_boiler_geothermal", GeothermalBoilerTileEntity.class);
        tiles.put("steam_tank", SteamTankTileEntity.class);
        tiles.put("steam_furnace", BlastFurnaceTileEntity.class);
        tiles.put("steam_crusher", RockCrusherTileEntity.class);
        tiles.put("steam_drill", SteamDrillTileEntity.class);
        tiles.put("steam_elevator", SteamElevatorTileEntity.class);
        tiles.put("drillbit", DrillBitTileEntity.class);
        tiles.put("steam_still", SteamStillTileEntity.class);
        tiles.put("steam_pump", SteamPumpTileEntity.class);
        tiles.put("steam_boiler_oil", OilBoilerTileEntity.class);

        Method register = Entities.class.getDeclaredMethod("registerTile", Class.class, String.class);
        register.setAccessible(true);
        Set<String> productionNames = new HashSet<>();
        ClassNode registration = bytecode(Entities.class);
        for (MethodNode method : registration.methods) {
            if (!method.name.equals("init")) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof LdcInsnNode && ((LdcInsnNode) instruction).cst instanceof String) {
                    productionNames.add((String) ((LdcInsnNode) instruction).cst);
                }
            }
        }
        assertEquals("The test matrix must cover the actual production tile names", tiles.keySet(), productionNames);

        for (Map.Entry<String, Class<? extends TileEntity>> entry : tiles.entrySet()) {
            String name = entry.getKey();
            register.invoke(null, entry.getValue(), name);
            NBTTagCompound original = new NBTTagCompound();
            original.setString("id", "steamadvantage." + name);
            original.setInteger("x", 19); original.setInteger("y", 65); original.setInteger("z", -7);
            TileEntity loaded = TileEntity.create(null, original);
            assertNotNull(name, loaded);
            assertEquals(name, entry.getValue(), loaded.getClass());
            NBTTagCompound saved = loaded.writeToNBT(new NBTTagCompound());
            assertEquals(name, "basesteam." + name, saved.getString("id"));
            assertEquals(19, saved.getInteger("x"));
            assertEquals(65, saved.getInteger("y"));
            assertEquals(-7, saved.getInteger("z"));
            assertEquals("Original fixture must retain its historical identity", "steamadvantage." + name,
                    original.getString("id"));
            assertEquals(entry.getValue(), TileEntity.create(null, saved).getClass());
        }
    }

    @Test public void renamedModHandlesOriginalNamespaceWithoutAnActiveLegacyContainer() {
        ListMultimap<String, FMLMissingMappingsEvent.MissingMapping> missing = ArrayListMultimap.create();
        FMLMissingMappingsEvent.MissingMapping block = mapping(GameRegistry.Type.BLOCK, "steamadvantage", "unknown_block");
        FMLMissingMappingsEvent.MissingMapping item = mapping(GameRegistry.Type.ITEM, "steamadvantage", "unknown_item");
        FMLMissingMappingsEvent.MissingMapping foreign = mapping(GameRegistry.Type.ITEM, "basemetals", "unknown_item");
        missing.put("steamadvantage", block); missing.put("steamadvantage", item); missing.put("basemetals", foreign);
        new SteamAdvantage().missingMappings(new FMLMissingMappingsEvent(missing));
        assertEquals(FMLMissingMappingsEvent.Action.FAIL, block.getAction());
        assertEquals(FMLMissingMappingsEvent.Action.FAIL, item.getAction());
        assertEquals(FMLMissingMappingsEvent.Action.DEFAULT, foreign.getAction());
    }

    @Test public void enchantmentKeysAndNumericStackRoundTripRemainLegacyCompatible() {
        int[] expectedIds = new int[4];
        boolean firstInitialization = Enchantments.high_explosive == null;
        if (firstInitialization) {
            int found = 0;
            for (int id = 0; id < 255 && found < expectedIds.length; id++) {
                if (Enchantment.REGISTRY.getObjectById(id) == null) expectedIds[found++] = id;
            }
            assertEquals("Four IDs must be available in the historical range", 4, found);
        }
        Enchantments.init();
        Enchantment[] values = {Enchantments.high_explosive, Enchantments.powderless,
                Enchantments.rapid_reload, Enchantments.recoil};
        String[] names = {"high_explosive", "powderless", "rapid_reload", "recoil"};
        for (int i = 0; i < values.length; i++) {
            assertSame(values[i], Enchantment.REGISTRY.getObject(new ResourceLocation("steamadvantage", names[i])));
            assertNull("Do not register a second numeric identity", Enchantment.REGISTRY.getObject(
                    new ResourceLocation("basesteam", names[i])));
            if (firstInitialization) assertEquals("Keep the original first-free allocation order",
                    expectedIds[i], Enchantment.getEnchantmentID(values[i]));
            ItemStack stack = new ItemStack(net.minecraft.init.Items.STICK);
            stack.addEnchantment(values[i], 1);
            NBTTagCompound saved = stack.writeToNBT(new NBTTagCompound());
            NBTTagList savedEnchantments = saved.getCompoundTag("tag").getTagList("ench", NBT.TAG_COMPOUND);
            assertEquals("One actual enchantment entry must be saved", 1, savedEnchantments.tagCount());
            NBTTagCompound savedEnchantment = savedEnchantments.getCompoundTagAt(0);
            assertTrue("1.10 persists the ID as a numeric short", savedEnchantment.hasKey("id", NBT.TAG_SHORT));
            int savedId = savedEnchantment.getShort("id");
            assertEquals("Saved ID must resolve to the retained legacy registration",
                    Enchantment.getEnchantmentID(values[i]), savedId);
            assertSame(values[i], Enchantment.REGISTRY.getObjectById(savedId));
            ItemStack restored = ItemStack.loadItemStackFromNBT(saved);
            assertNotNull(restored);
            assertEquals(1, net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(values[i], restored));
            NBTTagCompound resaved = restored.writeToNBT(new NBTTagCompound());
            assertEquals("Loading and saving must retain the numeric enchantment ID", savedId,
                    resaved.getCompoundTag("tag").getTagList("ench", NBT.TAG_COMPOUND).getCompoundTagAt(0).getShort("id"));
        }
        int[] ids = Arrays.stream(values).mapToInt(Enchantment::getEnchantmentID).toArray();
        Enchantments.init();
        assertArrayEquals("Repeated initialization must not allocate aliases", ids,
                Arrays.stream(values).mapToInt(Enchantment::getEnchantmentID).toArray());
    }

    @Test public void musketRemainsHitscanAndThereAreNoProjectileRegistryAliases() throws Exception {
        boolean rayTrace = false, directDamage = false;
        for (Class<?> type : new Class<?>[]{MusketItem.class, Entities.class}) {
            for (MethodNode method : bytecode(type).methods) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    assertFalse("This 1.10 baseline has no persistent custom entities to alias",
                            call.owner.equals("net/minecraftforge/fml/common/registry/EntityRegistry"));
                    if (method.name.equals("fire")) {
                        rayTrace |= call.name.equals("rayTraceBlocksAndEntities");
                        directDamage |= call.name.equals("attackEntityFrom");
                        assertFalse(call.name.equals("spawnEntityInWorld"));
                    }
                }
            }
        }
        assertTrue(rayTrace);
        assertTrue(directDamage);
    }

    private static FMLMissingMappingsEvent.MissingMapping mapping(GameRegistry.Type type, String mod, String name) {
        return new FMLMissingMappingsEvent.MissingMapping(type, new ResourceLocation(mod, name), 30000);
    }

    private static ClassNode bytecode(Class<?> type) throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = type.getResourceAsStream("/" + Type.getInternalName(type) + ".class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        return node;
    }
}
