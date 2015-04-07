package com.gfts.testchat;

import android.app.Application;
import android.content.Intent;

import java.sql.SQLException;

import messageManagement.ManagerService;
import messageManagement.MessageDbAdapter;
import serverUtils.NetworkService;

public class MyApplication extends Application {
    private static MyApplication singleton;

    private MessageDbAdapter messageDbAdapter;
    private boolean authenticated = false;
    private boolean listening = false;

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public MyApplication getInstance(){
        return singleton;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        messageDbAdapter = MessageDbAdapter.getInstance(this);
        try {
            messageDbAdapter.open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        singleton = this;
        startService(new Intent(this, ManagerService.class));
        startService(new Intent(this, NetworkService.class));
    }

    public MessageDbAdapter getDatabaseAdapter(){
        return messageDbAdapter;
    }


}