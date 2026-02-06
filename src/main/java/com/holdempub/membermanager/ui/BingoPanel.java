package com.holdempub.membermanager.ui;

import com.holdempub.membermanager.data.BingoRepository;
import com.holdempub.membermanager.data.MemberRepository;
import com.holdempub.membermanager.domain.BingoData;
import com.holdempub.membermanager.domain.Member;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 빙고 화면 — 빙고판 표시, 칸별 이름·회원 배치, 이미지 내보내기.
 */
public class BingoPanel extends JPanel implements MainFrame.Refreshable {

    private final BingoRepository bingoRepo;
    private final MemberRepository memberRepo;
    private BingoData bingoData;
    private JPanel gridPanel;
    private JComboBox<Integer> rowsCombo;
    private JComboBox<Integer> colsCombo;
    private JTextField[][] cellLabelFields;
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

        JButton exportImageBtn = new JButton("이미지로 내보내기");
        exportImageBtn.addActionListener(e -> exportToImage());
        top.add(exportImageBtn);

        gridPanel = new JPanel();
        cellLabelFields = new JTextField[0][0];
        cellCombos = new JComboBox[0][0];
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        buildGrid();
    }

    private List<String> memberNicknames() {
        List<Member> members = memberRepo.load();
        return members.stream()
                .map(Member::getNickname)
                .filter(n -> n != null && !n.isEmpty())
                .collect(Collectors.toList());
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
        cellLabelFields = new JTextField[rows][cols];
        cellCombos = new JComboBox[rows][cols];
        gridPanel.setLayout(new GridLayout(rows, cols, 4, 4));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JPanel cellPanel = new JPanel(new BorderLayout(2, 2));
                cellPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

                JTextField labelField = new JTextField(8);
                labelField.setToolTipText("칸 이름 (예: 플랍 셋 승리)");
                labelField.setText(bingoData.getCellLabel(i, j));
                final int row = i, col = j;
                labelField.addFocusListener(new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusLost(java.awt.event.FocusEvent e) {
                        bingoData.setCellLabel(row, col, labelField.getText().trim());
                        bingoRepo.save(bingoData);
                    }
                });
                cellPanel.add(labelField, BorderLayout.NORTH);

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
                combo.addActionListener(e -> {
                    String selected = (String) combo.getSelectedItem();
                    String value = "(빈 칸)".equals(selected) || selected == null ? "" : selected;
                    bingoData.setCell(row, col, value);
                    bingoRepo.save(bingoData);
                });
                cellPanel.add(combo, BorderLayout.CENTER);
                cellCombos[i][j] = combo;
                cellLabelFields[i][j] = labelField;
                gridPanel.add(cellPanel);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void exportToImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("이미지로 저장");
        chooser.setSelectedFile(new java.io.File("빙고판.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase().matches(".*\\.(png|jpg|jpeg)$")) {
            path = path.resolveSibling(path.getFileName() + ".png");
        }
        int rows = bingoData.getRows();
        int cols = bingoData.getCols();
        int cellW = 140;
        int cellH = 70;
        int pad = 2;
        int w = cols * cellW + pad * 2;
        int h = rows * cellH + pad * 2;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = pad + j * cellW;
                int y = pad + i * cellH;
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, cellW - 1, cellH - 1);
                String label = bingoData.getCellLabel(i, j);
                String nick = bingoData.getCell(i, j);
                g.setColor(Color.BLACK);
                if (label != null && !label.isEmpty()) {
                    g.drawString(truncate(label, 10), x + 4, y + 18);
                }
                if (nick != null && !nick.isEmpty()) {
                    g.setFont(g.getFont().deriveFont(Font.BOLD));
                    g.drawString(truncate(nick, 8), x + 4, y + 42);
                    g.setFont(g.getFont().deriveFont(Font.PLAIN));
                }
            }
        }
        g.dispose();
        try {
            javax.imageio.ImageIO.write(img, "png", path.toFile());
            JOptionPane.showMessageDialog(this, "저장했습니다: " + path, "이미지 내보내기", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "저장 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "…";
    }

    @Override
    public void refresh() {
        bingoData = bingoRepo.load();
        rowsCombo.setSelectedItem(bingoData.getRows());
        colsCombo.setSelectedItem(bingoData.getCols());
        buildGrid();
    }
}
