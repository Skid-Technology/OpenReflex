package rip.reflex.util.protocol.packetaccess;

import com.comphenix.protocol.events.PacketContainer;
import java.util.Arrays;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class PacketPlayInUseEntity extends PacketAccessor {
   public PacketPlayInUseEntity(PacketContainer packet) {
      super(packet);
   }

   public Entity getEntity(World world) {
      Object[] entities = world.getEntities().toArray();
      int entityId = this.readInt(0);
      return (Entity)Arrays.stream(entities).filter(entity -> ((Entity)entity).getEntityId() == entityId).findFirst().orElse(null);
   }

   public PacketPlayInUseEntity setEntity(Entity entity) {
      this.writeInt(0, entity.getEntityId());
      return this;
   }
}
