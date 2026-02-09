package com.holdempub.membermanager.ui;

import com.holdempub.membermanager.data.MemberRepository;
import com.holdempub.membermanager.data.ScoreCriteriaRepository;
import com.holdempub.membermanager.domain.Member;
import com.holdempub.membermanager.domain.ScoreCriterion;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 점수 관리 화면 — 기준별 버튼으로 점수 추가, 사용자 지정 추가, 점수 차감.
 */
public class ScoreManagePanel extends JPanel implements MainFrame.Refreshable {

    private final MemberRepository memberRepo;
    private final ScoreCriteriaRepository criteriaRepo;
    private final Runnable onSave;
    private final MemberSearchField memberSearchField;
    private final JLabel currentScoreLabel;
    private final JPanel criterionButtonsPanel;

    public ScoreManagePanel(MemberRepository memberRepo, ScoreCriteriaRepository criteriaRepo, Runnable onSave) {
        this.memberRepo = memberRepo;
        this.criteriaRepo = criteriaRepo;
        this.onSave = onSave != null ? onSave : () -> {};
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("회원 검색:"));
        memberSearchField = new MemberSearchField(18);
        memberSearchField.addSelectionListener(e -> updateCurrentScoreLabel());
        top.add(memberSearchField);

        JButton criteriaSetupBtn = new JButton("기준 설정");
        criteriaSetupBtn.addActionListener(e -> openCriteriaSetup());
        top.add(criteriaSetupBtn);

        currentScoreLabel = new JLabel("현재 점수: -");
        currentScoreLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        top.add(currentScoreLabel);

        add(top, BorderLayout.NORTH);

        criterionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        criterionButtonsPanel.setBorder(BorderFactory.createTitledBorder("점수 추가 (기준별)"));
        add(criterionButtonsPanel, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton customAddBtn = new JButton("사용자 지정 추가");
        customAddBtn.addActionListener(e -> addCustomScore());
        south.add(customAddBtn);

        JButton subtractBtn = new JButton("점수 차감");
        subtractBtn.addActionListener(e -> subtractScore());
        south.add(subtractBtn);

        add(south, BorderLayout.SOUTH);
        refreshCriterionButtons();
    }

    private void openCriteriaSetup() {
        List<ScoreCriterion> criteria = new ArrayList<>(criteriaRepo.load());
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "점수 기준 설정", true);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setSize(400, 380);
        dialog.setLocationRelativeTo(this);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (ScoreCriterion c : criteria) {
            listModel.addElement(c.getName() + " — " + c.getPoints() + "점");
        }
        JList<String> list = new JList<>(listModel);
        JScrollPane scroll = new JScrollPane(list);

        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        JTextField nameField = new JTextField(10);
        JSpinner pointsSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
        JButton addCriterionBtn = new JButton("추가");
        addCriterionBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "기준 이름을 입력하세요.");
                return;
            }
            int pts = (Integer) pointsSpinner.getValue();
            criteria.add(new ScoreCriterion(name, pts));
            listModel.addElement(name + " — " + pts + "점");
            nameField.setText("");
        });
        JButton deleteCriterionBtn = new JButton("삭제");
        deleteCriterionBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) {
                criteria.remove(idx);
                listModel.remove(idx);
            }
        });
        editPanel.add(new JLabel("이름:"));
        editPanel.add(nameField);
        editPanel.add(new JLabel("점수:"));
        editPanel.add(pointsSpinner);
        editPanel.add(addCriterionBtn);
        editPanel.add(deleteCriterionBtn);

        JButton okBtn = new JButton("저장");
        okBtn.addActionListener(e -> {
            criteriaRepo.save(criteria);
            refreshCriterionButtons();
            dialog.dispose();
        });
        JButton cancelBtn = new JButton("취소");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancelBtn);
        bottom.add(okBtn);

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(editPanel, BorderLayout.NORTH);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void refreshCriterionButtons() {
        criterionButtonsPanel.removeAll();
        List<ScoreCriterion> criteria = criteriaRepo.load();
        for (ScoreCriterion c : criteria) {
            if (c.getName().isEmpty()) continue;
            JButton btn = new JButton(c.getName() + " (" + c.getPoints() + "점)");
            int points = c.getPoints();
            btn.addActionListener(e -> addScoreByCriterion(points));
            criterionButtonsPanel.add(btn);
        }
        criterionButtonsPanel.revalidate();
        criterionButtonsPanel.repaint();
    }

    private void addScoreByCriterion(int points) {
        String nick = memberSearchField.getSelectedMember();
        if (nick == null || nick.isEmpty()) {
            JOptionPane.showMessageDialog(this, "회원을 선택하세요.", "선택 필요", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Member> members = memberRepo.load();
        for (Member m : members) {
            if (m.getNickname().equals(nick)) {
                m.addScore(points);
                memberRepo.save(members);
                updateCurrentScoreLabel();
                onSave.run();
                JOptionPane.showMessageDialog(this, nick + "님에게 " + points + "점을 추가했습니다.", "점수 추가", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "선택한 회원을 찾을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
    }

    private void addCustomScore() {
        String nick = memberSearchField.getSelectedMember();
        if (nick == null || nick.isEmpty()) {
            JOptionPane.showMessageDialog(this, "회원을 선택하세요.", "선택 필요", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this, "추가할 점수를 입력하세요.", "사용자 지정 추가", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            int delta = Integer.parseInt(input.trim());
            if (delta <= 0) {
                JOptionPane.showMessageDialog(this, "1 이상의 숫자를 입력하세요.");
                return;
            }
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
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "올바른 숫자를 입력하세요.");
        }
    }

    private void subtractScore() {
        String nick = memberSearchField.getSelectedMember();
        if (nick == null || nick.isEmpty()) {
            JOptionPane.showMessageDialog(this, "회원을 선택하세요.", "선택 필요", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Member> members = memberRepo.load();
        Member target = null;
        for (Member m : members) {
            if (m.getNickname().equals(nick)) {
                target = m;
                break;
            }
        }
        if (target == null) {
            JOptionPane.showMessageDialog(this, "선택한 회원을 찾을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this, "차감할 점수를 입력하세요. (현재 " + target.getScore() + "점)", "점수 차감", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            int delta = Integer.parseInt(input.trim());
            if (delta <= 0) {
                JOptionPane.showMessageDialog(this, "1 이상의 숫자를 입력하세요.");
                return;
            }
            int newScore = Math.max(0, target.getScore() - delta);
            target.setScore(newScore);
            memberRepo.save(members);
            updateCurrentScoreLabel();
            onSave.run();
            JOptionPane.showMessageDialog(this, nick + "님 점수를 " + delta + "점 차감했습니다. (현재 " + newScore + "점)", "점수 차감", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "올바른 숫자를 입력하세요.");
        }
    }

    private void updateCurrentScoreLabel() {
        String nick = memberSearchField.getSelectedMember();
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

    @Override
    public void refresh() {
        List<Member> members = memberRepo.load();
        List<String> nicknames = new ArrayList<>();
        for (Member m : members) {
            if (m.getNickname() != null && !m.getNickname().isEmpty()) {
                nicknames.add(m.getNickname());
            }
        }
        String selected = memberSearchField.getSelectedMember();
        memberSearchField.setMembers(nicknames);
        if (selected != null && nicknames.contains(selected)) {
            memberSearchField.setSelectedMember(selected);
        } else {
            memberSearchField.setSelectedMember(null);
        }
        updateCurrentScoreLabel();
        refreshCriterionButtons();
    }
}
