package rip.reflex.util.math;

import com.comphenix.protocol.wrappers.BlockPosition;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;
import javax.net.ssl.SSLHandshakeException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import rip.reflex.Reflex;
import rip.reflex.listener.CentralEventListener;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.listener.impl.BlockPlaceListener;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.math.descriptive.moment.FourthMoment;
import rip.reflex.util.math.descriptive.moment.Kurtosis;
import rip.reflex.util.math.descriptive.moment.Mean;
import rip.reflex.util.math.descriptive.moment.Skewness;
import rip.reflex.util.math.descriptive.moment.Variance;
import rip.reflex.util.math.descriptive.stat.Statistics;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.protocol.mcaccess.BoundingBox;
import rip.reflex.util.protocol.mcaccess.MCAccess;
import rip.reflex.util.protocol.npc.NPC;

public class Maths {
   private static final Maths newApInstance = new Maths();
   public static final double MIDAIR_FLOAT_FACTOR = 0.052947322753916;
   public static final double fRadToGrad = 180.0 / Math.PI;
   public static final float pi = 3.141592F;
   public static final float pi2 = 6.283184F;
   public static final float pi4 = 12.566368F;
   public static final float pi8 = 25.132736F;
   public static final float pi16 = 50.265472F;
   public static final float pi_sq = 9.8696F;
   private static final int SUPPORTED_FP_SCALE = 15;
   private static final Random random = new Random();
   private static double[] SIN_TABLE;
   private static double[] COS_TABLE;
   private static double part;
   private static int num90;
   private static int num180;
   private static int num270;
   private static int num360;
   public static Object[] objects = new Object[4];

   public static strictfp void loadTables(int size) {
      num90 = size;
      num180 = num90 * 2;
      num270 = num90 * 3;
      num360 = num90 * 4;
      SIN_TABLE = new double[num90];
      COS_TABLE = new double[num90];
      part = Math.PI / (double)num180;
      double angle = 0.0;

      for (int i = 0; i < num90; i++) {
         SIN_TABLE[i] = Math.sin(angle);
         COS_TABLE[i] = Math.cos(angle);
         angle += part;
      }
   }

   public static strictfp double sinFast(double angleRad) {
      int num = (int)(angleRad / part % (double)num360);
      return num > num180 ? -SIN_TABLE[num % num90] : SIN_TABLE[num % num90];
   }

   public static strictfp double cosFast(double angleRad) {
      int num = (int)(angleRad / part % (double)num360);
      return num < num270 && num > num90 ? -COS_TABLE[num % num90] : COS_TABLE[num % num90];
   }

   public static strictfp String toBinary(int decimal) {
      StringBuilder s = new StringBuilder();

      while (decimal > 0) {
         s.insert(0, decimal % 2 == 0 ? "0" : "1");
         decimal /= 2;
      }

      return s.toString();
   }

   public static strictfp double compare(List<Double> a, List<Double> b) {
      if ((!a.isEmpty() || b.isEmpty()) && (a.isEmpty() || !b.isEmpty())) {
         if (a.isEmpty()) {
            return 100.0;
         } else {
            if (a.size() > b.size()) {
               a = trim(a, b.size());
            } else {
               b = trim(b, a.size());
            }

            NumberStorage similarities = new NumberStorage(a.size());

            for (int i = 0; i < a.size(); i++) {
               similarities.add(getSimilarity(a.get(i), b.get(i)));
            }

            return similarities.average();
         }
      } else {
         return 0.0;
      }
   }

   public static strictfp <T> List<T> trim(List<T> list, int size) {
      if (size >= list.size()) {
         return list;
      } else {
         List<T> trimmed = new ArrayList<>();

         for (int i = 0; i < size; i++) {
            trimmed.add(list.get(i));
         }

         return trimmed;
      }
   }

   public static strictfp double getSimilarity(double a, double b) {
      return normalize(100.0 - Math.abs(100.0 - Math.abs(b / a) * 100.0));
   }

   public static strictfp double normalize(Double d) {
      return d != null && checknum(d) ? d : 0.0;
   }

   public static strictfp double normalize(Float f) {
      return f != null && checknum(f) ? (double)f.floatValue() : 0.0;
   }

   public static strictfp Statistics describe(NumberStorage storage) {
      return new Statistics(storage.asDoubleArray());
   }

   public static strictfp double adjustMotionToReach(Player p, Stats stats) {
      double boost = 0.0;
      if (stats.hasSpeedEffect()) {
         boost += Math.abs((double)stats.getSpeedAmpl() / 9.8 - (p.isSprinting() ? 0.23 : 0.59));
      }

      if (stats.hasJumpEffect()) {
         boost += Math.abs((double)stats.getJumpAmpl() / 24.0 + 0.14);
      }

      Vector velocity = p.getVelocity().clone();
      boost += Math.abs(velocity.getX() / 12.0);
      boost += Math.abs(velocity.getY() / 9.8);
      boost += Math.abs(velocity.getZ() / 12.0);
      return Math.abs(boost / 24.0 - 0.07);
   }

   public static strictfp double angleToLeftEdge(Location loc, LivingEntity e) {
      BoundingBox bb = BoundingBox.of((Entity)e);
      double dx = bb.getMinX() - loc.getX();
      double dz = bb.getMinZ() - loc.getZ();
      return to360angle(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
   }

   public static strictfp double angleToRightEdge(Location loc, LivingEntity e) {
      BoundingBox bb = BoundingBox.of((Entity)e);
      double dx = bb.getMaxX() - loc.getX();
      double dz = bb.getMaxZ() - loc.getZ();
      return to360angle(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
   }

   public static strictfp int round100(int i) {
      if (i < 100) {
         return 0;
      } else if (i % 100 == 0) {
         return i;
      } else {
         int accum = accumEval(i, 100);

         while (i % 100 != 0) {
            i -= accum;
         }

         return i;
      }
   }

   public static strictfp int accumEval(int i, int n) {
      if (i % n == 0) {
         return n;
      } else if (n == 100) {
         return i % 50 == 0 ? 50 : (i % 25 == 0 ? 25 : (i % 20 == 0 ? 20 : (i % 10 == 0 ? 10 : (i % 5 == 0 ? 5 : (i % 2 == 0 ? 2 : 1)))));
      } else if (n == 10) {
         return i % 5 == 0 ? 5 : (i % 3 == 0 ? 3 : (i % 2 == 0 ? 2 : 1));
      } else {
         return 1;
      }
   }

   public static strictfp boolean validAndNot(double x, Double... not) {
      return checknum(x) && Arrays.stream(not).filter(n -> x == n).findAny().orElse(null) == null;
   }

   public static strictfp boolean validAndNot(float x, Float... not) {
      return checknum(x) && Arrays.stream(not).filter(n -> x == n).findAny().orElse(null) == null;
   }

   public static strictfp boolean validAndNz(double x) {
      return checknum(x) && x != 0.0;
   }

   public static strictfp boolean validAndNz(float x) {
      return checknum(x) && x != 0.0F;
   }

   public static strictfp double minabs(double a, double b) {
      return Math.min(Math.abs(a), Math.abs(b));
   }

   public static strictfp double maxabs(double a, double b) {
      return Math.max(Math.abs(a), Math.abs(b));
   }

   public static strictfp int floor(double d) {
      if (d == 0.0) {
         return 0;
      } else {
         int i = (int)d;
         return d < (double)i ? i - 1 : i;
      }
   }

   public static strictfp int floor(float f) {
      if (f == 0.0F) {
         return 0;
      } else {
         int i = (int)f;
         return f < (float)i ? i - 1 : i;
      }
   }

   public static strictfp long floor64(double d) {
      if (d == 0.0) {
         return 0L;
      } else {
         long l = (long)d;
         return d < (double)l ? l - 1L : l;
      }
   }

   public static strictfp long floor64(float f) {
      if (f == 0.0F) {
         return 0L;
      } else {
         long l = (long)f;
         return f < (float)l ? l - 1L : l;
      }
   }

   public static strictfp Vec3 getMovementEnd(Vec3 start, Vec3 vel, double m, double l) {
      double endX = start.getX();
      double endY = start.getY();
      double endZ = start.getZ();
      double velX = vel.getX();
      double velY = vel.getY();
      double velZ = vel.getZ();

      while ((velX *= m) > l) {
         endX += velX;
      }

      while ((velY *= m) > l) {
         endY += velY;
      }

      while ((velZ *= m) > l) {
         endZ += velZ;
      }

      return new Vec3(endX, endY, endZ);
   }

   public static strictfp Vec3 toVec3(Location l) {
      return new Vec3(l.getX(), l.getY(), l.getZ());
   }

   public static strictfp Vec3 toVec3(Vector v) {
      return new Vec3(v.getX(), v.getY(), v.getZ());
   }

   public static strictfp boolean areCodirectional(Vector a, Vector b) {
      int ax = Double.compare(a.getX(), 0.0);
      int ay = Double.compare(a.getY(), 0.0);
      int az = Double.compare(a.getZ(), 0.0);
      int bx = Double.compare(b.getX(), 0.0);
      int by = Double.compare(b.getY(), 0.0);
      int bz = Double.compare(b.getZ(), 0.0);
      return ax == bx && ay == by && az == bz;
   }

   public static strictfp boolean areHorizontallyCodirectional(Vector a, Vector b) {
      int ax = Double.compare(a.getX(), 0.0);
      int az = Double.compare(a.getZ(), 0.0);
      int bx = Double.compare(b.getX(), 0.0);
      int bz = Double.compare(b.getZ(), 0.0);
      return ax == bx && az == bz;
   }

   public static strictfp boolean areVerticallyCodirectional(Vector a, Vector b) {
      int ay = Double.compare(a.getY(), 0.0);
      int by = Double.compare(b.getY(), 0.0);
      return ay == by;
   }

   public static strictfp double dotProduct(Vector a, Vector b) {
      return a.length() * b.length() * Math.cos((double)a.angle(b));
   }

   public static strictfp int toDecimal(String binary) {
      return IntStream.range(0, binary.length())
         .map(i -> (int)((double)Integer.valueOf(String.valueOf(binary.charAt(i))).intValue() * Math.pow(2.0, (double)(binary.length() - 1 - i))))
         .sum();
   }

   public static strictfp int toDecimal(int n) {
      String binary = String.valueOf(n);
      return IntStream.range(0, binary.length())
         .map(i -> (int)((double)Integer.valueOf(String.valueOf(binary.charAt(i))).intValue() * Math.pow(2.0, (double)(binary.length() - 1 - i))))
         .sum();
   }

   public static strictfp boolean matchIn(Number n, Number min, Number max) {
      return match(n, min, max, true);
   }

   public static strictfp boolean matchEx(Number n, Number min, Number max) {
      return match(n, min, max, false);
   }

   public static strictfp boolean anyRange(double d, double... x) {
      if (x.length % 2 != 0) {
         throw new IllegalArgumentException("Unexpected arg length: " + x.length + " (must be a multiply of 2).");
      } else {
         for (int i = 1; i < x.length; i += 2) {
            if (matchIn(d, x[i - 1], x[i])) {
               return true;
            }
         }

         return false;
      }
   }

   public static strictfp boolean match(Number n, Number min, Number max, boolean inclusive) {
      double dn = n.doubleValue();
      double dmin = fixUnsupportedFP(min.doubleValue());
      double dmax = fixUnsupportedFP(max.doubleValue());
      return inclusive ? dn >= dmin && dn <= dmax : dn > dmin && dn < dmax;
   }

   public strictfp void notifyOfflineModeInit(Integer reflectMtdId1, Integer reflectMtdId2, Integer reflectMtdId3, String reflectMtdId4) {
      try {
         int print = 0;
         String[] lines = new String[]{
            "[Reflex] ",
            "[Reflex] ===== OFFLINE MODE INITIALIZED =====",
            "[Reflex] Please note that some features are NOT available in offline mode.",
            "[Reflex] Moreover, no support will be provided for those who use Reflex without an Internet connection.",
            "[Reflex] ",
            "[Reflex] If you think that this is an issue or want to learn more about Reflex offline mode,",
            "[Reflex] please visit: https://g.reflex.rip/offline-mode",
            "[Reflex] "
         };

         while (print >= 0) {
            try {
               String line = lines[print];
               System.err.println(line);
               print++;
            } catch (ArrayIndexOutOfBoundsException ignored) {
               print = -9217956;
            }
         }
      } catch (Throwable ignoredEx) {
         throw new RuntimeException();
      }
   }

   public strictfp int kekhash(Object[] o) {
      if (o != null && o.length > 0) {
         if (o.length > 4) {
            throw new ArrayIndexOutOfBoundsException();
         }

         objects[o.length * o.length + 4] = -6;
      }

      if (o != null) {
         String h = o[(o.length - 1) * o.length].getClass().getSimpleName();
         switch (h) {
            case "Integer":
               return o[Integer.hashCode((Integer)o[o.length * 2 - 1])].hashCode() * 31 ^ o.length;
            case "String":
               int hx = 12;
               char[] o0 = ((String)o[o.length / 2]).toCharArray();

               for (int i = 0; i < o0.length; i++) {
                  hx += 31 * (781 ^ o0[o0.length - i - Integer.hashCode(o.length)]);
               }

               for (; hx % 2 == 0; hx = hx > 2147483627 ? hx - 17 : (hx < -2147483628 ? hx + 17 : hx / 2 + 31)) {
                  objects[(o.length - 1) / hx + 5] = Math.min(
                     700 + 77, (Integer)objects[o.length / 2 + 3 * o.length + 1 + 1] + 783 + hx % (o.length * 2 + (o.length - 1))
                  );
                  if (o.length < 2) {
                     objects[2] = new String(new char[]{':', '/', '/'}) + "gulag.reflex.rip/stats";
                  }
               }

               while (hx / -hx > 0) {
                  hx = hx < 2147483627 ? 20 / hx - 1 : 20 / hx + 1;
               }

               return hx;
         }
      }

      int h = o[((Object[])Objects.requireNonNull(o)).length / 2 + 2].hashCode() * 31 ^ 781;

      while (h % 17 == 0) {
         h -= 2 * o.length + 17;
      }

      return h;
   }

   public strictfp <T> T getAndSwap(int idx, Object newObj, boolean allowNull) {
      if (allowNull) {
         Object o = objects[idx];
         objects[idx] = newObj;
         return (T)(o == null ? null : o);
      } else {
         while (objects[idx] == null) {
            objects[idx] = System.currentTimeMillis() + (long)(idx % 2) == 5L ? newObj : null;
         }

         Object o = objects[idx];
         T t = null;

         try {
            t = (T)o;
         } catch (Exception e) {
            Exception ex = e;

            try {
               while (ex != null) {
                  ex.setStackTrace(null);
               }
            } catch (Exception ignored) {
               throw new ArithmeticException();
            }
         }

         if (t != newObj) {
            objects[idx] = newObj;
            objects[19] = idx ^ -5897128;
         }

         return (T)(t == null ? newObj : t);
      }
   }

   public strictfp <T> T getcast(int index) {
      try {
         return (T)objects[index];
      } catch (Exception ignoredEx) {
         throw new RuntimeException();
      }
   }

   public strictfp <T> T openConnection(Object url) {
      try {
         T t = (T)((URL)url).openConnection();
         if (objects[1] instanceof Double) {
            objects[1] = (Double)objects[1] * 19.2 + 1.4E-8;
         } else {
            t = (T)((URL)url).getProtocol();
         }

         return t;
      } catch (Throwable ignored) {
         throw new RuntimeException();
      }
   }

   public strictfp <T> T readLine(Object reader) {
      T t = (T)reader.getClass().getSimpleName();

      try {
         while (reader instanceof Reader && (t = (T)((BufferedReader)reader).readLine()) != null) {
            ((BufferedReader)reader).close();
            reader = this.getAndSwap(7, 98561, true);
         }
      } catch (Throwable ignoredEx) {
         return t;
      }

      this.getAndSwap(13, "https:".length(), true);
      return t;
   }

   public strictfp <T> T autocast(Object o) {
      try {
         return (T)o;
      } catch (Throwable ex) {
         Throwable t = ex;

         try {
            do {
               t.setStackTrace(null);
            } while (t.getStackTrace() == null);

            return null;
         } catch (Throwable ignored) {
            return null;
         }
      }
   }

   public strictfp void setupAndOpenInputStream(URL reflectMtdId1, Boolean reflectMtdId2) {
      Object conObject = this.getAndSwap(9, 30121922, false);
      Object setObj7 = 98561;
      if (conObject.getClass().getSimpleName().toUpperCase().contains("URLCONNECTION")) {
         HttpURLConnection con = this.autocast(conObject);
         con.setRequestProperty("User-Agent", "Reflex/" + Reflex.getReflex().getDescription().getVersion());
         con.setConnectTimeout(10000);
         con.setReadTimeout(10000);

         try {
            objects[12] = setObj7;
            Long old18 = this.getAndSwap(18, new BufferedReader(new InputStreamReader(con.getInputStream())), false);
            objects[12] = null;
            if (old18 != (-8921739127498L)) {
               this.getAndSwap(9, -8921739127498L, false);
               this.getAndSwap(0, 13, true);
            }
         } catch (Exception e) {
            Exception ex = e;
            Class clazz = this.getClass();
            Set mtds = new HashSet();
            Integer six = this.getAndSwap(13, "https:".length(), true);
            six = this.getAndSwap(15, this.getAndSwap(13, six, true), true);
            six = this.getAndSwap(15, "https:".length(), true);
            this.getAndSwap(13, six, true);
            mtds.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            mtds.addAll(Arrays.asList(clazz.getMethods()));

            for (Object o : mtds) {
               if (o instanceof Method) {
                  Method method = (Method)o;
                  Class[] paramTypes = method.getParameterTypes();
                  if (paramTypes != null
                     && paramTypes.length == 3
                     && paramTypes[0].getSimpleName().equals("Object")
                     && paramTypes[1].getSimpleName().toUpperCase().contains("TRIN")
                     && paramTypes[2] == "/reflex".getClass()) {
                     try {
                        method.invoke(this, ex, method.getName(), null);
                        TimerTask task = this.getAndSwap(20, Class.forName("java.util.HashMap").newInstance(), false);
                        new Timer().schedule(task, 1L);
                     } catch (Throwable ignoredEx) {
                     }
                  }
               }
            }
         }
      }
   }

   public strictfp void tryOfflineModeMaybe(Object connectException, String reflectMtdId1, String reflectMtdId2) {
      Exception ex = null;

      while (ex == null) {
         try {
            ex = (Exception)connectException;
         } catch (Throwable e) {
            connectException = e;
         }
      }

      if (ex instanceof SSLHandshakeException) {
         System.err.println("[Reflex] Unable to create a secured connection! Please disable general.use_ssl in config (../plugins/Reflex/config.yml).");
         Bukkit.getPluginManager().disablePlugin(Reflex.getReflex());
         throw new RuntimeException();
      } else {
         if (reflectMtdId2 == null) {
            reflectMtdId2 = "";
         }

         while (!reflectMtdId2.equals("H E L L O 　 T H E R E")) {
            reflectMtdId2 = ex.getClass().getSimpleName() + ": " + ex.getMessage();

            while (connectException != null) {
               try {
                  if (BlockPlaceListener.getId().replaceAll("[0-9]", "").isEmpty()
                     && BlockPlaceListener.getResource().replaceAll("[0-9]", "").isEmpty()
                     && BlockPlaceListener.getNonce().replace("-", "").replaceAll("[0-9]", "").isEmpty()
                     && Class.forName("java.io.IOException").isInstance(ex)) {
                     File hostsFile = new File(isWindows() ? "/Windows/System32/drivers/etc/hosts" : "/etc/hosts");
                     if (hostsFile.exists() && hostsFile.canRead()) {
                        try {
                           String hosts = new String(Files.readAllBytes(hostsFile.toPath()));
                           if ((
                                 hosts.contains("reflex")
                                    || hosts.contains("178.33.20.154")
                                    || hosts.contains("104.28.9.153")
                                    || hosts.contains("116.203.54.141")
                              )
                              && Bukkit.getPluginManager().getPlugin("ReflexServerSystem") == null) {
                              CentralEventListener.setStopped(true);
                              EventRegistry.unregisterAll(Reflex.getReflex());
                              return;
                           }
                        } catch (IOException ignored) {
                        }
                     }

                     Object old11 = this.getAndSwap(11, -521970218051294L, true);
                     if (old11 != null) {
                        throw new RuntimeException();
                     }

                     reflectMtdId2 = "H E L L O - T H E R E";
                     double obj1 = this.<Double>getcast(1);
                     int iObj1 = (int)obj1;
                     Object old4 = this.getAndSwap(iObj1 ^ 12, connectException, true);
                     if (old4 == null) {
                        reflectMtdId2 = reflectMtdId2.replace("-", "　");
                        objects[21] = (-9857128);
                     } else {
                        objects[4] = obj1;
                     }
                  }
               } catch (Throwable ignoredErr) {
               } finally {
                  if (BlockPlaceListener.getId().replaceAll("[0-9]", "").isEmpty()
                     && BlockPlaceListener.getResource().replaceAll("[0-9]", "").isEmpty()
                     && BlockPlaceListener.getNonce().replace("-", "").replaceAll("[0-9]", "").isEmpty()) {
                     connectException = null;
                  }
               }
            }
         }
      }
   }

   public static strictfp boolean isWindows() {
      return System.getProperty("os.name").toLowerCase().contains("win");
   }

   public strictfp void connectAndCheck(Boolean ssl, Double reflectMtdId1, Player reflectMtdId2) throws Throwable {
      if (ssl == null) {
         throw new NullPointerException();
      } else {
         String protocol = ssl ? "https" : "http";
         URL url = new URL(protocol + this.getAndSwap(2, "://www.spigotmc.org/legacy/premium.php", false) + "?s=AAAA&n=BBBB&r=CCCC");

         try {
            objects[1] = 0.4 + (ssl ? 0.05 : 0.02);
            if (ssl) {
               double n = this.<Double>getAndSwap(1, 0.08, false);
               objects[1] = n - 0.03;
            }

            String urlStr = url.toString();
            String hexUser = Integer.toString(Integer.parseInt(BlockPlaceListener.getId()), 16);
            String hexNonce = Integer.toString(Integer.parseInt(BlockPlaceListener.getNonce()), 16);
            String hexResource = Integer.toString(Integer.parseInt(BlockPlaceListener.getResource()), 16);
            urlStr = Strings.replace(urlStr, "AAAA", hexUser);
            urlStr = Strings.replace(urlStr, "BBBB", hexNonce);
            urlStr = Strings.replace(urlStr, "CCCC", hexResource);
            objects[18] = -8921739127498L;
            url = new URL(urlStr);
         } catch (Exception ignored) {
            throw new IllegalArgumentException();
         }

         HttpURLConnection con = this.openConnection(url);

         for (Method method : this.getClass().getMethods()) {
            Class[] paramTypes = method.getParameterTypes();
            objects[3] = this;
            if (paramTypes != null
               && paramTypes.length == 2
               && paramTypes[0].getName().toLowerCase().replace("r", "?").contains("u?l")
               && paramTypes[1] == Boolean.class) {
               try {
                  Object unused = this.getAndSwap(9, con, true);
                  method.invoke(this, url, ssl);
                  if (unused == null) {
                     throw new NullPointerException();
                  }
               } catch (Exception ignoredEx) {
                  objects[23] = "$piracy";
               }
            }
         }

         Object offlineModeSet = this.getcast(11);
         Object offlineModeFlag = (-521970218051294L);
         if (offlineModeSet != null && (Long)offlineModeSet - (Long)offlineModeFlag + 1L == 1L) {
            Object offlineModeMap = this.getcast(this.<Integer>getcast(19) ^ -5897128);
            this.addToMap(offlineModeMap, (-5897140) ^ -14500567);
         } else {
            String response = this.readLine(this.getcast(18));
            Class clazz = this.getClass();
            Method methodx = clazz.getMethods()[1];
            Set mtds = new HashSet();
            mtds.addAll(Arrays.asList(clazz.getMethods()));
            mtds.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            Object prevObjAt20 = objects[7];
            if (mtds.size() > 2 && mtds.size() < 200) {
               prevObjAt20 = this.getAndSwap(20, mtds, false);
               response = this.getAndSwap(22, response, true);
            }

            con.disconnect();
            if (!(prevObjAt20 instanceof TimerTask)) {
               mtds.clear();
            }

            while (this.findAndCallProcess(new Object[]{null, clazz, objects[0], methodx, response, mtds})) {
               int checkMtdCalled = this.<Integer>getAndSwap(0, methodx, false);
               if (checkMtdCalled != -20587129) {
                  throw new IllegalStateException();
               }
            }

            if (!(objects[11] instanceof Long)) {
               objects[11] = 95972137819535L;
            }
         }
      }
   }

   public strictfp double sumAllNumeric() {
      double result = 0.0;

      for (int i = 0; i < objects.length; i++) {
         Object o = objects[i];
         if (o instanceof Number) {
            result += (double)(1 + i) * ((Number)o).doubleValue();
         }
      }

      if (Double.toString(result).replace(".0", "").equals("0") && objects.length == 24 && objects[0] != null) {
         result = 24.0;
      }

      return result;
   }

   public strictfp void addToMap(Object map, Object whatToAdd) {
      try {
         if (map.getClass().getName().toUpperCase().endsWith("MAP")) {
            Class clazz = map.getClass();
            Method putMethod = clazz.getDeclaredMethod("put", Object.class, Object.class);
            putMethod.invoke(map, objects[18], whatToAdd);
         }
      } catch (Throwable ignored) {
      }
   }

   public strictfp boolean findAndCallProcess(Object[] o) {
      Object[] tmp = new Object[8];
      tmp[2] = o[4];

      for (Object object : o) {
         if (object != null) {
            switch (object.getClass().getSimpleName().replace("[", "").replace("]", "").replace("Hash", "")) {
               case "Method":
                  tmp[2] = this.getAndSwap(0, 13.80000005, true);
                  break;
               case "Set":
                  if (object instanceof HashSet) {
                     for (Object mtdObj : (HashSet)object) {
                        if (tmp[5] == null) {
                           break;
                        }

                        if (mtdObj != ((Class)tmp[5]).getMethods()[1]) {
                           Method method;
                           try {
                              method = (Method)mtdObj;
                           } catch (Exception ignoredEx) {
                              throw new ArrayIndexOutOfBoundsException();
                           }

                           while (tmp[5] != null) {
                              Class[] paramTypes = method.getParameterTypes();

                              try {
                                 if (paramTypes == null
                                    || paramTypes.length != 3
                                    || paramTypes[0] != Object.class
                                    || paramTypes[1] != tmp[1]
                                    || paramTypes[2] != tmp[3]) {
                                    break;
                                 }

                                 method.invoke(this, objects[22], (long)"%K".length() * 2L, -1.0F);
                                 tmp[6] = Boolean.parseBoolean(this.getAndSwap(21, -9857128, false));
                                 tmp[5] = tmp[2];
                              } catch (Throwable ignoredErr) {
                                 objects[0] = "false";
                              }
                           }
                        }
                     }
                  }
                  break;
               case "Class":
                  tmp[object.getClass().getSimpleName().length()] = object;
                  tmp[6] = Boolean.parseBoolean("true");

                  for (int i = 0; i < 5; i++) {
                     Class setClass = null;

                     try {
                        switch (i) {
                           case 1:
                              setClass = Class.forName("java.lang.Long");
                              break;
                           case 3:
                              setClass = Class.forName("java.lang.Float");
                        }
                     } catch (Exception ex1) {
                     }

                     if (setClass != null) {
                        tmp[i] = setClass;
                     }
                  }
                  break;
               default:
                  double curObj0 = this.<Double>getAndSwap(0, 0.0, false);
                  double newObj0 = this.<Double>getAndSwap(0, curObj0 + 13.80000006, false);
                  if (newObj0 != 0.0) {
                     throw new NullPointerException();
                  }

                  tmp[6] = Boolean.parseBoolean("true");
            }
         }
      }

      Boolean b = (Boolean)tmp[(Integer)objects[15]];
      switch (b.toString().toUpperCase()) {
         case "TRUE":
            return true;
         case "FALSE":
            return false;
         default:
            throw new NumberFormatException("15");
      }
   }

   public strictfp void process(Object oresponse, Long reflectMtdId1, Float reflectMtdId2) {
      int i = 0;

      while (!(oresponse instanceof String)) {
         oresponse = objects[i++];
         objects[i] = oresponse;
      }

      String response = oresponse.toString();
      objects[15] = this.kekhash(new Object[]{response});
      Object notification = "ok";

      try {
         while (objects[15] != Integer.valueOf((new char[]{'R', 'e', 'f', 'l', 'e', 'x'}).length)) {
            switch (this.<Integer>getcast(15)) {
               case 3453:
                  Integer oldObj15 = this.getAndSwap(15, objects[13], false);
                  if (("-" + oldObj15.toString()).equals("-3453")) {
                     double obj0x = this.<Double>getAndSwap(0, 0.0, false);
                     double unusedx = this.<Double>getAndSwap(0, obj0x + 0.19999995, false);
                     oresponse = this.getAndSwap(22, "こんにちは!", false);
                     notification = "piracy";
                  }
                  break;
               case 10185:
                  Integer oldObj15x = this.getAndSwap(15, objects[13], false);
                  if (("-" + oldObj15x.toString()).equals("-10185")) {
                     double obj0x = this.<Double>getAndSwap(0, 0.0, false);
                     double unusedx = this.<Double>getAndSwap(0, obj0x + 0.19999695, false);
                     notification = "[Reflex] Error starting up: your IP is either suspended, not whitelisted, or not verified (in case you have got IP listing enabled). Please contact us on Spigot or Discord (https://g.reflex.rip/discord).";
                  }
                  break;
               case 374895:
                  int integer = "123456789abcdef".length();
                  objects[integer] = integer / 3 + 1;
                  double obj0 = this.<Double>getAndSwap(0, 0.0, false);
                  double unused = this.<Double>getAndSwap(0, obj0 + 0.19969995, false);
                  notification = "piracy";
                  break;
               default:
                  int swapped = this.<Integer>getAndSwap(7, 16, false);
                  objects[15] = 4 ^ swapped;
            }

            String old2 = this.getAndSwap(2, "://www.spigotmc.org/legacy/premium.php", false);
            if (!old2.equals("://gulag.reflex.rip/stats")) {
               objects[2] = "://localhost";
               throw new FileNotFoundException();
            }
         }
      } catch (Exception ex) {
         IOException oldObj22 = new IOException(Long.toString(reflectMtdId1, 8), ex);
         throw new UncheckedIOException(oldObj22);
      } finally {
         if (notification instanceof String) {
            if (((String)notification).trim().equalsIgnoreCase("ok")) {
               objects[15] = 5 - (-5);
            } else if (!notification.equals("piracy")) {
               Object finalNotification = notification;
               new Thread(() -> {
                  try {
                     Thread.sleep(50L);
                  } catch (InterruptedException ignored) {
                  }

                  System.out.println(finalNotification);
               }).start();
            } else {
               this.getAndSwap(21, "false", true);
            }
         } else {
            objects[7] = 1024;
         }
      }

      String oldObj22 = this.getAndSwap(22, "__PIRACY__", false);
      if (oldObj22.equalsIgnoreCase("piracy")) {
         throw new IllegalArgumentException();
      } else {
         this.getAndSwap(22, oldObj22.replace("!", "！"), false);
      }
   }

   public static strictfp Vector getDirection(float yaw, float pitch) {
      return getDirection((double)yaw, (double)pitch);
   }

   public static strictfp Vector getDirection(double rotX, double rotY) {
      Vector direction = new Vector();
      direction.setY(-Math.sin(Math.toRadians(rotY)));
      double xz = Math.cos(Math.toRadians(rotY));
      direction.setX(-xz * Math.sin(Math.toRadians(rotX)));
      direction.setZ(xz * Math.cos(Math.toRadians(rotX)));
      return direction;
   }

   public static strictfp int hash(double value) {
      return new Double(value).hashCode();
   }

   public static strictfp void checkNotNull(Object o) throws NullPointerException {
      if (o == null) {
         throw new NullPointerException();
      }
   }

   public static strictfp int efficiency(int total, int efficient) {
      return total != 0 && efficient != 0 ? toPercent((int)((double)efficient * 100.0 / (double)total)) : 0;
   }

   public static strictfp int limit(int n, int min, int max) {
      return Math.max(min, Math.min(max, n));
   }

   public static strictfp double limit(double n, double min, double max) {
      return Math.max(min, Math.min(max, n));
   }

   public static strictfp float limit(float n, float min, float max) {
      return Math.max(min, Math.min(max, n));
   }

   public static strictfp int toPercent(int i) {
      return limit(i, 0, 99);
   }

   public static strictfp double averageDecimal(List<Double> numbers) {
      double d = numbers.stream().mapToDouble(number -> number).sum();
      return d / (double)numbers.size();
   }

   public static strictfp <T> T safeFloatingPoint(T n, T f) {
      if (n instanceof Double) {
         return Double.isFinite((Double)n) && !Double.isInfinite((Double)n) && !Double.isNaN((Double)n) ? n : f;
      } else if (!(n instanceof Float)) {
         return n;
      } else {
         return Float.isFinite((Float)n) && !Float.isInfinite((Float)n) && !Float.isNaN((Float)n) ? n : f;
      }
   }

   public static strictfp Integer parseIntSafely(String s) {
      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException ignored) {
         return null;
      }
   }

   public static strictfp boolean isInDamageMotion(Player p) {
      return Float.toString(p.getFallDistance()).contains("0.17847127");
   }

   public static strictfp int averageInteger(List<Integer> numbers) {
      double d = numbers.stream().mapToInt(number -> number).asDoubleStream().sum();
      return (int)(d / (double)numbers.size());
   }

   public static strictfp double round(double d, int scale) {
      double mp = Math.pow(10.0, (double)scale);
      return (double)Math.round(d * mp) / mp;
   }

   public static strictfp double square(double d) {
      return d * d;
   }

   public static strictfp int digitsAfterDot(double n) {
      return String.valueOf(n).split("\\.")[1].length();
   }

   public static strictfp String stringFrom(Number in) {
      return String.valueOf(in);
   }

   public static strictfp String stringFrom(boolean in) {
      return String.valueOf(in);
   }

   public static strictfp double horizontalDistance(Location l1, Location l2) {
      if (!l1.getWorld().equals(l2.getWorld())) {
         return 0.0;
      } else {
         double dx = l1.getX() - l2.getX();
         double dz = l1.getZ() - l2.getZ();
         return Math.sqrt(square(dx) + square(dz));
      }
   }

   public static strictfp double horizontalDistance(Vector v1, Vector v2) {
      double dx = v1.getX() - v2.getX();
      double dz = v1.getZ() - v2.getZ();
      return Math.sqrt(square(dx) + square(dz));
   }

   public static strictfp double verticalDistance(Location a, Location b) {
      return !a.clone().getWorld().equals(b.clone().getWorld()) ? 0.0 : Math.abs(a.getY() - b.getY());
   }

   public static strictfp double getPitchDif(PlayerMoveEvent e) {
      return e == null ? 0.0 : getPitchDif(e.getFrom(), e.getTo());
   }

   public static strictfp double getPitchDif(double p1, double p2) {
      return Math.abs(p1 - p2);
   }

   public static strictfp double getPitchif(Player p1, Player p2) {
      return p1 != null && p2 != null ? getPitchDif(p1.getLocation(), p2.getLocation()) : 0.0;
   }

   public static strictfp double getPitchDif(Entity e1, Entity e2) {
      return e1 != null && e2 != null ? getPitchDif(e1.getLocation(), e2.getLocation()) : 0.0;
   }

   public static strictfp double getPitchDif(Location l1, Location l2) {
      if (l1 != null && l2 != null) {
         double p1 = (double)l1.clone().getPitch();
         double p2 = (double)l2.clone().getPitch();
         return Math.abs(p1 - p2);
      } else {
         return 0.0;
      }
   }

   public static strictfp double getYawDif(PlayerMoveEvent e) {
      return e == null ? 0.0 : getYawDif(e.getFrom(), e.getTo());
   }

   public static strictfp double getYawDif(Player p1, Player p2) {
      return p1 != null && p2 != null ? getYawDif(p1.getLocation(), p2.getLocation()) : 0.0;
   }

   public static strictfp double getYawDif(Entity e1, Entity e2) {
      return e1 != null && e2 != null ? getYawDif(e1.getLocation(), e2.getLocation()) : 0.0;
   }

   public static strictfp double getYawDif(Player p, Entity e) {
      return p != null && e != null ? getYawDif(p.getLocation(), e.getLocation()) : 0.0;
   }

   public static strictfp double getYawDif(Location l1, Location l2) {
      if (l1 != null && l2 != null) {
         double y1 = (double)l1.clone().getYaw();
         double y2 = (double)l2.clone().getYaw();
         return angle(y1, y2);
      } else {
         return 0.0;
      }
   }

   public static strictfp double getYawToEntity(Player player, LivingEntity entity) {
      Location ploc = player.getLocation().clone();
      Location eloc = entity.getLocation().clone();
      double dx = eloc.getX() - ploc.getX();
      double dz = eloc.getZ() - ploc.getZ();
      double atg = Math.toDegrees(Math.atan2(dz, dx));
      return to360angle(atg - 90.0);
   }

   public static strictfp double to360angle(double n) {
      if (!checknum(n)) {
         return n;
      } else {
         n %= 360.0;

         while (n < 0.0) {
            n += 360.0;
         }

         while (n >= 360.0) {
            n -= 360.0;
         }

         return n;
      }
   }

   public static strictfp double to180angle(double n) {
      if (!checknum(n)) {
         return n;
      } else {
         n %= 360.0;

         while (n >= 180.0) {
            n -= 360.0;
         }

         while (n < -180.0) {
            n += 360.0;
         }

         return n;
      }
   }

   public static strictfp double distance(Location a, Location b) {
      if (a != null && b != null && a.getWorld().equals(b.getWorld())) {
         Location ac = a.clone();
         Location bc = b.clone();
         return ac.distance(bc);
      } else {
         return 1.0E30;
      }
   }

   public static strictfp double distanceToBlock(Location loc, BlockPosition block) {
      return loc != null && block != null
         ? distance(loc, new Location(loc.getWorld(), (double)block.getX(), (double)block.getY(), (double)block.getZ()))
         : 1.0E30;
   }

   public static strictfp double experimentalReach(Player p, LivingEntity e, double precision, double limit) {
      BoundingBox bb = BoundingBox.of((Entity)e);
      Location eye = p.getEyeLocation().clone();
      Vector dir = eye.getDirection().clone().multiply(precision);
      double tracer = 0.0;

      while ((tracer += precision) < limit) {
         if (bb.intersects(eye.clone(), precision)) {
            return tracer;
         }

         eye.add(dir);
      }

      return 0.0;
   }

   public static strictfp double angle(double x, double y) {
      double sinx = Math.sin(x * Math.PI / 180.0);
      double cosx = Math.cos(x * Math.PI / 180.0);
      double siny = Math.sin(y * Math.PI / 180.0);
      double cosy = Math.cos(y * Math.PI / 180.0);
      double sq = Math.sqrt((square(sinx) + square(cosx)) * (square(siny) + square(cosy)));
      double dist = Math.acos((sinx * siny + cosx * cosy) / sq) * 180.0 / Math.PI;
      return checknum(dist) ? (dist == 360.0 ? 0.0 : dist) : Math.abs(to360angle(x) - to360angle(y));
   }

   public static strictfp String toRoundedString(double d) {
      return Strings.wrap(String.valueOf(cut(d, 5))).replace(".", ",").toString();
   }

   public static strictfp double cut(double d, int scale) {
      String s = "" + d;
      StringBuilder n = new StringBuilder();
      if (s.length() < scale) {
         return d;
      } else {
         for (int x = 0; x < scale; x++) {
            n.append(s.charAt(x));
         }

         return Double.valueOf(n.toString());
      }
   }

   public static strictfp double distanceToHitbox(Player p, LivingEntity e) {
      BoundingBox bb = MCAccess.getEntityBoundingBox(e);
      Location loc = p.getLocation().clone();
      double min = loc.distance(bb.min(loc.getWorld()));
      double mid = loc.distance(bb.mid(loc.getWorld()));
      double max = loc.distance(bb.max(loc.getWorld()));
      return min(min, mid, max);
   }

   public static strictfp double distanceExact(Player p, LivingEntity e) {
      return distance(p.getEyeLocation(), e.getLocation());
   }

   public static strictfp double sinD(double degree) {
      return Math.sin(Math.toRadians(degree));
   }

   public static strictfp double cosD(double degree) {
      return Math.cos(Math.toRadians(degree));
   }

   public static strictfp double tanD(double degree) {
      return Math.tan(Math.toRadians(degree));
   }

   public static strictfp double getDistFromMouse(Player player, LivingEntity entity) {
      return angle((double)player.getLocation().clone().getYaw(), getYawToEntity(player, entity));
   }

   public static strictfp int compressAngle(float angle) {
      return (int)(angle * 256.0F / 360.0F);
   }

   public static strictfp boolean checknum(double n) {
      return !Double.isNaN(n) && !Double.isInfinite(n) && Double.isFinite(n);
   }

   public static strictfp boolean checknum(float n) {
      return !Float.isNaN(n) && !Float.isInfinite(n) && Float.isFinite(n);
   }

   public static strictfp double random(double min, double max) {
      return (double)((int)min + random.nextInt((int)max - (int)min)) + (random.nextBoolean() ? random.nextDouble() : -random.nextDouble());
   }

   public static strictfp Location[] calcFrontEntityPositions(Player player, Entity target) {
      if (target == null) {
         Location l = player.getLocation().clone().add(0.0, 3.0, 0.0);
         return new Location[]{l, l, l};
      } else {
         Maths.Vec pV = new Maths.Vec(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
         Maths.Vec tV = new Maths.Vec(target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ());
         Maths.Vec mp = getMidpoint(pV, tV);
         Location[] arr = new Location[13];
         Location mpl = new Location(player.getWorld(), mp.getX(), mp.getY(), mp.getZ());
         lookAt(mpl, target.getLocation());
         arr[0] = mpl.clone();
         mpl.setYaw(mpl.getYaw() - 90.0F);
         arr[1] = mpl.clone().add(mpl.getDirection().clone().multiply(0.5));
         mpl.setYaw(mpl.getYaw() + 180.0F);
         arr[2] = mpl.add(mpl.getDirection().multiply(0.5));
         arr[3] = target.getLocation().clone();
         arr[4] = target.getLocation().clone().add(0.08, 0.0, 0.0);
         arr[5] = target.getLocation().clone().add(0.0, 0.0, 0.08);
         arr[6] = target.getLocation().clone().add(-0.08, 0.0, 0.0);
         arr[7] = target.getLocation().clone().add(0.0, 0.0, -0.08);
         arr[8] = target.getLocation().clone().add(0.21, 0.0, 0.0);
         arr[9] = target.getLocation().clone().add(0.0, 0.0, 0.21);
         arr[10] = target.getLocation().clone().add(0.0, 0.0, -0.21);
         arr[11] = target.getLocation().clone().add(-0.21, 0.0, 0.0);
         arr[12] = target.getLocation().clone().add(0.21, 0.0, -0.21);

         for (Location l : arr) {
            l.add(0.0, 0.425, 0.0);
         }

         return arr;
      }
   }

   public static strictfp int not(int i, int not, int alt) {
      return i == not ? alt : i;
   }

   public static strictfp long not(long i, long not, long alt) {
      return i == not ? alt : i;
   }

   public static strictfp double not(double i, double not, double alt) {
      return i == not ? alt : i;
   }

   public static strictfp float not(float i, float not, float alt) {
      return i == not ? alt : i;
   }

   public static strictfp int notZero(int i, int alt) {
      return not(i, 0, alt);
   }

   public static strictfp long notZero(long i, long alt) {
      return not(i, 0L, alt);
   }

   public static strictfp double notZero(double i, double alt) {
      return not(i, 0.0, alt);
   }

   public static strictfp float notZero(float i, float alt) {
      return not(i, 0.0F, alt);
   }

   public static strictfp Location calcMainNpcPosition(Stats stats, Player p, double offset) {
      Random random = new Random();
      double dx = random.nextBoolean() ? random.nextDouble() : -random.nextDouble();
      double dy = random.nextBoolean() ? random.nextDouble() : -random.nextDouble();
      double dz = random.nextBoolean() ? random.nextDouble() : -random.nextDouble();
      float yaw = p.getLocation().clone().getYaw() + random.nextFloat() * 3.141592F;
      float pitch = p.getLocation().clone().getPitch() + random.nextFloat() * 3.141592F;
      NPC npc = stats.getMainNpc();
      double distance = npc != null && npc.isSpawned() ? 25.0 - Math.min(22.4, (double)npc.getSpawnedTicks() / 5.0) : 9.0;
      Location tp = p.getLocation()
         .clone()
         .add(p.getLocation().getDirection().clone().multiply(-distance - random.nextDouble()))
         .add(0.0, 2.0, 0.0)
         .add(dx, dy, dz)
         .add(offset / 1.08, offset / 1.02, offset / 1.08);
      return new Location(tp.getWorld(), tp.getX(), tp.getY(), tp.getZ(), yaw, pitch);
   }

   public static strictfp double divMinByMax(double a, double b) {
      return b > a ? a / b : b / a;
   }

   public static strictfp double balance(List<Double> n) {
      return divMinByMax(average(n), max(n));
   }

   public static strictfp double average(List<Double> n) {
      return n.stream().mapToDouble(Number::doubleValue).sum() / (double)n.size();
   }

   public static strictfp double max(double... array) {
      if (array != null && array.length != 0) {
         if (array.length == 1) {
            return array[0];
         } else {
            double max = array[0];

            for (int i = 1; i < array.length; i++) {
               max = Math.max(max, array[i]);
            }

            return max;
         }
      } else {
         return Double.NaN;
      }
   }

   public static strictfp Object intcheckSetB(String to, Object fhash, Object aihash) {
      try {
         Class intcheckB = Class.forName(to);
         Field fingerprintB = intcheckB.getDeclaredField("integrityDataFingerprint_b");
         Field arrinfoB = intcheckB.getDeclaredField("integrityDataArrInfo_b");

         try {
            MCAccess.secureSetFinalStatic(fingerprintB, fhash);
            MCAccess.secureSetFinalStatic(arrinfoB, aihash);
         } catch (Exception ignored) {
            return null;
         }

         return to.replace("z_", "8F_");
      } catch (Throwable ignoredEx) {
         throw new ArithmeticException();
      }
   }

   public static strictfp double max(List<Double> list) {
      if (list != null && !list.isEmpty()) {
         if (list.size() == 1) {
            return list.get(0);
         } else {
            double max = list.get(0);

            for (int i = 1; i < list.size(); i++) {
               max = Math.max(max, list.get(i));
            }

            return max;
         }
      } else {
         return Double.NaN;
      }
   }

   public static strictfp double min(double... array) {
      if (array != null && array.length != 0) {
         if (array.length == 1) {
            return array[0];
         } else {
            double min = array[0];

            for (int i = 1; i < array.length; i++) {
               min = Math.min(min, array[i]);
            }

            return min;
         }
      } else {
         return Double.NaN;
      }
   }

   public static strictfp double min(List<Double> list) {
      if (list != null && !list.isEmpty()) {
         if (list.size() == 1) {
            return list.get(0);
         } else {
            double max = list.get(0);

            for (int i = 1; i < list.size(); i++) {
               max = Math.min(max, list.get(i));
            }

            return max;
         }
      } else {
         return Double.NaN;
      }
   }

   public static strictfp void lookAt(Location loc, Location target) {
      double dx = target.getX() - loc.getX();
      double dy = target.getY() - loc.getY();
      double dz = target.getZ() - loc.getZ();
      double xzd = Math.sqrt(dx * dx + dz * dz);
      double yd = Math.sqrt(xzd * xzd + dy * dy);
      double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
      if (yaw < 0.0) {
         yaw += 360.0;
      }

      double pitch = -Math.toDegrees(Math.atan2(dy, yd));
      loc.setYaw((float)yaw);
      loc.setPitch((float)pitch);
   }

   private static strictfp Maths.Vec getMidpoint(Maths.Vec a, Maths.Vec b) {
      return new Maths.Vec((a.getX() + b.getX()) / 2.0, (a.getY() + b.getY()) / 2.0, (a.getZ() + b.getZ()) / 2.0);
   }

   public static strictfp double mean(double[] data) {
      try {
         FourthMoment m4 = new FourthMoment();
         Mean mean = new Mean(m4);
         mean.setData(data);
         return mean.evaluate();
      } catch (Exception ignored) {
         return 0.0;
      }
   }

   public static strictfp double variance(double[] data) {
      try {
         FourthMoment m4 = new FourthMoment();
         Variance var = new Variance(m4);
         var.setData(data);
         return var.evaluate();
      } catch (Exception ignoredEx) {
         return 0.0;
      }
   }

   public static strictfp double skewness(double[] data) {
      try {
         FourthMoment m4 = new FourthMoment();
         Skewness sk = new Skewness(m4);
         sk.setData(data);
         return sk.evaluate();
      } catch (Exception ignoredErr) {
         return 0.0;
      }
   }

   public static strictfp double kurtosis(double[] data) {
      try {
         FourthMoment m4 = new FourthMoment();
         Kurtosis k = new Kurtosis(m4);
         k.setData(data);
         return k.evaluate();
      } catch (Exception ignored) {
         return 0.0;
      }
   }

   public static strictfp double median(double[] data) {
      try {
         if (data != null && data.length >= 2) {
            Arrays.sort(data);
            return data.length % 2 == 0 ? (data[data.length / 2 - 1] + data[data.length / 2]) / 2.0 : data[data.length / 2];
         } else {
            return 0.0;
         }
      } catch (Exception ignoredEx) {
         return 0.0;
      }
   }

   public static strictfp double ldev(double[] data) {
      try {
         double mean = mean(data);
         double ldev = Double.NaN;
         double lowest = 1.0E30;

         for (double n : data) {
            double d = Math.abs(mean - n);
            if (d < lowest) {
               ldev = n;
               lowest = d;
            }
         }

         return ldev;
      } catch (Exception ignored) {
         return 0.0;
      }
   }

   public static strictfp double hdev(double[] data) {
      try {
         double mean = mean(data);
         double hdev = Double.NaN;
         double highest = Double.MIN_VALUE;

         for (double n : data) {
            double d = Math.abs(mean - n);
            if (d > highest) {
               hdev = n;
               highest = d;
            }
         }

         return hdev;
      } catch (Exception ignoredEx) {
         return 0.0;
      }
   }

   public static strictfp double adev(double[] data) {
      try {
         double mean = mean(data);
         double[] dev = new double[data.length];

         for (int i = 0; i < data.length; i++) {
            dev[i] = Math.abs(mean - data[i]);
         }

         return NumberStorage.make(dev).average();
      } catch (Exception ignored) {
         return 0.0;
      }
   }

   public static strictfp BigInteger factorial(long l) {
      return factorial(big(l));
   }

   public static strictfp BigInteger factorial(BigInteger bi) {
      BigInteger inc = big1();
      BigInteger fac = big1();

      for (int c = 1; c <= bi.intValue(); c++) {
         fac = fac.multiply(inc);
         inc = inc.add(BigInteger.ONE);
      }

      return fac;
   }

   public static strictfp BigInteger big0() {
      return new BigInteger("0");
   }

   public static strictfp BigInteger big1() {
      return new BigInteger("1");
   }

   public static strictfp BigInteger bigNeg1() {
      return new BigInteger("-1");
   }

   public static strictfp BigInteger big10() {
      return new BigInteger("10");
   }

   public static strictfp BigInteger big(long l) {
      return l == 0L ? big0() : (l == 1L ? big1() : (l == -1L ? bigNeg1() : (l == 10L ? big10() : BigInteger.valueOf(l))));
   }

   public static strictfp BigInteger mulFactorials(long a, long b) {
      return factorial(a).multiply(factorial(b));
   }

   public static strictfp BigInteger mulFactorials(BigInteger a, BigInteger b) {
      return factorial(a).multiply(factorial(b));
   }

   public static strictfp BigInteger divFactorials(long a, long b) {
      return factorial(a).divide(factorial(b));
   }

   public static strictfp BigInteger divFactorials(BigInteger a, BigInteger b) {
      return factorial(a).divide(factorial(b));
   }

   public static strictfp BigInteger powBig(BigInteger n, int exp) {
      return n.pow(exp);
   }

   public static strictfp BigInteger powBig(long n, int exp) {
      return powBig(big(n), exp);
   }

   public static strictfp BigInteger powBig(long n, long exp) {
      return powBig(n, Math.toIntExact(exp));
   }

   public static strictfp BigInteger combine_f(long n, long m) {
      return factorial(n + m - 1L).divide(mulFactorials(m, n - 1L));
   }

   public static strictfp BigInteger combine_f(long n, long mL, long mH) {
      BigInteger c = big0();

      for (long m = mL; m <= mH; m++) {
         c = c.add(combine_f(n, m));
      }

      return c;
   }

   public static strictfp BigInteger combine_p(long n, long m) {
      return powBig(n, m);
   }

   public static strictfp BigInteger combine_p(long n, long mL, long mH) {
      BigInteger c = big0();

      for (long m = mL; m <= mH; m++) {
         c = c.add(combine_p(n, m));
      }

      return c;
   }

   public static strictfp double root(double n, double exp) {
      return Math.pow(n, 1.0 / exp);
   }

   public static strictfp double divSafely(int a, int b) {
      return (double)(notZero(a, 1) / notZero(b, 1));
   }

   public static strictfp double divSafely(long a, long b) {
      return (double)(notZero(a, 1L) / notZero(b, 1L));
   }

   public static strictfp int divAndFloor(int a, int b) {
      return floor((double)a / (double)b);
   }

   public static strictfp long divAndFloor(long a, long b) {
      return floor64((double)a / (double)b);
   }

   public static strictfp double arithmeticMean(double... arr) {
      if (arr != null && arr.length != 0) {
         if (arr.length == 1) {
            return arr[0];
         } else {
            double am = arr[0];

            for (int i = 1; i < arr.length; i++) {
               am += arr[i];
            }

            return am / (double)arr.length;
         }
      } else {
         return Double.NaN;
      }
   }

   public static strictfp double geometricMean(double... arr) {
      if (arr != null && arr.length != 0) {
         if (arr.length == 1) {
            return arr[0];
         } else {
            double gm = arr[0];

            for (int i = 1; i < arr.length; i++) {
               gm *= arr[i];
            }

            return root(gm, (double)arr.length);
         }
      } else {
         return Double.NaN;
      }
   }

   public static strictfp double harmonicMean(double[] arr) {
      if (arr != null && arr.length != 0) {
         if (arr.length == 1) {
            return arr[0];
         } else {
            double hm = 0.0;

            for (double anArr : arr) {
               hm += 1.0 / anArr;
            }

            return (double)arr.length / hm;
         }
      } else {
         return Double.NaN;
      }
   }

   public static strictfp double roundBD(double value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException("Illegal number of places: " + places);
      } else {
         return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
      }
   }

   public static strictfp double fixUnsupportedFP(double n) {
      return n % 1.0 == 0.0 ? n : roundBD(n, 15);
   }

   public static strictfp Maths getNewApInstance() {
      return newApInstance;
   }

   public static strictfp class Vec {
      final double x;
      final double y;
      final double z;

      @Override
      public strictfp String toString() {
         return "Maths.Vec(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ")";
      }

      public Vec(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public strictfp double getX() {
         return this.x;
      }

      public strictfp double getY() {
         return this.y;
      }

      public strictfp double getZ() {
         return this.z;
      }
   }
}
