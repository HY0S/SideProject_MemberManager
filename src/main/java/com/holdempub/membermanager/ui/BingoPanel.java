package com.holdempub.membermanager.ui;

import com.holdempub.membermanager.data.BingoRepository;
import com.holdempub.membermanager.data.MemberRepository;
import com.holdempub.membermanager.domain.BingoData;
import com.holdempub.membermanager.domain.Member;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 빙고 화면 — 빙고판 표시 및 칸별 회원 배치.
 */
public class BingoPanel extends JPanel implements MainFrame.Refreshable {

    private final BingoRepository bingoRepo;
    private final MemberRepository memberRepo;
    private BingoData bingoData;
    private JPanel gridPanel;
    private JComboBox<Integer> rowsCombo;
    private JComboBox<Integer> colsCombo;
    private JComboBox<String>[][] cellCombos;

    @SuppressWarnings("unchecked")
    public BingoPanel(BingoRepository bingoRepo, MemberRepository memberRepo) {
        this.bingoRepo = bingoRepo;
        this.memberRepo = memberRepo;
        this.bingoData = bingoRepo.load();
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("빙고 판 크기:"));
        rowsCombo = new JComboBox<>(new Integer[] { 2, 3, 4, 5, 6 });
        colsCombo = new JComboBox<>(new Integer[] { 2, 3, 4, 5, 6 });
        rowsCombo.setSelectedItem(bingoData.getRows());
        colsCombo.setSelectedItem(bingoData.getCols());
        rowsCombo.addActionListener(e -> resizeGrid());
        colsCombo.addActionListener(e -> resizeGrid());
        top.add(rowsCombo);
        top.add(new JLabel("×"));
        top.add(colsCombo);

        gridPanel = new JPanel();
        cellCombos = new JComboBox[0][0];
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        buildGrid();
    }

    private List<String> memberNicknames() {
        List<Member> members = memberRepo.load();
        List<String> list = members.stream()
                .map(Member::getNickname)
                .filter(n -> n != null && !n.isEmpty())
                .collect(Collectors.toList());
        return list;
    }

    private void resizeGrid() {
        int r = (Integer) rowsCombo.getSelectedItem();
        int c = (Integer) colsCombo.getSelectedItem();
        bingoData.resize(r, c);
        bingoRepo.save(bingoData);
        buildGrid();
    }

    private void buildGrid() {
        gridPanel.removeAll();
        int rows = bingoData.getRows();
        int cols = bingoData.getCols();
        List<String> nicknames = memberNicknames();
        cellCombos = new JComboBox[rows][cols];
        gridPanel.setLayout(new GridLayout(rows, cols, 4, 4));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JComboBox<String> combo = new JComboBox<>();
                combo.addItem("(빈 칸)");
                for (String n : nicknames) {
                    combo.addItem(n);
                }
                String current = bingoData.getCell(i, j);
                if (current != null && !current.isEmpty()) {
                    boolean found = false;
                    for (int k = 0; k < combo.getItemCount(); k++) {
                        if (current.equals(combo.getItemAt(k))) {
                            combo.setSelectedIndex(k);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        combo.addItem(current);
                        combo.setSelectedItem(current);
                    }
                } else {
                    combo.setSelectedIndex(0);
                }
                final int row = i, col = j;
                combo.addActionListener(e -> {
                    String selected = (String) combo.getSelectedItem();
                    String value = "(빈 칸)".equals(selected) || selected == null ? "" : selected;
                    bingoData.setCell(row, col, value);
                    bingoRepo.save(bingoData);
                });
                cellCombos[i][j] = combo;
                gridPanel.add(combo);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    @Override
    public void refresh() {
        bingoData = bingoRepo.load();
        rowsCombo.setSelectedItem(bingoData.getRows());
        colsCombo.setSelectedItem(bingoData.getCols());
        buildGrid();
    }
}
