package me.egg82.echo.compression;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPCompressionStream extends AbstractCompressionStream {
    public byte[] compress(byte[] buf) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(buf.length);
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(out)) {
            gzipOut.write(buf);
        }
        return out.toByteArray();
    }

    public byte[] decompress(byte[] buf) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        try (GZIPInputStream gzipIn = new GZIPInputStream(in)) {
            return ByteStreams.toByteArray(gzipIn);
        }
    }
}
