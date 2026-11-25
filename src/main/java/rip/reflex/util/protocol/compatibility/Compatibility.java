package rip.reflex.util.protocol.compatibility;

import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;

public final class Compatibility {
   private static final CompatibilityProvider provider_1_8 = new CompatibilityProvider_1_8();
   private static final CompatibilityProvider provider_1_11 = new CompatibilityProvider_1_11();
   private static final CompatibilityProvider provider_1_13 = new CompatibilityProvider_1_13();

   private Compatibility() {
   }

   public static Material getWoodenItem(String materialName) {
      if (VersionUtils.getServerVersion().hasMinimum(MCVersion.V1_13_X)) {
         materialName = Strings.replace(materialName, "WOOD", "WOODEN");
      }

      return Material.valueOf(materialName);
   }

   public static Material getGoldenItem(String materialName) {
      if (VersionUtils.getServerVersion().hasMinimum(MCVersion.V1_13_X)) {
         materialName = Strings.replace(materialName, "GOLD", "GOLDEN");
      }

      return Material.valueOf(materialName);
   }

   public static boolean isAny(ItemStack is, String... items) {
      String s = is.getType().toString().toLowerCase();

      for (String item : items) {
         if (s.contains(item)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isEntityOfType(Entity e, String type) {
      return e.getType().toString().toLowerCase().replace("_", "").trim().contains(type);
   }

   public static boolean isEntityOfAnyOfType(Entity e, String... types) {
      String t = e.getType().toString().toLowerCase().replace("_", "").trim();
      return Arrays.stream(types).filter(t::contains).findFirst().orElse(null) != null;
   }

   public static boolean isAny(Material m, String... items) {
      String s = m.toString().toLowerCase();

      for (String item : items) {
         if (s.contains(item)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isNearby(Player p, int range, String... blocks) {
      for (String block : blocks) {
         Location l = LocationUtils.getBlockLocation(p);

         for (int i = 0; i <= range; i++) {
            Block[] b = new Block[]{
               l.clone().add((double)i, (double)i, (double)i).getBlock(),
               l.clone().add((double)(-i), (double)i, (double)i).getBlock(),
               l.clone().add((double)i, (double)i, (double)(-i)).getBlock(),
               l.clone().add((double)(-i), (double)i, (double)(-i)).getBlock(),
               l.clone().add((double)i, (double)(-i), (double)i).getBlock(),
               l.clone().add((double)(-i), (double)(-i), (double)i).getBlock(),
               l.clone().add((double)i, (double)(-i), (double)(-i)).getBlock(),
               l.clone().add((double)(-i), (double)(-i), (double)(-i)).getBlock()
            };

            for (Block b0 : b) {
               if (b0.getType().toString().toLowerCase().contains(block)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static double[] getBlockHeight(Block block) {
      double[] legacy = getHeightOfLegacyBlock(block);
      if (legacy != null) {
         return legacy;
      } else {
         switch (block.getType()) {
            case ACACIA_FENCE:
            case BIRCH_FENCE:
            case DARK_OAK_FENCE:
            case JUNGLE_FENCE:
            case NETHER_BRICK_FENCE:
            case OAK_FENCE:
            case SPRUCE_FENCE:
            case COBBLESTONE_WALL:
            case MOSSY_COBBLESTONE_WALL:
               return Compatibility.BlockHeights.HEIGHT_1_5;
            case GRASS_PATH:
            case FARMLAND:
            case CACTUS:
               return Compatibility.BlockHeights.HEIGHT_0_9375;
            case SOUL_SAND:
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
               return Compatibility.BlockHeights.HEIGHT_0_875;
            case END_PORTAL_FRAME:
            case CHORUS_PLANT:
               return Compatibility.BlockHeights.HEIGHT_0_8125;
            case ENCHANTING_TABLE:
            case COCOA_BEANS:
            case CREEPER_WALL_HEAD:
            case DRAGON_WALL_HEAD:
            case PLAYER_WALL_HEAD:
            case ZOMBIE_WALL_HEAD:
               return Compatibility.BlockHeights.HEIGHT_0_75;
            case BLACK_BED:
            case BLUE_BED:
            case BROWN_BED:
            case CYAN_BED:
            case GRAY_BED:
            case GREEN_BED:
            case LIGHT_BLUE_BED:
            case LIGHT_GRAY_BED:
            case LIME_BED:
            case MAGENTA_BED:
            case ORANGE_BED:
            case PINK_BED:
            case PURPLE_BED:
            case RED_BED:
            case WHITE_BED:
            case YELLOW_BED:
               return Compatibility.BlockHeights.HEIGHT_0_5625;
            case CAKE:
            case CREEPER_HEAD:
            case DRAGON_HEAD:
            case PLAYER_HEAD:
            case ZOMBIE_HEAD:
               return Compatibility.BlockHeights.HEIGHT_0_5;
            case DAYLIGHT_DETECTOR:
            case FLOWER_POT:
               return Compatibility.BlockHeights.HEIGHT_0_375;
            case REPEATER:
            case COMPARATOR:
            case SNOW:
               return Compatibility.BlockHeights.HEIGHT_0_125;
            case LILY_PAD:
               return Compatibility.BlockHeights.HEIGHT_0_09375;
            case BLACK_CARPET:
            case BLUE_CARPET:
            case BROWN_CARPET:
            case CYAN_CARPET:
            case GRAY_CARPET:
            case GREEN_CARPET:
            case LIGHT_BLUE_CARPET:
            case LIGHT_GRAY_CARPET:
            case LIME_CARPET:
            case MAGENTA_CARPET:
            case ORANGE_CARPET:
            case PINK_CARPET:
            case PURPLE_CARPET:
            case RED_CARPET:
            case WHITE_CARPET:
            case YELLOW_CARPET:
               return Compatibility.BlockHeights.HEIGHT_0_0625;
            case SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
               return Compatibility.BlockHeights.SHULKER_BOX_HEIGHTS;
            case HOPPER:
               return Compatibility.BlockHeights.HOPPER_HEIGHTS;
            case END_ROD:
               return Compatibility.BlockHeights.END_ROD_HEIGHTS;
            case ACACIA_SLAB:
            case BIRCH_SLAB:
            case BRICK_SLAB:
            case COBBLESTONE_SLAB:
            case DARK_OAK_SLAB:
            case DARK_PRISMARINE_SLAB:
            case SANDSTONE_SLAB:
            case JUNGLE_SLAB:
            case NETHER_BRICK_SLAB:
            case OAK_SLAB:
            case PETRIFIED_OAK_SLAB:
            case PRISMARINE_BRICK_SLAB:
            case PRISMARINE_SLAB:
            case PURPUR_SLAB:
            case QUARTZ_SLAB:
            case RED_SANDSTONE_SLAB:
            case SPRUCE_SLAB:
            case STONE_BRICK_SLAB:
            case STONE_SLAB:
               return Compatibility.BlockHeights.SLAB_HEIGHTS;
            case ACACIA_STAIRS:
            case BIRCH_STAIRS:
            case BRICK_STAIRS:
            case COBBLESTONE_STAIRS:
            case DARK_OAK_STAIRS:
            case DARK_PRISMARINE_STAIRS:
            case JUNGLE_STAIRS:
            case NETHER_BRICK_STAIRS:
            case OAK_STAIRS:
            case PRISMARINE_BRICK_STAIRS:
            case PRISMARINE_STAIRS:
            case PURPUR_STAIRS:
            case QUARTZ_STAIRS:
            case RED_SANDSTONE_STAIRS:
            case SANDSTONE_STAIRS:
            case SPRUCE_STAIRS:
            case STONE_BRICK_STAIRS:
               return Compatibility.BlockHeights.STAIRS_HEIGHTS;
            case CAULDRON:
               return Compatibility.BlockHeights.CAULDRON_HEIGHTS;
            case ANVIL:
               return Compatibility.BlockHeights.ANVIL_HEIGHTS;
            case ACACIA_TRAPDOOR:
            case BIRCH_TRAPDOOR:
            case DARK_OAK_TRAPDOOR:
            case IRON_TRAPDOOR:
            case JUNGLE_TRAPDOOR:
            case OAK_TRAPDOOR:
            case SPRUCE_TRAPDOOR:
               return Compatibility.BlockHeights.TRAPDOOR_HEIGHTS;
            case BREWING_STAND:
               return Compatibility.BlockHeights.BREWING_StAND_HEIGHTS;
            default:
               return Compatibility.BlockHeights.HEIGHT_1_0;
         }
      }
   }

   private static double[] getHeightOfLegacyBlock(Block block) {
      String type = block.getType().name();
      switch (type) {
         case "BED":
            return Compatibility.BlockHeights.HEIGHT_0_5625;
         case "WATER_LILY":
            return Compatibility.BlockHeights.HEIGHT_0_09375;
         case "CARPET":
            return Compatibility.BlockHeights.HEIGHT_0_0625;
         case "TRAP_DOOR":
         case "IRON_TRAPDOOR":
            return Compatibility.BlockHeights.TRAPDOOR_HEIGHTS;
         default:
            return null;
      }
   }

   public static CompatibilityProvider getProvider_1_8() {
      return provider_1_8;
   }

   public static CompatibilityProvider getProvider_1_11() {
      return provider_1_11;
   }

   public static CompatibilityProvider getProvider_1_13() {
      return provider_1_13;
   }

   public static class BlockHeights {
      public static final double[] HEIGHT_1_5 = new double[]{1.5};
      public static final double[] HEIGHT_1_0 = new double[]{1.0};
      public static final double[] HEIGHT_0_9375 = new double[]{0.9375};
      public static final double[] HEIGHT_0_875 = new double[]{0.875};
      public static final double[] HEIGHT_0_8125 = new double[]{0.8125};
      public static final double[] HEIGHT_0_75 = new double[]{0.75};
      public static final double[] HEIGHT_0_625 = new double[]{0.625};
      public static final double[] HEIGHT_0_5625 = new double[]{0.5625};
      public static final double[] HEIGHT_0_5 = new double[]{0.5};
      public static final double[] HEIGHT_0_375 = new double[]{0.375};
      public static final double[] HEIGHT_0_3125 = new double[]{0.3125};
      public static final double[] HEIGHT_0_25 = new double[]{0.25};
      public static final double[] HEIGHT_0_1875 = new double[]{0.1875};
      public static final double[] HEIGHT_0_125 = new double[]{0.125};
      public static final double[] HEIGHT_0_09375 = new double[]{0.09375};
      public static final double[] HEIGHT_0_0625 = new double[]{0.0625};
      public static final double[] SHULKER_BOX_HEIGHTS = new double[]{1.5, 1.0};
      public static final double[] HOPPER_HEIGHTS = new double[]{1.0, 0.625};
      public static final double[] END_ROD_HEIGHTS = new double[]{1.0, 0.625};
      public static final double[] SLAB_HEIGHTS = new double[]{1.0, 0.5};
      public static final double[] STAIRS_HEIGHTS = new double[]{1.0, 0.5};
      public static final double[] CAULDRON_HEIGHTS = new double[]{1.0, 0.3125};
      public static final double[] ANVIL_HEIGHTS = new double[]{1.0, 0.25};
      public static final double[] TRAPDOOR_HEIGHTS = new double[]{1.0, 0.1875};
      public static final double[] BREWING_StAND_HEIGHTS = new double[]{0.875, 0.125};
   }

   public static class EntityID {
      public static final int PLAYER = 92;
   }
}
