package rip.reflex.util.protocol.npc;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import java.util.Map;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;

public class ImpWrappedDataWatcher extends WrappedDataWatcher {
   private final MapCreator<Integer, Object> creator = new MapCreator<>(Integer.class, Object.class);

   public void setEntireMap(Object... objects) {
      this.setAll(this.creator.wrapMap(objects));
   }

   public ImpWrappedDataWatcher setEntire(Object... objects) {
      for (int i = 0; i < objects.length; i++) {
         this.set(i, objects[i]);
      }

      return this;
   }

   public void setAll(Map<Integer, Object> map) {
      map.forEach(this::set);
   }

   public ImpWrappedDataWatcher set(Integer index, Object value) {
      try {
         if (this.hasIndex(index)) {
            this.getWatchableObject(index).setValue(value);
         } else if (VersionUtils.getServerVersion().getId() >= MCVersion.V1_9_X.getId()) {
            this.setObject(index, Registry.get(value.getClass()), value);
         } else {
            this.setObject(index, value);
         }
      } catch (Exception ex) {
         ex.printStackTrace();
         System.out.println("Index: " + index + ", Object: " + value + ", Type: " + value.getClass().getSimpleName());
      }

      return this;
   }
}
