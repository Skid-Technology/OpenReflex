package rip.reflex.util.net.http;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class HttpRequest<RS extends InputStream, WS extends OutputStream> {
   private boolean read = false;
   private boolean write = false;

   protected abstract void execute();

   protected abstract void read(RS readStream) throws Exception;

   protected abstract void write(WS writeStream) throws Exception;

   protected abstract RS getReadStream(InputStream inputStream);

   protected abstract WS getWriteStream(OutputStream outputStream) throws Exception;

   public boolean isRead() {
      return this.read;
   }

   public boolean isWrite() {
      return this.write;
   }

   public void setRead(boolean read) {
      this.read = read;
   }

   public void setWrite(boolean write) {
      this.write = write;
   }
}
