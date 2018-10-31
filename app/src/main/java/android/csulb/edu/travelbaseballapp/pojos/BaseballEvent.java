package android.csulb.edu.travelbaseballapp.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

public class BaseballEvent implements Parcelable {
    private String eventEnd;                    //end time of the event in ISO 8601 format
    private String eventStart;                  //start time of the event in ISO 8601 format
    private Date startDate;                     //used to find current event
    private String displayTimeEnd;              //end time used for UI
    private String displayTimeStart;            //start time used for UI
    private String month;                       //used to easily section events by month
    private int monthNumber;                    //numerical month
    private String dayOfTheWeek;                //the day of the week
    private String day;                         //numerical day
    private String year;                        //year of the event
    private Integer popupReminder;              //set popup reminder time
    private Integer emailReminder;              //set email reminder time
    private ArrayList<String> emails;           //guest invited to the event
    private String location;                    //location of the event
    private String summary;                     //e.g. name of the place
    private String description;                 //about th event
    private String databaseId;                  //the id created after the event is added to the database

    public BaseballEvent(){
        popupReminder = 24 * 60;                //set default values
        emailReminder = 24 * 60;
    }

    public BaseballEvent(Parcel in){
        setEnd(in.readString());
        setStart(in.readString());
        setStartDate((java.util.Date) in.readSerializable());
        setDisplayTimeStart(in.readString());
        setDisplayTimeEnd(in.readString());
        setMonth(in.readString());
        setMonthNumber(in.readInt());
        setDayOfTheWeek(in.readString());
        setDay(in.readString());
        setYear(in.readString());
        setPopupReminder(in.readInt());
        setEmailReminder(in.readInt());
        setEmails(in.createStringArrayList());
        setLocation(in.readString());
        setSummary(in.readString());
        setDescription(in.readString());
        setDatabaseId(in.readString());
    }

    public String getEnd() {
        return eventEnd;
    }

    public void setEnd(String eventEnd) {
        this.eventEnd = eventEnd;
    }

    public String getStart() {
        return eventStart;
    }

    public void setStart(String eventStart) {
        this.eventStart = eventStart;
    }

    public Date getStartDate(){
        return startDate;
    }

    public void setStartDate(Date startDate){
        this.startDate = startDate;
    }

    public String getDisplayTimeEnd(){
        return displayTimeEnd;
    }

    public void setDisplayTimeEnd(String displayTimeEnd){
        this.displayTimeEnd = displayTimeEnd;
    }

    public String getDisplayTimeStart(){
        return displayTimeStart;
    }

    public void setDisplayTimeStart(String displayTimeStart){
        this.displayTimeStart = displayTimeStart;
    }

    public String getMonth(){
        return month;
    }

    public void setMonth(String month){
        this.month = month;
    }

    public int getMonthNumber(){
        return monthNumber;
    }

    public void setMonthNumber (int monthNumber){
        this.monthNumber = monthNumber;
    }

    public String getDayOfTheWeek(){
        return dayOfTheWeek;
    }

    public void setDayOfTheWeek(String dayOfTheWeek){
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public String getDay(){
        return day;
    }

    public void setDay(String day){
        this.day = day;
    }

    public String getYear(){
        return year;
    }

    public void setYear(String year){
        this.year = year;
    }

    public void setPopupReminder(Integer popupReminder){
        this.popupReminder = popupReminder;
    }

    public Integer getPopupReminder() {
        return popupReminder;
    }

    public void setEmailReminder(Integer emailReminder){
        this.emailReminder = emailReminder;
    }

    public Integer getEmailReminder(){
        return emailReminder;
    }

    public void setEmails(ArrayList<String> emails){
        this.emails = emails;
    }
    public ArrayList<String> getEmails() {
        return emails;
    }

    public void setAttendees(ArrayList<String> emails) {
        this.emails = emails;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSummary(){
        return summary;
    }

    public void setSummary(String summary){
        this.summary = summary;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getEnd());
        parcel.writeString(getStart());
        parcel.writeSerializable(getStartDate());
        parcel.writeString(getDisplayTimeStart());
        parcel.writeString(getDisplayTimeEnd());
        parcel.writeString(getMonth());
        parcel.writeInt(getMonthNumber());
        parcel.writeString(getDayOfTheWeek());
        parcel.writeString(getDay());
        parcel.writeString(getYear());
        parcel.writeInt(getPopupReminder());
        parcel.writeInt(getEmailReminder());
        parcel.writeStringList(getEmails());
        parcel.writeString(getLocation());
        parcel.writeString(getSummary());
        parcel.writeString(getDescription());
        parcel.writeString(getDatabaseId());
    }

    public static final Parcelable.Creator<BaseballEvent> CREATOR = new Parcelable.Creator<BaseballEvent>(){

        @Override
        public BaseballEvent createFromParcel(Parcel parcel) {
            return new BaseballEvent(parcel);
        }

        @Override
        public BaseballEvent[] newArray(int i) {
            return new BaseballEvent[i];
        }
    };
}