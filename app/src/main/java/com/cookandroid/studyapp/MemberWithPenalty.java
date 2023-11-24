package com.cookandroid.studyapp;

public class MemberWithPenalty {
    private String memberName;
    private int penaltyAmount;

    public MemberWithPenalty(String memberName, int penaltyAmount) {
        this.memberName = memberName;
        this.penaltyAmount = penaltyAmount;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public int getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(int penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }
}
