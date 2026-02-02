package com.holdempub.membermanager.ui;

import com.holdempub.membermanager.data.MemberRepository;
import com.holdempub.membermanager.domain.Member;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 회원 관리 화면 — 회원 추가/삭제.
 */
public class MemberManagePanel extends JPanel implements MainFrame.Refreshable {

    private final MemberRepository memberRepo;
    private final Runnable onSave;
    private final JTextField nicknameField;
    private final DefaultListModel<String> listModel;
    private final JList<String> memberList;

    public MemberManagePanel(MemberRepository memberRepo, Runnable onSave) {
        this.memberRepo = memberRepo;
        this.onSave = onSave != null ? onSave : () -> {};
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.add(new JLabel("닉네임:"));
        nicknameField = new JTextField(12);
        top.add(nicknameField);

        JButton addBtn = new JButton("추가");
        addBtn.addActionListener(e -> addMember());
        top.add(addBtn);

        JButton deleteBtn = new JButton("삭제");
        deleteBtn.addActionListener(e -> deleteMember());
        top.add(deleteBtn);

        listModel = new DefaultListModel<>();
        memberList = new JList<>(listModel);
        memberList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(memberList);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void addMember() {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "닉네임을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Member> members = memberRepo.load();
        for (Member m : members) {
            if (m.getNickname().equalsIgnoreCase(nickname)) {
                JOptionPane.showMessageDialog(this, "이미 등록된 닉네임입니다.", "중복", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        members.add(new Member(nickname, 0));
        memberRepo.save(members);
        nicknameField.setText("");
        refresh();
        onSave.run();
    }

    private void deleteMember() {
        int idx = memberList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "삭제할 회원을 선택하세요.", "선택 필요", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String nickname = listModel.getElementAt(idx);
        int confirm = JOptionPane.showConfirmDialog(this,
                "「" + nickname + "」 회원을 삭제하시겠습니까?",
                "삭제 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        List<Member> members = memberRepo.load();
        members.removeIf(m -> m.getNickname().equals(nickname));
        memberRepo.save(members);
        refresh();
        onSave.run();
    }

    @Override
    public void refresh() {
        List<Member> members = memberRepo.load();
        listModel.clear();
        for (Member m : members) {
            listModel.addElement(m.getNickname());
        }
    }
}
