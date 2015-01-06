package com.caseybrooks.scripturememory.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.RemoteViews;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VOTDService;
import com.caseybrooks.scripturememory.data.VerseDB;

public class VOTDWidget extends AppWidgetProvider {
    public static final String REFRESH_ALL = ".VOTDWidget.REFRESH_ALL";

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(intent.getAction().equals(REFRESH_ALL)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, VOTDWidget.class));

            VOTDWidget myWidget = new VOTDWidget();
            myWidget.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.context = context;

        //set pendingIntents on entire class of widgets
        setWidgetClick(context, appWidgetManager);

        //if(cacheIsGood) return cachedVerse;
        //else if(connected) return downloadedVerse;
        //else return errorMessageVerse;

        //get new verse if connected, else just don't update widgets
//        if(Util.isConnected(context)) {
//            new VOTDGetTask(context, MetaSettings.getBibleVersion(context), new OnTaskCompletedListener() {
//                @Override
//                public void onTaskCompleted(Object param) {
//                    if (param != null) {
//                        Passage passage = (Passage) param;
//                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_votd);
//                        views.setTextViewText(R.id.widget_votd_reference, passage.getReference().toString());
//                        views.setTextViewText(R.id.widget_votd_verse, passage.getText());
//
//                        // Instruct the widget manager to update the widget
//                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//
//                        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, VOTDWidget.class));
//
//                        for (int i = 0; i < appWidgetIds.length; i++) {
//                            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
//                        }
//                    }
//                }
//            }).execute();
//        }


        Passage currentVerse = VOTDService.getCurrentVerse(context);

        //if verse is old, delete it from database (no need to keep it around, its not in any lists),
        // and set currentVerse to null so that we download it again
        if(currentVerse != null && !currentVerse.getMetaData().getBoolean("IS_CURRENT")) {
            VerseDB db = new VerseDB(context).open();
            db.deleteVerse(currentVerse);
            db.close();
            currentVerse = null;
        }

        if(currentVerse == null) {
            new VOTDService.GetVOTD(context, new VOTDService.GetVerseListener() {

                @Override
                public void onPreDownload() {
                }

                @Override
                public void onVerseDownloaded(Passage passage) {
                    if(passage != null) {

                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_votd);
                        views.setTextViewText(R.id.widget_votd_reference, passage.getReference().toString());
                        views.setTextViewText(R.id.widget_votd_verse, passage.getText());

                        // Instruct the widget manager to update the widget
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, VOTDWidget.class));

                        for (int i = 0; i < appWidgetIds.length; i++) {
                            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
                        }
                    }

                }
            }).execute();
        }
        else {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_votd);
            views.setTextViewText(R.id.widget_votd_reference, currentVerse.getReference().toString());
            views.setTextViewText(R.id.widget_votd_verse, currentVerse.getText());

            // Instruct the widget manager to update the widget
            AppWidgetManager appWidgetManager2 = AppWidgetManager.getInstance(context);

            int[] appWidgetIds2 = appWidgetManager2.getAppWidgetIds(new ComponentName(context, VOTDWidget.class));

            for (int i = 0; i < appWidgetIds2.length; i++) {
                appWidgetManager2.updateAppWidget(appWidgetIds2[i], views);
            }
        }
    }

    private void setWidgetClick(Context context, AppWidgetManager appWidgetManager) {
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_votd);
        remoteViews.setOnClickPendingIntent(R.id.widget_votd_layout, resultPI);

        ComponentName watchWidget = new ComponentName(context, VOTDWidget.class);
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }
}


