package main.probabilities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.FileManager;

//@author Michael Haertling
public class Connection {

    final static String EOF = "";

    Socket socket;
    ObjectInputStream oin;
    ObjectOutputStream oos;

    boolean validConnection = true;

    public Connection(Socket s) throws IOException {

        socket = s;
        oos = new ObjectOutputStream(socket.getOutputStream());
        oin = new ObjectInputStream(socket.getInputStream());
    }

    public Object readObject() throws IOException {
        Object obj = null;
        try {
            obj = oin.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return obj;
    }

    public void sendObject(Object o) throws IOException {
        oos.writeObject(o);
        oos.flush();
    }

    public void sendInt(int data) throws IOException {
        oos.write(data);
        oos.flush();

    }

    public int readInt() throws IOException {
        return oin.read();

    }

    public void sendString(String str) throws IOException {
        sendObject(str);
    }

    public String readString() throws IOException {
        return (String) readObject();
    }

    public void readFile(String path) throws IOException {
        try (PrintWriter write = FileManager.openFileForWriting(path)) {
            String next;
            boolean first = true;
            while (!(next = readString()).equals(EOF)) {
                if (first) {
                    first = false;
                    write.print(next);
                } else {
                    write.print("\n" + next);
                }
            }
        }
    }

    public void sendFile(String path) throws IOException {
        try (Scanner in = FileManager.openFileForReading(path)) {
            while (in.hasNextLine()) {
                sendString(in.nextLine());
            }
            sendString(EOF);
        }
    }

    public void close() {
        try {
            oin.close();
            oos.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
