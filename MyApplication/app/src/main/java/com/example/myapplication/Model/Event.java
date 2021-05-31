package com.example.myapplication.Model;

import java.util.ArrayList;

/**
 * Author: Le Anh Tuan
 * Modified Date: 17/5/2021.
 * Description: A model class to fetch into Recycler View for displaying all events.
 */
public class Event {

    private long Limit, createAt;
    private boolean isOnline;
    private String description, end_date, event_name, place, start_date, uid;
    private ArrayList<String> ImgUri_list;

    public Event() {
    }

    public Event(ArrayList<String> ImgUri_list, long limit, long createAt, boolean isOnline, String description, String end_date,
                 String event_name, String place, String start_date, String uid) {
        Limit = limit;
        this.createAt = createAt;
        this.isOnline = isOnline;
        this.description = description;
        this.end_date = end_date;
        this.event_name = event_name;
        this.place = place;
        this.start_date = start_date;
        this.uid = uid;
        this.ImgUri_list = ImgUri_list;
    }

    public long getLimit() {
        return Limit;
    }

    public void setLimit(long limit) {
        Limit = limit;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getImgUri_list() {
        return ImgUri_list;
    }

    public void setImgUri_list(ArrayList<String> ImgUri_list) {
        this.ImgUri_list = ImgUri_list;
    }
}
