package com.cookandroid.studyapp;

import java.util.HashMap;
import java.util.Map;

public class UploadData {
    private String photo_title;
    private String user_id;
    private Long upload_time;
    private String photo_url;
    private String post_id;

    public UploadData() {
    }

    public UploadData(String post_id, String photo_title, String user_id, Long upload_time, String photo_url) {
        this.post_id = post_id;
        this.photo_title = photo_title;
        this.user_id = user_id;
        this.upload_time = upload_time;
        this.photo_url = photo_url;

    }

    public String getPost_id() { return post_id; }

    public void setPost_id(String post_id) { this.post_id = post_id; }

    public String getPhoto_title() {
        return photo_title;
    }

    public void setPhoto_title(String photo_title) {
        this.photo_title = photo_title;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Long getUpload_time() {
        return upload_time;
    }

    public void setUpload_time(Long upload_time) { this.upload_time = upload_time;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("post_id", post_id);
        map.put("photo_title", photo_title);
        map.put("user_id", user_id);
        map.put("upload_time", upload_time);
        map.put("photo_url", photo_url);
        return map;
    }
}
