package com.example.finalyearproject.hollyboothroyd.sync.Activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Model.NotificationBase;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import static android.nfc.NdefRecord.createMime;

public class NFCActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {

    NfcAdapter mNfcAdapter;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mNdefExchangeFilters;

    DatabaseManager mDatabaseManager;
    AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabaseManager = new DatabaseManager();
        mAccountManager = new AccountManager();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check NFC permissions
        if (ActivityCompat.checkSelfPermission(NFCActivity.this, Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            // Request NFC permissions
            ActivityCompat.requestPermissions(NFCActivity.this, new String[]{android.Manifest.permission.NFC}, 2);
        } else {
            setUpNFC();

            //mNfcAdapter.setNdefPushMessageCallback(NFCActivity.this, NFCActivity.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        enableNdefExchangeMode();

        // Check to see that the Activity started due to an Android Beam
        /*if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }*/
    }


    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        //setIntent(intent);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] msgs = getNdefMessages(intent);
            // only one message sent during the beam
            NdefMessage msg = (NdefMessage) msgs[0];
            String userId = new String(msg.getRecords()[0].getPayload());
            addConnection(userId);
            Toast.makeText(this, "sent friend request via nfc!", Toast.LENGTH_LONG).show();
        }
    }

    NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record =
                        new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {
                        record
                });
                msgs = new NdefMessage[] {
                        msg
                };
            }
        } else {
            //Log.d(t, "Unknown intent.");
            finish();
        }
        return msgs;
    }



    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        TextView textView = (TextView) findViewById(R.id.NFC_text);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type
        textView.setText(new String(msg.getRecords()[0].getPayload()));
    }


    private void setUpNFC(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Generic PendingIntent that is delivered to the activity
        // The intent is later filled with the tag's details before sending it to this activity
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Intent filters for exchanging over p2p.
        // There is a dispatch to the foreground activity when Android receives
        // an intent matching the IntentFilter created
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
        }
        mNdefExchangeFilters = new IntentFilter[]{ndefDetected};
    }



    private void enableNdefExchangeMode() {
        if (ContextCompat.checkSelfPermission(NFCActivity.this, android.Manifest.permission.NFC)
                == PackageManager.PERMISSION_GRANTED) {

            String userId = mAccountManager.getCurrentUser().getUid();

            NdefMessage message = new NdefMessage(new NdefRecord[]{createMime("text/plain", userId.getBytes())});
            mNfcAdapter.setNdefPushMessage(message, NFCActivity.this);

            // Set up listener for the intent that is filtered for
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                    mNdefExchangeFilters, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(NFCActivity.this, android.Manifest.permission.NFC)
                    == PackageManager.PERMISSION_GRANTED) {
                setUpNFC();
                //mNfcAdapter.setNdefPushMessageCallback(NFCActivity.this, NFCActivity.this);
            }
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        AccountManager accountManager = new AccountManager();
        String userId = accountManager.getCurrentUser().getUid();

        return new NdefMessage(
                new NdefRecord[]{createMime(
                        "text/plain", HexStringToByteArray(userId))});
    }



    private void addConnection(final String otherUserId) {
        mDatabaseManager.getUserPeopleDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String currentUserId = mAccountManager.getCurrentUser().getUid();
                // Create a new connection item in the connection database
                DatabaseReference connectionRef = mDatabaseManager.getNewConnectionReference();
                final String dbRef = connectionRef.getKey();
                mDatabaseManager.addNewConnection(connectionRef, otherUserId, currentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Add connection reference key to current users database
                            mDatabaseManager.addConnectionReference(dbRef, otherUserId, currentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Add connection reference key to other users database
                                        mDatabaseManager.addConnectionReference(dbRef, currentUserId, otherUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(NFCActivity.this, R.string.connection_accepted_toast_text, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        // TODO LOG
                                                    }
                                                } else {
                                                    //TODO:Log
                                                }
                                            }
                                        });
                                    } else {
                                        //TODO:Log
                                    }
                                }
                            });
                        } else {
                            //TODO: Log
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO:Log
            }
        });
    }



    /**
     * Utility method to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    /**
     * Utility method to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     * @throws java.lang.IllegalArgumentException if input length is incorrect
     */
    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
