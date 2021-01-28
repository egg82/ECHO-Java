package me.egg82.echo;

import java.util.concurrent.ExecutionException;
import me.egg82.echo.commands.RoryCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoryCommandTests {
    @Test
    void testResponse() throws ExecutionException, InterruptedException {
        Assertions.assertNotNull(RoryCommand.get(-1).get());
        System.out.println(RoryCommand.get(-1).get());

        Assertions.assertNotNull(RoryCommand.get(14).get());
        System.out.println(RoryCommand.get(14).get());

        Assertions.assertThrows(ExecutionException.class, () -> RoryCommand.get(394857934).get());
    }
}
