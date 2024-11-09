import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class FlappyBirdGame extends JFrame implements ActionListener, KeyListener {

    private JPanel startPanel, gamePanel;
    private JLabel counterLabel;
    private Timer gameTimer;
    private long startTime;
    private boolean isPlaying = false;
    private int birdY = 300;
    private int birdVelocity = 0;
    private ArrayList<Rectangle> pipes = new ArrayList<>();
    private JButton startButton, highScoreButton, creditsButton;
    private final String username;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/FlappyBirdDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public FlappyBirdGame(String username) {
        this.username = username;

        setTitle("Flappy Bird");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        startPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(new ImageIcon("StartBG.jpg").getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        };
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
        startPanel.setOpaque(false);

        // Center the buttons by adding struts for spacing
        startButton = new JButton("Start Game");
        customizeButton(startButton);

        highScoreButton = new JButton("View High Scores");
        customizeButton(highScoreButton);

        creditsButton = new JButton("Credits");
        customizeButton(creditsButton);

        // Create a Box container for buttons to align them vertically and push them towards the center
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.add(Box.createVerticalGlue());  // Push buttons towards the center
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(highScoreButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(creditsButton);
        buttonPanel.add(Box.createVerticalGlue());  // Keep buttons centered

        startPanel.add(buttonPanel);
        add(startPanel);

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(new ImageIcon("GameBG.jpg").getImage(), 0, 0, getWidth(), getHeight(), null);
                g.drawImage(new ImageIcon("TheBird.png").getImage(), 100, birdY, 100, 72, null);

                // Draw pipes with images
                Image pipeImage = new ImageIcon("Pipe.png").getImage();  // Replace with your pipe image path
                for (Rectangle pipe : pipes) {
                    g.drawImage(pipeImage, pipe.x, pipe.y, pipe.width, pipe.height, null);
                }
            }
        };
        gamePanel.setBackground(Color.CYAN);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        counterLabel = new JLabel("Time: 0s", SwingConstants.CENTER);
        counterLabel.setFont(new Font("Arial", Font.BOLD, 20));
        counterLabel.setForeground(Color.WHITE);  // Set counter label text to white
        gamePanel.add(counterLabel);

        gameTimer = new Timer(20, e -> gameLoop());
    }

    private void customizeButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.BOLD, 24));  // Bold font for better visibility
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setForeground(Color.BLACK);  // Set button text to black
        button.addActionListener(this);
    }

    private void startGame() {
        isPlaying = true;
        birdY = 300;
        birdVelocity = 0;
        pipes.clear();
        startTime = System.nanoTime();
        counterLabel.setText("Time: 0s");
        add(gamePanel);
        startPanel.setVisible(false);
        gamePanel.setVisible(true);
        gamePanel.requestFocus();
        gameTimer.start();
    }

    private void gameLoop() {
        long elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000;
        counterLabel.setText("Time: " + elapsedTime + "s");

        birdVelocity += 1;
        birdY += birdVelocity;

        if (birdY > getHeight() || birdY < 0 || checkCollision()) {
            endGame(elapsedTime);
        } else {
            movePipes();
            gamePanel.repaint();
        }
    }

    private boolean checkCollision() {
        Rectangle birdHitbox = new Rectangle(100, birdY, 100, 72);
        for (Rectangle pipe : pipes) {
            if (pipe.intersects(birdHitbox)) {
                return true;
            }
        }
        return false;
    }

    private void movePipes() {
        for (int i = pipes.size() - 1; i >= 0; i--) {
            Rectangle pipe = pipes.get(i);
            pipe.x -= 5;
            if (pipe.x + pipe.width < 0) {
                pipes.remove(i);
            }
        }

        if (pipes.isEmpty() || pipes.get(pipes.size() - 1).x < getWidth() - 300) {
            int pipeHeight = 150 + (int)(Math.random() * 200);
            pipes.add(new Rectangle(getWidth(), 0, 80, pipeHeight));
            pipes.add(new Rectangle(getWidth(), pipeHeight + 200, 80, getHeight() - pipeHeight - 200));
        }
    }

    private void endGame(long time) {
        gameTimer.stop();
        JOptionPane.showMessageDialog(this, "Game Over! Time survived: " + time + " seconds.");
        saveHighScore(time);
        gamePanel.setVisible(false);
        startPanel.setVisible(true);
    }

    private void saveHighScore(long time) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO HighScores (Username, Score) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setLong(2, time);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showHighScores() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT Username, Score FROM HighScores ORDER BY Score DESC LIMIT 10";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                StringBuilder highScores = new StringBuilder("High Scores:\n");
                boolean hasEntries = false;
                while (rs.next()) {
                    highScores.append(rs.getString("Username")).append(": ").append(rs.getLong("Score")).append("s\n");
                    hasEntries = true;
                }
                if (!hasEntries) {
                    JOptionPane.showMessageDialog(this, "No Entries");
                } else {
                    JOptionPane.showMessageDialog(this, highScores.toString());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showCredits() {
        JOptionPane.showMessageDialog(this, "<html><center>Thank you for Playing!<br><br>Devs:<br>Spandan(918)<br>Nadin(912)<br>Jacob(919)<br>Aseem(890)</center></html>", 
                                      "Credits", JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            startGame();
        } else if (e.getSource() == highScoreButton) {
            showHighScores();
        } else if (e.getSource() == creditsButton) {
            showCredits();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isPlaying && e.getKeyCode() == KeyEvent.VK_SPACE) {
            birdVelocity = -10;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
