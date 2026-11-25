package me.darksidecode.kantanj.formatting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.darksidecode.kantanj.types.Check;

public final class CommonJson {
   private static final Gson simpleGsonNoHtmlEsc = new GsonBuilder().disableHtmlEscaping().create();
   private static final Gson prettyGsonNoHtmlEsc = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

   private CommonJson() {
   }

   public static <T> T fromJson(String json, Class<T> typeOfT) {
      return (T)simpleGsonNoHtmlEsc.fromJson(Check.notNull(json, "json cannot be null"), Check.notNull(typeOfT, "typeOfT cannot be null"));
   }

   public static String toJson(Object obj) {
      return simpleGsonNoHtmlEsc.toJson(Check.notNull(obj, "obj cannot be null"));
   }

   public static String toPrettyJson(Object obj) {
      return prettyGsonNoHtmlEsc.toJson(Check.notNull(obj, "obj cannot be null"));
   }

   public static Gson getSimpleGsonNoHtmlEsc() {
      return simpleGsonNoHtmlEsc;
   }

   public static Gson getPrettyGsonNoHtmlEsc() {
      return prettyGsonNoHtmlEsc;
   }
}
