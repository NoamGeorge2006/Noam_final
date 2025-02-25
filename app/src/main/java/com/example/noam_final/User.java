package com.example.noam_final;

public class User {
    private String uid;
    private String email;

    public User() {}
    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }
}
