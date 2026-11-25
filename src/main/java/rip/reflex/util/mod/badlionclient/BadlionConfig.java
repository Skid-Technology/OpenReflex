package rip.reflex.util.mod.badlionclient;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class BadlionConfig {
   private Map<String, BadlionConfig.DisallowedMods> modsDisallowed = new HashMap<>();

   public Map<String, BadlionConfig.DisallowedMods> getModsDisallowed() {
      return this.modsDisallowed;
   }

   private class DisallowedMods {
      private boolean disabled;
      private JsonObject extra_data;
   }
}
