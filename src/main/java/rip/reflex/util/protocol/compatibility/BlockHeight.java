package rip.reflex.util.protocol.compatibility;

import java.util.Arrays;
import org.bukkit.block.Block;

public class BlockHeight {
   public static final int REGULAR_BLOCK = 0;
   public static final int OPEN_SHULKER_BOX = 0;
   public static final int CLOSED_SHULKER_BOX = 1;
   public static final int HOPPER_BLOCK = 0;
   public static final int INSIDE_OF_HOPPER = 1;
   public static final int END_ROD_ON_GROUND = 0;
   public static final int END_ROD_SIDEWAYS = 1;
   public static final int UPPER_SLAB = 0;
   public static final int BOTTOM_SLAB = 1;
   public static final int TOP_PART_OF_STAIRS = 0;
   public static final int BOTTOM_PART_OF_STAIRS = 1;
   public static final int CAULDRON_BLOCK = 0;
   public static final int INSIDE_OF_CAULDRON = 1;
   public static final int ANVIL_BLOCK = 0;
   public static final int BOTTOM_SLAB_OF_ANVIL = 1;
   public static final int TOP_TRAPDOOR = 0;
   public static final int BOTTOM_TRAPDOOR = 1;
   public static final int TOP_OF_BREWING_STAND = 0;
   public static final int BASE_OF_BREWING_STAND = 1;
   private final Block block;
   private final String type;
   private final double[] possibleHeights;

   public BlockHeight(Block block) {
      this.block = block;
      this.type = block.getType().name();
      this.possibleHeights = Compatibility.getBlockHeight(block);
   }

   public int getVariations() {
      return this.possibleHeights.length;
   }

   public double[] allPossible() {
      return this.possibleHeights;
   }

   public double get(int index) {
      if (index >= 0 && index < this.possibleHeights.length) {
         return this.possibleHeights[index];
      } else {
         throw new ArrayIndexOutOfBoundsException(
            String.format(
               "error getting block height: block %s has only %s possible heights, but height index %s was requested",
               this.type,
               this.possibleHeights.length,
               index
            )
         );
      }
   }

   public int getHeightIndex(double height) {
      for (int i = 0; i < this.possibleHeights.length; i++) {
         if (this.possibleHeights[i] == height) {
            return i;
         }
      }

      return -1;
   }

   public boolean hasHeight(double height) {
      return Arrays.stream(this.possibleHeights).anyMatch(h -> h == height);
   }

   public double regular() {
      if (this.possibleHeights.length != 1) {
         throw new IllegalArgumentException("error geting block height: " + this.type + " is not a regular block");
      } else {
         return this.possibleHeights[0];
      }
   }

   public double openShulkerBox() {
      this.checkType("SHULKER_BOX", false);
      return this.possibleHeights[0];
   }

   public double closedShulkerBox() {
      this.checkType("SHULKER_BOX", false);
      return this.possibleHeights[1];
   }

   public double hopperBlock() {
      this.checkType("HOPPER", true);
      return this.possibleHeights[0];
   }

   public double insideOfHopper() {
      this.checkType("HOPPER", true);
      return this.possibleHeights[1];
   }

   public double endRodOnGround() {
      this.checkType("END_ROD", true);
      return this.possibleHeights[0];
   }

   public double endRodSideways() {
      this.checkType("END_ROD", true);
      return this.possibleHeights[1];
   }

   public double upperSlab() {
      this.checkType("SLAB", false);
      return this.possibleHeights[0];
   }

   public double bottomSlab() {
      this.checkType("SLAB", false);
      return this.possibleHeights[1];
   }

   public double topPartOfStairs() {
      this.checkType("STAIRS", false);
      return this.possibleHeights[0];
   }

   public double bottomPartOfStairs() {
      this.checkType("STAIRS", false);
      return this.possibleHeights[1];
   }

   public double cauldronBlock() {
      this.checkType("CAULDRON", true);
      return this.possibleHeights[0];
   }

   public double insideOfCauldron() {
      this.checkType("CAULDRON", true);
      return this.possibleHeights[1];
   }

   public double anvilBlock() {
      this.checkType("ANVIL", true);
      return this.possibleHeights[0];
   }

   public double bottomSlabOfAnvil() {
      this.checkType("ANVIL", true);
      return this.possibleHeights[1];
   }

   public double topTrapdoor() {
      this.checkType("TRAPDOOR", false);
      return this.possibleHeights[0];
   }

   public double bottomTrapdoor() {
      this.checkType("TRAPDOOR", false);
      return this.possibleHeights[1];
   }

   public double topOfBrewingStand() {
      this.checkType("BREWING_STAND", true);
      return this.possibleHeights[0];
   }

   public double baseOfBrewingStand() {
      this.checkType("BREWING_STAND", true);
      return this.possibleHeights[1];
   }

   private void checkType(String required, boolean strict) {
      if (strict && !this.type.equals(required) || !this.type.contains(required)) {
         throw new IllegalArgumentException(String.format("error getting block height: block type mismatch: expected %s, got %s", required, this.type));
      }
   }

   public Block getBlock() {
      return this.block;
   }
}
