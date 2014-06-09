package com.handwin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-16 下午4:11
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String id;

    private String nickname;

    private String mobile;

    private Integer sex;

    private String avatar_url;

    private String countrycode;

    private String[] tcpServer;

    private int gameStatus;

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String[] getTcpServer() {
        return tcpServer;
    }

    public void setTcpServer(String[] tcpServer) {
        this.tcpServer = tcpServer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    public int getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(int gameStatus) {
        this.gameStatus = gameStatus;
    }
}