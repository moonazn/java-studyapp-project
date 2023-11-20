package com.cookandroid.studyapp;

public class GoalItem {

    private String goalId;
    private String goalName;
    private String dday;
    private String target_date;
    private int target_hour;
    private int target_minute;

    // Firebase를 위한 기본 생성자
    public GoalItem() {}

    public GoalItem(String goalId, String goalName, String dday, String target_date, int target_hour, int target_minute) {
        this.goalId = goalId;
        this.goalName = goalName;
        this.dday = dday;
        this.target_date = target_date;
        this.target_hour = target_hour;
        this.target_minute = target_minute;
    }

    public String getGoalId() {
        return goalId;
    }

    public String getGoalName() {
        return goalName;
    }

    public String getDday() {
        return dday;
    }

    public String getTarget_date() {
        return target_date;
    }

    public int getTarget_hour() {
        return target_hour;
    }

    public int getTarget_minute() {
        return target_minute;
    }
}
