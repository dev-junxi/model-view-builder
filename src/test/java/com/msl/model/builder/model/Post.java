package com.msl.model.builder.model;

import java.util.List;

/**
 * @author w.vela
 */
public class Post implements HasUser, HasId<Long> {

    private final long id;
    private final int userId;
    private final List<Long> commentIds;
    private List<Comment> comments;

    public Post(long id, int userId, List<Long> commentIds) {
        this.id = id;
        this.userId = userId;
        this.commentIds = commentIds;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Integer getUserId() {
        return userId;
    }

    public List<Comment> comments() {
        return comments;
    }

    public List<Long> getCommentIds() {
        return commentIds;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", userId=" + userId +
                ", commentIds=" + commentIds +
                ", comments=" + comments +
                '}';
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Post)) {
            return false;
        }
        Post other = (Post) obj;
        return id == other.id;
    }
}
