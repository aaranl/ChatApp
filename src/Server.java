import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ArrayList<ConnectionHandler> connected;
    private ServerSocket server;
    private boolean running;
    private ExecutorService threadpool;

    public Server() {
        connected = new ArrayList<>();
        running = true;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(53812);
            threadpool = Executors.newCachedThreadPool();
            while (running) {
                Socket client = server.accept();

                ConnectionHandler handler = new ConnectionHandler(client);
                connected.add(handler);
                threadpool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }

    }

    public void connectUsers(String message) {
        for (ConnectionHandler users : connected) {
            if (users != null) {
                users.sendMessage(message);
            }
        }
    }

    public void shutdown()  {
        try {
            running = false;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler users : connected) {
                users.shutdown();
            }
        } catch (IOException e) {
            //Unfixable
        }
    }


    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        private String password;
        public ConnectionHandler(Socket client) {
            this.client = client;

        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Enter your username");
                username = in.readLine();
                //TODO: Catch statements for invalid usernames.
                out.println("Enter your password");
                //TODO: Catch statements for invalid passwords.
                password = in.readLine();

                System.out.println("Welcome " + username);
                connectUsers(username + " joined!");

                String message;
                while((message = in.readLine()) != null) {
                    if (message.startsWith("/quit")) {
                        connectUsers(username + " left the chat.");
                        shutdown();
                    } else {
                        connectUsers(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }

        }

        public void sendMessage(String message) {
           out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                //Unfixable
            }
        }
    }

    public static void main(String[] args) {

        Server server = new Server();
        server.run();
    }
}
