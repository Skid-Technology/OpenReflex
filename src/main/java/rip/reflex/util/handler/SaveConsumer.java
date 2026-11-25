package rip.reflex.util.handler;

import java.util.function.Consumer;

public interface SaveConsumer<T> extends Consumer<T> {
   void acceptSafe(T value) throws Exception;

   @Override
   default void accept(T t) {
      try {
         this.acceptSafe(t);
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }
}
