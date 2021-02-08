package me.egg82.echo.compression;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ZstdCompressionStream extends AbstractCompressionStream {
    public byte[] compress(byte[] buf) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(buf.length);
        try (ZstdOutputStream zstdOut = new ZstdOutputStream(out, 9)) {
            zstdOut.write(buf);
        }
        return out.toByteArray();
    }

    public byte[] decompress(byte[] buf) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        try (ZstdInputStream zstdIn = new ZstdInputStream(in)) {
            return ByteStreams.toByteArray(zstdIn);
        }
    }
}
