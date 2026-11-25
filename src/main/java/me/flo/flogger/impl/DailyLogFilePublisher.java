package me.flo.flogger.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import me.flo.flogger.types.FlogFormatter;
import me.flo.flogger.types.FlogRecord;

public class DailyLogFilePublisher extends LogWriterPublisher {
   private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY");
   private final File dir;
   private File currentFile;
   private int lastDayOfMonth = -1;

   public DailyLogFilePublisher(File directory) throws IOException {
      super(new FileWriter(newFile(directory), true));
      this.currentFile = newFile(directory);
      this.dir = directory;
   }

   private static File newFile(File dir) throws IOException {
      File f = new File(dir, format.format(new Date(System.currentTimeMillis())) + ".log");
      if (!f.exists()) {
         f.createNewFile();
      }

      return f;
   }

   @Override
   public void handle(FlogRecord record, FlogFormatter formatter) {
      LocalDate date = LocalDate.now();
      if (this.lastDayOfMonth != -1 && this.lastDayOfMonth != date.getDayOfMonth()) {
         try {
            this.writer.close();
            File nf = newFile(this.dir);
            this.writer = new FileWriter(nf, true);
            this.writerSwitched((FileWriter)this.writer, this.currentFile);
            this.currentFile = nf;
         } catch (IOException ex) {
            ex.printStackTrace();
         }
      }

      this.lastDayOfMonth = date.getDayOfMonth();
      super.handle(record, formatter);
   }

   protected void writerSwitched(FileWriter newWriter, File oldFile) {
   }

   public File getCurrentFile() {
      return this.currentFile;
   }
}
