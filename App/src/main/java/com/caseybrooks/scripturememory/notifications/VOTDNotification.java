package com.caseybrooks.scripturememory.notifications;

public class VOTDNotification {
//Data Members
//------------------------------------------------------------------------------
//	Context context;
//
//	Notification notification;
//	NotificationManager manager;
//
//	String ref, ver, ref_save, ver_save;
//
//Constructors and Initialization
//------------------------------------------------------------------------------
//	public VOTDNotification(Context context) {
//		this.context = context;
//
//		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//	}
//
//	public VOTDNotification show() {
//		manager.notify(2, notification);
//		return this;
//	}
//
//	public VOTDNotification dismiss() {
//		manager.cancel(2);
//		return this;
//	}
//
//
//
//Call when the verse has already been set in the object
//	public VOTDNotification create() {
//		if(ref_save != null && ver_save != null) {
//			int ledColor = context.getResources().getColor(R.color.forest_green);
//			Uri ringtone = Uri.parse(MetaSettings.getVOTDSound(context));
//
//			//Opens the dashboard when clicked
//			Intent resultIntent = new Intent(context, MainActivity.class);
//			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//				stackBuilder.addParentStack(MainActivity.class);
//				stackBuilder.addNextIntent(resultIntent);
//			PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//			//Stores the verse when the user hits "Save"
//			Intent save = new Intent(context, VOTDSaveVerseReceiver.class);
//			save.putExtra("REF_SAVE", ref_save);
//			save.putExtra("VER_SAVE", ver_save);
//			PendingIntent savePI = PendingIntent.getBroadcast(context, 0, save, PendingIntent.FLAG_CANCEL_CURRENT);
//
//			//Builds the notification
//			NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
//
//			mb.setOngoing(false);
//		    mb.setSmallIcon(R.drawable.ic_cross);
//		    mb.setContentTitle("Verse of the Day");
//			mb.setContentText(ref);
//			mb.setPriority(NotificationCompat.PRIORITY_DEFAULT);
//			mb.setStyle(new NotificationCompat.BigTextStyle().bigText(ref + ver));
//			mb.setContentIntent(resultPI);
//			mb.setAutoCancel(true);
//			mb.addAction(R.drawable.ic_action_save_dark, "Save Verse", savePI);
//			mb.setLights(ledColor, 500, 4500);
//			notification = mb.build();
//			notification.sound = ringtone;
//		}
//		else {
//	   		new QuickNotification(context, "Verse of the Day", "Click here to see today's new Scripture!");
//		}
//		return this;
//	}
//
//Call when you can't show a verse but want to inform the user regardless
//	public VOTDNotification create(String message) {
//		ref = message;
//		ver = "";
//		create();
//		return this;
//	}
//
//Get Verse of the Day for the notification
//------------------------------------------------------------------------------
//	//Call to download and issue a notification for the VOTD
//	public VOTDNotification retrieveInternetVerse() {
//        Passage currentVerse = VOTDService.getCurrentVerse(context);
//
//        //if verse is old, delete it from database (no need to keep it around, its not in any lists),
//        // and set currentVerse to null so that we download it again
//        if(currentVerse != null && !currentVerse.getMetadata().getBoolean("IS_CURRENT")) {
//            if(currentVerse.getMetadata().getInt(DefaultMetaData.STATE) == VerseDB.VOTD) {
//                VerseDB db = new VerseDB(context).open();
//                db.deleteVerse(currentVerse);
//                db.close();
//            }
//            currentVerse = null;
//        }
//
//        if(currentVerse == null) {
//            new VOTDService.GetVOTD(context, new VOTDService.GetVerseListener() {
//
//                @Override
//                public void onPreDownload() {
//                }
//
//                @Override
//                public void onVerseDownloaded(Passage passage) {
//                    if(passage != null) {
//                        int currentAPIVersion = android.os.Build.VERSION.SDK_INT;
//                        if (currentAPIVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                            //If device has Jelly Bean expanded notifications, set getText to
//                            //	only show reference on small, and verse getText on big
//                            ref = passage.getReference().toString();
//                            ver = " - " + passage.getText();
//                        }
//                        else {
//                            //If device does not have expanded notifications, let notification
//                            //	always show reference and getText
//                            ref = passage.getReference() + " - " + passage.getText();
//                            ver = "";
//                        }
//                        ref_save = passage.getReference().toString();
//                        ver_save = passage.getText();
//                        create();
//                        show();
//                    }
//                    else {
//
//                    }
//                }
//            }).execute();
//        }
//        else {
//            int currentAPIVersion = android.os.Build.VERSION.SDK_INT;
//            if (currentAPIVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                //If device has Jelly Bean expanded notifications, set getText to
//                //	only show reference on small, and verse getText on big
//                ref = currentVerse.getReference().toString();
//                ver = " - " + currentVerse.getText();
//            }
//            else {
//                //If device does not have expanded notifications, let notification
//                //	always show reference and getText
//                ref = currentVerse.getReference() + " - " + currentVerse.getText();
//                ver = "";
//            }
//            ref_save = currentVerse.getReference().toString();
//            ver_save = currentVerse.getText();
//            create();
//            show();
//        }
//        return this;
//	}
//
//Broadcast Receivers relevant to this notification
//------------------------------------------------------------------------------
//	public static class VOTDSaveVerseReceiver extends BroadcastReceiver {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			try {
//                Passage newVerse = VOTDService.getCurrentVerse(context);
//                if(newVerse != null && !newVerse.getMetadata().getBoolean("IS_CURRENT")) {
//                    newVerse.getMetadata().putInt(DefaultMetaData.STATE, 1);
//                    VerseDB db = new VerseDB(context);
//                    db.open();
//                    db.updateVerse(newVerse);
//                    db.close();
//                }
//                else {
//                    new QuickNotification(context, "Verse of the Day", "Today's verse could not be added, try again within the app");
//                }
//			}
//			catch(Exception e) {
//				new QuickNotification(context, "Verse of the Day", "There was an error saving the verse");
//			}
//			finally {
//				new VOTDNotification(context).dismiss();
//				new QuickNotification(context, "Verse of the Day", intent.getStringExtra("REF_SAVE") + " saved successfully");
//			}
//		}
//	}
}
