package rip.reflex.util.machinelearning.sync;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.Locale;
import rip.reflex.Reflex;
import rip.reflex.util.math.Maths;
import rip.reflex.util.misc.Files;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.net.http.GetRequest;
import rip.reflex.util.time.Timer;

public class FileSync implements DataSync {
   private final File localFile;
   private final File localSyncFile;
   private final String coreUrl;
   private final URL fileUrl;
   private final URL syncFileUrl;
   private String remoteHash;
   private String localHash;
   private boolean encryption = true;
   private final String dlFileName;

   public FileSync(File localDir, String url, String fn, boolean keepName) throws MalformedURLException {
      String nameHash = this.md5(fn.getBytes());
      this.dlFileName = keepName ? fn : nameHash + ".rflx";
      this.localFile = keepName ? Files.getFile(localDir, fn) : Files.getFile(localDir, nameHash + ".rflx");
      this.localSyncFile = keepName ? Files.getFile(localDir, fn + ".si.rflx") : Files.getFile(localDir, nameHash + ".si.rflx");
      this.fileUrl = newUrl(url + nameHash + "/data.xls");
      this.syncFileUrl = newUrl(url + nameHash + "/info.txt");
      this.coreUrl = url;
   }

   private static URL newUrl(String url) {
      return Reflex.getReflex().getURL(url);
   }

   @Override
   public void download() {
      if (this.needSync()) {
         try {
            this.download0();
         } catch (Exception ex) {
            throw new RuntimeException("Error while downloading", ex);
         }
      }
   }

   @Override
   public void loadLocal() {
      if (!this.localFile.exists()) {
         throw new RuntimeException("Local file doesn't exist");
      } else {
         try {
            this.useInstant(this.load(this.localFile));
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
   }

   @Override
   public int getVersion() {
      try {
         this.fetchLocalHash();
      } catch (IOException ex) {
         ex.printStackTrace();
      }

      if (!this.localHash.equals("null") && !this.localHash.equals("NO-HASH-IN-DB")) {
         final int[] version = new int[]{-88};
         GetRequest<InputStream> req = new GetRequest<InputStream>(
            this.coreUrl + "version/" + this.localHash, "Reflex File Sync/" + Reflex.getReflex().getDescription().getVersion()
         ) {
            @Override
            protected void read(InputStream rs) throws Exception {
               version[0] = Integer.parseInt(Misc.readFully(rs));
            }

            @Override
            protected InputStream getReadStream(InputStream is) {
               return is;
            }
         };
         req.setRead(true);
         req.execute();
         return version[0];
      } else {
         return -1;
      }
   }

   @Override
   public boolean needSync() {
      try {
         return this.needSync0();
      } catch (IOException ex) {
         throw new RuntimeException("Sync check error", ex);
      }
   }

   private void download0() throws Exception {
      Timer downloadBeginTracker = new Timer();
      URLConnection fileCon = this.connect(this.fileUrl);
      if (this.localFile.exists()) {
         this.localFile.delete();
      }

      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      this.redirectStream(fileCon.getInputStream(), bout, 512);
      byte[] data = bout.toByteArray();
      String hash = this.md5(data);
      if (!hash.equals(this.remoteHash)) {
         throw new RuntimeException("Error while downloading. Hashes don't match.");
      } else {
         this.useInstant(data);
         this.save(this.localFile, data);
         bout.close();
         java.nio.file.Files.write(this.localSyncFile.toPath(), this.remoteHash.getBytes());
         this.fetchLocalHash();
         if (Reflex.getReflex().getSetting("debug_downloads", false)) {
            long millisTaken = downloadBeginTracker.getTimePassed();
            int avgKbps = Maths.floor((double)data.length / 1024.0 / ((double)millisTaken / 1000.0));
            Reflex.getReflex()
               .info(
                  "Downloaded file "
                     + this.dlFileName
                     + " of size "
                     + data.length
                     + " in "
                     + millisTaken
                     + " ms. Average download speed: "
                     + avgKbps
                     + " kb/s. If your server is in China please write us these results --> https://g.reflex.rip/discord"
               );
         }
      }
   }

   protected void useInstant(byte[] data) {
   }

   protected void save(File localFile, byte[] data) throws Exception {
      java.nio.file.Files.write(localFile.toPath(), data);
   }

   protected byte[] load(File localFile) throws Exception {
      return java.nio.file.Files.readAllBytes(localFile.toPath());
   }

   private boolean needSync0() throws IOException {
      Timer checkBeginTracker = new Timer();
      if (this.remoteHash == null) {
         this.fetchRemoteHash();
      }

      if (this.localHash == null) {
         this.fetchLocalHash();
      }

      boolean needSync = !this.remoteHash.equals(this.localHash);
      if (Reflex.getReflex().getSetting("debug_downloads", false)) {
         long millisTaken = checkBeginTracker.getTimePassed();
         Reflex.getReflex()
            .info(
               "Fetched remote hash of file "
                  + this.dlFileName
                  + " in "
                  + millisTaken
                  + " ms. If your server is in China please write us these results --> https://g.reflex.rip/discord"
            );
      }

      return needSync;
   }

   private void fetchLocalHash() throws IOException {
      this.localHash = this.localSyncFile.exists() ? new String(java.nio.file.Files.readAllBytes(this.localSyncFile.toPath())) : "null";
   }

   private void fetchRemoteHash() throws IOException {
      URLConnection syncCon;
      try {
         syncCon = this.connect(this.syncFileUrl);
      } catch (Exception ex) {
         throw new RuntimeException("Cant get remote sync file", ex);
      }

      this.remoteHash = this.readAndClose(syncCon);
   }

   private void redirectStream(InputStream in, OutputStream out, int buffSize) throws IOException {
      byte[] buff = new byte[buffSize];

      int i;
      while ((i = in.read(buff)) > 0) {
         out.write(this.crypt(buff), 0, i);
      }
   }

   private String readAndClose(URLConnection con) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      return reader.readLine();
   }

   public byte[] crypt(byte[] data) {
      if (!this.encryption) {
         return data;
      } else {
         for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] ^ 38 ^ -18 ^ 109 ^ 92);
         }

         return data;
      }
   }

   public String md5(byte[] input) {
      try {
         return String.format(Locale.ROOT, "%032x", new BigInteger(1, MessageDigest.getInstance("MD5").digest(input)));
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   private URLConnection connect(URL url) throws IOException {
      URLConnection con = url.openConnection();
      con.setConnectTimeout(5000);
      con.setDoInput(true);
      con.setRequestProperty("User-Agent", "Reflex File Sync/" + Reflex.getReflex().getDescription().getVersion());
      return con;
   }

   @Override
   public String getLocalHash() {
      return this.localHash;
   }

   public void setEncryption(boolean encryption) {
      this.encryption = encryption;
   }

   public boolean isEncryption() {
      return this.encryption;
   }
}
