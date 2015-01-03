package com.caseybrooks.scripturememory.notifications;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaReceiver;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;

import java.util.Calendar;

public class VOTDNotification {
//Data Members
//------------------------------------------------------------------------------
	Context context;

	Notification notification;
	NotificationManager manager;
	
	String ref, ver, ref_save, ver_save;
	
//Constructors and Initialization
//------------------------------------------------------------------------------
	public VOTDNotification(Context context) {
		this.context = context;

		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public VOTDNotification show() {
		manager.notify(2, notification);
		return this;
	}
	
	public VOTDNotification dismiss() {
		manager.cancel(2);
		return this;
	}
	
	public static void setAlarm(Context context) {

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
        Intent intent = new Intent(MetaReceiver.VOTD_ALARM);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.setRepeating(
				AlarmManager.RTC_WAKEUP,
				alarmTime.getTimeInMillis(),
				1000*60*60*24,
				alarmIntent);	
	}
	
//	public void cancelAlarm() {
//		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		Intent intent = new Intent(context, VOTDAlarmReceiver.class);
//		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//
//		alarmManager.cancel(alarmIntent);
//	}
	
	//Call when the verse has already been set in the object
	public VOTDNotification create() {
		if(ref_save != null && ver_save != null) {
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
			save.putExtra("REF_SAVE", ref_save);
			save.putExtra("VER_SAVE", ver_save);
			PendingIntent savePI = PendingIntent.getBroadcast(context, 0, save, PendingIntent.FLAG_CANCEL_CURRENT);
			
			//Builds the notification
			NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
			
			mb.setOngoing(false);
		    mb.setSmallIcon(R.drawable.ic_cross);
		    mb.setContentTitle("Verse of the Day");
			mb.setContentText(ref);
			mb.setPriority(NotificationCompat.PRIORITY_DEFAULT);
			mb.setStyle(new NotificationCompat.BigTextStyle().bigText(ref + ver));
			mb.setContentIntent(resultPI);
			mb.setAutoCancel(true);
			mb.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Save Verse", savePI);
			mb.setLights(ledColor, 500, 4500);
			notification = mb.build();
			notification.sound = ringtone;
		}
		else {
	   		new QuickNotification(context, "Verse of the Day", "Click here to see today's new Scripture!");
		}
		return this;
	}
	
	//Call when you can't show a verse but want to inform the user regardless
	public VOTDNotification create(String message) {
		ref = message;
		ver = "";
		create();
		return this;
	}

//Get Verse of the Day for the notification
//------------------------------------------------------------------------------
	//Call to download and issue a notification for the VOTD
	public VOTDNotification retrieveInternetVerse() {
//        new VOTDGetTask(context, MetaSettings.getBibleVersion(context), new OnTaskCompletedListener() {
//            @Override
//            public void onTaskCompleted(Object param) {
//                if(param != null) {
//					Passage passage = (Passage) param;
//                    int currentAPIVersion = android.os.Build.VERSION.SDK_INT;
//                    if (currentAPIVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                        //If device has Jelly Bean expanded notifications, set getText to
//                        //	only show reference on small, and verse getText on big
//                        ref = passage.getReference().toString();
//                        ver = " - " + passage.getText();
//                    }
//                    else {
//                        //If device does not have expanded notifications, let notification
//                        //	always show reference and getText
//                        ref = passage.getReference() + " - " + passage.getText();
//                        ver = "";
//                    }
//                    ref_save = passage.getReference().toString();
//                    ver_save = passage.getText();
//                    create();
//                    show();
//                }
//            }
//        }).execute();
        return this;
	}
	
//Broadcast Receivers relevant to this notification
//------------------------------------------------------------------------------
	public static class VOTDSaveVerseReceiver extends BroadcastReceiver {	
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
                Passage newVerse = new Passage(intent.getStringExtra("REF_SAVE"));
                newVerse.setText(intent.getStringExtra("VER_SAVE"));
                newVerse.setVersion(MetaSettings.getBibleVersion(context));
                newVerse.getMetaData().putInt(DefaultMetaData.STATE, 1);
                newVerse.getMetaData().putLong(DefaultMetaData.TIME_CREATED, Calendar.getInstance().getTimeInMillis());
				VerseDB db = new VerseDB(context);
				db.open();
				db.insertVerse(newVerse);
				db.close();
			}
			catch(Exception e) {
				new QuickNotification(context, "Verse of the Day", "There was an error saving the verse");
			}
			finally {
				new VOTDNotification(context).dismiss();
				new QuickNotification(context, "Verse of the Day", intent.getStringExtra("REF_SAVE") + " saved successfully");
			}
		}
	}
}
