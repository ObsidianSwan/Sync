package com.example.finalyearproject.hollyboothroyd.sync.Model;

import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Date;

/**
 * Created by hollyboothroyd on 2/25/2018.
 */

public class Notification {

    private String mDbRefKey;
    private String mItemId;
    NotificationType mType;
    Object mTimeStamp;

    // Used during notification list population
    public Notification(){

    }

    public Notification(String dbRefKey, String itemId, NotificationType type) {
        this.mDbRefKey = dbRefKey;
        this.mItemId = itemId;
        this.mType = type;
        this.mTimeStamp = ServerValue.TIMESTAMP;
    }

    // TODO remove

    public Notification(String dbRefKey, String itemId, NotificationType type, Object timeStamp) {
        this.mDbRefKey = dbRefKey;
        this.mItemId = itemId;
        this.mType = type;
        this.mTimeStamp = timeStamp;
    }

    public String getDbRefKey() {
        return mDbRefKey;
    }

    public void setDbRefKey(String dbRefKey) { this.mDbRefKey = dbRefKey; }


    public String getItemId() {
        return mItemId;
    }

    public void setItemId(String itemId) { this.mItemId = itemId; }

    public NotificationType getType() {
        return mType;
    }

    public void setType(NotificationType type) {
        this.mType = type;
    }

    public Object getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.mTimeStamp = timeStamp;
    }

    @Exclude
    public Date getTimeStampDate(){
        return new Date((long) mTimeStamp);
    }
}
