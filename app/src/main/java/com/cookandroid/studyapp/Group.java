package com.cookandroid.studyapp;

import java.util.Map;

public class Group {
    public String groupName;
    public Map<String, Boolean> members;

    public Group() {
        // Default constructor required for Firebase
    }

    public Group(String groupName, Map<String, Boolean> members) {
        this.groupName = groupName;
        this.members = members;
    }
}
