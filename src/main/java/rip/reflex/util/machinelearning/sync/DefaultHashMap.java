package rip.reflex.util.machinelearning.sync;

import java.util.HashMap;
import java.util.function.Supplier;

public class DefaultHashMap<K, V> extends HashMap<K, V> {
   private final Supplier<V> defSupplier;

   @Override
   public V get(Object key) {
      V obj = super.get(key);
      if (obj != null) {
         return obj;
      } else {
         V v;
         this.put((K)key, v = this.defSupplier.get());
         return v;
      }
   }

   public DefaultHashMap(Supplier<V> defSupplier) {
      this.defSupplier = defSupplier;
   }
}
