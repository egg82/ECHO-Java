package me.egg82.echo.messaging;

import me.egg82.echo.utils.ValidationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

public class ServerIDUtil {
    private static final Logger logger = LoggerFactory.getLogger(ServerIDUtil.class);

    private ServerIDUtil() { }

    public static @NotNull UUID getId(@NotNull File idFile) {
        UUID retVal;

        try {
            retVal = readId(idFile);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            retVal = null;
        }

        if (retVal == null) {
            retVal = UUID.randomUUID();
            try {
                writeId(idFile, retVal);
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        return retVal;
    }

    private static @Nullable UUID readId(@NotNull File idFile) throws IOException {
        if (!idFile.exists() || (idFile.exists() && idFile.isDirectory())) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        try (FileReader reader = new FileReader(idFile); BufferedReader in = new BufferedReader(reader)) {
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        }
        String retVal = builder.toString().trim();

        return ValidationUtil.isValidUuid(retVal) ? UUID.fromString(retVal) : null;
    }

    private static void writeId(@NotNull File idFile, @NotNull UUID id) throws IOException {
        if (idFile.exists() && idFile.isDirectory()) {
            Files.delete(idFile.toPath());
        }
        if (!idFile.exists()) {
            if (!idFile.createNewFile()) {
                throw new IOException("Stats file could not be created.");
            }
        }

        try (FileWriter out = new FileWriter(idFile)) {
            out.write(id.toString() + System.lineSeparator());
        }
    }
}
