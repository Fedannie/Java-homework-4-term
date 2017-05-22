import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**Class which represents one task of one client.*/
class RunnableTask implements Runnable {
    /**Path to root directory for this particular task.*/
    private Path path;
    /**Client which hash sent this task to server.*/
    private Socket client;

    /**
     * Constructs new {@code RunnableTask} object by a path to root directory and a clientSocket.
     * @param path -- path to root directory
     * @param client -- client which has given this task
     */
    RunnableTask(Path path, Socket client) {
        this.path = path;
        this.client = client;
    }

    /**
     * Listens to client's input and output streams and handles its commands.
     * Command 1 -- command list
     * Command 2 -- command get
     */
    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(client.getInputStream());
             DataOutputStream out = new DataOutputStream(client.getOutputStream())){
            while (true) {
                int command = in.readInt();
                switch (command) {
                    case 1:
                        Path dir = path.resolve(in.readUTF());
                        if (!Files.isDirectory(dir)) {
                            out.writeInt(0);
                            return;
                        }
                        List<Path> content = Files.list(dir)
                                        .sorted(Comparator.comparing(Path::getFileName))
                                        .collect(Collectors.toList());
                        out.writeInt(content.size());
                        for (Path inside : content) {
                            out.writeUTF(inside.getFileName().toString());
                            out.writeBoolean(Files.isDirectory(inside));
                        }
                        break;
                    case 2:
                        Path file = path.resolve(in.readUTF());
                        try (InputStream fin = Files.newInputStream(file)) {
                            out.writeInt((int) Files.size(file));
                            IOUtils.copyLarge(fin, out);
                        } catch (NoSuchFileException | UnsupportedOperationException e) {
                            out.writeInt(0);
                        }
                        break;
                    default:
                        throw new RuntimeException("Invalid command received from client");
                }
            }
        } catch (EOFException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (!client.isInputShutdown() && !client.isClosed()) {
                    client.shutdownInput();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (!client.isOutputShutdown() && !client.isClosed()) {
                    client.shutdownOutput();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
