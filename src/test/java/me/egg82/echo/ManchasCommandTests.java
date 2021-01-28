package me.egg82.echo;

import java.util.concurrent.ExecutionException;
import me.egg82.echo.commands.ManchasCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ManchasCommandTests {
    @Test
    void testResponse() throws ExecutionException, InterruptedException {
        Assertions.assertNotNull(ManchasCommand.get(-1).get());
        System.out.println(ManchasCommand.get(-1).get());

        Assertions.assertNotNull(ManchasCommand.get(14).get());
        System.out.println(ManchasCommand.get(14).get());

        Assertions.assertThrows(ExecutionException.class, () -> ManchasCommand.get(394857934).get());
    }
}
