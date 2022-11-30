package com.example.gamenew;

public class MoHinhBaiDang {

    //Tạo các biến

    String author,content,id,published,selfLink,title,updated,url;

    /// Tạo hàm dựng

    public MoHinhBaiDang(String author, String content, String id, String published, String selfLink, String title, String updated, String url) {
        this.author = author;
        this.content = content;
        this.id = id;
        this.published = published;
        this.selfLink = selfLink;
        this.title = title;
        this.updated = updated;
        this.url = url;
    }


    ///Tạo get set


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
