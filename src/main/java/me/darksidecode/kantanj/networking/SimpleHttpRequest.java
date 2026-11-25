package me.darksidecode.kantanj.networking;

import java.util.HashMap;
import java.util.Map;
import me.darksidecode.kantanj.formatting.Formatting;
import me.darksidecode.kantanj.types.Check;

public class SimpleHttpRequest implements HttpRequest {
   public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
   public static final int DEFAULT_READ_TIMEOUT = 5000;
   private String userAgent;
   private RequestMethod requestMethod;
   private String baseUrl;
   private String path;
   private final Map<String, String> queryParams = new HashMap<>();
   private final Map<String, String> requestProperties = new HashMap<>();
   private int connectTimeout = 5000;
   private int readTimeout = 5000;
   private boolean followRedirects;
   private boolean doInput = true;
   private boolean doOutput;

   public SimpleHttpRequest done() {
      return this;
   }

   public SimpleHttpRequest userAgent(String userAgent) {
      return this.userAgent(userAgent, false);
   }

   public SimpleHttpRequest userAgent(String userAgent, boolean removeJavaSuffix) {
      this.userAgent = Check.notNull(userAgent, "userAgent cannot be null");
      if (removeJavaSuffix) {
         Networking.removeJavaUserAgentSuffixGlobally();
      }

      return this;
   }

   public SimpleHttpRequest requestMethod(RequestMethod requestMethod) {
      Check.state(this.requestMethod != null, "requestMethod already set");
      this.requestMethod = Check.notNull(requestMethod, "requestMethod cannot be null");
      return this;
   }

   public SimpleHttpRequest baseUrl(String baseUrl) {
      Check.state(this.baseUrl != null, "baseUrl already set");
      Check.notNull(baseUrl, "baseUrl cannot be null");
      if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
         throw new IllegalArgumentException("non-http protocol in baseUrl: " + baseUrl);
      } else {
         this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
         return this;
      }
   }

   public SimpleHttpRequest path(String path) {
      Check.state(this.path != null, "path already set");
      Check.notNull(path, "path cannot be null");
      if (path.isEmpty()) {
         throw new IllegalArgumentException("path cannot be empty");
      } else {
         if (path.charAt(0) == '/') {
            if (path.length() == 1) {
               throw new IllegalArgumentException("path cannot consist of a single slash");
            }

            path = path.substring(1);
         }

         this.path = path;
         return this;
      }
   }

   public SimpleHttpRequest queryParam(String queryParam, Object value) {
      if (this.queryParams.containsKey(Check.notNull(queryParam, "queryParam key cannot be null"))) {
         throw new IllegalStateException("duplicate query param \"" + queryParam + "\"");
      } else {
         this.queryParams.put(queryParam, Check.notNull(value, "queryParam value cannot be null").toString());
         return this;
      }
   }

   public SimpleHttpRequest encodedQueryParam(String queryParam, String valueToEncode) {
      return this.queryParam(queryParam, Formatting.urlEncodeUtf8(valueToEncode));
   }

   public SimpleHttpRequest queryParams(Object... queryParams) {
      if (Check.notEmpty(queryParams, "queryParams cannot be empty").length % 2 != 0) {
         throw new IllegalArgumentException(
            "queryParams length must be a multple of two, with query params keys (String) at even indexes and their values (Object) at the adjacent odd ones"
         );
      } else {
         for (int i = 0; i < queryParams.length - 1; i += 2) {
            Object objAtEven = Check.notNull(queryParams[i], "query param key cannot be null (index %s)", i);
            String qpKey = Check.instanceOf(
               objAtEven,
               String.class,
               "even indexes must contain query params keys; expected String at index %s, but got %s",
               i,
               objAtEven.getClass().getName()
            );
            this.queryParam(qpKey, queryParams[i + 1]);
         }

         return this;
      }
   }

   public SimpleHttpRequest requestProperty(String propertyName, String value) {
      if (Check.notNull(propertyName, "propertyName cannot be null").equals("User-Agent")) {
         throw new IllegalArgumentException("User-Agent must be set using method userAgent(...)");
      } else {
         this.requestProperties.put(propertyName, Check.notNull(value, "value cannot be null"));
         return this;
      }
   }

   public SimpleHttpRequest connectTimeout(int timeoutMillis) {
      if (timeoutMillis < 0) {
         throw new IllegalArgumentException("timeoutMillis must be a positive integer");
      } else {
         this.connectTimeout = timeoutMillis;
         return this;
      }
   }

   public SimpleHttpRequest readTimeout(int timeoutMillis) {
      if (timeoutMillis < 0) {
         throw new IllegalArgumentException("timeoutMillis must be a positive integer");
      } else {
         this.readTimeout = timeoutMillis;
         return this;
      }
   }

   public SimpleHttpRequest followRedirects(boolean follow) {
      this.followRedirects = follow;
      return this;
   }

   public SimpleHttpRequest doInput(boolean doInput) {
      this.doInput = doInput;
      return this;
   }

   public SimpleHttpRequest doOutput(boolean doOutput) {
      this.doOutput = doOutput;
      return this;
   }

   @Override
   public String getUserAgent() {
      Check.state(this.userAgent == null, "userAgent not set");
      return this.userAgent;
   }

   @Override
   public RequestMethod getRequestMethod() {
      Check.state(this.requestMethod == null, "requestMethod not set");
      return this.requestMethod;
   }

   @Override
   public boolean isSecured() {
      Check.state(this.baseUrl == null, "baseUrl not set");
      return this.baseUrl.startsWith("https://");
   }

   @Override
   public Map<String, String> getRequestProperties() {
      return this.requestProperties;
   }

   @Override
   public int getConnectTimeout() {
      return this.connectTimeout;
   }

   @Override
   public int getReadTimeout() {
      return this.readTimeout;
   }

   @Override
   public boolean shouldFollowRedirects() {
      return this.followRedirects;
   }

   @Override
   public boolean shouldDoInput() {
      return this.doInput;
   }

   @Override
   public boolean shouldDoOutput() {
      return this.doOutput;
   }

   @Override
   public String getURL() throws IllegalStateException {
      Check.state(this.baseUrl == null, "baseUrl not set");
      Check.state(this.path == null, "path not set");
      StringBuilder url = new StringBuilder(this.baseUrl + '/' + this.path);
      boolean firstQParam = true;

      for (String queryParam : this.queryParams.keySet()) {
         char appendChar = '&';
         if (firstQParam) {
            appendChar = '?';
            firstQParam = false;
         }

         url.append(appendChar).append(queryParam).append('=').append(this.queryParams.get(queryParam));
      }

      return url.toString();
   }

   public GetHttpRequest asGetRequest() {
      if (this instanceof GetHttpRequest) {
         return (GetHttpRequest)this;
      } else {
         throw new ClassCastException("not a GET request: " + this.getClass().getName());
      }
   }

   public PostHttpRequest asPostRequest() {
      if (this instanceof PostHttpRequest) {
         return (PostHttpRequest)this;
      } else {
         throw new ClassCastException("not a POST request: " + this.getClass().getName());
      }
   }
}
