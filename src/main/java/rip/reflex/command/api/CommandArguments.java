package rip.reflex.command.api;

import java.util.Arrays;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import rip.reflex.util.misc.Strings;

public class CommandArguments {
   private String[] args;

   public String get(int i) {
      return this.has(i) ? this.args[i] : null;
   }

   public String joinFrom(int start) {
      return this.has(start) ? Strings.join(start, this.args, " ") : null;
   }

   public int getInt(int i) {
      return this.has(i) ? Integer.valueOf(this.get(i)) : -1;
   }

   public Player getPlayer(int i) {
      return this.has(i) ? Bukkit.getPlayer(this.get(i)) : null;
   }

   public OfflinePlayer getOfflinePlayer(int i) {
      return this.has(i) ? Bukkit.getOfflinePlayer(this.get(i)) : null;
   }

   public UUID getUUID(int i) {
      return this.has(i) ? UUID.fromString(this.get(i)) : null;
   }

   public boolean has(int i) {
      return i < this.args.length;
   }

   public CommandArguments cut() {
      this.args = Arrays.copyOfRange(this.args, 1, this.args.length);
      return this;
   }

   public Boolean getBool(int i) {
      return this.has(i) ? Boolean.valueOf(this.get(i)) : null;
   }

   public CommandArguments(String[] args) {
      this.args = args;
   }
}
