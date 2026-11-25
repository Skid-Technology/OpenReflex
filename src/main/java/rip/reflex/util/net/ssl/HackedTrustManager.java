package rip.reflex.util.net.ssl;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class HackedTrustManager implements X509TrustManager {
   private static final HackedTrustManager instance = new HackedTrustManager();
   private X509TrustManager orig;
   private SSLSocketFactory defFactory;
   private boolean enabled = false;

   public static HackedTrustManager get() {
      return instance;
   }

   @Override
   public void checkClientTrusted(X509Certificate[] certs, String s) throws CertificateException {
      if (!this.enabled) {
         if (this.orig != null) {
            this.orig.checkClientTrusted(certs, s);
         }
      }
   }

   @Override
   public void checkServerTrusted(X509Certificate[] certs, String s) throws CertificateException {
      if (!this.enabled) {
         if (this.orig != null) {
            this.orig.checkServerTrusted(certs, s);
         }
      }
   }

   @Override
   public X509Certificate[] getAcceptedIssuers() {
      if (this.enabled) {
         return null;
      } else {
         return this.orig == null ? null : this.orig.getAcceptedIssuers();
      }
   }

   public void inject() {
      this.orig();
      System.setProperty("https.protocols", "TLSv1.1,TLSv1.2,SSLv3");

      try {
         SSLContext sc = SSLContext.getInstance("SSL");
         sc.init(null, new TrustManager[]{this}, SecureRandom.getInstanceStrong());
         this.defFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      } catch (Exception ex) {
         ex.printStackTrace();
      }

      this.enabled = true;
   }

   public void uninject() {
      this.enabled = false;
      HttpsURLConnection.setDefaultSSLSocketFactory(this.defFactory);
   }

   private void orig() {
      try {
         TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
         tmf.init((KeyStore)null);
         this.orig = (X509TrustManager)Arrays.stream(tmf.getTrustManagers()).filter(tm -> tm instanceof X509TrustManager).findFirst().orElse(null);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isEnabled() {
      return this.enabled;
   }
}
