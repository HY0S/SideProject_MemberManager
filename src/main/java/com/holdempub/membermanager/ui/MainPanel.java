package com.holdempub.membermanager.ui;

import com.holdempub.membermanager.data.MemberRepository;
import com.holdempub.membermanager.domain.Member;
import com.holdempub.membermanager.util.ExcelExportUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

/**
 * 메인 화면 — 회원 목록 및 요약 정보, 엑셀 내보내기.
 */
public class MainPanel extends JPanel implements MainFrame.Refreshable {

    private final MemberRepository memberRepo;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JLabel summaryLabel;

    public MainPanel(MemberRepository memberRepo) {
        this.memberRepo = memberRepo;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tableModel = new DefaultTableModel(new String[] { "닉네임", "누적 점수" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(table);

        summaryLabel = new JLabel("회원 0명 | 총 점수: 0");
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JButton exportExcelBtn = new JButton("엑셀로 내보내기");
        exportExcelBtn.addActionListener(e -> exportToExcel());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        south.add(summaryLabel);
        south.add(Box.createHorizontalStrut(16));
        south.add(exportExcelBtn);

        add(scroll, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void exportToExcel() {
        List<Member> members = memberRepo.load();
        if (members.isEmpty()) {
            JOptionPane.showMessageDialog(this, "내보낼 회원이 없습니다.", "내보내기", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("엑셀 파일로 저장");
        chooser.setSelectedFile(new java.io.File("회원목록.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase().endsWith(".xlsx")) {
            path = path.resolveSibling(path.getFileName() + ".xlsx");
        }
        try {
            ExcelExportUtil.exportMembers(members, path);
            JOptionPane.showMessageDialog(this, "저장했습니다: " + path, "엑셀 내보내기", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        List<Member> members = memberRepo.load();
        tableModel.setRowCount(0);
        int totalScore = 0;
        for (Member m : members) {
            tableModel.addRow(new Object[] { m.getNickname(), m.getScore() });
            totalScore += m.getScore();
        }
        summaryLabel.setText(String.format("회원 %d명 | 총 점수: %d", members.size(), totalScore));
    }
}
