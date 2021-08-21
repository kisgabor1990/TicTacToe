package tictactoe;

import java.net.Socket;

public class Jatekos {
    private String nev;
    private Socket socket;
    private String XO;
    private boolean ready, wantMore;
    private int wins;

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

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void win() {
        this.wins++;
    }

    public boolean isWantMore() {
        return wantMore;
    }

    public void setWantMore(boolean wantMore) {
        this.wantMore = wantMore;
    }
}
