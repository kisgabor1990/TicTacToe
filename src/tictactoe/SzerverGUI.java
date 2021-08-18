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
import java.util.concurrent.TimeUnit;

public class SzerverGUI {

    private final int SZELESSEG = 450, MAGASSAG = 540;
    private final int KEPERNYO_SZELESSEG = Toolkit.getDefaultToolkit().getScreenSize().width;
    private final int KEPERNYO_MAGASSAG = Toolkit.getDefaultToolkit().getScreenSize().height;

    public JFrame szerverAblak;
    private JLabel tablaMeretLabel, portLabel;
    private JFormattedTextField tablaMeretText, portText;
    private JCheckBox csatlakozasInditasUtan;
    private JButton inditasGomb;
    private JTextArea log;
    private JScrollPane logPane;

    private MaskFormatter tablaMeretFormatter, portFormatter;
    private String IPcim;
    private boolean inditva = false;
    private int port;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss] ");
    private ExecutorService es = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private Jatekos jatekosX = null, jatekosO = null;
    private DefaultCaret caret;

    public void szerverMain() {
        IPcim = getIP();
        try {
            tablaMeretFormatter = new MaskFormatter("##");
            portFormatter = new MaskFormatter("####");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        szerverAblak();
        komponensek();
        szerverAblak.setVisible(true);

        caret = (DefaultCaret) log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }


    public void szerverAblak() {
        szerverAblak = new JFrame("Szerver indítása");
        szerverAblak.setSize(SZELESSEG, MAGASSAG);
        szerverAblak.setLocation((KEPERNYO_SZELESSEG - SZELESSEG) / 2, (KEPERNYO_MAGASSAG - MAGASSAG) / 2);
        szerverAblak.setResizable(false);
        szerverAblak.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        szerverAblak.setLayout(null);
    }

    public void komponensek() {
        tablaMeretLabel = new JLabel("Tábla mérete:", SwingConstants.RIGHT);
        tablaMeretLabel.setSize(300, 40);
        tablaMeretLabel.setLocation(20, 20);
        tablaMeretLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        szerverAblak.add(tablaMeretLabel);

        tablaMeretText = new JFormattedTextField(tablaMeretFormatter);
        tablaMeretText.setText("35");
        tablaMeretText.setSize(80, 40);
        tablaMeretText.setLocation(330, 20);
        tablaMeretText.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        szerverAblak.add(tablaMeretText);

        portLabel = new JLabel("IP: " + IPcim + " Port:", SwingConstants.RIGHT);
        portLabel.setSize(300, 40);
        portLabel.setLocation(20, 80);
        portLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        szerverAblak.add(portLabel);

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
        log.setLineWrap(true);
        log.setWrapStyleWord(true);

        logPane = new JScrollPane(log);
        logPane.setSize(400, 200);
        logPane.setLocation(20, 270);
        szerverAblak.add(logPane);
    }

    private void szerverInditas() {
        port = Integer.parseInt(portText.getText());

        if (!inditva) {
            tablaMeretText.setEnabled(false);
            portText.setEnabled(false);
            csatlakozasInditasUtan.setEnabled(false);
            inditasGomb.setEnabled(false);

            log.append(dateFormat.format(new Date()) + "Szerver indítása...\n");
            try {
                serverSocket = new ServerSocket(port);
                log.append(dateFormat.format(new Date()) + "Szerver elindítva.\n");
                inditva = true;
            } catch (IOException e) {
                log.append(dateFormat.format(new Date()) + "Nem sikerült a szerver indítása!\n");
                log.append(dateFormat.format(new Date()) + e + "\n");
            }
            log.append(dateFormat.format(new Date()) + "Port nyitása...\n");

            es.submit(() -> {
                UPnP.openPortTCP(port);
                log.append(dateFormat.format(new Date()) + "Port nyitva: " + port + "\n");
                inditasGomb.setText("Leállítás");
                inditasGomb.setEnabled(true);
                log.append(dateFormat.format(new Date()) + "A szerver várakozik a következő porton: " + port + "\n");
            });

            es.submit(() -> {
                while (inditva) {
                    Jatekos jatekos = new Jatekos();
                    try {
                        jatekos.setSocket(serverSocket.accept());

                        es.submit(() -> {
                            try {
                                BufferedReader bemenet = new BufferedReader(new InputStreamReader(jatekos.getSocket().getInputStream()));
                                PrintWriter kimenet = new PrintWriter(jatekos.getSocket().getOutputStream(), true);

                                jatekos.setNev(bemenet.readLine());
                                if (jatekosX == null || jatekosO == null) {

                                    if (jatekosX == null) {
                                        jatekos.setXO("X");
                                        jatekosX = jatekos;
                                    } else {
                                        jatekos.setXO("O");
                                        jatekosO = jatekos;
                                    }

                                    kimenet.println(tablaMeretText.getText());

                                    kimenet.println(dateFormat.format(new Date()) + "Üdvözöllek " + jatekos.getNev() + "!");
                                    log.append(dateFormat.format(new Date()) + jatekos.getXO() + " játékos becsatlakozott (" + jatekos.getNev() + ").\n");
                                    kozvetit(jatekos.getXO() + " játékos becsatlakozott (" + jatekos.getNev() + ").");

                                    if (jatekosX != null && jatekosO != null) {
                                        specKozvetit("--readycheck");
                                    }

                                    String uzenet, specUzenet;
                                    while ((uzenet = bemenet.readLine()) != null) {
                                        if (uzenet.startsWith("--")) {
                                            specUzenet = uzenet.substring(2);
                                            if (specUzenet.startsWith("ready:") && !jatekos.isReady()) {
                                                boolean ready = Boolean.parseBoolean(specUzenet.split(":")[1]);
                                                if (ready) {
                                                    log.append(dateFormat.format(new Date()) + jatekos.getXO() + " játékos készen áll.\n");
                                                    kozvetit(jatekos.getXO() + " játékos készen áll.");
                                                } else {
                                                    log.append(dateFormat.format(new Date()) + jatekos.getXO() + " játékos nem áll készen.\n");
                                                    kozvetit(jatekos.getXO() + " játékos nem áll készen.");
                                                    kimenet.println(dateFormat.format(new Date()) + "Ha úgy érzed, készen állsz, írd be: --readynow");
                                                }
                                                if (jatekos.getXO().equals("X")) {
                                                    jatekosX.setReady(ready);
                                                } else {
                                                    jatekosO.setReady(ready);
                                                }
                                            }
                                        } else {
                                            log.append(dateFormat.format(new Date()) + "[" + jatekos.getNev() + "] " + uzenet + "\n");
                                            kozvetit("[" + jatekos.getNev() + "] " + uzenet);
                                        }
                                        if (jatekosX.isReady() && jatekosO.isReady()) {
                                            specKozvetit("--everybodyready");
                                            kozvetit("Mindenki készen áll. Kezdőjön a játék!");
                                        }
                                    }
                                    log.append(dateFormat.format(new Date()) + jatekos.getXO() + " játékos kilépett.\n");
                                    kozvetit(jatekos.getXO() + " játékos kilépett.");
                                    if (jatekos.getXO().equals("X")) {
                                        jatekosX = null;
                                    } else {
                                        jatekosO = null;
                                    }
                                } else {
                                    kimenet.println("0");
                                    kimenet.println(dateFormat.format(new Date()) + "Sajnálom kedves " + jatekos.getNev() + ", a szerver megtelt!");
                                    jatekos.getSocket().close();
                                }
                            } catch (IOException e) {
                                log.append(dateFormat.format(new Date()) + jatekos.getXO() + " játékos kilépett.\n");
                                kozvetit(jatekos.getXO() + " játékos kilépett.");
                                if (jatekos.getXO().equals("X")) {
                                    jatekosX = null;
                                } else {
                                    jatekosO = null;
                                }
                            }
                        });
                    } catch (IOException e) {
                        if (jatekos.getXO().equals("X")) {
                            jatekosX = null;
                        } else {
                            jatekosO = null;
                        }
                    }
                }
            });

        } else {
            inditva = false;
            tablaMeretText.setEnabled(true);
            portText.setEnabled(true);
            csatlakozasInditasUtan.setEnabled(true);
            kozvetit("A szerver leállt!");
            log.append(dateFormat.format(new Date()) + "Szerver leállítása...\n");
            inditasGomb.setEnabled(false);
            try {
                serverSocket.close();
                log.append(dateFormat.format(new Date()) + "Szerver leállítva.\n");
                for (Jatekos j : new Jatekos[] { jatekosX, jatekosO }) {
                    if (j != null) {
                        j.getSocket().close();
                    }
                }
            } catch (IOException e) {
                log.append(dateFormat.format(new Date()) + "Nem sikerült a szerver leállítása!\n");
                log.append(dateFormat.format(new Date()) + e + "\n");
            }

            jatekosX = null;
            jatekosO = null;

            log.append(dateFormat.format(new Date()) + "Port lezárása...\n");
            es.submit(() -> {
                UPnP.closePortTCP(port);
                log.append(dateFormat.format(new Date()) + "Port lezárva: " + port + "\n");
                inditasGomb.setText("Indítás");
                inditasGomb.setEnabled(true);
            });
        }

    }

    public String getIP() {
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

    private void kozvetit(String uzenet) {
        PrintWriter sKimenet;
        for (Jatekos j : new Jatekos[] { jatekosX, jatekosO }) {
            if (j != null) {
                try {
                    sKimenet = new PrintWriter(j.getSocket().getOutputStream(), true);
                    sKimenet.println(dateFormat.format(new Date()) + uzenet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void specKozvetit(String uzenet) {
        PrintWriter sKimenet;
        for (Jatekos j : new Jatekos[] { jatekosX, jatekosO }) {
            if (j != null) {
                try {
                    sKimenet = new PrintWriter(j.getSocket().getOutputStream(), true);
                    sKimenet.println(uzenet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean csatlakozvaVan(Socket socket) {
        return false;
    }
}
