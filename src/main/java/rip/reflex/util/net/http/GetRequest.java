package rip.reflex.util.net.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import rip.reflex.Reflex;

public abstract class GetRequest<RS extends InputStream> extends HttpRequest<RS, OutputStream> {
   private final String url;
   private final String userAgent;

   @Override
   public void execute() {
      try {
         this.get0();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   private void get0() throws Exception {
      URL url = Reflex.getReflex().getURL(this.url);
      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("User-Agent", this.userAgent);
      if (this.isRead()) {
         con.setDoInput(true);
         RS rs = this.getReadStream(con.getInputStream());
         this.read(rs);
         rs.close();
      }

      con.disconnect();
   }

   @Override
   public void setWrite(boolean write) {
      throw new RuntimeException("Write is not supported in GET requests.");
   }

   @Override
   protected void write(OutputStream writeStream) throws Exception {
   }

   @Override
   protected OutputStream getWriteStream(OutputStream os) throws Exception {
      return null;
   }

   public GetRequest(String url, String userAgent) {
      this.url = url;
      this.userAgent = userAgent;
   }
}
