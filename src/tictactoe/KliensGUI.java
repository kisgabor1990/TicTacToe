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
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private JPanel boardPanel;
    private JButton[][] board;

    private MaskFormatter portFormatter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss] ");
    private int port, tablaMeret;
    private String cim, nev;
    private boolean csatlakozva = false;

    private DefaultCaret caret;
    private ExecutorService es = Executors.newCachedThreadPool();
    private PrintWriter kimenet;
    private BufferedReader bemenet;
    private Socket socket;

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

    private void setKomponensekTrue() {
        cimText.setEnabled(true);
        portText.setEnabled(true);
        nevText.setEnabled(true);
        csatlakozasGomb.setEnabled(true);
    }

    private void board(int meret) {
        int scrollPaneMaxWidth = 580, scrollPaneMaxHeight = 550;
        int boardPanelSize = meret * 15 + 10;
        int scrollPaneWidth = Math.min(boardPanelSize, scrollPaneMaxWidth);
        int scrollPaneHeight = Math.min(boardPanelSize, scrollPaneMaxHeight);

        kliensAblak.setSize(1000, 600);
        board = new JButton[meret][meret];

        boardPanel = new JPanel();
        boardPanel.setSize(boardPanelSize, boardPanelSize);
        boardPanel.setLayout(new GridLayout(meret, meret));


        for (int i = 0; i < meret; i++) {
            for (int j = 0; j < meret; j++) {
                board[i][j] = new JButton();
                board[i][j].setPreferredSize(new Dimension(15,15));
                boardPanel.add(board[i][j]);
            }
        }
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        container.add(boardPanel);
        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setLocation(400, 5);
        scrollPane.setSize(scrollPaneWidth, scrollPaneHeight);
        kliensAblak.getContentPane().add(scrollPane);
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
                tablaMeret = Integer.parseInt(bemenet.readLine());
                board(tablaMeret);

                es.submit(() -> {
                    try (BufferedReader be = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        String s;
                        while ((s = be.readLine()) != null) {
                            log.append(s + "\n");
                        }
                    } catch (IOException e) {
                        log.append(dateFormat.format(new Date()) + "Kapcsolat megszakadt.\n");
                        setKomponensekTrue();
                    }
                });
            } catch (IOException e) {
                log.append(dateFormat.format(new Date()) + "Csatlakozás sikertelen.\n");
                setKomponensekTrue();
            }
        } else {
            cimText.setEnabled(true);
            portText.setEnabled(true);
            nevText.setEnabled(true);
            csatlakozva = false;
            csatlakozasGomb.setText("Csatlakozás");
            uzenetText.setEnabled(false);
            kuldesGomb.setEnabled(false);

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uzenetKuldes() {
        if (!uzenetText.getText().equals("")) {
            kimenet.println(uzenetText.getText());
            uzenetText.setText("");
        }
    }

}
