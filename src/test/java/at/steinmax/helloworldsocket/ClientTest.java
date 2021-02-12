package at.steinmax.helloworldsocket;

import at.steinmax.helloworldsocket.logic.Client;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientTest {
    @Test
    public void shouldAnswerWithTrue() {
        Client client = new Client("localhost", 30_000);
        client.connect();
        String time = client.getResponse("gettime");
        System.out.println(time);
        assertTrue(client.disconnect());
    }
}
