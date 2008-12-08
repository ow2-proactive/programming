package org.objectweb.proactive.ic2d.debug.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;


public class Connection implements Serializable {

    private static final long serialVersionUID = 4143722330891057902L;
    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private boolean activated = false;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        reader = socket.getInputStream();
        writer = socket.getOutputStream();
    }

    public Data read() throws IOException {
        Data data = new Data(512);
        data.read(reader);
        return data;
    }

    public boolean isActive() {
        return activated;
    }

    public void write(Data data) throws IOException {
        data.write(writer);
    }

    public void activate() {
        activated = true;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
        }

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        }
    }
}