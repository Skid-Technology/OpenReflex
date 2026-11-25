package rip.reflex.api;

import java.io.Serializable;
import rip.reflex.Reflex;

public class ReflexPlayerData implements Serializable {
   private static final long serialVersionUID = 2013242925633046363L;
   private final Object tpd;

   public Object tpd(String s, int i) {
      if (i < 0) {
         throw new NullPointerException();
      } else {
         return Reflex.getApiWrapper()._i(s) == -1546460115 ? this.tpd : null;
      }
   }

   public ReflexPlayerData(Object tpd) {
      this.tpd = tpd;
   }
}
