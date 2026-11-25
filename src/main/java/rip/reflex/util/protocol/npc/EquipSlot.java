package rip.reflex.util.protocol.npc;

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;

public enum EquipSlot {
   MAIN_HAND,
   OFF_HAND,
   BOOTS,
   LEGGINGS,
   CHESTPLATE,
   HELMET;

   public ItemSlot toWrapped() {
      switch (this) {
         case MAIN_HAND:
            return ItemSlot.MAINHAND;
         case OFF_HAND:
            return ItemSlot.OFFHAND;
         case BOOTS:
            return ItemSlot.FEET;
         case LEGGINGS:
            return ItemSlot.LEGS;
         case CHESTPLATE:
            return ItemSlot.CHEST;
         case HELMET:
            return ItemSlot.HEAD;
         default:
            return null;
      }
   }
}
