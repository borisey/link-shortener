package com.borisey.link_shortener.models;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Value;

@Entity
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String shortUrl, fullUrl;

    private Integer count = null;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Link() {
    }

    public Link(String fullUrl) {
        this.fullUrl  = fullUrl;
    }
}
