package me.darksidecode.kantanj.networking;

import java.util.Map;

public interface HttpRequest {
   String getUserAgent() throws IllegalStateException;

   RequestMethod getRequestMethod() throws IllegalStateException;

   boolean isSecured() throws IllegalStateException;

   Map<String, String> getRequestProperties();

   int getConnectTimeout();

   int getReadTimeout();

   boolean shouldFollowRedirects();

   boolean shouldDoInput();

   boolean shouldDoOutput();

   String getURL() throws IllegalStateException;
}
