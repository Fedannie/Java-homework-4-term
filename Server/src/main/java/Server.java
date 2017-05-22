import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exceptions.SocketException;

/**Class of server objects.*/
class Server {
    /**Tread which handle all clients and create tasks.*/
    private final Thread listenThread;
    /**TreadPool for all clients' tasks.*/
    private final ExecutorService clientThreads;
    /**ServerSocket which accepts clients which want to connect.*/
    private final ServerSocket serverSocket;

    /**
     * Constructs {@code Server} object by port (to listen from) and path to root directory.
     * @param port -- port which server should listen from for the clients
     * @param rootPath -- path to root directory
     * @return a new {@code Server} object
     * @throws SocketException if an error appears while working with {@code Socket}
     */
     static Server start(int port, Path rootPath) throws SocketException {
        return new Server(port, rootPath);
    }

    /**
     * Constructs {@code Server} object by port (to listen from) and path to root directory.
     *
     * Creates new {@code RunnableTask} object for each task of each client and add the tasks
     * to {@code clientThreads} array.
     * All clients are handled in main {@code listenTread} thread.
     * @param port -- port which server should listen from for the clients
     * @param rootPath -- path to root directory
     * @throws SocketException if an error appears while working with {@code Socket}
     */
    private Server(int port, Path rootPath) throws SocketException {
        try {
            clientThreads = Executors.newCachedThreadPool();
            serverSocket = new ServerSocket(port);
            listenThread = new Thread(() -> {
                while (!Thread.interrupted()) {
                    Socket client;
                    try {
                        client = serverSocket.accept();
                    } catch (Exception e) {
                        break;
                    }
                    clientThreads.submit(new RunnableTask(rootPath, client));
                }
                try {
                    if (!serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            listenThread.start();
        } catch (IOException e){
            throw new SocketException("Error when handling a ServerSocket object in Server constructor");
        }
    }

    /**
     * Closes all threads with clients tasks and shuts down the main thread.
     * @throws SocketException if an error appears while working with {@code Socket}
     * @throws InterruptedException if there is a problem when joining {@code listenThread}
     */
    void shutdown() throws InterruptedException, SocketException {
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            throw new SocketException("Error when handling a ServerSocket object in Server.shutdown");
        }
        listenThread.interrupt();
        listenThread.join();
        clientThreads.shutdown();
    }
}