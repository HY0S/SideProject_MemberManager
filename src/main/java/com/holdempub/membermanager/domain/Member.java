package com.holdempub.membermanager.domain;

/**
 * 고객(회원) 도메인 모델.
 */
public class Member {

    private String nickname;
    private int score;

    public Member() {
        this("", 0);
    }

    public Member(String nickname, int score) {
        this.nickname = nickname != null ? nickname.trim() : "";
        this.score = Math.max(0, score);
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname != null ? nickname.trim() : "";
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, score);
    }

    public void addScore(int delta) {
        this.score = Math.max(0, this.score + delta);
    }
}
