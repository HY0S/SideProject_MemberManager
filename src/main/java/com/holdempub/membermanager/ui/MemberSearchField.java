package com.holdempub.membermanager.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 검색 가능한 회원 선택 필드.
 * 입력 시 검색어에 맞는 회원을 상단에 표시하고, 클릭하면 해당 회원이 선택됨.
 */
public class MemberSearchField extends JPanel {

    private final JTextField searchField;
    private final JLabel selectedLabel;
    private final JWindow popup;
    private final JList<String> suggestionList;
    private final DefaultListModel<String> listModel;
    private List<String> allMembers = new ArrayList<>();
    private String selectedMember;
    private boolean programmaticUpdate;

    public MemberSearchField(int width) {
        setLayout(new BorderLayout(4, 0));

        searchField = new JTextField(width);
        searchField.setMargin(new Insets(2, 6, 2, 6));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onTextChanged();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                onTextChanged();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                onTextChanged();
            }
        });
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showPopupIfHasFilter();
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (!popup.isVisible()) return;
                if (e.getOppositeComponent() != null && isDescendant(suggestionList, e.getOppositeComponent())) return;
                Timer t = new Timer(200, ev -> popup.setVisible(false));
                t.setRepeats(false);
                t.start();
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popup.setVisible(false);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && popup.isVisible() && suggestionList.getModel().getSize() > 0) {
                    int idx = suggestionList.getSelectedIndex();
                    if (idx >= 0) selectMember(suggestionList.getModel().getElementAt(idx));
                }
            }
        });

        selectedLabel = new JLabel(" ");
        selectedLabel.setForeground(new Color(0, 100, 0));

        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setVisibleRowCount(8);
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    int idx = suggestionList.locationToIndex(e.getPoint());
                    if (idx >= 0 && idx < listModel.getSize()) {
                        selectMember(listModel.getElementAt(idx));
                    }
                }
            }
        });
        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int idx = suggestionList.getSelectedIndex();
                    if (idx >= 0) selectMember(listModel.getElementAt(idx));
                }
            }
        });

        popup = new JWindow();
        JScrollPane scroll = new JScrollPane(suggestionList);
        scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        popup.getContentPane().add(scroll);
        popup.setFocusableWindowState(false);

        add(searchField, BorderLayout.CENTER);
        add(selectedLabel, BorderLayout.EAST);
    }

    private static boolean isDescendant(Component parent, Component c) {
        while (c != null) {
            if (c == parent) return true;
            c = c.getParent();
        }
        return false;
    }

    private void onTextChanged() {
        if (programmaticUpdate) return;
        selectedMember = null;
        selectedLabel.setText(" ");
        showPopupIfHasFilter();
    }

    private void showPopupIfHasFilter() {
        String q = searchField.getText().trim();
        List<String> filtered = filterAndSort(q);
        listModel.clear();
        for (String n : filtered) {
            listModel.addElement(n);
        }
        if (listModel.getSize() > 0 && searchField.hasFocus()) {
            suggestionList.setSelectedIndex(0);
            showPopup();
        } else {
            popup.setVisible(false);
        }
    }

    private List<String> filterAndSort(String query) {
        if (query.isEmpty()) {
            return new ArrayList<>(allMembers);
        }
        String lower = query.toLowerCase();
        List<String> startsWith = new ArrayList<>();
        List<String> contains = new ArrayList<>();
        for (String n : allMembers) {
            if (n == null || n.isEmpty()) continue;
            if (n.toLowerCase().startsWith(lower)) {
                startsWith.add(n);
            } else if (n.toLowerCase().contains(lower)) {
                contains.add(n);
            }
        }
        startsWith.addAll(contains);
        return startsWith;
    }

    private void showPopup() {
        if (listModel.getSize() == 0) {
            popup.setVisible(false);
            return;
        }
        int h = Math.min(200, suggestionList.getPreferredSize().height + 4);
        popup.setSize(Math.max(searchField.getWidth(), 180), h);
        Point loc = searchField.getLocationOnScreen();
        popup.setLocation(loc.x, loc.y + searchField.getHeight());
        popup.setVisible(true);
        suggestionList.requestFocusInWindow();
    }

    private void selectMember(String nickname) {
        programmaticUpdate = true;
        try {
            selectedMember = nickname;
            searchField.setText(nickname);
            selectedLabel.setText(" ✓ 선택됨");
            popup.setVisible(false);
        } finally {
            programmaticUpdate = false;
        }
        fireSelectionChanged();
    }

    private void fireSelectionChanged() {
        for (ActionListener l : getListeners(ActionListener.class)) {
            l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "memberSelected"));
        }
    }

    public void setMembers(List<String> nicknames) {
        this.allMembers = nicknames != null ? new ArrayList<>(nicknames) : new ArrayList<>();
    }

    public String getSelectedMember() {
        return selectedMember;
    }

    public void addSelectionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    public void setSelectedMember(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            selectedMember = null;
            searchField.setText("");
            selectedLabel.setText(" ");
        } else {
            programmaticUpdate = true;
            try {
                selectedMember = nickname;
                searchField.setText(nickname);
                selectedLabel.setText(" ✓ 선택됨");
            } finally {
                programmaticUpdate = false;
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        searchField.setEnabled(enabled);
        if (!enabled) popup.setVisible(false);
    }
}
