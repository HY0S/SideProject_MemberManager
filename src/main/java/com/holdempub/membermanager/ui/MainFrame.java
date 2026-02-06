package com.holdempub.membermanager.ui;

import com.holdempub.membermanager.data.BingoRepository;
import com.holdempub.membermanager.data.MemberRepository;
import com.holdempub.membermanager.data.ScoreCriteriaRepository;

import javax.swing.*;
import java.nio.file.Path;

/**
 * 메인 프레임 — 탭: 메인(회원 목록), 회원 관리, 점수 관리, 빙고.
 */
public class MainFrame extends JFrame {

    private final MemberRepository memberRepo;
    private final BingoRepository bingoRepo;
    private final JTabbedPane tabbedPane;
    private final MainPanel mainPanel;
    private final MemberManagePanel memberManagePanel;
    private final ScoreManagePanel scoreManagePanel;
    private final BingoPanel bingoPanel;

    public MainFrame(Path dataPath) {
        this.memberRepo = new MemberRepository(dataPath);
        this.bingoRepo = new BingoRepository(dataPath);
        ScoreCriteriaRepository scoreCriteriaRepo = new ScoreCriteriaRepository(dataPath);

        setTitle("홀덤펍 유저 관리");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 520);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        mainPanel = new MainPanel(memberRepo);
        memberManagePanel = new MemberManagePanel(memberRepo, this::refreshAllTabs);
        scoreManagePanel = new ScoreManagePanel(memberRepo, scoreCriteriaRepo, this::refreshAllTabs);
        bingoPanel = new BingoPanel(bingoRepo, memberRepo);

        tabbedPane.addTab("메인", mainPanel);
        tabbedPane.addTab("회원 관리", memberManagePanel);
        tabbedPane.addTab("점수 관리", scoreManagePanel);
        tabbedPane.addTab("빙고", bingoPanel);

        tabbedPane.addChangeListener(e -> {
            JPanel selected = (JPanel) tabbedPane.getSelectedComponent();
            if (selected instanceof Refreshable r) {
                r.refresh();
            }
        });

        add(tabbedPane);
        mainPanel.refresh();
    }

    private void refreshAllTabs() {
        mainPanel.refresh();
        memberManagePanel.refresh();
        scoreManagePanel.refresh();
        bingoPanel.refresh();
    }

    /** 탭 전환 시 데이터 다시 불러오기용. */
    public interface Refreshable {
        void refresh();
    }
}
