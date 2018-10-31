package android.csulb.edu.travelbaseballapp;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.csulb.edu.travelbaseballapp.adapters.MonthSectionedAdapter;
import android.csulb.edu.travelbaseballapp.addeventui.DateAndTimePicker;
import android.csulb.edu.travelbaseballapp.addeventui.EventDialogFragment;
import android.csulb.edu.travelbaseballapp.pojos.BaseballEvent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import static android.csulb.edu.travelbaseballapp.addeventui.DateAndTimePicker.TAG_DATETIME_FRAGMENT;

public class ScheduleActivity extends AppCompatActivity implements EventDialogFragment.EditEventInfoDialogListener, MonthSectionedAdapter.returnEventId, MonthSectionedAdapter.returnEventLocation{
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    public static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 123;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final String EVENT_LIST = "event_list";
    private static final String GUEST_LIST = "guest_list";

    private List<String> monthList = new ArrayList<>();
    private MonthSectionedAdapter mMonthSectionedAdapter;
    private List<BaseballEvent> mBaseballEventList = new ArrayList<>();
    private List<BaseballEvent> mSectionedEventList;
    private List<String> mGuestList = new ArrayList<>();
    private DateAndTimePicker mDateAndTimePicker;
    private Toolbar mToolbar;
    private FragmentManager mFragmentManager;
    private RecyclerView mRecyclerView;
    private String userEmail;

    private GoogleAccountCredential mCredential;
    private static final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private Calendar mCalendar;
    private MyTaskParams myTaskParams;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mEventsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        mToolbar = findViewById(R.id.schedule_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //get users email
        Intent emailIntent = getIntent();
        if (emailIntent == null) {
            finish();
        }
        userEmail = emailIntent.getStringExtra(MainActivity.GOOGLE_CREDENTIAL_EMAIL);

        //get a GoogleAccountCredential to manage authorization and account selection for Google Accounts
        mCredential = getGoogleAccountCredential(userEmail);
        mCalendar = new Calendar.Builder(httpTransport, JSON_FACTORY, mCredential)
                .setApplicationName(getString(R.string.app_name)).build();

        //Initialize FirebaseDatabase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mEventsDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.events));
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(getString(R.string.users));

        monthList = Arrays.asList(getResources().getStringArray(R.array.months));
        mMonthSectionedAdapter = new MonthSectionedAdapter(this, this);

        mDateAndTimePicker = new DateAndTimePicker(this);
        setDateAndTimePickerListener();

        mRecyclerView = findViewById(R.id.section_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(savedInstanceState == null) {
            //load any events saved in the database
            loadEventsFromDatabase();
            loadGuestsFromDatabase();
        }
        else {
            mGuestList = savedInstanceState.getStringArrayList(GUEST_LIST);
            mBaseballEventList = savedInstanceState.getParcelableArrayList(EVENT_LIST);
            sectionByMonth();
            mRecyclerView.setAdapter(mMonthSectionedAdapter);
        }
    }

    private void sectionByMonth(){
        for(int i = 0; i < monthList.size(); i++){
            mSectionedEventList = sectionEventByMonth(i);
            String month = monthList.get(i);
            mMonthSectionedAdapter.addSection(mMonthSectionedAdapter.new MonthSection(month, mSectionedEventList));
        }
    }

    private void loadEventsFromDatabase(){
        mEventsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    BaseballEvent baseballEvent = postSnapshot.getValue(BaseballEvent.class);
                    if(mMonthSectionedAdapter.getMonthSectionList().size() == 0)
                        mBaseballEventList.add(baseballEvent);
                }

                if(mMonthSectionedAdapter.getMonthSectionList().size() == 0) {
                    sectionByMonth();
                    mRecyclerView.setAdapter(mMonthSectionedAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("onCancelledAddValue: ", databaseError.getDetails());
            }
        });
    }

    private void loadGuestsFromDatabase(){
        mUsersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String guestEmail = postSnapshot.getValue(String.class);
                    mGuestList.add(guestEmail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("onCancelledAddValue: ", databaseError.getDetails());
            }
        });
    }


    public List<BaseballEvent> sectionEventByMonth(int month) {
        List<BaseballEvent> sectionedEventList = new ArrayList<>();

        for (int i = 0; i < mBaseballEventList.size(); i++) {
            if (mBaseballEventList.get(i).getMonthNumber() == month) {
                sectionedEventList.add(mBaseballEventList.get(i));
            }
        }

        return sectionedEventList;
    }

    private void updateAndDisplayEvents(BaseballEvent event){
        int month = event.getMonthNumber();
        mMonthSectionedAdapter.getMonthSection(month).addEvent(event);
    }

    public void addEventDate(Date date){
        BaseballEvent event = new BaseballEvent();
        String ISOFormattedDate = mDateAndTimePicker.getmSimpleDateFormat().format(date);
        String dayOfTheWeek = (String) DateFormat.format("EEEE", date); // Thursday
        String day          = (String) DateFormat.format("dd",   date); // 13
        String monthString  = (String) DateFormat.format("MMM",  date); // September
        String monthNumber  = (String) DateFormat.format("MM",   date); // 09
        String year         = (String) DateFormat.format("yyyy", date); // 2018
        String startTime    = (String) DateFormat.format("H:mm", date); // 8

        event.setStartDate(date);
        event.setStart(ISOFormattedDate);
        event.setDayOfTheWeek(dayOfTheWeek);
        event.setDay(day);
        event.setMonth(monthString);
        event.setMonthNumber(Integer.parseInt(monthNumber) - 1); //the month arrayList is 0-11
        event.setYear(year);
        event.setDisplayTimeStart(startTime);

        mBaseballEventList.add(event);
    }

    public void addEventLocation(BaseballEvent event, String location, String address, String description){
        event.setSummary(location);
        event.setLocation(address);
        event.setDescription(description);
    }

    private void setDateAndTimePickerListener(){
        mDateAndTimePicker.getTimeFragment().setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {
            @Override
            public void onNeutralButtonClick(Date date) {

            }

            @Override
            public void onPositiveButtonClick(Date date) {
                /*add the ISO formattedDate for Google Calendar and the day and date for the UI*/

                mFragmentManager = getSupportFragmentManager();
                EventDialogFragment eventDialogFragment = new EventDialogFragment();
                eventDialogFragment.show(mFragmentManager, null);
                addEventDate(date);
            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedule_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_event_dateandtime:
                int[] dateTime = setCalendarPickerStartDate();
                mDateAndTimePicker.getTimeFragment().startAtCalendarView();
                mDateAndTimePicker.getTimeFragment().setDefaultDateTime(new GregorianCalendar(dateTime[0],
                        dateTime[1], dateTime[2], dateTime[3], dateTime[4]).getTime());
                mDateAndTimePicker.getTimeFragment().show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
                return true;
            case 2:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int[] setCalendarPickerStartDate(){
        int[] dateTime = new int[5];
        java.util.Calendar today = java.util.Calendar.getInstance();
        dateTime[0] = today.get(java.util.Calendar.YEAR);
        dateTime[1] = today.get(java.util.Calendar.MONTH);
        dateTime[2] = today.get(java.util.Calendar.DAY_OF_MONTH);
        dateTime[3] = today.get(java.util.Calendar.HOUR_OF_DAY);
        dateTime[4] = today.get(java.util.Calendar.MINUTE);

        return dateTime;
    }

    @Override
    public void onFinishEditDialog(Bundle eventBundle) {
        String location = eventBundle.getString(EventDialogFragment.LOCATION_NAME);
        String address = eventBundle.getString(EventDialogFragment.ADDRESS);
        String description = eventBundle.getString(EventDialogFragment.DESCRIPTION);

        //find the most recently added event and insert the rest of the event information
        final BaseballEvent event = mBaseballEventList.get(mBaseballEventList.size() - 1);
        addEventLocation(event, location, address, description);
        final DatabaseReference pushedPostRef = mEventsDatabaseReference.push();
        //get the key of the event to be saved
        event.setDatabaseId(pushedPostRef.getKey());
        //save event to the database
        pushedPostRef.setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //add the event to the month sectioned Calendar
                updateAndDisplayEvents(event);
                //add event to Google Calendar
                CalendarAPI calendarAPI = new CalendarAPI(event, (ArrayList<String>) mGuestList, mCredential);
                myTaskParams = new MyTaskParams(mCalendar, calendarAPI.createGoogleCalendarEvent());
                if(isGooglePlayServicesAvailable() && myTaskParams != null)
                    refreshResults();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Add value to database", e.getMessage());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(EVENT_LIST, (ArrayList<BaseballEvent>) mBaseballEventList);
        outState.putStringArrayList(GUEST_LIST, (ArrayList<String>) mGuestList);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable() && myTaskParams != null) {
            refreshResults();
        } else {
            Log.d("onResume:", "Google Play Services required");
        }
    }

    @Override
    public void returnDeletedEventId(final String deletedEventId) {
        //the event has been deleted from the month sectioned calendar now delete it from the database
        DatabaseReference deleteEventRef = FirebaseDatabase.getInstance().getReference().getRoot().child(getString(R.string.events));
        deleteEventRef.child(deletedEventId).removeValue();

        for(Iterator eventIterator = mBaseballEventList.iterator();
            eventIterator.hasNext();){
            BaseballEvent event = (BaseballEvent) eventIterator.next();
            if(event.getDatabaseId() == deletedEventId){
                eventIterator.remove();
            }
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (GoogleApiAvailability.getInstance().isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(ScheduleActivity.this,
                        connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    private GoogleAccountCredential getGoogleAccountCredential(String userEmail){
        GoogleAccountCredential gac;

        gac = GoogleAccountCredential.usingOAuth2(this, SCOPES).setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(userEmail);

        return gac;
    }

    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new AsyncCalendarTask().execute(myTaskParams);
            } else {
                Toast.makeText(this, getString(R.string.no_network_connection), Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void chooseAccount() {
        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    @Override
    public void returnLocationString(String location) {
        //launch Google Maps navigation with the returned location
        Toast.makeText(this, location, Toast.LENGTH_LONG).show();
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage(getString(R.string.map_package));
        startActivity(mapIntent);
    }

    private class AsyncCalendarTask extends AsyncTask<MyTaskParams, Void, String> {

        @Override
        protected String doInBackground(MyTaskParams... myTaskParams) {
            String calendarId = getString(R.string.google_calendar_id);

            try {
                myTaskParams[0].service.events().insert(calendarId, myTaskParams[0].event).execute();
            } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
                showGooglePlayServicesAvailabilityErrorDialog(availabilityException.getConnectionStatusCode());

            } catch (UserRecoverableAuthIOException userRecoverableException) {
                startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);

            } catch (IOException e) {
                Log.d("doInBackground: ", "The following error occurred: " + e.getMessage());

                Log.d("doInBackground: ", "eventHtml: " + myTaskParams[0].event.getHtmlLink());
                return myTaskParams[0].event.getHtmlLink();
            }

            return calendarId;
        }
    }

    private static class MyTaskParams {
        Calendar service;
        Event event;

        MyTaskParams(Calendar service, Event event) {
            this.service = service;
            this.event = event;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        refreshResults();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d("onActivityResult: ", "Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    chooseAccount();
                }
                break;
        }
    }
}