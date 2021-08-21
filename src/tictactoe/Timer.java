package tictactoe;

import javax.swing.*;

public class Timer {
    private int perc = 0;
    private int mperc = 0;
    private boolean started = false;
    private JLabel label;

    public Timer(JLabel label) {
        this.label = label;
    }

    public void start() {
        new Thread(() -> {
            started = true;
            while (started) {
                try {
                    mperc++;
                    if (mperc == 60) {
                        perc++;
                        mperc = 0;
                    }
                    this.label.setText("%02d:%02d".formatted(perc, mperc));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() {
        this.started = false;
    }

    public void reset() {
        this.perc = 0;
        this.mperc = 0;
        this.label.setText("00:00");
    }
}
