package com.parkme.admin.Model;

public class Request {
    String car_no,date,status,time;

    public Request() {
    }

    public Request(String car_no, String date, String status, String time) {
        this.car_no = car_no;
        this.date = date;
        this.status = status;
        this.time = time;
    }

    public String getCar_no() {
        return car_no;
    }

    public void setCar_no(String car_no) {
        this.car_no = car_no;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
