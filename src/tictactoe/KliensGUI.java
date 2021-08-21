package tictactoe;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class KliensGUI {

    private final int SZELESSEG = 400, MAGASSAG = 600;
    private final int KEPERNYO_SZELESSEG = Toolkit.getDefaultToolkit().getScreenSize().width;
    private final int KEPERNYO_MAGASSAG = Toolkit.getDefaultToolkit().getScreenSize().height;

    public JFrame kliensAblak;
    private JLabel cimLabel, portLabel, nevLabel;
    private JFormattedTextField cimText, portText, nevText, uzenetText;
    private JButton csatlakozasGomb, kuldesGomb;
    private JTextArea log;
    private JScrollPane logPane, scrollPane;
    private JPanel boardPanel;
    private JButton[][] board;
    private JLabel idoLabel, jelenlegiJatekosLabel, jatekosLabel;

    private MaskFormatter portFormatter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss] ");
    private int port, tablaMeret;
    private String cim, nev, jelenlegiJatekos, jatekos;
    private boolean csatlakozva = false, idozitoInditva = false;
    private int[] xGyoztesKoord = new int[5];
    private int[] yGyoztesKoord = new int[5];

    private DefaultCaret caret;
    private ExecutorService es = Executors.newCachedThreadPool();
    private PrintWriter kimenet;
    private BufferedReader bemenet;
    private Socket socket;
    private Timer timer;

    public void jatekMain() {
        try {
            portFormatter = new MaskFormatter("####");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        kliensAblak();
        komponensek();
        kliensAblak.setVisible(true);

        caret = (DefaultCaret) log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

    }

    public void kliensAblak() {
        kliensAblak = new JFrame("TicTacToe Játék");
        kliensAblak.setSize(SZELESSEG, MAGASSAG);
        kliensAblak.setLocation((KEPERNYO_SZELESSEG - SZELESSEG) / 2, (KEPERNYO_MAGASSAG - MAGASSAG) / 2);
        kliensAblak.setResizable(false);
        kliensAblak.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        kliensAblak.setLayout(null);
    }

    private void komponensek() {
        cimLabel = new JLabel("Szerver címe:");
        cimLabel.setSize(300, 40);
        cimLabel.setLocation(20, 20);
        cimLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        kliensAblak.add(cimLabel);

        cimText = new JFormattedTextField("kisgabor.sytes.net");
        cimText.setSize(350, 40);
        cimText.setLocation(20, 60);
        cimText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        kliensAblak.add(cimText);

        portLabel = new JLabel("Port:");
        portLabel.setSize(100, 40);
        portLabel.setLocation(20, 120);
        portLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        kliensAblak.add(portLabel);

        portText = new JFormattedTextField(portFormatter);
        portText.setText("9000");
        portText.setSize(80, 40);
        portText.setLocation(120, 120);
        portText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        kliensAblak.add(portText);

        nevLabel = new JLabel("Neved:");
        nevLabel.setSize(100, 40);
        nevLabel.setLocation(20, 180);
        nevLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        kliensAblak.add(nevLabel);

        nevText = new JFormattedTextField();
        nevText.setSize(250, 40);
        nevText.setLocation(120, 180);
        nevText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        kliensAblak.add(nevText);


        csatlakozasGomb = new JButton("Csatlakozás");
        csatlakozasGomb.setSize(200, 40);
        csatlakozasGomb.setLocation(100, 240);
        csatlakozasGomb.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        csatlakozasGomb.addActionListener(e -> csatlakozas());
        kliensAblak.add(csatlakozasGomb);

        log = new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);

        logPane = new JScrollPane(log);
        logPane.setSize(350, 200);
        logPane.setLocation(20, 290);
        kliensAblak.add(logPane);

        uzenetText = new JFormattedTextField();
        uzenetText.setSize(250, 40);
        uzenetText.setLocation(20, 500);
        uzenetText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        uzenetText.setEnabled(false);
        uzenetText.addActionListener(e -> uzenetKuldes());
        kliensAblak.add(uzenetText);

        kuldesGomb = new JButton("Küldés");
        kuldesGomb.setSize(90, 40);
        kuldesGomb.setLocation(280, 500);
        kuldesGomb.setMargin(new Insets(0, 5, 0, 5));
        kuldesGomb.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        kuldesGomb.setEnabled(false);
        kuldesGomb.addActionListener(e -> uzenetKuldes());
        kliensAblak.add(kuldesGomb);
    }

    private void szetkapcsolas() {
        csatlakozva = false;
        cimText.setEnabled(true);
        portText.setEnabled(true);
        nevText.setEnabled(true);
        csatlakozasGomb.setText("Csatlakozás");
        csatlakozasGomb.setEnabled(true);
        uzenetText.setEnabled(false);
        kuldesGomb.setEnabled(false);

        kliensAblak.getContentPane().remove(scrollPane);
        kliensAblak.setSize(SZELESSEG, MAGASSAG);
        kliensAblak.setLocation((KEPERNYO_SZELESSEG - SZELESSEG) / 2, (KEPERNYO_MAGASSAG - MAGASSAG) / 2);

        kliensAblak.remove(scrollPane);
        kliensAblak.remove(idoLabel);
        kliensAblak.remove(jelenlegiJatekosLabel);
        kliensAblak.remove(jatekosLabel);

        timer.stop();
        timer.reset();
    }

    private void board(int meret) {
        int scrollPaneWidth = 580, scrollPaneHeight = 550;
        int boardPanelSize = meret * 15 + 10;

        kliensAblak.setSize(SZELESSEG + 800, MAGASSAG);
        kliensAblak.setLocation((KEPERNYO_SZELESSEG - (SZELESSEG + 800)) / 2, (KEPERNYO_MAGASSAG - MAGASSAG) / 2);
        board = new JButton[meret][meret];

        boardPanel = new JPanel();
        boardPanel.setSize(boardPanelSize, boardPanelSize);
        boardPanel.setLayout(new GridLayout(meret, meret));


        for (int i = 0; i < meret; i++) {
            for (int j = 0; j < meret; j++) {
                board[i][j] = new JButton();
                board[i][j].setPreferredSize(new Dimension(15, 15));
                board[i][j].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                board[i][j].setMargin(new Insets(0, 0, 0, 0));
                board[i][j].setEnabled(false);
                int x = i;
                int y = j;
                board[i][j].addActionListener((e) -> {
                    JButton src = (JButton) e.getSource();
                    if (jelenlegiJatekos.equals(jatekos) && src.getText().equals("")) {
                        kimenet.println("--next:" + x + ":" + y);
                    }
                });
                boardPanel.add(board[i][j]);
            }
        }
        JPanel container = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        container.add(boardPanel, gbc);
        scrollPane = new JScrollPane(container);
        scrollPane.setLocation(400, 5);
        scrollPane.setSize(scrollPaneWidth, scrollPaneHeight);
        kliensAblak.add(scrollPane);

        idoLabel = new JLabel("00:00", SwingConstants.CENTER);
        idoLabel.setSize(200, 40);
        idoLabel.setLocation(980, 20);
        idoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        kliensAblak.add(idoLabel);

        jatekosLabel = new JLabel("", SwingConstants.CENTER);
        jatekosLabel.setSize(200, 40);
        jatekosLabel.setLocation(980, 60);
        jatekosLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        kliensAblak.add(jatekosLabel);

        jelenlegiJatekosLabel = new JLabel("", SwingConstants.CENTER);
        jelenlegiJatekosLabel.setSize(200, 40);
        jelenlegiJatekosLabel.setLocation(980, 100);
        jelenlegiJatekosLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        kliensAblak.add(jelenlegiJatekosLabel);

        timer = new Timer(idoLabel);
    }

    private void csatlakozas() {
        if (!csatlakozva) {
            cim = cimText.getText();
            port = Integer.parseInt(portText.getText());
            nev = nevText.getText();

            cimText.setEnabled(false);
            portText.setEnabled(false);
            nevText.setEnabled(false);
            csatlakozasGomb.setEnabled(false);

            log.append(dateFormat.format(new Date()) + "Csatlakozás...\n");
            try {
                socket = new Socket(cim, port);
                log.append(dateFormat.format(new Date()) + "Csatlakozás sikeres.\n");
                csatlakozva = true;
                kimenet = new PrintWriter(socket.getOutputStream(), true);
                bemenet = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                csatlakozasGomb.setText("Szétkapcsolás");
                csatlakozasGomb.setEnabled(true);
                uzenetText.setEnabled(true);
                kuldesGomb.setEnabled(true);

                kimenet.println(nev);

                String[] adatok = bemenet.readLine().split(":");
                jatekos = adatok[0];
                tablaMeret = Integer.parseInt(adatok[1]);

                if (tablaMeret != 0) {
                    board(tablaMeret);
                    jatekosLabel.setText(jatekos);
                } else {
                    log.append(bemenet.readLine() + "\n");
                    socket.close();
                }

                es.submit(() -> {
                    try (BufferedReader be = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        String uzenet, specUzenet;
                        while ((uzenet = be.readLine()) != null) {
                            if (uzenet.startsWith("--")) {
                                specUzenet = uzenet.substring(2);
                                if (specUzenet.equals("readycheck")) {
                                    kimenet.println("--ready:" + readyCheck());
                                }
                                if (specUzenet.equals("everybodyready")) {
                                    for (int i = 0; i < tablaMeret; i++) {
                                        for (int j = 0; j < tablaMeret; j++) {
                                            board[i][j].setEnabled(true);
                                            board[i][j].setText("");
                                        }
                                    }
                                    timer.reset();
                                    timer.start();
                                }
                                if (specUzenet.startsWith("currentplayer:")) {
                                    jelenlegiJatekos = specUzenet.split(":")[1];
                                    if (jelenlegiJatekos.equals(jatekos)) {
                                        jelenlegiJatekosLabel.setText("Te következel");
                                    } else {
                                        jelenlegiJatekosLabel.setText(jelenlegiJatekos + " következik");
                                    }
                                }
                                if (specUzenet.startsWith("next:")) {
                                    String[] next = specUzenet.split(":");
                                    board[Integer.parseInt(next[1])][Integer.parseInt(next[2])].setText(jelenlegiJatekos);
                                }
                                if (specUzenet.startsWith("xwinnercoord:")) {
                                    String[] x = specUzenet.split(":");
                                    for (int i = 0; i < 5; i++) {
                                        xGyoztesKoord[i] = Integer.parseInt(x[i+1]);
                                    }
                                }
                                if (specUzenet.startsWith("ywinnercoord:")) {
                                    String[] x = specUzenet.split(":");
                                    for (int i = 0; i < 5; i++) {
                                        yGyoztesKoord[i] = Integer.parseInt(x[i+1]);
                                    }                                }
                                if (specUzenet.equals("haswinner")) {
                                    vanGyoztes();
                                    if (jelenlegiJatekos.equals(jatekos)) {
                                        jelenlegiJatekosLabel.setText("Te győztél");
                                    } else {
                                        jelenlegiJatekosLabel.setText(jelenlegiJatekos + " győzött");
                                    }
                                }
                                if (specUzenet.equals("stop")) {
                                    timer.stop();
                                    for (int i = 0; i < tablaMeret; i++) {
                                        for (int j = 0; j < tablaMeret; j++) {
                                            board[i][j].setEnabled(false);
                                        }
                                    }
                                }
                            } else {
                                log.append(uzenet + "\n");
                            }
                        }
                        socket.close();
                        szetkapcsolas();
                    } catch (IOException e) {
                        log.append(dateFormat.format(new Date()) + "Kapcsolat megszakadt.\n");
                        szetkapcsolas();
                    }
                });
            } catch (IOException e) {
                log.append(dateFormat.format(new Date()) + "Csatlakozás sikertelen.\n");
                szetkapcsolas();
            }
        } else {
            szetkapcsolas();

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void vanGyoztes() {
        timer.stop();

        for (int i = 0; i < tablaMeret; i++) {
            for (int j = 0; j < tablaMeret; j++) {
                board[i][j].setEnabled(false);
            }
        }

        for (int i = 0; i < 5; i++) {
            board[xGyoztesKoord[i]][yGyoztesKoord[i]].setEnabled(true);
        }
    }

    private boolean readyCheck() {
        return JOptionPane.showConfirmDialog(kliensAblak, "Készen állsz?", "Ready Check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void uzenetKuldes() {
        if (!uzenetText.getText().equals("")) {
            if (uzenetText.getText().equals("--readynow")) {
                kimenet.println("--ready:true");
            } else {
                kimenet.println(uzenetText.getText());
            }
            uzenetText.setText("");
        }
    }

}
