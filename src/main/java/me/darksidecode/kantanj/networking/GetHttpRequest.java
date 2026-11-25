package me.darksidecode.kantanj.networking;

public class GetHttpRequest extends SimpleHttpRequest {
   public GetHttpRequest done() {
      return this;
   }

   @Override
   public SimpleHttpRequest requestMethod(RequestMethod requestMethod) {
      throw new IllegalStateException("cannot change request method for " + this.getClass().getName());
   }

   @Override
   public RequestMethod getRequestMethod() {
      return RequestMethod.GET;
   }

   @Override
   public SimpleHttpRequest doInput(boolean doInput) {
      throw new IllegalStateException("cannot change doInput for " + this.getClass().getName());
   }

   @Override
   public boolean shouldDoInput() {
      return true;
   }

   @Override
   public SimpleHttpRequest doOutput(boolean doOutput) {
      throw new IllegalStateException("cannot change doOutput for " + this.getClass().getName());
   }

   @Override
   public boolean shouldDoOutput() {
      return false;
   }
}
