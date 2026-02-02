package com.holdempub.membermanager.ui;

import com.holdempub.membermanager.data.MemberRepository;
import com.holdempub.membermanager.domain.Member;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 점수 관리 화면 — 점수 입력 및 추가.
 */
public class ScoreManagePanel extends JPanel implements MainFrame.Refreshable {

    private final MemberRepository memberRepo;
    private final Runnable onSave;
    private final JComboBox<String> memberCombo;
    private final JSpinner scoreSpinner;
    private final JLabel currentScoreLabel;

    public ScoreManagePanel(MemberRepository memberRepo, Runnable onSave) {
        this.memberRepo = memberRepo;
        this.onSave = onSave != null ? onSave : () -> {};
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("회원:"));
        memberCombo = new JComboBox<>();
        memberCombo.setPreferredSize(new Dimension(160, 28));
        memberCombo.addActionListener(e -> updateCurrentScoreLabel());
        top.add(memberCombo);

        top.add(new JLabel("추가 점수:"));
        scoreSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        top.add(scoreSpinner);

        JButton addBtn = new JButton("점수 추가");
        addBtn.addActionListener(e -> addScore());
        top.add(addBtn);

        currentScoreLabel = new JLabel("현재 점수: -");
        currentScoreLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        top.add(currentScoreLabel);

        add(top, BorderLayout.NORTH);
    }

    private void updateCurrentScoreLabel() {
        String nick = (String) memberCombo.getSelectedItem();
        if (nick == null || nick.isEmpty()) {
            currentScoreLabel.setText("현재 점수: -");
            return;
        }
        List<Member> members = memberRepo.load();
        for (Member m : members) {
            if (m.getNickname().equals(nick)) {
                currentScoreLabel.setText("현재 점수: " + m.getScore());
                return;
            }
        }
        currentScoreLabel.setText("현재 점수: -");
    }

    private void addScore() {
        String nick = (String) memberCombo.getSelectedItem();
        if (nick == null || nick.isEmpty()) {
            JOptionPane.showMessageDialog(this, "회원을 선택하세요.", "선택 필요", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int delta = (Integer) scoreSpinner.getValue();
        List<Member> members = memberRepo.load();
        for (Member m : members) {
            if (m.getNickname().equals(nick)) {
                m.addScore(delta);
                memberRepo.save(members);
                updateCurrentScoreLabel();
                onSave.run();
                JOptionPane.showMessageDialog(this, nick + "님에게 " + delta + "점을 추가했습니다.", "점수 추가", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "선택한 회원을 찾을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void refresh() {
        List<Member> members = memberRepo.load();
        String selected = (String) memberCombo.getSelectedItem();
        memberCombo.removeAllItems();
        memberCombo.addItem("");
        for (Member m : members) {
            memberCombo.addItem(m.getNickname());
        }
        if (selected != null) {
            memberCombo.setSelectedItem(selected);
        }
        updateCurrentScoreLabel();
    }
}
