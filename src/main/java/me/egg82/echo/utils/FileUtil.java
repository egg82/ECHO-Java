package me.egg82.echo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() { }

    public static File getCwd() {
        try {
            return new File(new File(FileUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), "config");
        } catch (URISyntaxException ex) {
            logger.warn(ex.getMessage(), ex);
            return new File(new File(System.getProperty("user.dir")), "config");
        }
    }
}
