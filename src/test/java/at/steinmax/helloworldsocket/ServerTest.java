package at.steinmax.helloworldsocket;

import at.steinmax.helloworldsocket.logic.MultiServer;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class ServerTest {

    @Test
    public void shouldAnswerWithTrue() {
        MultiServer server = new MultiServer(30_000);
        server.start();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.stop();
        assertTrue(true);
    }
}
