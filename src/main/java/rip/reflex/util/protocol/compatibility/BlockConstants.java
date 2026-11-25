package rip.reflex.util.protocol.compatibility;

public final class BlockConstants {
   public static final int SLAB_TYPE_TOP = 0;
   public static final int SLAB_TYPE_BOTTOM = 1;
   public static final int SLAB_TYPE_DOUBLE = 2;
   public static final int BISECTED_HALF_TOP = 0;
   public static final int BISECTED_HALF_BOTTOM = 1;
   public static final int FLUID_SOURCE_BLOCK = 0;
   public static final int REGULAR_FLUID_MIN = 1;
   public static final int REGULAR_FLUID_MAX = 7;
   public static final int FALLING_FLUID_MIN = 8;
   public static final int FALLING_FLUID_MAX = 15;

   private BlockConstants() {
   }

   public static int convertFluidLevel(int fluidLevel) {
      if (fluidLevel >= 0 && fluidLevel <= 15) {
         return fluidLevel >= 8 ? fluidLevel - 8 : fluidLevel;
      } else {
         throw new IllegalArgumentException("invalid fluid level: " + fluidLevel);
      }
   }
}
