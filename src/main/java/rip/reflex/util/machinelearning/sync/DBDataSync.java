package rip.reflex.util.machinelearning.sync;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import rip.reflex.api.db.DataImpl;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.net.http.GetRequest;

public class DBDataSync implements DataSync {
   private final DataImpl impl;
   private final String coreUrl;
   private final URL fileUrl;
   private final URL syncFileUrl;
   private String remoteHash;
   private String localHash;
   private boolean encryption = true;

   public DBDataSync(DataImpl impl, String url, String fn) throws MalformedURLException {
      String nameHash = this.md5(fn.getBytes());
      this.fileUrl = newUrl(url + nameHash + "/data.xls");
      this.syncFileUrl = newUrl(url + nameHash + "/info.txt");
      this.impl = impl;
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
      if (!this.impl.contains()) {
         throw new RuntimeException("Data in mysql doesnt exist");
      } else {
         try {
            this.useInstant(this.load(this.impl));
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
   }

   @Override
   public boolean needSync() {
      try {
         return this.needSync0();
      } catch (IOException err) {
         throw new RuntimeException("Sync check error", err);
      }
   }

   private void download0() throws Exception {
      URLConnection fileCon = this.connect(this.fileUrl);
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      this.redirectStream(fileCon.getInputStream(), bout);
      byte[] data = bout.toByteArray();
      String hash = this.md5(data);
      if (!hash.equals(this.remoteHash)) {
         throw new RuntimeException("Error while downloading. Hashes don't match.");
      } else {
         this.useInstant(data);
         this.save(this.impl, data, this.remoteHash);
         bout.close();
         this.fetchLocalHash();
      }
   }

   protected void useInstant(byte[] data) {
   }

   protected void save(DataImpl impl, byte[] data, String hash) throws Exception {
      impl.saveData(hash, data);
   }

   protected byte[] load(DataImpl impl) throws Exception {
      return impl.getData();
   }

   private boolean needSync0() throws IOException {
      if (this.remoteHash == null) {
         this.fetchRemoteHash();
      }

      if (this.localHash == null) {
         this.fetchLocalHash();
      }

      return !this.remoteHash.equals(this.localHash);
   }

   private void fetchLocalHash() {
      this.localHash = this.impl.contains() ? this.impl.getHash() : "NO-HASH-IN-DB";
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

   private void redirectStream(InputStream in, OutputStream out) throws IOException {
      byte[] buff = new byte[4096];

      int i;
      while ((i = in.read(buff)) > 0) {
         out.write(this.crypt(buff), 0, i);
      }
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

   private String readAndClose(URLConnection con) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      return reader.readLine();
   }

   private URLConnection connect(URL url) throws IOException {
      URLConnection con = url.openConnection();
      con.setRequestProperty("User-Agent", "Reflex DBFile Sync/" + Reflex.getReflex().getDescription().getVersion());
      return con;
   }

   public String md5(byte[] input) {
      try {
         return String.format(Locale.ROOT, "%032x", new BigInteger(1, MessageDigest.getInstance("MD5").digest(input)));
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   @Override
   public int getVersion() {
      this.fetchLocalHash();
      if (!this.localHash.equals("null") && !this.localHash.equals("NO-HASH-IN-DB")) {
         final int[] version = new int[]{-88};
         GetRequest<InputStream> req = new GetRequest<InputStream>(
            this.coreUrl + "version/" + this.localHash, "Reflex File Sync/" + Reflex.getReflex().getDescription().getVersion()
         ) {
            @Override
            protected void read(InputStream rs) throws Exception {
               version[0] = Integer.valueOf(Misc.readFully(rs));
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
