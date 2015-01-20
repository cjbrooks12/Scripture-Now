package com.caseybrooks.scripturememory.views;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.androidbibletools.search.VerseOfTheDay;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.notifications.MainNotification;
import com.caseybrooks.scripturememory.notifications.QuickNotification;
import com.caseybrooks.scripturememory.widgets.MainVerseWidget;

import java.io.IOException;
import java.util.Calendar;

public class VOTD {
//Data Members
//------------------------------------------------------------------------------
    Context context;
    public Passage currentVerse;

    private VOTDView view;
    private VOTDNotification notification;

//Constructors and Initialization
//------------------------------------------------------------------------------
    public VOTD(Context context) {
        this.context = context;

        getCurrentVerse();

        if(currentVerse == null) {
            new DownloadCurrentVerse().execute();
        }
    }

    public VOTDView getView() {
        view = new VOTDView(context);
        if(currentVerse != null) {
            view.update();
        }

        return view;
    }

    public VOTDNotification getNotification() {
        notification = new VOTDNotification();
        return notification.create();
    }

//dashboard card component of VOTD
//------------------------------------------------------------------------------
    public class VOTDView extends FrameLayout {
        private TextView ref, ver;
        private ProgressBar progress;
        ImageButton overflowButton;

        public VOTDView(Context context) {
            super(context);

            initialize();
        }

        private void initialize() {
            LayoutInflater.from(context).inflate(R.layout.card_votd, this);

            overflowButton = (ImageButton) findViewById(R.id.overflowButton);
            overflowButton.setOnClickListener(overflowClick);
            ref = (TextView) findViewById(R.id.votdReference);
            ver = (TextView) findViewById(R.id.votdVerse);
            progress = (ProgressBar) findViewById(R.id.progress);

            this.setOnClickListener(cardClick);
        }

        public void removeFromParent() {
            setVisibility(View.GONE);
            ((ViewGroup)getParent()).removeView(VOTDView.this);
        }

        private OnClickListener overflowClick = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(currentVerse != null) {
                    PopupMenu popup = new PopupMenu(context, v);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.overflow_votd_card_remove:
                                    removeFromParent();
                                    return true;
                                case R.id.overflow_votd_card_redownload:
                                    new DownloadCurrentVerse().execute();
                                    return true;
                                case R.id.overflow_votd_card_save:
                                    saveVerse();
                                    Toast.makeText(context, currentVerse.getReference().toString() + " has been saved", Toast.LENGTH_SHORT).show();
                                    return true;
                                case R.id.overflow_votd_card_post:
                                    setAsNotification();
                                    return true;
                                case R.id.overflow_votd_card_browser:
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("http://www.verseoftheday.com/"));
                                    context.startActivity(i);
                                    Toast.makeText(context, "Opening browser...", Toast.LENGTH_SHORT).show();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.overflow_votd_card, popup.getMenu());

                    if(currentVerse.getMetadata().getInt(DefaultMetaData.STATE) == VerseDB.VOTD) {
                        popup.getMenu().findItem(R.id.overflow_votd_card_save).setVisible(true);
                    }
                    else {
                        popup.getMenu().findItem(R.id.overflow_votd_card_save).setVisible(false);
                    }

                    popup.show();
                }
            }
        };

        private OnClickListener cardClick = new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(currentVerse != null) {
                    final View view = LayoutInflater.from(context).inflate(R.layout.popup_add_verse, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(view);

                    final AlertDialog dialog = builder.create();
                    view.findViewById(R.id.scroll_area).setVisibility(View.GONE);

                    TextView verseList = (TextView) view.findViewById(R.id.description);
                    verseList.setText("Add " + currentVerse.getReference().toString() + " to your list?");

                    TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    TextView addVerseButton = (TextView) view.findViewById(R.id.add_verse_button);
                    addVerseButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveVerse();
                            Toast.makeText(context, currentVerse.getReference().toString() + " has been saved", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
                else {

                }
            }
        };

        public void setWorking(boolean isWorking) {
            if(isWorking) {
                ref.setVisibility(View.GONE);
                ver.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
            }
            else {
                ref.setVisibility(View.VISIBLE);
                ver.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            }
        }

        public void update() {
            if(currentVerse != null) {
                ref.setText(currentVerse.getReference().toString());
                ver.setText(currentVerse.getText());
            }
            else {
                ref.setText("Problem Retrieving Verse");
                ver.setText("Please check your internet connection and click to try again");
            }
        }
    }

//notification component of VOTD
//------------------------------------------------------------------------------
    public class VOTDNotification {
        Notification notification;
        NotificationManager manager;

        public VOTDNotification() {
            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        public void setAlarm() {
            //Get the time the alarm should go off
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            Calendar storedTime = Calendar.getInstance();
            storedTime.setTimeInMillis(
                    preferences.getLong("PREF_VOTD_TIME", storedTime.getTimeInMillis()));

            Calendar alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, storedTime.get(Calendar.HOUR_OF_DAY));
            alarmTime.set(Calendar.MINUTE, storedTime.get(Calendar.MINUTE));

            if(alarmTime.get(Calendar.HOUR_OF_DAY) < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                alarmTime.add(Calendar.DATE, 1);
            }
            else if(alarmTime.get(Calendar.HOUR_OF_DAY) == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                if(alarmTime.get(Calendar.MINUTE) <= Calendar.getInstance().get(Calendar.MINUTE)) {
                    alarmTime.add(Calendar.DATE, 1);
                }
            }

            //Create an alarm to go off at that time
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, VOTDAlarmReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.getTimeInMillis(),
                    1000*60*60*24,
                    alarmIntent);
        }

        public VOTDNotification create() {
            if(currentVerse != null) {
                int ledColor = context.getResources().getColor(R.color.forest_green);
                Uri ringtone = Uri.parse(MetaSettings.getVOTDSound(context));

                //Opens the dashboard when clicked
                Intent resultIntent = new Intent(context, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                //Stores the verse when the user hits "Save"
                Intent save = new Intent(context, VOTDSaveVerseReceiver.class);
                PendingIntent savePI = PendingIntent.getBroadcast(context, 0, save, PendingIntent.FLAG_CANCEL_CURRENT);

                //Builds the notification
                NotificationCompat.Builder mb = new NotificationCompat.Builder(context);

                mb.setOngoing(false);
                mb.setSmallIcon(R.drawable.ic_cross);
                mb.setContentTitle("Verse of the Day");
                mb.setContentText(currentVerse.getReference().toString());
                mb.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                mb.setStyle(new NotificationCompat.BigTextStyle().bigText(currentVerse.getReference().toString() + " - " + currentVerse.getText()));
                mb.setContentIntent(resultPI);
                mb.setAutoCancel(true);
                mb.addAction(R.drawable.ic_action_save_dark, "Save Verse", savePI);
                mb.setLights(ledColor, 500, 4500);
                notification = mb.build();
                notification.sound = ringtone;
            }
            else {
                new QuickNotification(context, "Verse of the Day", "Click here to see today's new Scripture!");
            }
            return this;
        }

        public VOTDNotification show() {
            manager.notify(2, notification);
            return this;
        }

        public VOTDNotification dismiss() {
            manager.cancel(2);
            return this;
        }
    }

//VOTD lifecycle and verse management
//------------------------------------------------------------------------------

    public static class VOTDSaveVerseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            VOTD votd = new VOTD(context);
            if(votd.currentVerse != null) {
                votd.saveVerse();
                votd.getNotification().dismiss();
                new QuickNotification(context, "Verse of the Day", votd.currentVerse.getReference().toString() + " added to list").show();
            }
        }
    }

    public static class VOTDAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(MetaSettings.getVOTDShow(context)) {
                new VOTD(context).getNotification().show();
            }
        }
    }

    public void saveVerse() {
        VerseDB db = new VerseDB(context).open();
        currentVerse.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.CURRENT_NONE);
        currentVerse.addTag("VOTD");
        int id = db.getVerseId(currentVerse.getReference());
        if (id == -1) {
            id = db.insertVerse(currentVerse);
            currentVerse.getMetadata().putInt(DefaultMetaData.ID, id);
        }
        else {
            currentVerse.getMetadata().putInt(DefaultMetaData.ID, id);
            db.updateVerse(currentVerse);
        }
        db.close();
    }

    public void setAsNotification() {
        if(currentVerse != null) {
            saveVerse();
            VerseDB db = new VerseDB(context).open();
            int id = db.getVerseId(currentVerse.getReference());
            MetaSettings.putVerseId(context, id);
            MetaSettings.putNotificationActive(context, true);
            MetaSettings.putActiveList(context, VerseListFragment.TAGS, (int) db.getTagID("VOTD"));
            db.close();

            //update widgets
            context.sendBroadcast(new Intent(MainVerseWidget.UPDATE_ALL_WIDGETS));
            //update dashboard card
            context.sendBroadcast(new Intent(DashboardFragment.REFRESH));
            //post main notification
            MainNotification.notify(context).show();
        }
    }

    public void getCurrentVerse() {
        //get all verses that are either tagged or in the state of VOTD
        VerseDB db = new VerseDB(context).open();
        currentVerse = db.getMostRecentVOTD();
        db.close();

        //no VOTD exist in database at this time
        if(currentVerse != null) {
            //check the timestamp of the most recent verse against the current time.
            //if the current verse is on the same day as today, then it is current.
            //if the current verse is not today, it needs to get updated
            Calendar today = Calendar.getInstance();
            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(currentVerse.getMetadata().getLong(DefaultMetaData.TIME_CREATED));

            boolean isCurrent =
                    (today.get(Calendar.ERA) == current.get(Calendar.ERA)
                            && today.get(Calendar.YEAR) == current.get(Calendar.YEAR)
                            && today.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR));

            if(!isCurrent) {
                db.open();
                db.deleteVerse(currentVerse);
                db.close();
                currentVerse = null;
            }
        }
    }

    private class DownloadCurrentVerse extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (view != null) {
                view.setWorking(true);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (view != null) {
                view.setWorking(false);
                view.update();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (view != null) {
                view.setWorking(false);
                view.update();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                currentVerse = VerseOfTheDay.retrieve(MetaSettings.getBibleVersion(context));

                if (currentVerse != null) {
                    currentVerse.addTag("VOTD");
                    currentVerse.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.VOTD);

                    VerseDB db = new VerseDB(context).open();
                    int id = db.getVerseId(currentVerse.getReference());
                    if (id == -1) {
                        id = db.insertVerse(currentVerse);
                        currentVerse.getMetadata().putInt(DefaultMetaData.ID, id);
                    } else {
                        currentVerse.getMetadata().putInt(DefaultMetaData.ID, id);
                        db.updateVerse(currentVerse);
                    }
                    db.close();
                }

                return null;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return null;
            }
        }
    }
}
