package com.borisey.link_shortener.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String short_url, full_url;
    private int count;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShort_url() {
        return short_url;
    }

    public void setShort_url(String short_url) {
        this.short_url = short_url;
    }

    public String getFull_url() {
        return full_url;
    }

    public void setFull_url(String full_url) {
        this.full_url = full_url;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
