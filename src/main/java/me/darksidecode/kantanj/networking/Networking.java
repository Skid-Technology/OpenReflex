package me.darksidecode.kantanj.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import me.darksidecode.kantanj.types.Check;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;

public final class Networking {
   private Networking() {
   }

   public static void removeJavaUserAgentSuffixGlobally() {
      System.setProperty("http.agent", "");
   }

   public static String resolveIPv4(String host) {
      try {
         Check.notNull(host, "host cannot be null");

         for (InetAddress inetAddress : InetAddress.getAllByName(host)) {
            if (inetAddress instanceof Inet4Address) {
               return inetAddress.getHostAddress();
            }
         }

         throw new UnknownHostException("unknown host " + host);
      } catch (Exception ex) {
         throw new RuntimeException("failed to resolve IPv4 of " + host, ex);
      }
   }

   public static final class Http {
      private Http() {
      }

      public static String post(PostHttpRequest request) {
         try {
            HttpURLConnection con = openConnection(request);
            StringBuilder response = new StringBuilder();
            OutputStream outputStream = con.getOutputStream();
            outputStream.write(request.getPostData());
            outputStream.close();
            if (request.shouldDoInput()) {
               return readResponseAndDisconnect(con, response);
            } else {
               con.disconnect();
               return null;
            }
         } catch (Exception e) {
            throw new RuntimeException("http POST request failed", e);
         }
      }

      public static String get(GetHttpRequest request) {
         try {
            HttpURLConnection con = openConnection(request);
            StringBuilder response = new StringBuilder();
            return readResponseAndDisconnect(con, response);
         } catch (Exception err) {
            throw new RuntimeException("http GET request failed", err);
         }
      }

      public static HttpURLConnection openConnection(HttpRequest request) {
         Check.notNull(request, "request cannot be null");

         try {
            URL url = new URL(request.getURL());
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod(request.getRequestMethod().name());
            Map<String, String> requestProps = request.getRequestProperties();

            for (String prop : requestProps.keySet()) {
               con.setRequestProperty(prop, requestProps.get(prop));
            }

            if (request instanceof PostHttpRequest) {
               if (!requestProps.containsKey("Content-Type")) {
                  con.setRequestProperty("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());
               }

               con.setRequestProperty("Content-Length", String.valueOf(((PostHttpRequest)request).getPostData().length));
            }

            con.setRequestProperty("User-Agent", request.getUserAgent());
            con.setInstanceFollowRedirects(request.shouldFollowRedirects());
            con.setConnectTimeout(request.getConnectTimeout());
            con.setReadTimeout(request.getReadTimeout());
            con.setDoInput(request.shouldDoInput());
            con.setDoOutput(request.shouldDoOutput());
            return con;
         } catch (Exception cause) {
            throw new RuntimeException("failed to establish a new connection", cause);
         }
      }

      private static String readResponseAndDisconnect(HttpURLConnection con, StringBuilder response) throws IOException {
         InputStream inputStream = con.getInputStream();

         for (String line : IOUtils.readLines(inputStream, StandardCharsets.UTF_8)) {
            response.append(line).append('\n');
         }

         inputStream.close();
         con.disconnect();
         if (response.length() > 0 && response.charAt(response.length() - 1) == '\n') {
            response.deleteCharAt(response.length() - 1);
         }

         return response.toString();
      }
   }
}
