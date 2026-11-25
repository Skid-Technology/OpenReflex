package rip.reflex.util.machinelearning;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import me.darksidecode.e2eep4j.E2EKey;
import me.darksidecode.e2eep4j.api.E2EEP4J;
import me.darksidecode.e2eep4j.util.Convert;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.api.db.DataImpl;
import rip.reflex.api.event.ReflexHeurSnapshotEvent;
import rip.reflex.util.crypto.Encryptor;
import rip.reflex.util.machinelearning.layer.InputLayer;
import rip.reflex.util.machinelearning.layer.OutputLayer;
import rip.reflex.util.machinelearning.layer.SigmoidLayer;
import rip.reflex.util.machinelearning.sync.DBDataSync;
import rip.reflex.util.machinelearning.sync.DataSync;
import rip.reflex.util.machinelearning.sync.DefaultHashMap;
import rip.reflex.util.machinelearning.sync.FileSync;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.misc.Unzipper;

public class Classifier {
   private static final ArrayList<ArrayList<Dataset>> netlist = new ArrayList<>();
   private static final ArrayList<Dataset> net1 = new ArrayList<>();
   private static final ArrayList<Dataset> net2 = new ArrayList<>();
   private static final ArrayList<Dataset> net3 = new ArrayList<>();
   private static final ArrayList<Dataset> net4 = new ArrayList<>();
   private static final ArrayList<Dataset> net5 = new ArrayList<>();
   private static final ArrayList<Dataset> net6 = new ArrayList<>();
   private static final ArrayList<Dataset> net7 = new ArrayList<>();
   public static int checksCount = 0;
   private static int _LOL_ = -419293;
   private static Object e2e = null;
   private static final byte[] _KEK_ = new byte[]{-20, 11, -6, -117, -49, getNeg13B(), 29, 8};

   public static void init() {
      netlist.forEach(ArrayList::clear);
      netlist.clear();
      netlist.add(net1);
      netlist.add(net2);
      netlist.add(net3);
      netlist.add(net4);
      netlist.add(net5);
      netlist.add(net6);
      netlist.add(net7);

      try {
         loadData();
      } catch (MalformedURLException ex) {
         throw new RuntimeException(ex);
      }

      for (ArrayList<Dataset> net : netlist) {
         net.sort(Comparator.comparingInt(Dataset::compareValue));
      }
   }

   private static byte[] encryptDataset(byte[] ds) {
      byte[] b = E2EEP4J.encryptBytes(ds, Misc.castSafely(getDSKey()));

      for (int i = 0; i < b.length; i++) {
         b[i] = (byte)(b[i] ^ getIntNeg13() ^ i);
      }

      return (byte[])b.clone();
   }

   private static int getIntNeg13() {
      return getNeg13();
   }

   private static Object getDSKey() {
      if (_LOL_ % 2 == 0) {
         if (e2e != null && e2e instanceof E2EKey) {
            _LOL_ -= 3;
            return getDSKey();
         }

         e2e = new byte[]{23, -19, 110, -7, -4, 68, 90, -12};
      } else {
         e2e = Convert.bytes2e2ekey(
            new byte[]{
               -84,
               -19,
               0,
               5,
               117,
               114,
               0,
               19,
               91,
               76,
               106,
               97,
               118,
               97,
               46,
               108,
               97,
               110,
               103,
               46,
               79,
               98,
               106,
               101,
               99,
               116,
               59,
               -112,
               -50,
               88,
               -97,
               16,
               115,
               41,
               108,
               2,
               0,
               0,
               120,
               112,
               0,
               0,
               0,
               70,
               115,
               114,
               0,
               14,
               106,
               97,
               118,
               97,
               46,
               108,
               97,
               110,
               103,
               46,
               66,
               121,
               116,
               101,
               -100,
               78,
               96,
               -124,
               -18,
               80,
               -11,
               28,
               2,
               0,
               1,
               66,
               0,
               5,
               118,
               97,
               108,
               117,
               101,
               120,
               114,
               0,
               16,
               106,
               97,
               118,
               97,
               46,
               108,
               97,
               110,
               103,
               46,
               78,
               117,
               109,
               98,
               101,
               114,
               -122,
               -84,
               -107,
               29,
               11,
               -108,
               -32,
               -117,
               2,
               0,
               0,
               120,
               112,
               -68,
               115,
               113,
               0,
               126,
               0,
               2,
               64,
               115,
               113,
               0,
               126,
               0,
               2,
               -44,
               115,
               113,
               0,
               126,
               0,
               2,
               -106,
               115,
               113,
               0,
               126,
               0,
               2,
               56,
               113,
               0,
               126,
               0,
               6,
               115,
               113,
               0,
               126,
               0,
               2,
               115,
               115,
               113,
               0,
               126,
               0,
               2,
               57,
               115,
               113,
               0,
               126,
               0,
               2,
               -81,
               115,
               113,
               0,
               126,
               0,
               2,
               79,
               115,
               113,
               0,
               126,
               0,
               2,
               -122,
               115,
               113,
               0,
               126,
               0,
               2,
               24,
               115,
               113,
               0,
               126,
               0,
               2,
               5,
               115,
               113,
               0,
               126,
               0,
               2,
               27,
               115,
               113,
               0,
               126,
               0,
               2,
               -31,
               115,
               113,
               0,
               126,
               0,
               2,
               -23,
               115,
               113,
               0,
               126,
               0,
               2,
               60,
               115,
               113,
               0,
               126,
               0,
               2,
               65,
               115,
               113,
               0,
               126,
               0,
               2,
               32,
               115,
               113,
               0,
               126,
               0,
               2,
               -46,
               115,
               113,
               0,
               126,
               0,
               2,
               -71,
               115,
               113,
               0,
               126,
               0,
               2,
               -15,
               115,
               113,
               0,
               126,
               0,
               2,
               127,
               115,
               113,
               0,
               126,
               0,
               2,
               -124,
               115,
               113,
               0,
               126,
               0,
               2,
               87,
               113,
               0,
               126,
               0,
               10,
               115,
               113,
               0,
               126,
               0,
               2,
               54,
               115,
               113,
               0,
               126,
               0,
               2,
               -27,
               115,
               113,
               0,
               126,
               0,
               2,
               7,
               115,
               113,
               0,
               126,
               0,
               2,
               91,
               113,
               0,
               126,
               0,
               19,
               115,
               113,
               0,
               126,
               0,
               2,
               -2,
               115,
               113,
               0,
               126,
               0,
               2,
               9,
               115,
               113,
               0,
               126,
               0,
               2,
               -76,
               115,
               113,
               0,
               126,
               0,
               2,
               33,
               113,
               0,
               126,
               0,
               30,
               115,
               113,
               0,
               126,
               0,
               2,
               -57,
               115,
               113,
               0,
               126,
               0,
               2,
               119,
               115,
               113,
               0,
               126,
               0,
               2,
               -42,
               115,
               113,
               0,
               126,
               0,
               2,
               16,
               115,
               113,
               0,
               126,
               0,
               2,
               4,
               113,
               0,
               126,
               0,
               20,
               115,
               113,
               0,
               126,
               0,
               2,
               -51,
               115,
               113,
               0,
               126,
               0,
               2,
               83,
               115,
               113,
               0,
               126,
               0,
               2,
               76,
               115,
               113,
               0,
               126,
               0,
               2,
               -62,
               115,
               113,
               0,
               126,
               0,
               2,
               -1,
               115,
               113,
               0,
               126,
               0,
               2,
               28,
               115,
               113,
               0,
               126,
               0,
               2,
               8,
               113,
               0,
               126,
               0,
               24,
               113,
               0,
               126,
               0,
               39,
               113,
               0,
               126,
               0,
               19,
               115,
               113,
               0,
               126,
               0,
               2,
               0,
               115,
               113,
               0,
               126,
               0,
               2,
               -19,
               115,
               113,
               0,
               126,
               0,
               2,
               -102,
               115,
               113,
               0,
               126,
               0,
               2,
               70,
               113,
               0,
               126,
               0,
               6,
               115,
               113,
               0,
               126,
               0,
               2,
               71,
               115,
               113,
               0,
               126,
               0,
               2,
               89,
               115,
               113,
               0,
               126,
               0,
               2,
               11,
               115,
               113,
               0,
               126,
               0,
               2,
               40,
               115,
               113,
               0,
               126,
               0,
               2,
               48,
               115,
               113,
               0,
               126,
               0,
               2,
               14,
               115,
               113,
               0,
               126,
               0,
               2,
               -54,
               117,
               114,
               0,
               2,
               91,
               73,
               77,
               -70,
               96,
               38,
               118,
               -22,
               -78,
               -91,
               2,
               0,
               0,
               120,
               112,
               0,
               0,
               0,
               27,
               0,
               0,
               1,
               -74,
               2,
               -109,
               -42,
               -40,
               0,
               0,
               91,
               126,
               0,
               0,
               0,
               123,
               0,
               6,
               -107,
               74,
               0,
               0,
               91,
               127,
               0,
               0,
               1,
               106,
               0,
               1,
               93,
               3,
               0,
               0,
               97,
               -1,
               0,
               0,
               31,
               14,
               0,
               1,
               28,
               -73,
               0,
               1,
               68,
               0,
               0,
               0,
               93,
               121,
               0,
               0,
               112,
               -33,
               0,
               0,
               68,
               109,
               0,
               0,
               -40,
               -87,
               0,
               0,
               117,
               -39,
               0,
               0,
               88,
               3,
               0,
               1,
               83,
               127,
               0,
               0,
               -86,
               85,
               0,
               0,
               -128,
               -110,
               0,
               1,
               17,
               87,
               0,
               0,
               -75,
               103,
               0,
               1,
               73,
               47,
               0,
               1,
               74,
               15,
               0,
               0,
               123,
               9,
               0,
               1,
               46,
               48,
               117,
               113,
               0,
               126,
               0,
               59,
               0,
               0,
               0,
               28,
               0,
               0,
               7,
               -18,
               0,
               0,
               16,
               -26,
               0,
               0,
               25,
               -120,
               0,
               0,
               1,
               106,
               0,
               0,
               1,
               2,
               0,
               0,
               0,
               8,
               0,
               50,
               -31,
               35,
               -1,
               -1,
               -1,
               -123,
               0,
               1,
               71,
               -46,
               0,
               0,
               86,
               82,
               0,
               0,
               -9,
               29,
               0,
               0,
               -70,
               -18,
               0,
               0,
               39,
               -34,
               0,
               1,
               54,
               -31,
               0,
               1,
               57,
               58,
               0,
               0,
               49,
               102,
               0,
               1,
               76,
               60,
               0,
               0,
               110,
               84,
               0,
               1,
               63,
               109,
               0,
               0,
               -17,
               -96,
               0,
               0,
               -19,
               42,
               0,
               0,
               34,
               -14,
               0,
               1,
               25,
               33,
               0,
               1,
               74,
               -112,
               0,
               0,
               57,
               124,
               0,
               0,
               -124,
               1,
               0,
               0,
               -8,
               -2,
               0,
               1,
               78,
               -78,
               117,
               113,
               0,
               126,
               0,
               59,
               0,
               0,
               0,
               27,
               0,
               0,
               126,
               29,
               0,
               0,
               2,
               -102,
               0,
               5,
               33,
               98,
               0,
               9,
               -128,
               -64,
               0,
               0,
               1,
               -38,
               0,
               0,
               0,
               7,
               0,
               0,
               19,
               11,
               0,
               0,
               -36,
               -45,
               0,
               0,
               94,
               68,
               0,
               0,
               -39,
               10,
               0,
               0,
               15,
               12,
               0,
               0,
               122,
               98,
               0,
               0,
               58,
               70,
               0,
               0,
               -4,
               84,
               0,
               0,
               -18,
               -105,
               0,
               0,
               -60,
               -1,
               0,
               0,
               73,
               109,
               0,
               1,
               115,
               4,
               0,
               1,
               98,
               63,
               0,
               0,
               -80,
               -87,
               0,
               1,
               116,
               -90,
               0,
               1,
               49,
               111,
               0,
               0,
               22,
               -45,
               0,
               0,
               32,
               75,
               0,
               0,
               -25,
               93,
               0,
               1,
               117,
               6,
               0,
               0,
               98,
               -8,
               117,
               113,
               0,
               126,
               0,
               59,
               0,
               0,
               0,
               27,
               0,
               0,
               70,
               -39,
               0,
               0,
               24,
               -67,
               0,
               0,
               -6,
               -13,
               0,
               0,
               0,
               52,
               0,
               113,
               -54,
               -93,
               0,
               0,
               -119,
               -85,
               0,
               0,
               13,
               97,
               0,
               0,
               25,
               3,
               0,
               1,
               41,
               -25,
               0,
               0,
               -84,
               -107,
               0,
               0,
               -92,
               70,
               0,
               0,
               -8,
               -124,
               0,
               0,
               113,
               -59,
               0,
               0,
               91,
               98,
               0,
               0,
               54,
               -26,
               0,
               0,
               114,
               12,
               0,
               0,
               -101,
               -125,
               0,
               0,
               -114,
               -37,
               0,
               0,
               -101,
               114,
               0,
               1,
               5,
               81,
               0,
               0,
               29,
               13,
               0,
               0,
               -46,
               -1,
               0,
               1,
               38,
               -3,
               0,
               0,
               98,
               -84,
               0,
               1,
               11,
               -64,
               0,
               1,
               50,
               71,
               0,
               1,
               76,
               -41,
               117,
               113,
               0,
               126,
               0,
               59,
               0,
               0,
               0,
               25,
               -1,
               -1,
               106,
               -104,
               0,
               0,
               0,
               3,
               0,
               0,
               2,
               -121,
               0,
               0,
               2,
               44,
               0,
               0,
               1,
               -69,
               0,
               0,
               43,
               90,
               0,
               0,
               8,
               -92,
               0,
               0,
               57,
               -112,
               0,
               0,
               33,
               -84,
               0,
               1,
               62,
               -31,
               0,
               0,
               13,
               38,
               0,
               0,
               111,
               -79,
               0,
               0,
               -14,
               -44,
               0,
               0,
               -87,
               102,
               0,
               0,
               92,
               21,
               0,
               0,
               -105,
               21,
               0,
               0,
               -101,
               60,
               0,
               1,
               1,
               -3,
               0,
               0,
               89,
               48,
               0,
               1,
               39,
               -50,
               0,
               0,
               -108,
               -27,
               0,
               0,
               -8,
               -117,
               0,
               0,
               41,
               -73,
               0,
               1,
               9,
               -70,
               0,
               1,
               18,
               79,
               117,
               113,
               0,
               126,
               0,
               59,
               0,
               0,
               0,
               31,
               0,
               0,
               15,
               103,
               0,
               0,
               124,
               97,
               0,
               0,
               0,
               -2,
               0,
               0,
               0,
               6,
               0,
               0,
               0,
               67,
               0,
               0,
               2,
               -5,
               0,
               0,
               0,
               5,
               0,
               0,
               4,
               -1,
               0,
               0,
               0,
               25,
               0,
               0,
               0,
               6,
               0,
               0,
               0,
               56,
               0,
               0,
               -30,
               97,
               0,
               0,
               -83,
               -88,
               0,
               0,
               9,
               -23,
               0,
               0,
               111,
               55,
               0,
               1,
               1,
               -60,
               0,
               0,
               -60,
               4,
               0,
               1,
               65,
               52,
               0,
               1,
               91,
               -84,
               0,
               0,
               53,
               -48,
               0,
               1,
               79,
               -7,
               0,
               0,
               -110,
               -61,
               0,
               0,
               81,
               -68,
               0,
               1,
               25,
               -102,
               0,
               1,
               56,
               78,
               0,
               0,
               13,
               -22,
               0,
               0,
               -118,
               31,
               0,
               0,
               98,
               -117,
               0,
               1,
               96,
               16,
               0,
               0,
               113,
               59,
               0,
               0,
               55,
               39
            }
         );
         _LOL_ += 3;
         if (_LOL_ % 2 != 0) {
            e2e = new byte[]{99, -38, 62, 1, 7, 121, -17, 43};
         }
      }

      return e2e == null ? _KEK_ : e2e;
   }

   private static <T> T getNeg13() {
      int r = 8;
      double x = 5.0;
      double y = 7.0;
      double abc = x * y - 2.0;

      while (abc > 25.0) {
         abc--;
      }

      while (abc % 1.0 != 0.0) {
         abc -= 5.0;
      }

      double n = Math.sqrt(abc) - 10.0 - 2.0 * x;
      int k = (int)y;
      int v = (int)n + r;
      return (T)Integer.valueOf(v - k + 1);
   }

   public static byte[] encrypt(Dataset ds) {
      try {
         StringBuilder save = new StringBuilder();
         save.append(ds.getName());
         save.append('\n');
         save.append(ds.isLegit());
         save.append('\n');

         for (ArrayList<Double> list : ds.getData().values()) {
            StringBuilder v = new StringBuilder("" + list.get(0));
            int appended = 0;
            if (list.size() > 1) {
               for (int x = 1; x < list.size(); x++) {
                  v.append(", ").append(list.get(x));
                  appended++;
               }
            }

            while (appended < 200) {
               v.append(", ").append(0.0);
               appended++;
            }

            save.append(v.toString());
            save.append('\n');
         }

         return encryptDataset(save.toString().getBytes("UTF-8"));
      } catch (Exception ex) {
         throw new RuntimeException("Heur snapshot save failure (" + ds.getName() + ")", ex);
      }
   }

   private static void loadData() throws MalformedURLException {
      if (!Reflex.getReflex().getCloudService().isConnected()) {
         Reflex.getReflex().info("Hold on: performing magic... This might take up to a few minutes. API and some checks will not work until complete.");
         Reflex.getReflex().info("-----------------------------------------------");
         Reflex.getReflex().info("  HINT:");
         Reflex.getReflex()
            .info("  Servers which use Reflex Cloud Computings service skip this as the magic is done on our side! Learn more about Cloud on our overview page");
         Reflex.getReflex().info("-----------------------------------------------");
      }

      final HashMap<String, List<byte[]>> map = new DefaultHashMap<>(ArrayList::new);
      DataSync sync;
      switch (Reflex.getReflex().getUsedDB()) {
         case MYSQL:
            sync = new DBDataSync(Reflex.getReflex().getDataImpl(), Reflex.getReflex().getSyncURL(), "data.zip") {
               @Override
               protected void useInstant(byte[] data) {
                  new Unzipper(data, (e, arr) -> {
                     String[] split = e.getName().split("/");
                     String key = split[split.length - 3] + "." + split[split.length - 2];
                     List<byte[]> list = map.get(key);
                     list.add(arr);
                  }).exec();
               }

               @Override
               protected void save(DataImpl impl, byte[] data, String hash) throws Exception {
                  super.save(impl, Encryptor.encrypt(data), hash);
               }

               @Override
               protected byte[] load(DataImpl impl) throws Exception {
                  return Encryptor.decrypt(super.load(impl));
               }
            };
            break;
         case UNKNOWN:
            sync = new FileSync(new File(Reflex.getReflex().getWorkingDir()), Reflex.getReflex().getSyncURL(), "data.zip", false) {
               @Override
               protected void useInstant(byte[] data) {
                  new Unzipper(data, (e, arr) -> {
                     String[] split = e.getName().split("/");
                     String key = split[split.length - 3] + "." + split[split.length - 2];
                     List<byte[]> list = map.get(key);
                     list.add(arr);
                  }).exec();
               }

               @Override
               protected void save(File localFile, byte[] data) throws Exception {
                  super.save(localFile, Encryptor.encrypt(data));
               }

               @Override
               protected byte[] load(File localFile) throws Exception {
                  return Encryptor.decrypt(super.load(localFile));
               }
            };
            break;
         default:
            sync = null;
      }

      if (sync.needSync()) {
         sync.download();
      } else {
         sync.loadLocal();
      }

      for (int i = 0; i < netlist.size(); i++) {
         for (byte[] arr : map.get("lessons" + (i + 1) + ".hacks")) {
            netlist.get(i).add(Dataset.valueOf(new String(arr)));
         }

         for (byte[] arr : map.get("lessons" + (i + 1) + ".legit")) {
            netlist.get(i).add(Dataset.valueOf(new String(arr)));
         }
      }

      for (int i = 0; i < netlist.size() - 1; i++) {
         netlist.get(i).clear();
      }

      Reflex.getReflex().setDataVersion(sync.getVersion());
      if (!Reflex.getReflex().getCloudService().isConnected()) {
         Reflex.getReflex().info("Done");
      }
   }

   private static byte getNeg13B() {
      return Byte.valueOf("-13");
   }

   public static void debugHeur(boolean legit, Dataset ds, Player p, int level) {
      if (level != 0) {
         byte[] body = encrypt(ds);
         if (level >= 1) {
            Bukkit.getPluginManager().callEvent(new ReflexHeurSnapshotEvent(p, legit, ds.getName(), (byte[])body.clone()));
         }

         if (level == 2 || level == 3 && legit || level == 4 && !legit) {
            saveDataset(ds.getName(), body);
         }
      }
   }

   private static void saveDataset(String id, byte[] body) {
      try {
         Files.write(Reflex.getReflex().getReflexFile("/heur/" + id).toPath(), body);
      } catch (Exception ex) {
         throw new RuntimeException("Failed to save a " + body.length + " bytes snapshot ID " + id, ex);
      }
   }

   public static Classification classify(Dataset dataset) {
      if (netlist.size() < 7) {
         return new Classification(0, 0.0, null, 0, "");
      } else {
         checksCount++;
         ArrayList<Dataset> topMatched = new ArrayList<>();
         float confidence = 0.0F;
         ArrayList<Dataset> net = netlist.get(6);
         InputLayer input = new InputLayer(dataset);
         input.learn(net);
         input.computeOutput();
         OutputLayer output = input.getOutput();
         SigmoidLayer sigmoid = output.getSigmoid();
         sigmoid.computeOutput();
         output.learn(net);
         output.computeOutput();
         Dataset[] winners = output.getWinners();
         float[] probabilities = sigmoid.probabilityChances();
         int badPatterns = (int)Arrays.stream(winners).filter(ds -> !ds.isLegit()).count();
         topMatched.addAll(Arrays.asList(winners));

         for (int i = 0; i < winners.length; i++) {
            if (!winners[i].isLegit()) {
               confidence += probabilities[i];
            }
         }

         confidence = Math.max(0.0F, Math.min(100.0F, confidence));
         checksCount = Math.max(0, checksCount - 1);
         input.clear();
         sigmoid.clear();
         output.clear();
         return new Classification(badPatterns, (double)confidence, topMatched, input.getCheatPoints(), input.getCheatAttributes());
      }
   }

   public static void crash(String stage, ComputeError cause) {
      alert("---------------------------------------------------------");
      alert("// Something went wrong o:");
      alert("Current stage: " + stage);
      alert(" ");

      for (StackTraceElement element : cause.getStackTrace()) {
         String s = element.toString();
         if (s.contains("rip.reflex")) {
            alert("     -> " + s);
         }
      }

      alert(" ");
      alert("Reflex will be disabled in order to avoid fatal issues.");
      alert("Please open a report at go.reflex.rip/bug");
      alert("---------------------------------------------------------");
      Bukkit.getPluginManager().disablePlugin(Reflex.getReflex());
   }

   public static void alert(String s) {
      System.err.println("(!) " + s);
   }

   public static ArrayList<ArrayList<Dataset>> getNetlist() {
      return netlist;
   }
}
