package com.holdempub.membermanager.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 빙고 판 데이터. 가변 크기(행×열), 각 칸에 이름(예: 플랍 셋 승리)과 배치된 닉네임.
 */
public class BingoData {

    private int rows = 3;
    private int cols = 3;
    /** 칸 인덱스(행우선) → 닉네임. 빈 칸은 null 또는 빈 문자열. */
    private List<String> cells = new ArrayList<>();
    /** 칸 인덱스(행우선) → 칸 이름(예: 플랍 셋 승리). */
    private List<String> cellLabels = new ArrayList<>();

    public BingoData() {
        resize(3, 3);
    }

    public BingoData(int rows, int cols) {
        this.rows = Math.max(2, Math.min(6, rows));
        this.cols = Math.max(2, Math.min(6, cols));
        resize(this.rows, this.cols);
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public void setRows(int rows) {
        this.rows = Math.max(2, Math.min(6, rows));
        resize(this.rows, this.cols);
    }

    public void setCols(int cols) {
        this.cols = Math.max(2, Math.min(6, cols));
        resize(this.rows, this.cols);
    }

    public List<String> getCells() {
        return cells;
    }

    public void setCells(List<String> cells) {
        this.cells = cells != null ? new ArrayList<>(cells) : new ArrayList<>();
        ensureCapacity(rows * cols);
    }

    public List<String> getCellLabels() {
        return cellLabels;
    }

    public void setCellLabels(List<String> cellLabels) {
        this.cellLabels = cellLabels != null ? new ArrayList<>(cellLabels) : new ArrayList<>();
        ensureCapacity(rows * cols);
    }

    /** 행×열로 크기 변경. 기존 값은 유지, 새 칸은 빈 문자열. */
    public void resize(int rows, int cols) {
        this.rows = Math.max(2, Math.min(6, rows));
        this.cols = Math.max(2, Math.min(6, cols));
        ensureCapacity(this.rows * this.cols);
    }

    private void ensureCapacity(int size) {
        while (cells.size() < size) {
            cells.add("");
        }
        if (cells.size() > size) {
            cells.subList(size, cells.size()).clear();
        }
        while (cellLabels.size() < size) {
            cellLabels.add("");
        }
        if (cellLabels.size() > size) {
            cellLabels.subList(size, cellLabels.size()).clear();
        }
    }

    public String getCell(int row, int col) {
        int idx = row * cols + col;
        if (idx < 0 || idx >= cells.size()) return "";
        String v = cells.get(idx);
        return v != null ? v : "";
    }

    public void setCell(int row, int col, String nickname) {
        int idx = row * cols + col;
        ensureCapacity(rows * cols);
        cells.set(idx, nickname != null ? nickname.trim() : "");
    }

    /** 칸 이름(예: 플랍 셋 승리) 반환. */
    public String getCellLabel(int row, int col) {
        int idx = row * cols + col;
        if (idx < 0 || idx >= cellLabels.size()) return "";
        String v = cellLabels.get(idx);
        return v != null ? v : "";
    }

    public void setCellLabel(int row, int col, String label) {
        int idx = row * cols + col;
        ensureCapacity(rows * cols);
        cellLabels.set(idx, label != null ? label.trim() : "");
    }

    public int getCellIndex(int row, int col) {
        return row * cols + col;
    }
}
