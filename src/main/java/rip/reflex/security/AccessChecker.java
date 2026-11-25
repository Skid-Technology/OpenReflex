package rip.reflex.security;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class AccessChecker {
   private static final URL[] urls = new URL[]{
      getUrl("https://antipiracy.reflex.rip/uid.txt"),
      getUrl("http://antipiracy.reflex.rip/uid.txt"),
      getUrl("https://fckrussia.freigedacht.org/uid.txt"),
      getUrl("https://fckrussia.michivip.me/uid.txt"),
      getUrl("http://fckrussia.michivip.me/uid.txt"),
      getUrl("https://fckrussia.johand.io/uid.txt"),
      getUrl("http://fckrussia.cf/uid.txt")
   };

   static URL findWorkingURL() {
      URL[] result = new URL[1];
      List<Thread> threads = Arrays.stream(urls).map(url -> test(url, result)).collect(Collectors.toList());

      for (Thread thread : threads) {
         try {
            thread.join();
            if (result[0] != null) {
               killall(threads);
               break;
            }
         } catch (InterruptedException ignored) {
         }
      }

      return result[0];
   }

   private static void killall(List<Thread> threads) {
      for (Thread thread : threads) {
         thread.stop();
      }
   }

   private static Thread test(URL url, URL[] result) {
      Thread thread = new Thread(() -> {
         try {
            test0(url, result);
         } catch (IOException ignoredEx) {
         }
      });
      thread.start();
      return thread;
   }

   private static void test0(URL url, URL[] result) throws IOException {
      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      con.setRequestProperty("User-Agent", "PageChecker/1.0");
      con.setConnectTimeout(2000);
      con.setReadTimeout(2000);
      if (con.getInputStream().read(new byte[1]) < 1) {
         throw new IOException();
      } else {
         result[0] = url;
      }
   }

   private static URL getUrl(String s) {
      try {
         return new URL(s);
      } catch (MalformedURLException ex) {
         throw new RuntimeException(ex);
      }
   }
}
