package me.flo.flogger.impl;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.Writer;
import me.flo.flogger.types.FlogException;
import me.flo.flogger.types.FlogFormatter;
import me.flo.flogger.types.FlogPublisher;
import me.flo.flogger.types.FlogRecord;

public class LogWriterPublisher extends FlogPublisher {
   protected Writer writer;

   @Override
   public void handle(FlogRecord record, FlogFormatter formatter) {
      try {
         this.writer.write(formatter.format(record) + '\n');
         this.writer.flush();
      } catch (IOException ex) {
         throw new FlogException("Failed to write to writer", ex);
      }
   }

   @ConstructorProperties({"writer"})
   public LogWriterPublisher(Writer writer) {
      this.writer = writer;
   }
}
