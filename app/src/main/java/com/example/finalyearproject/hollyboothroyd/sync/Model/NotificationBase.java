package com.example.finalyearproject.hollyboothroyd.sync.Model;

import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;

import java.util.Date;

/**
 * Created by hollyboothroyd on 2/25/2018.
 */

public abstract class NotificationBase {

    private String mDbRefKey;
    private String mId;
    private String mDescription;
    private String mImageId;
    private NotificationType mType;
    private Date mTimeStamp;

    public NotificationBase() {
    }

    public NotificationBase(String dbRefKey, String id, String description, String imageId, NotificationType type, Date timeStamp) {
        this.mDbRefKey = dbRefKey;
        this.mId = id;
        this.mDescription = description;
        this.mImageId = imageId;
        this.mType = type;
        this.mTimeStamp = timeStamp;
    }

    public String getDbRefKey() {
        return mDbRefKey;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getImageId() {
        return mImageId;
    }

    public void setImageId(String imageId) {
        this.mImageId = imageId;
    }

    public NotificationType getType() {
        return mType;
    }

    public void setType(NotificationType type) {
        this.mType = type;
    }

    public Date getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.mTimeStamp = timeStamp;
    }
}
