package com.example.finalyearproject.hollyboothroyd.sync.Model;

import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hollyboothroyd on 2/25/2018.
 */

public class ConnectionRequestNotification extends NotificationBase {

    private Person mPerson;

    public ConnectionRequestNotification(String dbRefKey, Person person, Date timeStamp) {
        super(dbRefKey, person.getUserId(), person.getFirstName() + " " + person.getLastName() + ": " + person.getPosition(),
                person.getImageId(), NotificationType.CONNECTION_REQUEST, timeStamp);
        this.mPerson = person;
    }

    public Person getPerson() {
        return mPerson;
    }

    public void setPerson(Person person) {
        this.mPerson = person;
    }
}
