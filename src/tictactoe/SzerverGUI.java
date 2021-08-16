package tictactoe;

import com.dosse.upnp.UPnP;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SzerverGUI {

    private static final int SZELESSEG = 450, MAGASSAG = 540;
    private static final int KEPERNYO_SZELESSEG = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int KEPERNYO_MAGASSAG = Toolkit.getDefaultToolkit().getScreenSize().height;

    public static JFrame szerverAblak;
    private static JLabel tablaMeretLabel, portLabel;
    private static JFormattedTextField tablaMeretText, portText;
    private static JCheckBox csatlakozasInditasUtan;
    private static JButton inditasGomb;
    private static JTextArea log;
    private static JScrollPane logPane;

    private static MaskFormatter tablaMeretFormatter, portFormatter;
    private static String IPcim;
    private static boolean inditva = false;
    private static int port;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss] ");
    private static ExecutorService es = Executors.newFixedThreadPool(2);
    private static ServerSocket serverSocket;
    private static List <Jatekos> jatekosok = new ArrayList <>();

    private static DefaultCaret caret;


    public static void szerverAblak() {
        szerverAblak = new JFrame("Szerver indítása");
        szerverAblak.setSize(SZELESSEG, MAGASSAG);
        szerverAblak.setLocation((KEPERNYO_SZELESSEG - SZELESSEG) / 2, (KEPERNYO_MAGASSAG - MAGASSAG) / 2);
        szerverAblak.setResizable(false);
        szerverAblak.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        szerverAblak.setLayout(null);
    }

    public static void komponensek() {
        tablaMeretLabel = new JLabel("Tábla mérete:", SwingConstants.RIGHT);
        tablaMeretLabel.setSize(300, 40);
        tablaMeretLabel.setLocation(20, 20);
        tablaMeretLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        szerverAblak.add(tablaMeretLabel);

        try {
            tablaMeretFormatter = new MaskFormatter("##");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tablaMeretText = new JFormattedTextField(tablaMeretFormatter);
        tablaMeretText.setText("99");
        tablaMeretText.setSize(80, 40);
        tablaMeretText.setLocation(330, 20);
        tablaMeretText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        szerverAblak.add(tablaMeretText);

        IPcim = getIP();
        portLabel = new JLabel("IP: " + IPcim + " Port:", SwingConstants.RIGHT);
        portLabel.setSize(300, 40);
        portLabel.setLocation(20, 80);
        portLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        szerverAblak.add(portLabel);

        try {
            portFormatter = new MaskFormatter("####");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        portText = new JFormattedTextField(portFormatter);
        portText.setText("9000");
        portText.setSize(80, 40);
        portText.setLocation(330, 80);
        portText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        szerverAblak.add(portText);

        csatlakozasInditasUtan = new JCheckBox("Csatlakozás a játékhoz indítás után");
        csatlakozasInditasUtan.setSize(400, 40);
        csatlakozasInditasUtan.setLocation(20, 140);
        csatlakozasInditasUtan.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        szerverAblak.add(csatlakozasInditasUtan);

        inditasGomb = new JButton("Indítás");
        inditasGomb.setSize(200, 40);
        inditasGomb.setLocation(125, 200);
        inditasGomb.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        inditasGomb.addActionListener(e -> szerverInditas());
        szerverAblak.add(inditasGomb);

        log = new JTextArea();
        log.setEditable(false);
        caret = (DefaultCaret) log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        logPane = new JScrollPane(log);
        logPane.setSize(400, 200);
        logPane.setLocation(20, 270);
        szerverAblak.add(logPane);
    }

    private static void szerverInditas() {
        port = Integer.parseInt(portText.getText());

        if (!inditva) {
            tablaMeretText.setEnabled(false);
            portText.setEnabled(false);
            csatlakozasInditasUtan.setEnabled(false);
            inditva = true;


            log.append(dateFormat.format(new Date()) + "Szerver indítása...\n");
            Thread openPortThread = new Thread(() -> {
                inditasGomb.setEnabled(false);
                try {
                    serverSocket = new ServerSocket(port);
                } catch (IOException e) {
                    log.append(dateFormat.format(new Date()) + "Nem sikerült a szerver indítása!\n");
                    log.append(dateFormat.format(new Date()) + e + "\n");
                }
                UPnP.openPortTCP(port);
                inditasGomb.setText("Leállítás");
                inditasGomb.setEnabled(true);
                log.append(dateFormat.format(new Date()) + "A szerver várakozik a következő porton: " + port + "\n");
                while (inditva) {
                    Jatekos jatekos = new Jatekos();
                    try {
                        jatekos.setSocket(serverSocket.accept());
                        jatekosok.add(jatekos);

                        es.submit(() -> {
                            try {
                                BufferedReader bemenet = new BufferedReader(new InputStreamReader(jatekos.getSocket().getInputStream()));
                                PrintWriter kimenet = new PrintWriter(jatekos.getSocket().getOutputStream(), true);

                                if (jatekosok.size() == 1) {
                                    jatekos.setXO("X");
                                } else {
                                    jatekos.setXO("O");
                                }
                                kozvetit(jatekos.getXO() + " játékos becsatlakozot.");
                                log.append(dateFormat.format(new Date()) + jatekos.getXO() + " játékos becsatlakozott.");
                            } catch (IOException e) {
                                jatekosok.remove(jatekos);
                                log.append(dateFormat.format(new Date()) + e + "\n");
                            }
                        });
                    } catch (IOException e) {
                        jatekosok.remove(jatekos);
                    }
                }
            });
            openPortThread.start();


        } else {
            tablaMeretText.setEnabled(true);
            portText.setEnabled(true);
            csatlakozasInditasUtan.setEnabled(true);
            inditva = false;

            log.append(dateFormat.format(new Date()) + "Szerver leállítása...\n");
            new Thread(() -> {
                inditasGomb.setEnabled(false);
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    log.append(dateFormat.format(new Date()) + "Nem sikerült a szerver leállítása!\n");
                    log.append(dateFormat.format(new Date()) + e + "\n");
                }
                UPnP.closePortTCP(port);
                inditasGomb.setText("Indítás");
                inditasGomb.setEnabled(true);
                log.append(dateFormat.format(new Date()) + "A szerver leállt.\n");
            }, "closePortThread").start();
        }
    }

    public static String getIP() {
        String myip = "";
        try {
            URL checkip = new URL("http://checkip.amazonaws.com");
            BufferedReader br = new BufferedReader(new InputStreamReader(checkip.openStream()));
            myip = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myip;
    }

    private static void kozvetit(String uzenet) {
        for (Jatekos j : jatekosok) {
            PrintWriter sKimenet = null;
            try {
                sKimenet = new PrintWriter(j.getSocket().getOutputStream(), true);
                sKimenet.println(dateFormat.format(new Date()) + uzenet);
            } catch (IOException e) {
                log.append(dateFormat.format(new Date()) + e + "\n");
            }
        }
    }
}
