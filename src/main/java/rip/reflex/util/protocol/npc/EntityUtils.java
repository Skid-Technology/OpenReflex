package rip.reflex.util.protocol.npc;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import rip.reflex.Reflex;

public class EntityUtils {
   private static Random random = new Random(System.currentTimeMillis());

   public static int getFreeEntityID() {
      if (Reflex.getReflex().getSetting("killaura.realistic_entityids", true)) {
         World firstWorld = (World)Bukkit.getWorlds().get(0);
         Entity fake = firstWorld.spawnEntity(new Location(firstWorld, 0.0, 0.0, 0.0), EntityType.BAT);
         int id = fake.getEntityId();
         if (!fake.isDead() && Reflex.getReflex().getSetting("killaura.despawn_fakes", true)) {
            fake.remove();
         }

         return id;
      } else {
         try {
            return 10000000 + random.nextInt(10000000) + random.nextInt(500000);
         } catch (OutOfMemoryError ignored) {
            random = new Random(System.currentTimeMillis());
            return getFreeEntityID();
         }
      }
   }

   public static int getEntitySize(LivingEntity e) {
      switch (e.getType()) {
         case ENDERMITE:
         case SPIDER:
         case CAVE_SPIDER:
         case BAT:
         case RABBIT:
         case SILVERFISH:
         case SQUID:
         case CHICKEN:
         case WOLF:
         case OCELOT:
            return 0;
         case PIG:
         case COW:
         case HORSE:
         case GUARDIAN:
         case SHEEP:
         case MUSHROOM_COW:
            return 1;
         case VILLAGER:
         case PLAYER:
         case BLAZE:
         case ZOMBIE:
         case SKELETON:
         case PIG_ZOMBIE:
         case CREEPER:
         case WITCH:
         case SNOWMAN:
            return 2;
         case WITHER:
         case ENDERMAN:
         case ENDER_DRAGON:
         case IRON_GOLEM:
         case GIANT:
         case SLIME:
         case MAGMA_CUBE:
            return 3;
         default:
            return 4;
      }
   }
}
