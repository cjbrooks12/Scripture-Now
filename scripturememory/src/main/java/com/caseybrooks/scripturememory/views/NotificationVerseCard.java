package com.caseybrooks.scripturememory.views;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.EditVerse;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VersesDatabase;
import com.caseybrooks.scripturememory.notifications.MainNotification;

public class NotificationVerseCard extends LinearLayout { 
//Data Members
//------------------------------------------------------------------------------
	Context context;

	TextView ref, ver;
    ImageView contextMenu;
    RelativeLayout expandedSection;
    RadioButton normal, dashes, letters, letteredDashes;
    
    Passage passage;
    boolean notificationActive, /*visible,*/ isExpanded;
    int verseDisplayMode;

//Constructors and Initialization
//------------------------------------------------------------------------------    
    public NotificationVerseCard(Context context) {
        super(context);
		this.context = context;
        
		LayoutInflater.from(context).inflate(R.layout.card_notification_verse, this);
        
        initialize();
    }
    
    public NotificationVerseCard(Context context, AttributeSet attrs) {
        super(context, attrs);
		this.context = context;
		
		LayoutInflater.from(context).inflate(R.layout.card_notification_verse, this);
        
        initialize();
	}    
    
    void initialize() {
        ref = (TextView) findViewById(R.id.notificationReference);
        ver = (TextView) findViewById(R.id.notificationVerse);
		contextMenu = (ImageView) findViewById(R.id.contextMenu);
		contextMenu.setOnClickListener(contextMenuClick);
        this.setOnClickListener(expandCardClick);
				
		normal = (RadioButton) findViewById(R.id.radioNormal);
		normal.setOnClickListener(radioButtonClick);
		dashes = (RadioButton) findViewById(R.id.radioDashes);
		dashes.setOnClickListener(radioButtonClick);
		letters = (RadioButton) findViewById(R.id.radioLetters);
		letters.setOnClickListener(radioButtonClick);
		letteredDashes = (RadioButton) findViewById(R.id.radioLetteredDashes);
		letteredDashes.setOnClickListener(radioButtonClick);
				
		expandedSection = (RelativeLayout) findViewById(R.id.expandedLayout);
		expandedSection.setVisibility(View.GONE);
		isExpanded = false;
		
		refresh();
		
//        visible = false;
	}
    
    public void refresh() {

        verseDisplayMode = MetaSettings.getVerseDisplayMode(context);
        switch(verseDisplayMode) {
            case 0:
                ((RadioButton) findViewById(R.id.radioNormal)).setChecked(true);
                break;
            case 1:
                ((RadioButton) findViewById(R.id.radioDashes)).setChecked(true);
                break;
            case 2:
                ((RadioButton) findViewById(R.id.radioLetters)).setChecked(true);
                break;
            case 3:
                ((RadioButton) findViewById(R.id.radioLetteredDashes)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.radioNormal)).setChecked(true);
                break;
        }

        VersesDatabase db = new VersesDatabase(context);
        db.open();
        try {
            passage = db.getEntryAt(MetaSettings.getVerseId(context));

            ref.setText(passage.getReference());
            ver.setText(passage.getText());
        }
        catch(SQLException e) {
            try {
                passage = db.getFirstEntry("current");
                MetaSettings.putVerseId(context, passage.getId());
                ref.setText(passage.getReference());
                ver.setText(passage.getText());
            }
            catch(SQLException e1) {
                ref.setText("All verses memorized!");
                ver.setText("Why don't you try adding some more, or moving some verses back to your current list?");
            }
        }
        finally {
            db.close();
        }
    }
    
    public void removeFromParent() {
//    	visible = false;
		((ViewGroup)getParent()).removeView(NotificationVerseCard.this);
	}
    
//Public Getters and Setters
//------------------------------------------------------------------------------
//    public boolean isVisible() {
//		return visible;
//	}

//	public void setVisible(boolean visible) {
//		this.visible = visible;
//	}
	
//	public int getVerse() {
//		return passage;
//	}
	
	public boolean isExpanded() {
		return isExpanded;
	}
	
//Expand card to show display options for notification
//------------------------------------------------------------------------------
	public void expandCard() {
		expandedSection.setVisibility(View.VISIBLE);
		isExpanded = true;
	}
	
	public void shrinkCard() {
		expandedSection.setVisibility(View.GONE);
		isExpanded = false;
	}
	
//Click Listeners
//------------------------------------------------------------------------------      	
	private OnClickListener contextMenuClick = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			PopupMenu popup = new PopupMenu(context, v);
 		    popup.setOnMenuItemClickListener(menuItemClick);
 		    MenuInflater inflater = popup.getMenuInflater();
 		    inflater.inflate(R.menu.context_notification_card, popup.getMenu());
 		    popup.show();
  		}
 	};

    private OnClickListener expandCardClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
//			switchToEditFragment(notify_card.getId());
            if(isExpanded()) {
                shrinkCard();
            }
            else {
                expandCard();
            }
        }
    };
 	
 	private OnMenuItemClickListener menuItemClick = new OnMenuItemClickListener() {
		
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
	        case R.id.context_notification_card_edit:
	        	Intent intent = new Intent(context, EditVerse.class);
	 	    	intent.putExtra("KEY_ID", passage.getId());
	 	    	
	 	    	context.startActivity(intent);
	 	    	
	 	    	if(context instanceof ActionBarActivity) {
	 	 	    	((ActionBarActivity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	 	    	}
	        	return true;
	        case R.id.context_notification_card_toggle:
	        	notificationActive = MetaSettings.getNotificationActive(context);
	 			if(notificationActive) {
					MainNotification.getInstance().dismiss();
					notificationActive = false;
					
					MetaSettings.putNotificationActive(context, false);
				}
				else {
					MainNotification.notify(context).show();
					notificationActive = true;

                    MetaSettings.putNotificationActive(context, true);
				}
	        	return true;
	        default:
	            return false;
			}
		}
	};
 	
 	private OnClickListener radioButtonClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
            switch(v.getId()) {
                case R.id.radioNormal:
                    verseDisplayMode = 0;
                    break;
                case R.id.radioDashes:
                    verseDisplayMode = 1;
                    break;
                case R.id.radioLetters:
                    verseDisplayMode = 2;
                    break;
                case R.id.radioLetteredDashes:
                    verseDisplayMode = 3;
                    break;
            }
            MetaSettings.putVerseDisplayMode(context, verseDisplayMode);
            MetaSettings.putNotificationActive(context, true);

			MainNotification.notify(context).show();
		}
	};
}
