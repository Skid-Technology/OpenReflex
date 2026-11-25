package rip.reflex.check.player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import me.flo.flogger.types.FlogLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;
import rip.reflex.api.event.ReflexCommandEvent;
import rip.reflex.check.base.impl.CustomCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.mod.badlionclient.BadlionConfigMaker;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.mcaccess.MCAccess;

public class ClientControlCheck extends CustomCheck implements PluginMessageListener {
   private final List<String> whitelist = new ArrayList<>();

   public ClientControlCheck() {
      super("ClientControl", "ccontrol.mn", Cheat.valueOf("ClientControl"));
      if (VersionUtils.getServerVersion().getVerId() >= MCVersion.V1_13_X.getVerId()) {
         this.reflex
            .warn(
               "ClientControl does not currently support versions newer than 1.12! Reflex will assume that all players' client brand is 'vanilla'. No actions regarding client mods will be taken."
            );
         this.disable();
      } else if (this.isEnabledInConfig()) {
         Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this.reflex, "5zig_Set", this);
         Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this.reflex, "LOLIMAHCKER", this);
         Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this.reflex, "BSM", this);
         Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this.reflex, "WDL|INIT", this);
         Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this.reflex, "MC|Brand", this);
         Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this.reflex, "WDL|REQUEST", this);
         Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this.reflex, "5zig_Set");
         Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this.reflex, "WDL|CONTROL");
         Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this.reflex, "BSM");
         Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this.reflex, "schematica");
         Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this.reflex, "BLC|M");
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      if (!this.isDisabled() && this.isEnabledInConfig()) {
         StringBuilder s = new StringBuilder("§c §r§5 §r§1 §r§f");
         Player p = e.getPlayer();
         p.sendMessage("§8 §8 §1 §3 §3 §7 §8 ");
         int flag = 0;
         if (this.getLocalSetting("betterpvp.disable.minimap", Boolean.valueOf(true))) {
            flag |= 1;
         }

         if (this.getLocalSetting("betterpvp.disable.armor_status", Boolean.valueOf(true))) {
            flag |= 2;
         }

         if (this.getLocalSetting("betterpvp.disable.potion_status", Boolean.valueOf(true))) {
            flag |= 4;
         }

         if (this.getLocalSetting("betterpvp.disable.archery_status", Boolean.valueOf(true))) {
            flag |= 8;
         }

         if (this.getLocalSetting("betterpvp.disable.better_sprint", Boolean.valueOf(true))) {
            flag |= 64;
         }

         if (this.getLocalSetting("betterpvp.disable.toggle_sneak", Boolean.valueOf(true))) {
            flag |= 128;
         }

         if (this.getLocalSetting("betterpvp.disable.quick_item_use", Boolean.valueOf(true))) {
            flag |= 256;
         }

         if (this.getLocalSetting("betterpvp.disable.tnt_notifications", Boolean.valueOf(true))) {
            flag |= 524288;
         }

         if (this.getLocalSetting("betterpvp.disable.death_points", Boolean.valueOf(true))) {
            flag |= 2097152;
         }

         if (this.getLocalSetting("betterpvp.disable.entity_info", Boolean.valueOf(true))) {
            flag |= 8388608;
         }

         String x = String.valueOf(flag);

         for (int i = 0; i < x.length(); i++) {
            s.append(" ").append("§r§").append(x, i, i + 1);
         }

         p.sendMessage(s.append(" ").toString());

         try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteOut);
            out.writeByte(0);
            out.writeBoolean(!this.getLocalSetting("schematica.disable.printer", Boolean.valueOf(true)));
            out.writeBoolean(!this.getLocalSetting("schematica.disable.save", Boolean.valueOf(true)));
            out.writeBoolean(!this.getLocalSetting("schematica.disable.load", Boolean.valueOf(true)));
            MCAccess.sendPluginMessage(p, "schematica", byteOut.toByteArray());
         } catch (Exception ex) {
            throw new RuntimeException("Schematica check failed (" + p.getName() + ")", ex);
         }

         if (this.reflex.getSetting("clientcontrol.badlion_client_support", true)) {
            MCAccess.sendPluginMessage(p, "BLC|M", BadlionConfigMaker.make(this.reflex));
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onChannelRegister(PlayerRegisterChannelEvent e) {
      if (!this.isDisabled() && this.isEnabledInConfig()) {
         this.channelCheck(e.getPlayer(), e.getChannel(), new byte[0]);
      }
   }

   public void onPluginMessageReceived(String channel, Player p, byte[] data) {
      if (!this.isDisabled() && this.isEnabledInConfig()) {
         this.channelCheck(p, channel, data);
      }
   }

   private void channelCheck(Player p, String channel, byte[] data) {
      String chan = Strings.wrap(channel.toLowerCase()).replace("|", "").get();
      if (chan.contains("lol")) {
         this.dispatchCommands(p, "vape");
      } else {
         if (chan.contains("5zig")) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            byte flag = 0;
            if (this.getLocalSetting("5zig.disable.potion_effect_hud", Boolean.valueOf(true))) {
               flag = (byte)(flag | 1);
            }

            if (this.getLocalSetting("5zig.disable.potion_vignette", Boolean.valueOf(true))) {
               flag = (byte)(flag | 2);
            }

            if (this.getLocalSetting("5zig.disable.armor_hud", Boolean.valueOf(true))) {
               flag = (byte)(flag | 4);
            }

            if (this.getLocalSetting("5zig.disable.render_saturation", Boolean.valueOf(true))) {
               flag = (byte)(flag | 8);
            }

            if (this.getLocalSetting("5zig.disable.render_health", Boolean.valueOf(true))) {
               flag = (byte)(flag | 16);
            }

            if (this.getLocalSetting("5zig.disable.auto_reconnect", Boolean.valueOf(true))) {
               flag = (byte)(flag | 32);
            }

            if (flag != 0) {
               out.writeByte(flag);
               MCAccess.sendPluginMessage(p, "5zig_Set", out.toByteArray());
            }

            this.dispatchCommands(p, "5zig");
         }

         if (chan.contains("bsm") || chan.contains("bettersprint")) {
            if (this.getLocalSetting("better_sprinting.disable", Boolean.valueOf(true))) {
               ByteArrayDataOutput outx = ByteStreams.newDataOutput();
               outx.writeByte(1);
               MCAccess.sendPluginMessage(p, "BSM", outx.toByteArray());
            }

            this.dispatchCommands(p, "better_sprinting");
         }

         if (chan.contains("wdl") || chan.contains("worlddownloader")) {
            if (this.getLocalSetting("world_downloader.disable", Boolean.valueOf(true))) {
               ByteArrayDataOutput outx = ByteStreams.newDataOutput();
               outx.writeInt(0);
               outx.writeBoolean(false);
               MCAccess.sendPluginMessage(p, "WDL|CONTROL", outx.toByteArray());
            }

            this.dispatchCommands(p, "world_downloader");
         }

         if (chan.contains("mcbrand")) {
            String brand;
            try {
               brand = Strings.asciiOnly(new String(data, "UTF-8")).toLowerCase();
            } catch (Exception ex) {
               throw new RuntimeException("Failed to obtain client brand (" + p.getName() + ")", ex);
            }

            this.reflex.info(p.getName() + "'s client brand is \"" + brand + "\"");
            Stats.of(p).setStat("mcClientBrand", brand);
            if (this.getLocalSetting("paranoid_mode.enabled", Boolean.valueOf(false)) && !this.isWhitelisted(brand)) {
               this.dispatchAll(p, this.reflex.getConfig().getStringList("clientcontrol.paranoid_mode.actions"));
               return;
            }

            if (brand.contains("forge") || brand.contains("fml")) {
               this.dispatchCommands(p, "forge");
            }

            if (brand.contains("lite")) {
               this.dispatchCommands(p, "liteloader");
            }

            if (brand.contains("map")) {
               this.dispatchCommands(p, "betterpvp");
            }
         }
      }
   }

   private boolean isWhitelisted(String brand) {
      if (this.whitelist.isEmpty()) {
         this.reflex.getConfig().getStringList("clientcontrol.paranoid_mode.whitelist").forEach(s -> this.whitelist.add(s.toLowerCase()));
      }

      return this.whitelist.contains(brand);
   }

   private void notify(Player p, String mod) {
      if (this.getLocalSetting("notifications.enabled", Boolean.valueOf(false))) {
         LoopHelper.loopPermitted(
            staff -> staff.sendMessage(
                  Strings.wrap(
                        ChatColor.translateAlternateColorCodes(
                           '&', Reflex.getReflex().getTheme(p).getPrefix() + this.getLocalSetting("notifications.message", "&9%player% &ris using &9%mod%")
                        )
                     )
                     .replace("%player%", p.getName())
                     .replace("%ping%", "" + Stats.of(p).getPing())
                     .replace("%bannedplayers%", "" + this.reflex.getBanCount())
                     .replace("%date%", this.reflex.getDate())
                     .replace("%break%", "\n")
                     .replace("\\n", "\n")
                     .replace("%tps%", "" + this.reflex.getTpsMeter().getTps())
                     .replace("%mod%", Strings.replace(mod.toUpperCase(), "_", " "))
                     .replace("%time%", this.reflex.getTime())
                     .placeholderApi(p)
                     .colourizeAndGet()
               ),
            "reflex.notify"
         );
      }
   }

   private void dispatchCommands(Player p, String mod) {
      this.notify(p, mod);
      if (!this.getLocalSetting(mod + ".allow_join", Boolean.valueOf(true))) {
         this.dispatchAll(p, this.reflex.getConfig().getStringList("clientcontrol." + mod + ".actions"));
      }
   }

   private void dispatchAll(Player p, List<String> commands) {
      for (String command : commands) {
         String id = "#" + UUID.randomUUID().toString().split("-")[0].toUpperCase();
         ReflexCommandEvent cmdEvent = new ReflexCommandEvent(p, this.getCheat(), command, id);
         Bukkit.getPluginManager().callEvent(cmdEvent);
         if (!cmdEvent.isCancelled()) {
            command = cmdEvent.getCommand();
            int d = 0;
            if (command.contains("%bannedplayersinlast")) {
               d = Integer.valueOf(command.split(Pattern.quote("%bannedplayersinlast"))[1].split(Pattern.quote("days%"))[0]);
            }

            command = Strings.wrap(command)
               .replace("%player%", p.getName())
               .replace("%ping%", "" + Stats.of(p).getPing())
               .replace("%bannedplayers%", "" + this.reflex.getBanCount())
               .replace("%bannedplayersinlast" + d + "days%", "" + this.reflex.getBanCountIn(TimeUnit.DAYS, (long)d))
               .replace("%execid%", id)
               .replace("%id%", id)
               .replace("%break%", "\n")
               .replace("\\n", "\n")
               .replace("%date%", this.reflex.getDate())
               .replace("%tps%", "" + this.reflex.getTpsMeter().getTps())
               .replace("%time%", this.reflex.getTime())
               .placeholderApi(p)
               .colourizeAndGet();
            this.reflex
               .log(
                  FlogLevel.DEBUG,
                  "Dispatching command " + Strings.wrap(command).replace("\n", "%break%").replace(" ", "%space%") + " (execution id: " + id + ")"
               );
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
         }
      }
   }
}
