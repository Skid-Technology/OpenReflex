package rip.reflex.util.handler;

import java.util.function.Function;

public class ExceptionHandler {
   public static <T> T save(Class<T> tc, ExceptionHandler.SafeSupplier<T> sup) {
      try {
         return sup.apply();
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   public interface SafeFunction<T, R> extends Function<T, R> {
      R applySave(T value) throws Exception;

      @Override
      default R apply(T t) {
         try {
            return this.applySave(t);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
   }

   public interface SafeSupplier<T> {
      T apply() throws Exception;
   }
}
