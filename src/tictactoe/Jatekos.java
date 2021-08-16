package tictactoe;

import java.net.Socket;

public class Jatekos {
    private String nev;
    private Socket socket;
    private String XO;

    public String getNev() {
        return nev;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

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
