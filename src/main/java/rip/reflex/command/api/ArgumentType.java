package rip.reflex.command.api;

import java.util.function.Predicate;
import org.bukkit.Bukkit;
import rip.reflex.check.base.Check;
import rip.reflex.util.db.DatabaseType;
import rip.reflex.util.misc.Misc;

public enum ArgumentType {
   INT(Misc::isNumber),
   BOOLEAN(Misc::isBoolean),
   PLAYER(s -> Bukkit.getPlayer(s) != null),
   PLAYER_OFFLINE(s -> Bukkit.getOfflinePlayer(s) != null),
   UUID(Misc::isUUID),
   REFLEX_CHECK(s -> Check.correctName(s) != null),
   REFLEX_DBTYPE(s -> DatabaseType.byString(s) != DatabaseType.UNKNOWN),
   WEB_PORT(s -> INT.getPredicate().test(s) && Integer.valueOf(s) > 0 && Integer.valueOf(s) < 65536),
   STRING(s -> true);

   private final Predicate<String> predicate;

   private ArgumentType(Predicate<String> predicate) {
      this.predicate = predicate;
   }

   public Predicate<String> getPredicate() {
      return this.predicate;
   }
}
