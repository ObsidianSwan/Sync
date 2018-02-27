package com.example.finalyearproject.hollyboothroyd.sync.Model;

/**
 * Created by hollyboothroyd on 2/27/2018.
 */

public class Connection {

    private String mConnectionDbRef;
    private String mUserId;

    // Used for connection list population in UserConnections shown in ConnectionFragment
    public Connection() {
    }

    public Connection(String connectionDbRef, String userId) {
        this.mConnectionDbRef = connectionDbRef;
        this.mUserId = userId;
    }

    public String getConnectionDbRef() {
        return mConnectionDbRef;
    }

    public void setConnectionDbRef(String connectionDbRef) {
        this.mConnectionDbRef = connectionDbRef;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }
}
