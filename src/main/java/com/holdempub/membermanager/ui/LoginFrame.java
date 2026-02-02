package com.holdempub.membermanager.ui;

import com.holdempub.membermanager.data.ConfigRepository;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 관리자 비밀번호 로그인 화면.
 */
public class LoginFrame extends JFrame {

    private final ConfigRepository configRepo;
    private final JPasswordField passwordField;
    private final JLabel messageLabel;

    public LoginFrame() {
        this(Paths.get("data"));
    }

    public LoginFrame(Path dataPath) {
        this.configRepo = new ConfigRepository(dataPath);
        setTitle("홀덤펍 유저 관리 — 로그인");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("관리자 비밀번호");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        center.add(titleLabel, c);

        passwordField = new JPasswordField(16);
        passwordField.setMargin(new Insets(6, 8, 6, 8));
        c.gridy = 1; c.gridwidth = 1; c.weightx = 1;
        center.add(passwordField, c);

        JButton loginBtn = new JButton("로그인");
        c.gridx = 1; c.weightx = 0;
        center.add(loginBtn, c);

        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.GRAY);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        center.add(messageLabel, c);

        add(center, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());

        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String password = new String(passwordField.getPassword());
        if (password.isEmpty()) {
            messageLabel.setText("비밀번호를 입력하세요.");
            messageLabel.setForeground(Color.DARK_GRAY);
            return;
        }
        if (configRepo.verifyPassword(password)) {
            messageLabel.setText("로그인 성공.");
            messageLabel.setForeground(new Color(0, 128, 0));
            setVisible(false);
            dispose();
            Path dataPath = Path.of("data");
            MainFrame mainFrame = new MainFrame(dataPath);
            mainFrame.setVisible(true);
        } else {
            messageLabel.setText("비밀번호가 올바르지 않습니다.");
            messageLabel.setForeground(Color.RED);
            passwordField.selectAll();
        }
    }
}
