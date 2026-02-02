package com.holdempub.membermanager.domain;

/**
 * 점수 추가 기준 (예: 바이인 5점, 1등 20점).
 */
public class ScoreCriterion {

    private String name;
    private int points;

    public ScoreCriterion() {
        this("", 0);
    }

    public ScoreCriterion(String name, int points) {
        this.name = name != null ? name.trim() : "";
        this.points = Math.max(0, points);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = Math.max(0, points);
    }
}
