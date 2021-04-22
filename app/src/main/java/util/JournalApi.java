package util;

import android.app.Application;

public class JournalApi extends Application {
    //extends aplication to be acesible to entire application
    //we want this to be a singleton
    //we need to register to manifest file
    private String username;
    private String userID;
    private static  JournalApi instance;

    public static JournalApi getInstance(){
        if(instance == null){
            instance = new JournalApi();
        }
        return instance;
    }

    public JournalApi(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
