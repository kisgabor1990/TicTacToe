package tictactoe;

import javax.swing.*;
import java.awt.*;

public class Main {

    private static final int SZELESSEG = 350, MAGASSAG = 240;
    private static final int KEPERNYO_SZELESSEG = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int KEPERNYO_MAGASSAG = Toolkit.getDefaultToolkit().getScreenSize().height;

    private static JFrame foAblak;
    private static JButton szerverGomb, kliensGomb;

    public static void main(String[] args) {
        foAblak();

        komponensek();

        foAblak.setVisible(true);

    }

    public static void foAblak() {
        foAblak = new JFrame("TicTacToe Játék");
        foAblak.setSize(SZELESSEG, MAGASSAG);
        foAblak.setLocation((KEPERNYO_SZELESSEG - SZELESSEG) / 2, (KEPERNYO_MAGASSAG - MAGASSAG) / 2);
        foAblak.setResizable(false);
        foAblak.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        foAblak.setLayout(null);
    }

    public static void komponensek() {
        szerverGomb = new JButton("Szerver indítása");
        szerverGomb.setSize(300, 40);
        szerverGomb.setLocation(20, 40);
        szerverGomb.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        foAblak.add(szerverGomb);

        kliensGomb = new JButton("Csatlakozás játékhoz");
        kliensGomb.setSize(300, 40);
        kliensGomb.setLocation(20, 120);
        kliensGomb.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        foAblak.add(kliensGomb);
    }
}
