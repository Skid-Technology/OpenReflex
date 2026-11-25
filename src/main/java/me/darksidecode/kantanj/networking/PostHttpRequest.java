package me.darksidecode.kantanj.networking;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import me.darksidecode.kantanj.formatting.CommonJson;
import me.darksidecode.kantanj.types.Check;
import org.apache.http.entity.ContentType;

public class PostHttpRequest extends SimpleHttpRequest {
   private byte[] postData;

   public PostHttpRequest done() {
      return this;
   }

   @Override
   public SimpleHttpRequest requestMethod(RequestMethod requestMethod) {
      throw new IllegalStateException("cannot change request method for " + this.getClass().getName());
   }

   @Override
   public RequestMethod getRequestMethod() {
      return RequestMethod.POST;
   }

   @Override
   public SimpleHttpRequest doOutput(boolean doOutput) {
      throw new IllegalStateException("cannot change doOutput for " + this.getClass().getName());
   }

   @Override
   public boolean shouldDoOutput() {
      return true;
   }

   public PostHttpRequest postData(byte[] postData) {
      this.postData = Check.notNull(postData, "post data cannot be null");
      return this;
   }

   public PostHttpRequest postUtf8Data(String postDataUtf8) {
      this.postData = Check.notNull(postDataUtf8, "post data cannot be null").getBytes(StandardCharsets.UTF_8);
      return this;
   }

   public <T extends Serializable> PostHttpRequest postJsonData(T object) {
      this.requestProperty("Content-Type", ContentType.APPLICATION_JSON.toString());
      return this.postUtf8Data(CommonJson.toJson(object));
   }

   public byte[] getPostData() {
      if (this.postData == null) {
         throw new IllegalArgumentException("post data is not set");
      } else {
         return this.postData;
      }
   }
}
