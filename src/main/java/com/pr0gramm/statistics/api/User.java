package com.pr0gramm.statistics.api;

import java.io.Serializable;

/**
 * Created by koray on 28/01/2017.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 3998371916307884402L;

    private UserInfo user;
    private int commentCount;
    private int uploadCount;
    private boolean likesArePublic;
    private int likeCount;
    private int tagCount;
    private int followCount;

    public User(UserInfo user, int commentCount, int uploadCount, boolean likesArePublic, int likeCount, int tagCount,
        int followCount) {
        this.user = user;
        this.commentCount = commentCount;
        this.uploadCount = uploadCount;
        this.likesArePublic = likesArePublic;
        this.likeCount = likeCount;
        this.tagCount = tagCount;
        this.followCount = followCount;
    }

    public UserInfo getUser() {
        return user;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getUploadCount() {
        return uploadCount;
    }

    public boolean isLikesArePublic() {
        return likesArePublic;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getTagCount() {
        return tagCount;
    }

    public int getFollowCount() {
        return followCount;
    }

    @Override
    public String toString() {
        String info = "";
        info = info + "commentCount: " + commentCount + "\n";
        info = info + "uploadCount: " + uploadCount + "\n";
        info = info + "likesArePublic: " + likesArePublic + "\n";
        info = info + "likeCount: " + likeCount + "\n";
        info = info + "tagCount: " + tagCount + "\n";
        info = info + "followCount: " + followCount + "\n";

        info = info + "user: ";
        info = info + ("\n" + user.toString()).replace("\n", "\n    ");

        return info;
    }
}
