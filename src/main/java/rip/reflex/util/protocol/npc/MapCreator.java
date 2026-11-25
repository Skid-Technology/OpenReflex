package rip.reflex.util.protocol.npc;

import java.util.HashMap;
import java.util.Map;

public class MapCreator<K, V> {
   private final Class<K> keyClass;
   private final Class<V> valueClass;

   public MapCreator(Class<K> keyClass, Class<V> valueClass) {
      this.keyClass = keyClass;
      this.valueClass = valueClass;
   }

   public Map<K, V> wrapMap(Object... objects) {
      if (objects.length % 2 != 0) {
         throw new IllegalArgumentException("Length must be a multiple of 2!");
      } else {
         Map<K, V> map = new HashMap<>();
         int i = 0;

         while (i < objects.length) {
            Object key = objects[i++];
            Object value = objects[i++];
            if (!this.is(this.keyClass, key) || !this.is(this.valueClass, value)) {
               throw new IllegalArgumentException("Illegal types!");
            }

            map.put((K)key, (V)value);
         }

         return map;
      }
   }

   private boolean is(Class<?> c, Object o) {
      return c.isInstance(o);
   }
}
