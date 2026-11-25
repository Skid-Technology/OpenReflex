package rip.reflex.util.misc;

import java.util.Arrays;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.reflex.util.protocol.compatibility.Compatibility;

public class Items {
   private static final Random random = new Random();

   public static ItemStack getHelmet(Player p) {
      return p != null && p.getEquipment() != null ? p.getEquipment().getHelmet() : null;
   }

   public static ItemStack getChestplate(Player p) {
      return p != null && p.getEquipment() != null ? p.getEquipment().getChestplate() : null;
   }

   public static ItemStack getLeggings(Player p) {
      return p != null && p.getEquipment() != null ? p.getEquipment().getLeggings() : null;
   }

   public static ItemStack getBoots(Player p) {
      return p != null && p.getEquipment() != null ? p.getEquipment().getBoots() : null;
   }

   public static ItemStack randomWeapon() {
      switch (random.nextInt(11)) {
         case 0:
            return new ItemStack(Compatibility.getWoodenItem("WOOD_SWORD"));
         case 1:
            return new ItemStack(Compatibility.getGoldenItem("GOLD_SWORD"));
         case 2:
            return new ItemStack(Material.STONE_SWORD);
         case 3:
            return new ItemStack(Material.IRON_SWORD);
         case 4:
            return new ItemStack(Material.DIAMOND_SWORD);
         case 5:
            return new ItemStack(Compatibility.getWoodenItem("WOOD_AXE"));
         case 6:
            return new ItemStack(Compatibility.getGoldenItem("GOLD_AXE"));
         case 7:
            return new ItemStack(Material.STONE_AXE);
         case 8:
            return new ItemStack(Material.IRON_AXE);
         case 9:
            return new ItemStack(Material.DIAMOND_AXE);
         default:
            return null;
      }
   }

   public static ItemStack randomHelmet() {
      switch (random.nextInt(5)) {
         case 0:
            return new ItemStack(Material.LEATHER_HELMET);
         case 1:
            return new ItemStack(Compatibility.getGoldenItem("GOLD_HELMET"));
         case 2:
            return new ItemStack(Material.IRON_HELMET);
         case 3:
            return new ItemStack(Material.DIAMOND_HELMET);
         default:
            return null;
      }
   }

   public static ItemStack randomChestplate() {
      switch (random.nextInt(5)) {
         case 0:
            return new ItemStack(Material.LEATHER_CHESTPLATE);
         case 1:
            return new ItemStack(Compatibility.getGoldenItem("GOLD_CHESTPLATE"));
         case 2:
            return new ItemStack(Material.IRON_CHESTPLATE);
         case 3:
            return new ItemStack(Material.DIAMOND_CHESTPLATE);
         default:
            return null;
      }
   }

   public static ItemStack randomLeggings() {
      switch (random.nextInt(5)) {
         case 0:
            return new ItemStack(Material.LEATHER_LEGGINGS);
         case 1:
            return new ItemStack(Compatibility.getGoldenItem("GOLD_LEGGINGS"));
         case 2:
            return new ItemStack(Material.IRON_LEGGINGS);
         case 3:
            return new ItemStack(Material.DIAMOND_LEGGINGS);
         default:
            return null;
      }
   }

   public static ItemStack randomBoots() {
      switch (random.nextInt(5)) {
         case 0:
            return new ItemStack(Material.LEATHER_BOOTS);
         case 1:
            return new ItemStack(Compatibility.getGoldenItem("GOLD_BOOTS"));
         case 2:
            return new ItemStack(Material.IRON_BOOTS);
         case 3:
            return new ItemStack(Material.DIAMOND_BOOTS);
         default:
            return null;
      }
   }

   public static boolean isItemUsable(ItemStack item) {
      return item.getType().isEdible() || item.getType().toString().toLowerCase().contains("sword");
   }

   public static boolean allOfType(Material type, Block... blocks) {
      return blocks != null && Arrays.stream(blocks).allMatch(block -> block.getType() == type);
   }
}
