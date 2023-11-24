package com.cookandroid.studyapp;

public class RankItem {
    private String rank;
    private String groupName;
    private String totalStudyTime;

    public RankItem() {
    }

    public RankItem(String rank, String groupName, String totalStudyTime) {
        this.rank = rank;
        this.groupName = groupName;
        this.totalStudyTime = totalStudyTime;
    }

    public String getRank() {
        return rank;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getTotalStudyTime() {
        return totalStudyTime;
    }
}
