package rip.reflex.api;

import java.util.NoSuchElementException;

public enum Cheat {
   KillAura,
   Criticals,
   AntiKnockback,
   BlockActions,
   BadPackets,
   ClientControl,
   InvActions,
   Speed,
   VehicleMove,
   ElytraMove;

   public static String correctName(String cheat) {
      String s = cheat.toLowerCase().trim();

      for (Cheat c : values()) {
         if (c.name().equalsIgnoreCase(s)) {
            return c.name();
         }
      }

      throw new NoSuchElementException("No such cheat with name '" + cheat + "'");
   }
}
