package com.example.finalyearproject.hollyboothroyd.sync.Model;

/**
 * Created by hollyboothroyd on 2/27/2018.
 */

public class Connection {

    private String mConnectionDbRef;
    private String mUserAId;
    private String mUserBId;


    // Used for connection list population in UserConnections shown in ConnectionFragment
    public Connection() {
    }

    // TODO Delete?
/*    public Connection(String connectionDbRef, String userId) {
        this.mConnectionDbRef = connectionDbRef;
        this.mUserId = userId;
    }*/

    public Connection(String connectionDbRef, String userAId, String userBId) {
        this.mConnectionDbRef = connectionDbRef;
        this.mUserAId = userAId;
        this.mUserBId = userBId;
    }

    public String getConnectionDbRef() {
        return mConnectionDbRef;
    }

    public void setConnectionDbRef(String connectionDbRef) {
        this.mConnectionDbRef = connectionDbRef;
    }

    public String getUserAId() {
        return mUserAId;
    }

    public void setUserAId(String userId) {
        this.mUserAId = userId;
    }

    public String getUserBId() {
        return mUserBId;
    }

    public void setUserBId(String userId) {
        this.mUserBId = userId;
    }
}
