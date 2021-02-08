package me.egg82.echo;

import com.nixxcode.jvmbrotli.common.BrotliLoader;
import javax.security.auth.login.LoginException;
import joptsimple.OptionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static Bot bot;

    public static void main(String[] args) {
        if (!BrotliLoader.isBrotliAvailable()) {
            logger.error("Brotli is unavailable.");
            return;
        }

        OptionParser parser = new OptionParser();
        parser.accepts("token", "Discord bot token").withRequiredArg();

        try {
            bot = new Bot(parser.parse(args), "${project.version}");
        } catch (LoginException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
