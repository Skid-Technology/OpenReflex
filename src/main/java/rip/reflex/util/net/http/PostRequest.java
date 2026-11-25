package rip.reflex.util.net.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import rip.reflex.Reflex;
import rip.reflex.util.performance.MethodProfiler;

public abstract class PostRequest<WS extends OutputStream, RS extends InputStream> extends HttpRequest<RS, WS> {
   private final String url;
   private final String userAgent;
   private boolean autoSSL = false;

   @Override
   protected void execute() {
      try {
         this.post0();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public void post() {
      this.execute();
   }

   private void post0() throws Exception {
      MethodProfiler mp = new MethodProfiler("TB-498127391");
      URL url = this.autoSSL ? Reflex.getReflex().getURL(this.url) : new URL(this.url);
      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("User-Agent", this.userAgent);
      if (this.isWrite()) {
         MethodProfiler mpWrite = new MethodProfiler("TB-7490214");
         con.setDoOutput(true);
         WS ws = this.getWriteStream(con.getOutputStream());
         this.write(ws);
         ws.close();
         mpWrite.addToTimings();
      }

      if (this.isRead()) {
         MethodProfiler mpRead = new MethodProfiler("TB-62197391798");
         if (!this.isWrite()) {
            con.setDoInput(true);
         }

         RS rs = this.getReadStream(con.getInputStream());
         this.read(rs);
         rs.close();
         mpRead.addToTimings();
      }

      con.disconnect();
      mp.addToTimings();
   }

   public PostRequest(String url, String userAgent) {
      this.url = url;
      this.userAgent = userAgent;
   }

   public void setAutoSSL(boolean autoSSL) {
      this.autoSSL = autoSSL;
   }
}
