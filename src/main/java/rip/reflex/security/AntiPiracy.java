package rip.reflex.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.net.ssl.SSLHandshakeException;
import org.bukkit.plugin.java.JavaPlugin;
import ove.crypto.digest.Blake2b;
import rip.reflex.Reflex;
import rip.reflex.java9.J9Util;
import rip.reflex.util.crypto.Encryptor;
import rip.reflex.util.location.Movement;

public class AntiPiracy {
   public static final Integer[] values = new Integer[5];
   private static final AntiPiracy instance = new AntiPiracy();
   private static final String id = Movement.getApinstance().getId();
   private static int unnec;
   private static String resource = Movement.getApinstance().getResource();
   private static final String[] ANTI_ZKM = new String[]{
      skid(-98579872139012093L), skid(287391092830802185L), skid(-48379851018592132L), skid(618128937059123829L)
   };
   private static final String BKT = ("\u0000bukkit"
         + skid(-48379851018592132L)
         + "plu\u0000gi"
         + "N".toLowerCase()
         + ANTI_ZKM[2]
         + ".".replace(".", "jav\u0000a.")
         + "JavaP"
         + "LU".toLowerCase()
         + "g\u0000"
         + "i n".replace(" ", ""))
      .replace("\u0000", "");

   public static int addAll(Integer[] values) {
      if (values == null) {
         return -1;
      } else {
         for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
               values[i] = 0;
            }
         }

         return Arrays.stream(values).mapToInt(value -> value).sum();
      }
   }

   public static void offlineMode(Exception e) {
      System.out.println("Unable to check for updates, initializing offline mode...");
      System.out.println("Please note that some features are only available in online mode due to connection requirements!");
      File f = new File(isWindows() ? "/Windows/System32/drivers/etc/hosts" : "/etc/hosts");
      if (f.exists() && f.canRead()) {
         try {
            String hosts = new String(Files.readAllBytes(f.toPath()));
            if (hosts.contains("reflex") || hosts.contains("178.33.20.154") || hosts.contains("104.28.9.153") || hosts.contains("116.203.54.141")) {
               throw new AntiPiracy.EmptyException("a");
            }
         } catch (IOException ignored) {
         }
      }

      System.out.println("Offline mode initialized.");
      System.out.println("If you believe your server shouldn't run Reflex in offline mode, please report the following at go.reflex.rip/bug:");
      System.out.println("" + Base64.getEncoder().encodeToString((e.getMessage() + ":" + e.getClass().getName()).getBytes()));
   }

   public static boolean isWindows() {
      return System.getProperty("os.name").toLowerCase().contains("win");
   }

   public static void f(String[] args) {
      getInstance().check();
   }

   public static List<String> splitAll(List<String> list) {
      Blake2b blake2b = Blake2b.Digest.newInstance();
      return list.stream().flatMap(s -> Arrays.stream(s.split(" "))).map(s -> {
         try {
            return toHex(blake2b.digest(s.getBytes("UTF-8")));
         } catch (UnsupportedEncodingException ignored) {
            return null;
         }
      }).collect(Collectors.toList());
   }

   public static String toHex(byte[] b) {
      return String.format("%032x", new BigInteger(1, b));
   }

   public <T> T autoCast(Object o) {
      return (T)o;
   }

   public URL fetchUrl() throws MalformedURLException {
      return AccessChecker.findWorkingURL();
   }

   public URLConnection openCon(URL url) throws IOException {
      return url.openConnection();
   }

   public AntiPiracy.IFReader getReader(AntiPiracy.IFStreamReader inReader) {
      return new AntiPiracy.IFReader(inReader);
   }

   public URLConnection getCon(Object con) {
      return this.autoCast(con);
   }

   public BufferedReader getReader(Object reader) {
      return this.autoCast(reader);
   }

   public void valIntoArray(Object[] array) {
      array[4] = values;
   }

   public Integer[] toIntArr(Object o) {
      return this.autoCast(o);
   }

   public Class<?> getJavaPluginClass(String b) throws Exception {
      return Class.forName(ANTI_ZKM[0] + ANTI_ZKM[1] + ANTI_ZKM[3] + ANTI_ZKM[2] + BKT + (Objects.equals(b, ".class") ? "" : b));
   }

   private static String skid(long l) {
      return l == 287391092830802185L ? "r" : (l == -98579872139012093L ? "o" : (l == -48379851018592132L ? "." : "g"));
   }

   public Field getFileField(Class<?> c, long l, long l2) throws Exception {
      return Arrays.stream(c.getDeclaredFields())
         .filter(f -> f.getName().equals("file") && l != 7686768769078979867L && l2 == 7675637626378268742L)
         .findFirst()
         .orElse(JavaPlugin.class.getDeclaredField("dataFolder"));
   }

   public Object access(Object o) {
      if (o instanceof Field) {
         ((Field)o).setAccessible(true);
      } else if (o instanceof Class) {
         return ((Class)o).getClass().getDeclaredFields()[31 * ((Class)o).getName().length()];
      }

      return o;
   }

   public Field asField(Object o) throws Exception {
      try {
         unnec = Integer.parseInt(resource);
         unnec = Integer.parseInt(id);
      } catch (Exception ignored) {
         Reflex.getReflex()
            .warn(
               new String(
                  Encryptor.decrypt(
                     Base64.getDecoder()
                        .decode(
                           "S9pP7NjUqMETN8zN0geLn3s1Jj9tnflxA739hzWeKnEwAY6stxBxOeyHdoYOUKFcUVKCRuWrMmdcwTeAsK7Af60X/ZXROJVSQFYLXetHSe5l/UV/4lY/Zvf/g/R9TAqcCkrdOvc6PbDufEKuXyxcYswtjSdWCz7tKNc1xNf6/Ho="
                        )
                  )
               )
            );
         throw new AntiPiracy.EmptyException("b");
      }

      return (Field)o;
   }

   public JavaPlugin getReflex() {
      return Reflex.getReflex();
   }

   public File castToFile(Object o) throws Exception {
      try {
         unnec = Integer.parseInt(resource);
         unnec = Integer.parseInt(id);
      } catch (Exception ignored) {
         Reflex.getReflex()
            .warn(
               new String(
                  Encryptor.decrypt(
                     Base64.getDecoder()
                        .decode(
                           "S9pP7NjUqMETN8zN0geLn3s1Jj9tnflxA739hzWeKnEwAY6stxBxOeyHdoYOUKFcUVKCRuWrMmdcwTeAsK7Af60X/ZXROJVSQFYLXetHSe5l/UV/4lY/Zvf/g/R9TAqcCkrdOvc6PbDufEKuXyxcYswtjSdWCz7tKNc1xNf6/Ho="
                        )
                  )
               )
            );
         throw new AntiPiracy.EmptyException("b");
      }

      return (File)o;
   }

   public Path toPath(Object o) {
      return ((File)o).toPath();
   }

   public Object getMD5() throws NoSuchAlgorithmException {
      return MessageDigest.getInstance("MD5");
   }

   public Object getBlake2() {
      return Blake2b.Digest.newInstance();
   }

   public Blake2b asBlake(Object o) {
      return this.autoCast(o);
   }

   public byte[] asByteArray(Object b) throws Exception {
      try {
         unnec = Integer.parseInt(resource);
         unnec = Integer.parseInt(id);
      } catch (Exception ignored) {
         Reflex.getReflex()
            .warn(
               new String(
                  Encryptor.decrypt(
                     Base64.getDecoder()
                        .decode(
                           "S9pP7NjUqMETN8zN0geLn3s1Jj9tnflxA739hzWeKnEwAY6stxBxOeyHdoYOUKFcUVKCRuWrMmdcwTeAsK7Af60X/ZXROJVSQFYLXetHSe5l/UV/4lY/Zvf/g/R9TAqcCkrdOvc6PbDufEKuXyxcYswtjSdWCz7tKNc1xNf6/Ho="
                        )
                  )
               )
            );
         throw new AntiPiracy.EmptyException("b");
      }

      return (byte[])b;
   }

   public ZipInputStream asZiPStream(Object o) {
      return (ZipInputStream)o;
   }

   public boolean toBool(Object o) {
      return (Boolean)o;
   }

   public ZipEntry asZipEntry(Object o) throws Exception {
      try {
         unnec = Integer.parseInt(resource);
         unnec = Integer.parseInt(id);
      } catch (Exception ignored) {
         Reflex.getReflex()
            .warn(
               new String(
                  Encryptor.decrypt(
                     Base64.getDecoder()
                        .decode(
                           "S9pP7NjUqMETN8zN0geLn3s1Jj9tnflxA739hzWeKnEwAY6stxBxOeyHdoYOUKFcUVKCRuWrMmdcwTeAsK7Af60X/ZXROJVSQFYLXetHSe5l/UV/4lY/Zvf/g/R9TAqcCkrdOvc6PbDufEKuXyxcYswtjSdWCz7tKNc1xNf6/Ho="
                        )
                  )
               )
            );
         throw new AntiPiracy.EmptyException("b");
      }

      return (ZipEntry)o;
   }

   public int toInt(Object o) {
      return (Integer)o;
   }

   public Object openZipStream(Path p) throws FileNotFoundException {
      FileInputStream fileIn = new FileInputStream(p.toFile().toPath().toFile());
      return new ZipInputStream(fileIn);
   }

   public Object getConstantPool() throws Exception {
      Method getConstantPool = Class.class.getDeclaredMethod("getConstantPool");
      J9Util.setAccessible(getConstantPool);
      return getConstantPool.invoke(Class.forName("rip.reflex.Reflex"));
   }

   public void loop(boolean cond, Object[] array) throws Exception {
      if (cond) {
         if (!this.asZipEntry(array[13]).getName().endsWith(".class")) {
            this.loop((array[13] = this.asZiPStream(array[10]).getNextEntry()) != null, array);
         }

         while (this.toInt(array[12] = this.asZiPStream(array[10]).read(this.asByteArray(array[11]))) > 0) {
            this.asBlake(array[8]).update(this.asByteArray(array[11]), 0, this.toInt(array[12]));
         }

         array[16] = Arrays.asList(
            "8054e1059ec2cb401c3b6c002da952ad1b40902e0a1eade1fbfff9cdaee0fad4dac631c2b0d5ef661d0b08c0b78d355777714cda0abb738cf358b710a449993a",
            "2a54f714f676d0573c768762a43ea766425ac62e1ec726224f5f461a78569820a90645819a352089d48ed74fe2976a79bcfcaaf66ed3802d125efca7a62ddf9c",
            "faf847aba1356f1f95dce0dd17cc8eb51a1a93a217fc28aeb2c95a82daa682cd2de35094a7961e5ea80898514260e9cf831d20dd3fdbc64569a87d6f9d46d398",
            "cd96093db2d550b0b396007d7166699f202b3b551d0ce30bdfc006c5f630aecb0b0fb7d995811c6cca4b1ffaa6452cb20ce4e969e8e1ffb7b9f3033e6dd0c531",
            "729a84c6fe38173eb4b915d442c35eeb7dd2747317ec6b3ab591d8ab49ea9c4d4c6a7b52c6c6b1360f1f3f3764be7c70acec2fee1d456ffc39be35fd720bb7ab"
         );
         this.asZiPStream(array[10]).closeEntry();
         this.loop((array[13] = this.asZiPStream(array[10]).getNextEntry()) != null, array);
      }
   }

   public List<String> newArrayList() {
      return new ArrayList<>();
   }

   public void addTo(Object o, Object o2) {
      ((List)o).add(o2.toString());
   }

   public List<String> getList(Object o) {
      return this.autoCast(o);
   }

   public void validateId(int i) {
      if (i < 1 || i > 1000000) {
         throw new AntiPiracy.EmptyException("c");
      }
   }

   public void check() {
      try {
         Object[] array = new Object[20];
         this.valIntoArray(array);
         array[19] = new String(
            !J9Util.isJava9()
               ? Encryptor.decrypt(Base64.getDecoder().decode("ezaah/loURZhupO9QNO2WFIJ+8vT8y63ZX429F5ACzM="))
               : Encryptor.decrypt(Base64.getDecoder().decode("Us2BHa5tRa0EcnWMO3MqvuvLiSY5+1LZeJjgKnAyUFXZ/O2474V9ugea+rRHjKSr"))
         );
         array[5] = this.access(this.getFileField(this.getJavaPluginClass(".class"), 7686768769079879867L, 7675637626378268742L));
         array[8] = this.getBlake2();
         array[17] = Class.forName(String.class.cast(array[19]));
         array[14] = this.getConstantPool();
         this.toIntArr(array[4])[0] = 101892;
         array[15] = this.newArrayList();
         array[18] = Class.class.cast(array[17]).getDeclaredMethod("getSize");
         J9Util.setAccessible((AccessibleObject)array[18]);
         array[0] = this.autoCast(this.fetchUrl());
         if (!array[14].getClass().equals(Class.class.cast(array[17]))) {
            resource = "a";
         }

         for (int j = 0; j < (Integer)Method.class.cast(array[18]).invoke(array[14]); j++) {
            if (Reflex.getReflex().getLogger().getName().equals("NCP")) {
               return;
            }

            try {
               Method getClassAt = Class.class.cast(array[17]).getDeclaredMethod("getClassAt", int.class);
               if (Reflex.getReflex().getLogger().getName().equals("AAC")) {
                  return;
               }

               J9Util.setAccessible(getClassAt);
               this.addTo(array[15], getClassAt.invoke(array[14], j));
            } catch (Exception ignored) {
               try {
                  Method getMethodAt = Class.class.cast(array[17]).getDeclaredMethod("getMethodAt", int.class);
                  if (Reflex.getReflex().getLogger().getName().equals("SPARTAN")) {
                     return;
                  }

                  J9Util.setAccessible(getMethodAt);
                  this.addTo(array[15], getMethodAt.invoke(array[14], j));
               } catch (Exception ignoredEx) {
                  try {
                     Method getFieldAt = Class.class.cast(array[17]).getDeclaredMethod("getFieldAt", int.class);
                     if (Reflex.getReflex().getLogger().getName().equals("ANTIAURA")) {
                        return;
                     }

                     J9Util.setAccessible(getFieldAt);
                     this.addTo(array[15], getFieldAt.invoke(array[14], j));
                  } catch (Exception ignoredErr) {
                  }
               }
            }
         }

         try {
            unnec = Integer.parseInt(resource);
            unnec = Integer.parseInt(id);
         } catch (Exception ex1) {
            Reflex.getReflex()
               .warn(
                  new String(
                     Encryptor.decrypt(
                        Base64.getDecoder()
                           .decode(
                              "S9pP7NjUqMETN8zN0geLn3s1Jj9tnflxA739hzWeKnEwAY6stxBxOeyHdoYOUKFcUVKCRuWrMmdcwTeAsK7Af60X/ZXROJVSQFYLXetHSe5l/UV/4lY/Zvf/g/R9TAqcCkrdOvc6PbDufEKuXyxcYswtjSdWCz7tKNc1xNf6/Ho="
                           )
                     )
                  )
               );
            throw new AntiPiracy.EmptyException("b");
         }

         array[6] = this.castToFile(this.asField(array[5]).get(this.getReflex()));
         this.toIntArr(array[4])[1] = 173 ^ this.toIntArr(array[4])[0];
         array[10] = this.openZipStream(this.toPath(array[6]));
         array[11] = Array.newInstance(byte.class, 1024);
         array[15] = splitAll(this.getList(array[15]));
         this.loop((array[13] = this.asZiPStream(array[10]).getNextEntry()) != null, array);
         this.asZiPStream(array[10]).close();

         for (String blockedConstant : (List<String>)array[16]) {
            if (this.getList(array[15]).contains(blockedConstant)) {
               resource = "f";
            }
         }

         array[7] = this.asBlake(array[8]).digest();

         try {
            unnec = Integer.parseInt(resource);
            unnec = Integer.parseInt(id);
         } catch (Exception ex2) {
            Reflex.getReflex()
               .warn(
                  new String(
                     Encryptor.decrypt(
                        Base64.getDecoder()
                           .decode(
                              "S9pP7NjUqMETN8zN0geLn3s1Jj9tnflxA739hzWeKnEwAY6stxBxOeyHdoYOUKFcUVKCRuWrMmdcwTeAsK7Af60X/ZXROJVSQFYLXetHSe5l/UV/4lY/Zvf/g/R9TAqcCkrdOvc6PbDufEKuXyxcYswtjSdWCz7tKNc1xNf6/Ho="
                           )
                     )
                  )
               );
            throw new AntiPiracy.EmptyException("b");
         }

         array[1] = this.openCon(this.autoCast(array[0]));
         array[7] = String.format("%032x", new BigInteger(1, this.asByteArray(array[7])));
         this.getCon(array[1]).setRequestProperty("User-Agent", "Reflex Antipiracy/" + Reflex.getReflex().getDescription().getVersion());
         this.toIntArr(array[4])[2] = 380 ^ this.toIntArr(array[4])[1];
         array[2] = this.getReader(new AntiPiracy.IFStreamReader(this.getCon(array[1]).getInputStream()));

         try {
            unnec = Integer.parseInt(resource);
            this.validateId(unnec);
            unnec = Integer.parseInt(id);
            this.validateId(unnec);
         } catch (Exception ex3) {
            Reflex.getReflex()
               .warn(
                  new String(
                     Encryptor.decrypt(
                        Base64.getDecoder()
                           .decode(
                              "S9pP7NjUqMETN8zN0geLn3s1Jj9tnflxA739hzWeKnEwAY6stxBxOeyHdoYOUKFcUVKCRuWrMmdcwTeAsK7Af60X/ZXROJVSQFYLXetHSe5l/UV/4lY/Zvf/g/R9TAqcCkrdOvc6PbDufEKuXyxcYswtjSdWCz7tKNc1xNf6/Ho="
                           )
                     )
                  )
               );
            throw new AntiPiracy.EmptyException("b");
         }

         this.asBlake(array[8]).reset();

         while ((array[3] = this.getReader(array[2]).readLine()) != null) {
            this.toIntArr(array[4])[3] = this.toIntArr(array[4])[1] ^ 1384;
            if (this.toBool(
               array[9] = this.getList(array[15]).contains(array[3])
                  || array[3].equals(toHex(this.asBlake(array[8]).digest(id.getBytes("UTF-8"))))
                  || array[7].equals(array[3])
            )) {
               this.toIntArr(array[4])[4] = this.toIntArr(array[4])[3] * 2823 ^ 38;
               Reflex.getReflex()
                  .warn(
                     new String(
                        Encryptor.decrypt(
                           Base64.getDecoder()
                              .decode(
                                 "S9pP7NjUqMETN8zN0geLn3s1Jj9tnflxA739hzWeKnEwAY6stxBxOeyHdoYOUKFcUVKCRuWrMmdcwTeAsK7Af60X/ZXROJVSQFYLXetHSe5l/UV/4lY/Zvf/g/R9TAqcCkrdOvc6PbDufEKuXyxcYswtjSdWCz7tKNc1xNf6/Ho="
                              )
                        )
                     )
                  );
               throw new AntiPiracy.EmptyException("d");
            }
         }

         this.toIntArr(array[4])[4] = this.toIntArr(array[4])[3] ^ 193;
         this.getReader(array[2]).close();
      } catch (AntiPiracy.EmptyException ex) {
         throw new AntiPiracy.EmptyException("?", ex);
      } catch (Exception err) {
         if (err instanceof SSLHandshakeException) {
            System.out.println("Unable to create a secured connection, please disable this feature in config (general.use_ssl)");
            throw new AntiPiracy.EmptyException("e");
         }

         this.toIntArr(values)[0] = 101892;
         this.toIntArr(values)[1] = 173 ^ this.toIntArr(values)[0];
         this.toIntArr(values)[2] = 380 ^ this.toIntArr(values)[1];
         this.toIntArr(values)[3] = this.toIntArr(values)[1] ^ 1384;
         this.toIntArr(values)[4] = this.toIntArr(values)[3] ^ 193;
         offlineMode(err);
      }
   }

   public static AntiPiracy getInstance() {
      return instance;
   }

   public static String getResource() {
      return resource;
   }

   private static class EmptyException extends RuntimeException {
      public EmptyException(String message) {
         super(message);
      }

      public EmptyException(String message, Throwable cause) {
         super(message, cause);
      }

      @Override
      public StackTraceElement[] getStackTrace() {
         return new StackTraceElement[0];
      }

      @Override
      public synchronized Throwable fillInStackTrace() {
         return null;
      }
   }

   private class IFReader extends BufferedReader {
      public IFReader(Reader in) {
         super(in);
      }
   }

   private class IFStreamReader extends InputStreamReader {
      public IFStreamReader(InputStream in) {
         super(in);
      }
   }
}
