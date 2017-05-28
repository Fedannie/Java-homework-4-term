package logic;

import exceptions.DataStreamException;
import exceptions.SocketException;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Class needed to make work with clients and server easy.
 * Creates server and one client to work with it.
 * Provides interface to client's work.
 * Shutdowns everything at the end of working.
 */
public class Manager {
    /**Number of maximum port that could be used.*/
    private static final int MAX_PORT = 20000;
    /**Number of minimum port that could be used.*/
    private static final int MIN_PORT = 10000;
    /**Server which manager works with.*/
    private Server server;
    /**Client which manager works with.*/
    private Client client;
    /**Value for 'go-through-file-tree' command.*/
    static final int COMMAND_GO = 1;
    /**Value for 'download-file' command.*/
    static final int COMMAND_GET = 2;

    /**
     * Public constructor of {@code Manager} object.
     * Creates {@code Server} object and {@code Client} object which connected to server.
     * @throws SocketException when appears problem with using any {@code Socket} object
     * @throws IOException when appears problem with internal stuff like filesystem.
     * @throws DataStreamException when appears problem with using
     * {@code DataInputStream} or {@code DataOutputStream}.
     */
    public Manager() throws SocketException, IOException, DataStreamException{
        int port;
        Random rnd = new Random();
        port = rnd.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
        server = Server.start(port, Paths.get(""));
        client = new Client(new Socket("localhost", port));
    }

    /**
     * Calls {@code Client.getList} function to get list of objects in given directory.
     * @param path -- path to directory which content should be returned
     * @return list of all folders and files in given directory
     * @throws DataStreamException when appears problem with using
     */
    public Client.Element[] getList(String path) throws DataStreamException{
        return client.list(path);
    }

    /**
     * Calls {@code Client.get} function to get content of given file.
     * @param path -- path to file which content should be returned
     * @return content of file as {@code byte} array
     * @throws DataStreamException when appears problem with using
     */
    public byte[] download(String path) throws DataStreamException{
        return client.get(path);
    }

    /**Turn off Server and client and shuts everything down.*/
    public void shutdown(){
        try {
            if (server != null) server.shutdown();
        } catch (Exception ignored) {
        }
        try {
            if (client != null) client.close();
        } catch (Exception ignored) {
        }
    }
}
