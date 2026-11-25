package rip.reflex.moonlight;

import java.util.HashMap;
import me.darksidecode.kantanj.networking.Networking;
import me.darksidecode.kantanj.networking.PostHttpRequest;
import org.bukkit.Bukkit;
import rip.reflex.Reflex;
import rip.reflex.util.location.Movement;
import rip.reflex.util.misc.Strings;

public class ReflexMoonlight {
   private static final String BASE_URL = "https://gulag.reflex.rip";
   private static final String PROTOCOL_ENCRYPTION_SALT_BASE64 = "lv2mhRZSX1g=";
   private final Reflex reflex;
   private MoonlightKeyReader key;
   private boolean loaded;
   private HideMode hideMode;

   public ReflexMoonlight(Reflex reflex, String key) {
      this.reflex = reflex;
      if (Bukkit.getPluginManager().getPlugin("ReflexMoonlightLibs") == null) {
         reflex.warn("=====================================================================");
         reflex.warn("  ERROR: FAILED TO START REFLEX MOONLIGHT");
         reflex.warn("      Reflex Moonlight depends on our ReflexMoonlightLibs 'plugin'.");
         reflex.warn("      It appears that it is not installed. Please download it from:");
         reflex.warn("      --> https://g.reflex.rip/moonlight-libs");
         reflex.warn("=====================================================================");
      } else {
         reflex.info("(Moonlight) Authenticating...");

         try {
            this.key = new MoonlightKeyReader(key);
            MoonlightCrypto.setEncryptor(this.key.getProtocolEncryptionKey(), "lv2mhRZSX1g=");
            this.verifyKeyOnline();
         } catch (Exception ex) {
            reflex.warn("Failed to verify Reflex Moonlight key:");
            ex.printStackTrace();
            this.invalidKey();
         }
      }
   }

   private void verifyKeyOnline() {
      String response = this.postRequest("verifyMoonlightKey");
      if (response != null) {
         if (!response.equals("ok")) {
            throw new RuntimeException("failed to verify Reflex Moonlight key: unrecognized server response: " + response);
         }

         this.welcome();
      }
   }

   public void load() {
      if (this.key != null) {

         String hideVal;
         try {
            hideVal = this.reflex.getSetting("moonlight.hide_reflex", "no").toUpperCase();
         } catch (ClassCastException ignored) {
            hideVal = "no";
         }

         try {
            this.hideMode = HideMode.valueOf(hideVal);
         } catch (Exception ignoredEx) {
            this.reflex.warn("==== INVALID CONFIGURATION ====================================");
            this.reflex.warn("  Invalid value at...");
            this.reflex.warn("      >> moonlight:");
            this.reflex.warn("      >>   hide_reflex: " + hideVal + "  <--  what?");
            this.reflex.warn(" ");
            this.reflex.warn("  Please see comments in the configuration file for details.");
            this.reflex.warn("  Falling back to default value: 'no'");
            this.reflex.warn("===============================================================");
            this.hideMode = HideMode.NO;
         }

         this.loaded = true;
      }
   }

   public boolean isActive() {
      return this.loaded && this.key != null;
   }

   String postRequest(String path) {
      HashMap<String, String> params = new HashMap<>();
      params.put("moonlightKey", this.key.getOrigin());
      PostHttpRequest postRequest = new PostHttpRequest()
         .baseUrl("https://gulag.reflex.rip")
         .path(path)
         .queryParam("spigotUid", Movement.getApinstance().getId())
         .userAgent("Reflex/" + this.reflex.getDescription().getVersion())
         .doInput(true)
         .asPostRequest()
         .postJsonData(params);
      String response = Networking.Http.post(postRequest);
      if (response.contains("badRequest")) {
         throw new RuntimeException("Reflex Moonlight request failure: server said I'm bad");
      } else if (response.equals("xInvalidCredentials")) {
         this.invalidKey();
         return null;
      } else if (response.startsWith("!500internalError/")) {
         String errorId = Strings.replace(response, "!500internalError/", "");
         throw new RuntimeException("Reflex Moonlight request failure: server said something went wrong, please report this: error ID: " + errorId);
      } else {
         return MoonlightCrypto.decryptString(response);
      }
   }

   private void invalidKey() {
      this.reflex.warn(" ");
      this.reflex.warn("==================================================================================");
      this.reflex.warn("    (Moonlight) Invalid key. Refer to https://g.reflex.rip/moonlight for help.    ");
      this.reflex.warn("==================================================================================");
      this.reflex.warn(" ");
      this.loaded = false;
      this.key = null;
   }

   private void welcome() {
      this.reflex
         .info("(Moonlight) Successfully connected to Reflex Moonlight, thanks for purchasing! Your are a member since " + this.key.getCreationDateString());
   }

   public HideMode getHideMode() {
      return this.hideMode;
   }
}
