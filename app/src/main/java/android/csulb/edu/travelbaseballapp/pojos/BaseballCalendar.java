package android.csulb.edu.travelbaseballapp.pojos;

import com.google.api.services.calendar.model.ConferenceProperties;

public class BaseballCalendar {

    private String summary;                             //title of the calendar
    private ConferenceProperties conferenceProperties;  //(eventHangout, eventNamedHangout, hangoutMeets)
    private String id;                                  //identifier of the calendar
    private String description;                         //description of the calendar
    private String etag;                                //ETag of the resource
    private String kind;                                //type of resource(e.g. calendar#calendar
    private String location;                            //geographical location of the calendar
    private String timeZone;                            //time zone of the calendar

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ConferenceProperties getConferenceProperties() {
        return conferenceProperties;
    }

    public void setConferenceProperties(ConferenceProperties conferenceProperties) {
        this.conferenceProperties = conferenceProperties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}

/*This class is used to create new Calendar objects for coaches and players. For example a Baseball
calendar can be created to consolidate all events. Role must be set with Acl insert (e.g. reader/writer)
in order for the calendar to be visible to users other than the creator*/