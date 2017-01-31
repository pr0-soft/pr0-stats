package com.pr0gramm.statistics.api;

import java.io.Serializable;

/**
 * Created by koray on 27/01/2017.
 */
public class Item implements Serializable {

    private static final long serialVersionUID = -1857326501191336628L;

    private int id;
    private int promoted;
    private int up;
    private int down;
    private long created;
    private String image;
    private String thumb;
    private String fullsize;
    private int width;
    private int height;
    private boolean audio;
    private String source;
    private int flags;

    private String user;

    private int mark;

    public Item(int id, int promoted, int up, int down, long created, String image, String thumb, String fullsize,
        int width, int height, boolean audio, String source, int flags, String user, int mark) {
        this.id = id;
        this.promoted = promoted;
        this.up = up;
        this.down = down;
        this.created = created;
        this.image = image;
        this.thumb = thumb;
        this.fullsize = fullsize;
        this.width = width;
        this.height = height;
        this.audio = audio;
        this.source = source;
        this.flags = flags;
        this.user = user;
        this.mark = mark;
    }

    public int getId() {
        return id;
    }

    public int getPromoted() {
        return promoted;
    }

    public int getUp() {
        return up;
    }

    public int getDown() {
        return down;
    }

    public long getCreated() {
        return created;
    }

    public String getImage() {
        return image;
    }

    public String getThumb() {
        return thumb;
    }

    public String getFullsize() {
        return fullsize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isAudio() {
        return audio;
    }

    public String getSource() {
        return source;
    }

    public int getFlags() {
        return flags;
    }

    public String getUser() {
        return user;
    }

    public int getMark() {
        return mark;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Item) {
            if (id == ((Item) obj).getId()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        String info = "";
        info = info + "id: " + getId() + "\n";
        info = info + "promoted: " + getPromoted() + "\n";
        info = info + "up: " + getUp() + "\n";
        info = info + "down: " + getDown() + "\n";
        info = info + "created: " + getCreated() + "\n";
        info = info + "image: " + getImage() + "\n";
        info = info + "thumb: " + getThumb() + "\n";
        info = info + "fullsize: " + getFullsize() + "\n";
        info = info + "width: " + getWidth() + "\n";
        info = info + "height: " + getHeight() + "\n";
        info = info + "audio: " + isAudio() + "\n";
        info = info + "source: " + getSource() + "\n";
        info = info + "flags: " + getFlags() + "\n";
        info = info + "user: " + getUser() + "\n";

        return info;
    }
}
