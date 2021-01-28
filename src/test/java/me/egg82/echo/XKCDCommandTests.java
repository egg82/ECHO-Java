package me.egg82.echo;

import java.util.concurrent.ExecutionException;
import me.egg82.echo.commands.XKCDCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class XKCDCommandTests {
    @Test
    void testJSON() throws ExecutionException, InterruptedException {
        Assertions.assertNotNull(XKCDCommand.getModel("standards").get());
        System.out.println(XKCDCommand.getModel("standards").get());
    }
}
