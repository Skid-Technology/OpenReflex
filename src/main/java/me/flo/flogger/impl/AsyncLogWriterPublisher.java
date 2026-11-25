package me.flo.flogger.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import me.flo.flogger.types.FlogFormatter;
import me.flo.flogger.types.FlogPublisher;
import me.flo.flogger.types.FlogRecord;

public abstract class AsyncLogWriterPublisher extends FlogPublisher implements Runnable {
   private final LogWriterPublisher publisher;
   private final BlockingQueue<Object[]> queue;

   public AsyncLogWriterPublisher(LogWriterPublisher publisher, int queueSize) {
      this.publisher = publisher;
      this.queue = queueSize == -1 ? new LinkedBlockingQueue<>() : new ArrayBlockingQueue<>(queueSize);
      this.startAsync(this);
   }

   @Override
   public void run() {
      while (this.publisher != null) {
         try {
            Object[] array = this.queue.take();
            this.publisher.handle((FlogRecord)array[0], (FlogFormatter)array[1]);
         } catch (InterruptedException ex) {
            new RuntimeException("FLOGGER CRASH", ex).printStackTrace();
         }
      }
   }

   @Override
   public void handle(FlogRecord record, FlogFormatter formatter) {
      if (!this.queue.offer(new Object[]{record, formatter})) {
         throw new RuntimeException("No space left in logging queue.");
      }
   }

   protected abstract void startAsync(Runnable task);
}
