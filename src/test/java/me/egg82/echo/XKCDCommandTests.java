package me.egg82.echo;

import me.egg82.echo.commands.XKCDCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

class XKCDCommandTests {
    @Test
    void testResponse() throws ExecutionException, InterruptedException {
        Assertions.assertNotNull(XKCDCommand.getModel("standards").get());
        System.out.println(XKCDCommand.getModel("standards").get());
    }
}
