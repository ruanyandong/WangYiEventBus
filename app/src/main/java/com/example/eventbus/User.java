package com.example.eventbus;

import android.support.annotation.NonNull;

public class User {
    private String one;
    private String two;

    public String getOne() {
        return one;
    }

    public void setOne(String one) {
        this.one = one;
    }

    public String getTwo() {
        return two;
    }

    public void setTwo(String two) {
        this.two = two;
    }

    public User(String one, String two) {
        this.one = one;
        this.two = two;
    }

    @Override
    public String toString() {
        return "User{" +
                "one='" + one + '\'' +
                ", two='" + two + '\'' +
                '}';
    }
}
