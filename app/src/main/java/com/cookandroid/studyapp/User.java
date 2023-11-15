package com.cookandroid.studyapp;

public class User {
    private String uid;
    private String nickname;
    private int praisePoints; // 칭찬 점수

    public User() {
        // 기본 생성자 필요
    }

    public User(String uid, String nickname, int praisePoints) {
        this.uid = uid;
        this.nickname = nickname;
        this.praisePoints = praisePoints;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getPraisePoints() {
        return praisePoints;
    }

    public void setPraisePoints(int praisePoints) {
        this.praisePoints = praisePoints;
    }
}
