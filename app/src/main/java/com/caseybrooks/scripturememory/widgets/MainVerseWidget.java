package com.caseybrooks.scripturememory.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.caseybrooks.androidbibletools.basic.DefaultFormatter;
import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaReceiver;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;

/**
 * Implementation of App Widget functionality.
 */
public class MainVerseWidget extends AppWidgetProvider {
    public static final String NEXT_BUTTON_CLICKED = ".MainVerseWidget.nextButtonClicked";
    public static final String UPDATE_ALL_WIDGETS = ".MainVerseWidget.updateAllWidgets";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(intent.getAction().equals(NEXT_BUTTON_CLICKED)) {
            //user clicked button to go to next verse. Let notification class take
            //  care of hard work, and then send control back here
            Intent next = new Intent(MetaReceiver.NEXT_VERSE);
            next.putExtra("notificationId", 1);
            next.putExtra("SQL_ID", MetaSettings.getVerseId(context));
            context.sendBroadcast(next);
        }
        else if(intent.getAction().equals(UPDATE_ALL_WIDGETS)) {
            //Force refresh of all widgets
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MainVerseWidget.class));

            MainVerseWidget myWidget = new MainVerseWidget();
            myWidget.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //set pendingIntents on entire class of widgets
        setNextButtonClick(context, appWidgetManager);
        setWidgetClick(context, appWidgetManager);

        // There may be multiple widgets active, so update all of them
        for(int i = 0; i < appWidgetIds.length; i++) {
            configureKeyguardWidget(context, appWidgetManager, appWidgetIds[i]);
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    private void configureKeyguardWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            RemoteViews nextButton = new RemoteViews(context.getPackageName(), R.layout.widget_main_verse);

            Bundle myOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int category = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
            boolean isKeyguard = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;

            if(isKeyguard) {
                nextButton.setViewVisibility(R.id.widget_votd_refresh, View.GONE);
                appWidgetManager.updateAppWidget(appWidgetId, nextButton);
            }
        }
    }

    private void setNextButtonClick(Context context, AppWidgetManager appWidgetManager) {
        Intent nextIntent = new Intent(context, getClass());
        nextIntent.setAction(NEXT_BUTTON_CLICKED);
        PendingIntent nextPI = PendingIntent.getBroadcast(context, 0, nextIntent, 0);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main_verse);
        remoteViews.setOnClickPendingIntent(R.id.widget_votd_refresh, nextPI);

        ComponentName watchWidget = new ComponentName(context, MainVerseWidget.class);
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    private void setWidgetClick(Context context, AppWidgetManager appWidgetManager) {
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main_verse);
        remoteViews.setOnClickPendingIntent(R.id.widget_main_layout, resultPI);

        ComponentName watchWidget = new ComponentName(context, MainVerseWidget.class);
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        int id = MetaSettings.getVerseId(context);
        Passage verse;

        VerseDB db = new VerseDB(context);
        db.open();
        try {
            verse = db.getVerse(id);
			switch(MetaSettings.getVerseDisplayMode(context)) {
				case 0:
					verse.setFormatter(new DefaultFormatter.Normal());
					break;
				case 1:
                    verse.setFormatter(new DefaultFormatter.Dashes());
					break;
				case 2:
                    verse.setFormatter(new DefaultFormatter.FirstLetters());
					break;
				case 3:
                    verse.setFormatter(new DefaultFormatter.DashedLetter());
					break;
                case 4:
                    float randomness = MetaSettings.getRandomnessLevel(context);
                    verse.setFormatter(new DefaultFormatter.RandomWords(randomness));
                    break;
				default:
					break;
			}
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main_verse);
            views.setTextViewText(R.id.widget_main_reference, verse.getReference().toString());
            views.setTextViewText(R.id.widget_main_verse, verse.getText());


            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        catch(SQLException e) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main_verse);
            views.setTextViewText(R.id.widget_main_reference, "Error");
            views.setTextViewText(R.id.widget_main_verse, Log.getStackTraceString(e));

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        db.close();
    }





}


