package tictactoe;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class Board {
    private final int KEPERNYO_SZELESSEG = Toolkit.getDefaultToolkit().getScreenSize().width;
    private final int KEPERNYO_MAGASSAG = Toolkit.getDefaultToolkit().getScreenSize().height;

    private int size;
    private JFrame frame;
    private JButton[][] board;
    private JPanel boardPanel, container;
    private JScrollPane scrollPane;
    private JLabel timerLabel, playerLabel, currentPlayerLabel;
    private GridBagConstraints gbc = new GridBagConstraints();

    private Consumer <String> out;
    public Timer timer;

    public Board(int size, JFrame frame, Consumer <String> out) {
        this.size = size;
        this.frame = frame;
        this.out = out;

        this.create();
    }

    private void create() {
        frame.setSize(frame.getWidth() + 800, frame.getHeight());
        frame.setLocation((KEPERNYO_SZELESSEG - frame.getWidth()) / 2, (KEPERNYO_MAGASSAG - frame.getHeight()) / 2);

        board = new JButton[size][size];

        boardPanel = new JPanel();
        boardPanel.setSize(size * 15 + 10, size * 15 + 10);
        boardPanel.setLayout(new GridLayout(size, size));

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = new JButton();
                board[i][j].setPreferredSize(new Dimension(15, 15));
                board[i][j].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                board[i][j].setMargin(new Insets(0, 0, 0, 0));
                board[i][j].setEnabled(false);
                int x = i;
                int y = j;
                board[i][j].addActionListener((e) -> {
                    JButton src = (JButton) e.getSource();
                    if (KliensGUI.jelenlegiJatekos.equals(KliensGUI.jatekos) && src.getText().equals("")) {
                        out.accept("--next:" + x + ":" + y);
                    }
                });
                boardPanel.add(board[i][j]);
            }
        }

        container = new JPanel(new GridBagLayout());
        container.add(boardPanel, gbc);

        scrollPane = new JScrollPane(container);
        scrollPane.setLocation(400, 5);
        scrollPane.setSize(580, 550);
        frame.add(scrollPane);

        timerLabel = new JLabel("00:00", SwingConstants.CENTER);
        timerLabel.setSize(200, 40);
        timerLabel.setLocation(980, 20);
        timerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        frame.add(timerLabel);

        playerLabel = new JLabel("", SwingConstants.CENTER);
        playerLabel.setSize(200, 40);
        playerLabel.setLocation(980, 60);
        playerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        frame.add(playerLabel);

        currentPlayerLabel = new JLabel("", SwingConstants.CENTER);
        currentPlayerLabel.setSize(200, 40);
        currentPlayerLabel.setLocation(980, 100);
        currentPlayerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        frame.add(currentPlayerLabel);

        timer = new Timer(timerLabel);

    }

    public void enable() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j].setEnabled(true);
            }
        }
    }

    public void disable() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j].setEnabled(false);
            }
        }
    }

    public void reset() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j].setEnabled(true);
                board[i][j].setText("");
            }
        }
    }

    public void destroy() {
        frame.remove(scrollPane);
        frame.remove(timerLabel);
        frame.remove(currentPlayerLabel);
        frame.remove(playerLabel);
    }

    public void setPlayer(String player) {
        playerLabel.setText(player);
    }

    public void setCurrentPlayer(String text) {
        currentPlayerLabel.setText(text);
    }

    public void nextStep(int x, int y, String sign) {
        board[x][y].setText(sign);
    }

    public void showWinner(int x, int y) {
        board[x][y].setEnabled(true);
    }
}
