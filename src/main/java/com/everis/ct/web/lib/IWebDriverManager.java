package com.everis.ct.web.lib;

public interface IWebDriverManager {

    void setUpDriver();

    void navigateTo(String url);

    void maximize();

    void fullScreen();

    void quitDriver();

    boolean isDriverOn();
}
