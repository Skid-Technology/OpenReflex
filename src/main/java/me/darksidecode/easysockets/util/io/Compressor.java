package me.darksidecode.easysockets.util.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Compressor {
   public static InputStream newInflateInput(InputStream input) {
      return new InflaterInputStream(input);
   }

   public static OutputStream newDeflateOutput(OutputStream output) {
      return new DeflaterOutputStream(output, new Deflater(-1), true);
   }
}
