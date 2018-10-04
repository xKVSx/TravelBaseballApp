package android.csulb.edu.travelbaseballapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.csulb.edu.travelbaseballapp.pojos.BaseballEvent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static android.Manifest.permission.GET_ACCOUNTS;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 316;            // Choose an arbitrary request code value
    public static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 123;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final String GOOGLE_CREDENTIAL_EMAIL = "credential_email";
    public static final String CURRENT_EVENT = "current_event";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mUser;
    private Toolbar mToolbar;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mEventsDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Initialize FirebaseDatabase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
        mEventsDatabaseReference = mFirebaseDatabase.getReference().child("events");

        //initializes FirebaseAuth components
        initializeFirebaseAuth();
        initializeFirebaseAuthListener();

        loadMostRecentEvent();
    }

    private void loadMostRecentEvent(){
        mEventsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView summaryView = findViewById(R.id.eventSummary);
                TextView locationView = findViewById(R.id.eventLocation);
                TextView descriptionView = findViewById(R.id.eventDescription);
                TextView startTimeView = findViewById(R.id.eventStartTime);
                ArrayList<BaseballEvent> baseballEventList = new ArrayList<>();
                Date currentDate = new Date();
                Date minDate = null;
                int minIndex = 0;

                for(DataSnapshot outerPostSnapshot: dataSnapshot.getChildren()) {
                    BaseballEvent baseballEvent = outerPostSnapshot.getValue(BaseballEvent.class);
                    baseballEventList.add(baseballEvent);
                }

                //find the most current event
                if(baseballEventList.size() != 0) {
                    minDate = baseballEventList.get(0).getStartDate();

                    for (int i = 1; i < baseballEventList.size(); i++) {
                        Date nextDate = baseballEventList.get(i).getStartDate();

                        if (nextDate.before(minDate) && nextDate.after(currentDate)) {
                            minDate = nextDate;
                            minIndex = i;
                        }
                        else if(minDate.before(currentDate)){
                            minDate = nextDate;
                            minIndex = i;
                        }
                    }
                }
                else
                    summaryView.setText(getString(R.string.no_events));

                if(minDate != null) {
                    if (minDate.before(currentDate)) {
                        summaryView.setText(getString(R.string.no_events));
                    }
                    else {
                        //set the minDate to the View
                        summaryView.setText(baseballEventList.get(minIndex).getSummary());
                        descriptionView.setText(baseballEventList.get(minIndex).getDescription());
                        locationView.setText(baseballEventList.get(minIndex).getLocation());
                        startTimeView.setText(baseballEventList.get(minIndex).getDisplayTimeStart());
                        upDateWidgetViews(baseballEventList.get(minIndex));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.schedule:
                Intent scheduleIntent = new Intent(this, ScheduleActivity.class);
                scheduleIntent.putExtra(GOOGLE_CREDENTIAL_EMAIL, mUser.getEmail());
                startActivity(scheduleIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeFirebaseAuth(){
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    private void initializeFirebaseAuthListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    //user is signed in
                    mUser = user;
                    //request user permission to use access accounts. Needed for Google Calendar
                    requestAccountsPermission();

                    //check if user email is currently in the database, if not add it
                    final DatabaseReference userReference = mUsersDatabaseReference.child(user.getUid());
                    userReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                userReference.setValue(user.getEmail());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("onCancelledUserValue: ", databaseError.getDetails());
                        }
                    });
                }
                else{
                    //user is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void requestAccountsPermission(){
        if (ContextCompat.checkSelfPermission(this, GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            //Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    GET_ACCOUNTS)) {
            } else {
                //request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
            }
        } else {
            Log.d("accountPermission: ", "permission has already been granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("PermissionsResult: ", "view accounts granted");
                }
                else
                    Toast.makeText(this, "denied", Toast.LENGTH_LONG).show();
        }
    }

    private void upDateWidgetViews(BaseballEvent currentEvent){
        SharedPreferences mPrefs = getSharedPreferences(getString(R.string.current_event), MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        String currentEventSummary;
        String currentEventDescription;
        String currentEventLocation;
        String currentEventStartTime;

        currentEventSummary = currentEvent.getSummary();
        currentEventDescription = currentEvent.getDescription();
        currentEventLocation = currentEvent.getLocation();
        currentEventStartTime = currentEvent.getDisplayTimeStart();

        prefsEditor.putString(getString(R.string.summary), currentEventSummary);
        prefsEditor.putString(getString(R.string.description), currentEventDescription);
        prefsEditor.putString(getString(R.string.location), currentEventLocation);
        prefsEditor.putString(getString(R.string.start_time), currentEventStartTime);
        prefsEditor.commit();
    }
}