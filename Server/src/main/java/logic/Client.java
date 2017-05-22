package logic;

import exceptions.DataStreamException;
import exceptions.SocketException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/** Class of client which execute requests of server. */
public class Client implements AutoCloseable {
    /**Stream needed to read data from.*/
    private DataInputStream in;
    /**Stream needed to write data.*/
    private DataOutputStream out;
    /**Socket needed to connect to server.*/
    private final Socket socket;

    /**Class to keep information about files.*/
    public static final class Element {
        /**Name of file or directory.*/
        private final String name;
        /**Type of file, whether it is directory or not.*/
        private final boolean isDirectory;

        /**
         * Get name of file or directory.
         * @return name of file as {@code String} object
         */
        public String getName() {
            return name;
        }

        /**
         * Get type of file
         * @return {@code true} if file is directory and {@code false} if not
         */
        public boolean isDirectory() {
            return isDirectory;
        }

        /**
         * Construct {@code Element} object by name and type of file.
         * @param name -- name of given file
         * @param isDirectory -- type of given file; {@code true} if file is directory and {@code false} if not
         */
        Element(String name, boolean isDirectory) {
            this.name = name;
            this.isDirectory = isDirectory;
        }

        /**
         * Compares two {@code Element} objects by names and types.
         * @param obj -- second object to compare
         * @return {@code true} if objects are equals and {@code false} if not
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Element)) {
                return false;
            }
            Element other = (Element) obj;
            return name.equals(other.name) && isDirectory == other.isDirectory;
        }

        /**
         * Get hash code of {@code Element} object, based on name and type.
         * @return hash code as integer number
         */
        @Override
        public int hashCode() {
            return name.hashCode() * 31 + Boolean.hashCode(isDirectory);
        }
    }

    /**
     * Construct {@code logic.Client} object by socket to connect to server.
     * @param socket -- socket needed to connect to server
     * @throws DataStreamException if there is an error with using {@code DataInputStream} or {@code DataOutputStream}
     */
    public Client(Socket socket) throws DataStreamException {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex){
                try{
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException exc){
                    throw new DataStreamException("Error when handling streams in logic.Client constructor");
                }
                throw new DataStreamException("Error when handling streams in logic.Client constructor");
            }
            throw new DataStreamException("Error when handling streams in logic.Client constructor");
        }
    }

    /**
     * Get list of all files in root directory.
     * @param path -- path to root directory
     * @return list of all files and directories as {@code Element} array
     * @throws DataStreamException if there is an error with using {@code DataInputStream} or {@code DataOutputStream}
     */
    public Element[] list(String path) throws DataStreamException {
        try {
            out.writeInt(1);
            out.writeUTF(path);
            int count = in.readInt();
            Element[] result = new Element[count];
            for (int i = 0; i < count; i++) {
                String name = in.readUTF();
                boolean isDirectory = in.readBoolean();
                result[i] = new Element(name, isDirectory);
            }
            return result;
        } catch (IOException e){
            throw new DataStreamException("Error when handling streams in logic.Client.list");
        }
    }

    /**
     * Get content of given file.
     * @param path -- path to given file
     * @return content of file as array of bytes
     * @throws DataStreamException if there is an error with using {@code DataInputStream} or {@code DataOutputStream}
     */
    public byte[] get(String path) throws DataStreamException {
        try {
            out.writeInt(2);
            out.writeUTF(path);
            int length = in.readInt();
            byte[] result = new byte[length];
            int position = 0;
            while (position < result.length) {
                position += in.read(result, position, result.length - position);
            }
            return result;
        } catch (IOException e){
            throw new DataStreamException("Error when handling streams in logic.Client.get");
        }
    }

    /**
     * Closes all streams and shutdowns the socket.
     * @throws DataStreamException if there is an error with using {@code DataInputStream} or {@code DataOutputStream}
     * @throws SocketException if an error appears while working with {@code Socket}
     */
    @Override
    public void close() throws DataStreamException, SocketException {
        try {
            out.close();
            in.close();
        }catch (IOException e){
            throw new DataStreamException("Error when handling streams in logic.Client.close");
        }
        try {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        } catch (IOException e){
            throw new SocketException("Error when trying to shut down a Socket object in logic.Client.close");
        }
    }
}
