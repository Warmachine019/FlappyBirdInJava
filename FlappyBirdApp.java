import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FlappyBirdApp extends JFrame implements ActionListener {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, createAccountButton;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/FlappyBirdDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public FlappyBirdApp() {
        setTitle("Flappy Bird - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Login Form
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        panel.add(loginButton);

        createAccountButton = new JButton("Create Account");
        createAccountButton.addActionListener(this);
        panel.add(createAccountButton);

        add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (e.getSource() == loginButton) {
            if (authenticateUser(username, password)) {
                this.dispose();  // Close the login window
                new FlappyBirdGame(username).setVisible(true);  // Start the game
            } else {
                JOptionPane.showMessageDialog(this, "Invalid login. Please try again.");
            }
        } else if (e.getSource() == createAccountButton) {
            if (createAccount(username, password)) {
                JOptionPane.showMessageDialog(this, "Account created! Please log in.");
            } else {
                JOptionPane.showMessageDialog(this, "Account creation failed. Try a different username.");
            }
        }
    }

    private boolean authenticateUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM LoginInfo WHERE Username = ? AND Password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean createAccount(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO LoginInfo (Username, Password) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlappyBirdApp().setVisible(true));
    }
}