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
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.GMapFragment;
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

public class NFCActivity extends AppCompatActivity {

    private static final String TAG = "NFCActivity";

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
            // NFC is not available on this device. Go back to GMaps fragment
            Toast.makeText(this, R.string.NFC_unavailable_toast, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(NFCActivity.this, CoreActivity.class));
            return;
        }

        // Check NFC permissions
        if (ActivityCompat.checkSelfPermission(NFCActivity.this, Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            // Request NFC permissions
            ActivityCompat.requestPermissions(NFCActivity.this, new String[]{android.Manifest.permission.NFC}, 2);
        } else {
            // Set up NFC intents
            setUpNFC();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set up NFC listener and message to deliver
        enableNdefExchangeMode();
    }

    @Override
    public void onBackPressed() {
        // Go to GMaps fragment if back button is pressed
        startActivity(new Intent(NFCActivity.this, CoreActivity.class));
        super.onBackPressed();
    }

    private void setUpNFC() {
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
            Log.e(TAG, e.toString());
        }
        mNdefExchangeFilters = new IntentFilter[]{ndefDetected};
    }

    private void enableNdefExchangeMode() {
        // TODO: What if permissions are rejected. If they are later granted this is not called
        if (ContextCompat.checkSelfPermission(NFCActivity.this, android.Manifest.permission.NFC)
                == PackageManager.PERMISSION_GRANTED) {

            String userId = mAccountManager.getCurrentUser().getUid();

            // Create an NDEF message containing the user's ID
            NdefMessage message = new NdefMessage(new NdefRecord[]{createMime("text/plain", userId.getBytes())});
            mNfcAdapter.setNdefPushMessage(message, NFCActivity.this);

            // Set up listener for the intent that is filtered for
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                    mNdefExchangeFilters, null);
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] msgs = getNdefMessages(intent);
            // only one message sent during the beam
            NdefMessage msg = (NdefMessage) msgs[0];
            String userId = new String(msg.getRecords()[0].getPayload());
            addConnection(userId);
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
                byte[] empty = new byte[]{};
                NdefRecord record =
                        new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{
                        record
                });
                msgs = new NdefMessage[]{
                        msg
                };
            }
        } else {
            Log.d(TAG, getString(R.string.NFC_unknown_intent_error));
            finish();
        }
        return msgs;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if NFC permissions have been granted
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(NFCActivity.this, android.Manifest.permission.NFC)
                    == PackageManager.PERMISSION_GRANTED) {
                // Set up NFC intents if NFC permissions have been granted
                setUpNFC();
            }
        }
    }

    private void addConnection(final String otherUserId) {
        mDatabaseManager.getUserPeopleDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String currentUserId = mAccountManager.getCurrentUser().getUid();
                // Add connection to current users database
                mDatabaseManager.addConnection(otherUserId, currentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Add connection to other users database
                            mDatabaseManager.addConnection(currentUserId, otherUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(NFCActivity.this, R.string.connection_accepted_toast_text, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(NFCActivity.this, R.string.syncup_connection_error, Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.e(TAG, getString(R.string.add_connection_other_user_error));
                                    }
                                }
                            });
                        } else {
                            Log.e(TAG, getString(R.string.add_connection_current_user_error));
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
    }
}
