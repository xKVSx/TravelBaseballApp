package android.csulb.edu.travelbaseballapp.addeventui;

import android.csulb.edu.travelbaseballapp.R;
import android.support.v7.app.AppCompatActivity;

import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateAndTimePicker{
    public static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";
    public static final String STATE_TEXTVIEW = "STATE_TEXTVIEW";
    private SimpleDateFormat mSimpleDateFormat;
    private SwitchDateTimeDialogFragment mDateTimeFragment;

    public DateAndTimePicker(AppCompatActivity activity) {

        // Construct SwitchDateTimePicker
        mDateTimeFragment = (SwitchDateTimeDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
        if (mDateTimeFragment == null) {
            mDateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                    activity.getString(R.string.label_datetime_dialog),
                    activity.getString(android.R.string.ok),
                    activity.getString(android.R.string.cancel)
            );
        }

        //Use the default or current TimeZone
        mDateTimeFragment.setTimeZone(TimeZone.getDefault());
        //Init format
        mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.getDefault()){
            public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                StringBuffer toFix = super.format(date, toAppendTo, pos);
                return toFix.insert(toFix.length()-2, ':');
            };
        };
        //Assign unmodifiable values
        mDateTimeFragment.set24HoursMode(false);
        mDateTimeFragment.setHighlightAMPMSelection(true);
        mDateTimeFragment.setMinimumDateTime(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime());
        mDateTimeFragment.setMaximumDateTime(new GregorianCalendar(2025, Calendar.DECEMBER, 31).getTime());
    }

    public SwitchDateTimeDialogFragment getTimeFragment() {
        return mDateTimeFragment;
    }

    public SimpleDateFormat getmSimpleDateFormat() {
        return mSimpleDateFormat;
    }
}

