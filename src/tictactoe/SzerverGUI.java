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
    private int port, jelenlegiJatekosok = 0;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss] ");
    private ExecutorService es = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private List <Jatekos> jatekosok = new ArrayList <>();
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
                inditva = true;

                while (inditva && jelenlegiJatekosok != 2) {
                    Jatekos jatekos = new Jatekos();
                    try {
                        jatekos.setSocket(serverSocket.accept());
                        jatekosok.add(jatekos);
                        jelenlegiJatekosok = jatekosok.size();

                        es.submit(() -> {
                            try {
                                BufferedReader bemenet = new BufferedReader(new InputStreamReader(jatekos.getSocket().getInputStream()));
                                PrintWriter kimenet = new PrintWriter(jatekos.getSocket().getOutputStream(), true);
                                if (jatekosok.size() == 1) {
                                    jatekos.setXO("X");
                                } else {
                                    jatekos.setXO("O");
                                }

                                jatekos.setNev(bemenet.readLine());
                                kimenet.println(tablaMeretText.getText());

                                kimenet.println(dateFormat.format(new Date()) + "Üdvözöllek " + jatekos.getNev() + "!");
                                log.append(dateFormat.format(new Date()) + jatekos.getXO() + " játékos becsatlakozott (" + jatekos.getNev() + ").\n");
                                kozvetit(jatekos.getXO() + " játékos becsatlakozott (" + jatekos.getNev() + ").");
                                String uzenet;
                                while ((uzenet = bemenet.readLine()) != null) {
                                    log.append(dateFormat.format(new Date()) + "[" + jatekos.getNev() + "] " + uzenet + "\n");
                                    kozvetit("[" + jatekos.getNev() + "] " + uzenet);
                                }
                            } catch (IOException e) {
                                jatekosok.remove(jatekos);
                                log.append(dateFormat.format(new Date()) + jatekos.getXO() + " játékos kilépett.\n");
                            }
                        });
                    } catch (IOException e) {
                        jatekosok.remove(jatekos);
                    }
                }

            });
        } else {
            inditva = false;
            jatekosok.clear();
            jelenlegiJatekosok = 0;
            tablaMeretText.setEnabled(true);
            portText.setEnabled(true);
            csatlakozasInditasUtan.setEnabled(true);

            log.append(dateFormat.format(new Date()) + "Szerver leállítása...\n");
            inditasGomb.setEnabled(false);
            try {
                serverSocket.close();
                log.append(dateFormat.format(new Date()) + "Szerver leállítva.\n");
            } catch (IOException e) {
                log.append(dateFormat.format(new Date()) + "Nem sikerült a szerver leállítása!\n");
                log.append(dateFormat.format(new Date()) + e + "\n");
            }
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
        for (Jatekos j : jatekosok) {
            try {
                sKimenet = new PrintWriter(j.getSocket().getOutputStream(), true);
                sKimenet.println(dateFormat.format(new Date()) + uzenet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean csatlakozvaVan(Socket socket) {
        return false;
    }
}
