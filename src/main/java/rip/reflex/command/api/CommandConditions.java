package rip.reflex.command.api;

import java.util.Arrays;
import java.util.function.Predicate;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.util.misc.Strings;

public class CommandConditions {
   private final CommandArguments args;
   private final CommandSender sender;
   public static final int[] integrityRan = new int[]{9812749, 862183, -13981, 8756127, -5816283};
   private boolean ok = true;

   public void checkBool(boolean b, String message) {
      if (this.ok && b) {
         this.error(message);
      }
   }

   public void usage(String message) {
      if (this.ok && !this.args.has(0)) {
         this.error(message);
      }
   }

   public void isPlayer(String message) {
      if (this.ok) {
         if (!(this.sender instanceof Player)) {
            this.error(message);
         }
      }
   }

   public void check(String errormsg, Predicate<CommandArguments> pred) {
      if (this.ok && !pred.test(this.args)) {
         this.error(errormsg);
      }
   }

   public void checkArgument(String msg, int i, ArgumentType type) {
      if (this.ok) {
         if (!this.args.has(i) || !type.getPredicate().test(this.args.get(i))) {
            this.error(msg);
         }
      }
   }

   public void checkOptionalArg(String msg, int i, ArgumentType type) {
      if (this.ok) {
         if (this.args.has(i) && !type.getPredicate().test(this.args.get(i))) {
            this.error(msg);
         }
      }
   }

   public static int intcheck() {
      return Arrays.stream(integrityRan).sum() ^ -47825193;
   }

   private void error(String msg) {
      this.sender.sendMessage(Strings.color(Reflex.getReflex().getTheme(this.sender).getPrefix() + msg));
      this.ok = false;
   }

   public CommandConditions(CommandArguments args, CommandSender sender) {
      this.args = args;
      this.sender = sender;
   }

   public CommandSender getSender() {
      return this.sender;
   }

   public boolean isOk() {
      return this.ok;
   }

   public void setOk(boolean ok) {
      this.ok = ok;
   }
}
