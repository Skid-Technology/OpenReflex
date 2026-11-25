package me.darksidecode.kantanj.currency;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.darksidecode.kantanj.networking.GetHttpRequest;
import me.darksidecode.kantanj.networking.Networking;
import me.darksidecode.kantanj.types.Check;
import org.apache.http.entity.ContentType;

public class SimpleCurrencyConverter implements CurrencyConverter {
   public static final String FREE_URL = "https://free.currencyconverterapi.com";
   public static final String PREMIUM_URL = "https://api.currencyconverterapi.com";
   public static final String V6 = "v6";
   private final String baseUrl;
   private final String apiKey;
   private String apiVersion = "v6";

   public SimpleCurrencyConverter(String apiKey) {
      this(false, apiKey);
   }

   public SimpleCurrencyConverter(boolean premium, String apiKey) {
      this.baseUrl = premium ? "https://api.currencyconverterapi.com" : "https://free.currencyconverterapi.com";
      this.apiKey = Check.notNull(apiKey, "API key cannot be null");
   }

   public SimpleCurrencyConverter apiVersion(String apiVersion) {
      this.apiVersion = Check.notNull(apiVersion, "API version cannot be null");
      return this;
   }

   @Override
   public float convert(float amount, Currency from, Currency to) {
      if (amount < 0.0F) {
         throw new IllegalArgumentException("amount cannot be negative");
      } else if (amount == 0.0F) {
         return 0.0F;
      } else {
         Check.notNull(from, "source currency cannot be null");
         Check.notNull(to, "target currency cannot be null");

         try {
            String arg = from.name() + '_' + to.name();
            GetHttpRequest getRequest = (GetHttpRequest)new GetHttpRequest()
               .baseUrl(this.baseUrl)
               .path("api/" + this.apiVersion + "/convert")
               .queryParams("q", arg, "compact", "ultra", "apiKey", this.apiKey)
               .requestProperty("Content-Type", ContentType.APPLICATION_JSON.toString())
               .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101");
            String response = Networking.Http.get(getRequest);
            JsonObject json = new JsonParser().parse(response).getAsJsonObject();
            float conversionRate = json.get(arg).getAsFloat();
            return amount * conversionRate;
         } catch (Exception ex) {
            throw new RuntimeException("failed to convert " + amount + " " + from.name() + " to " + to.name(), ex);
         }
      }
   }
}
