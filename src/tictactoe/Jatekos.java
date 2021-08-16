package tictactoe;

import java.net.Socket;

public class Jatekos {
    private Socket socket;
    private String XO;

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getXO() {
        return XO;
    }

    public void setXO(String XO) {
        this.XO = XO;
    }
}
