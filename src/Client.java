import java.awt.image.DataBufferDouble;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.Buffer;

public class Client implements Runnable
{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean running;


    @Override
    public void run() {
        try {
            running = true;
            Socket client = new Socket("127.0.0.1", 53812);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread thread = new Thread(inHandler);
            thread.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            //TODO: handle
        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                while (running){
                    String message = inputReader.readLine();
                    if (message.equals("/quit")) {
                        inputReader.close();
                        shutdown();
                    } else {
                        out.println(message);
                    }

                }

            } catch (IOException e) {
                shutdown();
            }
        }
    }
}
