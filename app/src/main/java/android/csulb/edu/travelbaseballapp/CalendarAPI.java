package android.csulb.edu.travelbaseballapp;

import android.csulb.edu.travelbaseballapp.pojos.BaseballEvent;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;

public class CalendarAPI {
    private BaseballEvent mBaseballEvent;
    private ArrayList<String> mEmailList;
    private GoogleAccountCredential mCredential;

    CalendarAPI(BaseballEvent baseballEvent, ArrayList<String> emails, GoogleAccountCredential credential) {
        this.mBaseballEvent = baseballEvent;
        this.mEmailList = emails;
        this.mCredential = credential;
    }

    private Event setCalendarEvent() {
        BaseballEvent baseballEvent = getBaseballEvent();
        Event event = new Event();

        if (baseballEvent != null) {
            event.setSummary(baseballEvent.getSummary())
                    .setLocation(baseballEvent.getLocation())
                    .setDescription(baseballEvent.getDescription());
        }

        return event;
    }

    private EventDateTime setEventStartTime() {
        BaseballEvent baseballEvent = getBaseballEvent();
        EventDateTime start = new EventDateTime();
        String timeZoneString = TimeZone.getDefault().getID();

        if (baseballEvent != null) {
            DateTime startDateTime = new DateTime(baseballEvent.getStart());
            start.setDateTime(startDateTime)
                    .setTimeZone(timeZoneString);
        }

        return start;
    }

    private EventDateTime setEventEndTime() {
        BaseballEvent baseballEvent = getBaseballEvent();
        EventDateTime end = new EventDateTime();
        String timeZoneString = TimeZone.getDefault().getID();
        Log.d( "setEventEndTime: ", timeZoneString);

        if (baseballEvent != null) {
            DateTime endDateTime = new DateTime(baseballEvent.getEnd());
            end.setDateTime(endDateTime)
                    .setTimeZone(timeZoneString);
        }

        return end;
    }

    private EventAttendee[] setEventAttendees() {
        EventAttendee[] attendees = new EventAttendee[mEmailList.size()];

        for (int i = 0; i < mEmailList.size(); i++) {
           attendees[i] = new EventAttendee().setEmail(mEmailList.get(i));
        }

        return attendees;
    }

    private Event.Reminders setEventReminders(){
        EventReminder[] reminderOverrides = new EventReminder[]{
                new EventReminder().setMethod("email").setMinutes(mBaseballEvent.getEmailReminder()),
                new EventReminder().setMethod("popup").setMinutes(mBaseballEvent.getPopupReminder()),
        };

        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));

        return reminders;
    }

    public Event createGoogleCalendarEvent(){
        Event event = setCalendarEvent();
        EventDateTime startTime = setEventStartTime();
        EventAttendee[] attendees = setEventAttendees();
        Event.Reminders reminders = setEventReminders();

        event.setStart(startTime);
        event.setEnd(startTime);
        event.setAttendees(Arrays.asList(attendees));
        event.setReminders(reminders);

        return event;
    }

    private BaseballEvent getBaseballEvent(){
        return mBaseballEvent;
    }
}
