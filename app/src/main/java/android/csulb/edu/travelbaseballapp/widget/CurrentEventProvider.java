package android.csulb.edu.travelbaseballapp.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.csulb.edu.travelbaseballapp.R;
import android.util.Log;
import android.widget.RemoteViews;

import static android.content.Context.MODE_PRIVATE;

/**
 * Implementation of App Widget functionality.
 */
public class CurrentEventProvider extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences mPrefs = context.getSharedPreferences(context.getString(R.string.current_event), MODE_PRIVATE);
        String[] currentEventInfo = new String[4];

        currentEventInfo[0] = mPrefs.getString(context.getString(R.string.summary), "");
        currentEventInfo[1] = mPrefs.getString(context.getString(R.string.description), "");
        currentEventInfo[2] = mPrefs.getString(context.getString(R.string.location), "");
        currentEventInfo[3] = mPrefs.getString(context.getString(R.string.start_time), "");
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.current_event_provider);
        views.setTextViewText(R.id.eventSummary, currentEventInfo[0] + "\n" + currentEventInfo[1] + "\n" + currentEventInfo[2]);
        Log.d("updateAppWidget: ", currentEventInfo[0] + "\n" + currentEventInfo[1] + "\n" + currentEventInfo[2]);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

