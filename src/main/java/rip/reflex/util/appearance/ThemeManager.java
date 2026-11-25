package rip.reflex.util.appearance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.util.machinelearning.sync.FileSync;

public class ThemeManager {
   public static final Gson gson = new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
   public static final List<String> defaultThemes = Arrays.asList(
      "ModernBlue_DE",
      "ModernBlue_EN",
      "ModernBlue_ES",
      "ModernBlue_HR",
      "ModernBlue_IT",
      "ModernBlue_JA",
      "ModernBlue_NL",
      "ModernBlue_NO",
      "ModernBlue_PL",
      "ModernBlue_pt-BR",
      "ModernBlue_RU",
      "ModernBlue_VI",
      "ModernBlue_zh-CN",
      "ModernBlue_zh-TW"
   );
   private static final Map<String, String> mcLocalesMap = new HashMap<>();
   private static final Map<String, Theme> themeMap = new HashMap<>();
   private static final Map<String, Map<String, String>> rawThemeMap = new ConcurrentHashMap<>();
   private static final Map<Player, Theme> personalThemes = new ConcurrentHashMap<>();
   private static final Map<Player, Map<String, String>> personalRawThemes = new ConcurrentHashMap<>();

   public static void load() {
      loadMcLocalesMap();
      loadThemeMap();
   }

   private static void loadMcLocalesMap() {
      mcLocalesMap.put("de_at", "DE");
      mcLocalesMap.put("de_de", "DE");
      mcLocalesMap.put("en_au", "EN");
      mcLocalesMap.put("en_ca", "EN");
      mcLocalesMap.put("en_gb", "EN");
      mcLocalesMap.put("en_nz", "EN");
      mcLocalesMap.put("en_us", "EN");
      mcLocalesMap.put("es_ar", "ES");
      mcLocalesMap.put("es_es", "ES");
      mcLocalesMap.put("es_mx", "ES");
      mcLocalesMap.put("es_uy", "ES");
      mcLocalesMap.put("es_ve", "ES");
      mcLocalesMap.put("es_cl", "ES");
      mcLocalesMap.put("hr_hr", "HR");
      mcLocalesMap.put("sr_sp", "HR");
      mcLocalesMap.put("hu_hu", "HR");
      mcLocalesMap.put("it_it", "IT");
      mcLocalesMap.put("ja_jp", "JA");
      mcLocalesMap.put("nl_nl", "NL");
      mcLocalesMap.put("nn_no", "NO");
      mcLocalesMap.put("no_no", "NO");
      mcLocalesMap.put("nb_no", "NO");
      mcLocalesMap.put("pl_pl", "PL");
      mcLocalesMap.put("pt_br", "pt-BR");
      mcLocalesMap.put("ru_ru", "RU");
      mcLocalesMap.put("uk_ua", "RU");
      mcLocalesMap.put("be_by", "RU");
      mcLocalesMap.put("vi_vn", "VI");
      mcLocalesMap.put("zh_cn", "zh-CN");
      mcLocalesMap.put("zh_tw", "zh-TW");
   }

   private static void loadThemeMap() {
      if (Reflex.getReflex().getSetting("general.personalized_themes.enabled", true)) {
         String group = getThemeGroup();
         Reflex.getReflex()
            .info(
               "Personalized themes are enabled! Reflex will automatically pick up themes from the \""
                  + group
                  + "\" theme group for players according to their client locale"
            );
         loadLanguage("DE", group);
         loadLanguage("EN", group);
         loadLanguage("ES", group);
         loadLanguage("HR", group);
         loadLanguage("IT", group);
         loadLanguage("JA", group);
         loadLanguage("NL", group);
         loadLanguage("NO", group);
         loadLanguage("PL", group);
         loadLanguage("pt-BR", group);
         loadLanguage("RU", group);
         loadLanguage("VI", group);
         loadLanguage("zh-CN", group);
         loadLanguage("zh-TW", group);
      }
   }

   private static void loadLanguage(String reflexLocale, String group) {
      File f = Reflex.getReflex().getReflexFile("themes/" + group + "_" + reflexLocale + ".json");
      if (f.exists()) {
         Theme t = loadTheme(f, false);
         themeMap.put(reflexLocale, t);
         rawThemeMap.put(reflexLocale, (Map<String, String>)gson.fromJson(gson.toJson(t), ConcurrentHashMap.class));
      } else {
         Reflex.getReflex().warn(String.format("The selected theme group \"%s\" does not support one of default Reflex locales \"%s\"", group, reflexLocale));
      }
   }

   public static Theme pickupByClientLocale(String languageCode) {
      return byReflexLocale(toReflexLocale(languageCode));
   }

   public static Map<String, String> pickupByClientLocaleRaw(String languageCode) {
      return byReflexLocaleRaw(toReflexLocale(languageCode));
   }

   private static String toReflexLocale(String languageCode) {
      return mcLocalesMap.getOrDefault(languageCode.toLowerCase(), "EN");
   }

   public static Theme byReflexLocale(String locale) {
      return themeMap.getOrDefault(locale, Reflex.getTheme());
   }

   public static Map<String, String> byReflexLocaleRaw(String locale) {
      return rawThemeMap.get(locale);
   }

   public static String getThemeGroup() {
      return Reflex.getReflex().getSetting("general.personalized_themes.theme_group", "ModernBlue");
   }

   public static void downloadThemes(File folder) {
      try {
         for (String t : defaultThemes) {
            new FileSync(folder, Reflex.getReflex().getSyncURL(), "themes/" + t + ".json", true).download();
         }
      } catch (Exception ex) {
         throw new RuntimeException("Failed to download themes in " + folder.getAbsolutePath(), ex);
      }
   }

   public static Theme loadTheme(File f, boolean set) {
      try (FileReader reader = new FileReader(f)) {
         Theme t = (Theme)gson.fromJson(reader, Theme.class);
         reader.close();
         return set ? Reflex.setTheme(t) : t;
      } catch (Exception e) {
         throw new RuntimeException("Cannot load theme from file: " + f.getAbsolutePath(), e);
      }
   }

   public static String pickupDefaultBySystemLocale() {
      String locale = Locale.getDefault().getLanguage().toLowerCase().trim();
      Reflex.getReflex().info("System locale: " + locale);
      return defaultThemes.stream().filter(t -> locale.contains(t.split(Pattern.quote("_"))[1].toLowerCase())).findFirst().orElse("ModernBlue_EN");
   }

   public static Map<Player, Theme> getPersonalThemes() {
      return personalThemes;
   }

   public static Map<Player, Map<String, String>> getPersonalRawThemes() {
      return personalRawThemes;
   }
}
