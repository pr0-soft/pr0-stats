package com.pr0gramm.statistics.api;

import java.io.Serializable;

/**
 * Created by koray on 28/01/2017.
 */
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 2353122911683251968L;

    private int id;
    private String name;
    private long registered;
    private int score;
    private int mark;
    private int admin;
    private int banned;

    public UserInfo(int id, String name, long registered, int score, int mark, int admin, int banned) {
        this.id = id;
        this.name = name;
        this.registered = registered;
        this.score = score;
        this.mark = mark;
        this.admin = admin;
        this.banned = banned;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getRegistered() {
        return registered;
    }

    public int getScore() {
        return score;
    }

    public int getMark() {
        return mark;
    }

    public int getAdmin() {
        return admin;
    }

    public int getBanned() {
        return banned;
    }

    @Override
    public String toString() {
        String info = "";
        info = info + "id: " + id + "\n";
        info = info + "name: " + name + "\n";
        info = info + "registered: " + registered + "\n";
        info = info + "score: " + score + "\n";
        info = info + "mark: " + mark + "\n";
        info = info + "admin: " + admin + "\n";
        info = info + "banned: " + banned + "\n";

        return info;
    }
}
