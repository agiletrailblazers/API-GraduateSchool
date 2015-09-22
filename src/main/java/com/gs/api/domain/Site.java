package com.gs.api.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
public class Site {

    private String id;
    private String title;
    private String url;
    private String content;

    
    public Site() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    /**
     * Constructor
     * 
     * @param courseId
     * @param courseTitle
     * @param courseDescription
     */
    public Site(String id, String title, String url, String content) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.content = content;
    }



}
