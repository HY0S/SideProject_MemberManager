package com.holdempub.membermanager;

import com.holdempub.membermanager.ui.LoginFrame;

import javax.swing.*;

/**
 * 홀덤펍 유저 관리 프로그램 진입점.
 */
public class App {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 기본 L&F 유지
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
