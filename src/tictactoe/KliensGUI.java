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
    private JScrollPane logPane;

    private MaskFormatter portFormatter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss] ");
    private int port, tablaMeret, xWins, oWins;
    private String cim, nev;
    public static String jelenlegiJatekos, jatekos;
    private boolean csatlakozva = false;
    private int[] xGyoztesKoord = new int[5];
    private int[] yGyoztesKoord = new int[5];

    private DefaultCaret caret;
    private ExecutorService es = Executors.newCachedThreadPool();
    private PrintWriter kimenet;
    private BufferedReader bemenet;
    private Socket socket;
    private Board board;

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
        kliensAblak = new JFrame("TicTacToe J??t??k");
        kliensAblak.setSize(SZELESSEG, MAGASSAG);
        kliensAblak.setLocation((KEPERNYO_SZELESSEG - SZELESSEG) / 2, (KEPERNYO_MAGASSAG - MAGASSAG) / 2);
        kliensAblak.setResizable(false);
        kliensAblak.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        kliensAblak.setLayout(null);
    }

    private void komponensek() {
        cimLabel = new JLabel("Szerver c??me:");
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


        csatlakozasGomb = new JButton("Csatlakoz??s");
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

        kuldesGomb = new JButton("K??ld??s");
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
        csatlakozasGomb.setText("Csatlakoz??s");
        csatlakozasGomb.setEnabled(true);
        uzenetText.setEnabled(false);
        kuldesGomb.setEnabled(false);

        kliensAblak.setSize(SZELESSEG, MAGASSAG);
        kliensAblak.setLocation((KEPERNYO_SZELESSEG - SZELESSEG) / 2, (KEPERNYO_MAGASSAG - MAGASSAG) / 2);
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

            log.append(dateFormat.format(new Date()) + "Csatlakoz??s...\n");
            try {
                socket = new Socket(cim, port);
                log.append(dateFormat.format(new Date()) + "Csatlakoz??s sikeres.\n");
                csatlakozva = true;
                kimenet = new PrintWriter(socket.getOutputStream(), true);
                bemenet = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                csatlakozasGomb.setText("Sz??tkapcsol??s");
                csatlakozasGomb.setEnabled(true);
                uzenetText.setEnabled(true);
                kuldesGomb.setEnabled(true);

                kimenet.println(nev);

                String[] adatok = bemenet.readLine().split(":");
                jatekos = adatok[0];
                tablaMeret = Integer.parseInt(adatok[1]);

                if (tablaMeret != 0) {
                    board = new Board(tablaMeret, kliensAblak, kimenet::println);
                    board.setPlayer(jatekos);
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
                                    board.reset();
                                    board.disable();
                                    board.timer.reset();
                                    board.setWins(xWins, oWins);
                                    kimenet.println("--ready:" + readyCheck());
                                }
                                if (specUzenet.equals("everybodyready")) {
                                    board.enable();
                                    board.timer.start();
                                }
                                if (specUzenet.startsWith("currentplayer:")) {
                                    jelenlegiJatekos = specUzenet.split(":")[1];
                                    if (jelenlegiJatekos.equals(jatekos)) {
                                        board.setCurrentPlayer("Te k??vetkezel");
                                    } else {
                                        board.setCurrentPlayer(jelenlegiJatekos + " k??vetkezik");
                                    }
                                }
                                if (specUzenet.startsWith("next:")) {
                                    String[] next = specUzenet.split(":");
                                    board.nextStep(Integer.parseInt(next[1]), Integer.parseInt(next[2]), jelenlegiJatekos);
                                }
                                if (specUzenet.startsWith("xwinnercoord:")) {
                                    String[] x = specUzenet.split(":");
                                    for (int i = 0; i < 5; i++) {
                                        xGyoztesKoord[i] = Integer.parseInt(x[i + 1]);
                                    }
                                }
                                if (specUzenet.startsWith("ywinnercoord:")) {
                                    String[] x = specUzenet.split(":");
                                    for (int i = 0; i < 5; i++) {
                                        yGyoztesKoord[i] = Integer.parseInt(x[i + 1]);
                                    }
                                }
                                if (specUzenet.startsWith("haswinner:")) {
                                    String winner = specUzenet.split(":")[1];
                                    xWins = Integer.parseInt(specUzenet.split(":")[2]);
                                    oWins = Integer.parseInt(specUzenet.split(":")[3]);
                                    vanGyoztes();
                                    if (winner.equals(jatekos)) {
                                        board.setCurrentPlayer("Te gy??zt??l");
                                    } else {
                                        board.setCurrentPlayer(winner + " gy??z??tt");
                                    }
                                }
                                if (specUzenet.equals("wantmore")) {
                                    kimenet.println("--wantmore:" + wantMore());
                                }
                                if (specUzenet.equals("boardfull")) {
                                    JOptionPane.showMessageDialog(kliensAblak, "A j??t??kmez?? megtelt! ??res t??bl??val folytatjuk!");
                                    board.reset();
                                }
                                if (specUzenet.equals("stop")) {
                                    board.timer.stop();
                                    board.disable();
                                    xWins = 0;
                                    oWins = 0;
                                }
                            } else {
                                log.append(uzenet + "\n");
                            }
                        }
                        socket.close();
                        szetkapcsolas();
                        board.destroy();
                        board.timer.stop();
                        board.timer.reset();
                    } catch (IOException e) {
                        log.append(dateFormat.format(new Date()) + "Kapcsolat megszakadt.\n");
                        szetkapcsolas();
                        board.destroy();
                        board.timer.stop();
                        board.timer.reset();
                    }
                });
            } catch (IOException e) {
                log.append(dateFormat.format(new Date()) + "Csatlakoz??s sikertelen.\n");
                szetkapcsolas();
            }
        } else {
            szetkapcsolas();
            board.destroy();
            board.timer.stop();
            board.timer.reset();
            xWins = 0;
            oWins = 0;

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean wantMore() {
        return JOptionPane.showConfirmDialog(kliensAblak, "Szeretn??l m??gegyet j??tszani?", "Tov??bbi j??t??k", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void vanGyoztes() {
        board.timer.stop();
        board.disable();

        for (int i = 0; i < 5; i++) {
            board.showWinner(xGyoztesKoord[i], yGyoztesKoord[i]);
        }

        board.setWins(xWins, oWins);
    }

    private boolean readyCheck() {
        return JOptionPane.showConfirmDialog(kliensAblak, "K??szen ??llsz?", "Ready Check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void uzenetKuldes() {
        if (!uzenetText.getText().equals("")) {
            if (uzenetText.getText().equals("--readynow")) {
                kimenet.println("--ready:true");
            } else if (uzenetText.getText().equals("--wantmore")) {
                kimenet.println("--wantmore:true");
            } else {
                kimenet.println(uzenetText.getText());
            }
            uzenetText.setText("");
        }
    }

    public void setAddress(String text) {
        cimText.setText(text);
        cimText.setEnabled(false);
    }

    public void setPort(String port) {
        portText.setText(port);
        portText.setEnabled(false);
    }

}
