package com.caseybrooks.scripturememory.views;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.container.Verses;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.EditVerse;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.notifications.MainNotification;

public class NotificationVerseCard extends FrameLayout {
//Data Members
//------------------------------------------------------------------------------
	Context context;

	TextView ref, ver;
    ImageView contextMenu;
    RelativeLayout expandedSection;
    RadioButton normal, dashes, letters, letteredDashes, randomWords;
    SeekBar randomnessLevelSlider;
    
    Passage passage;
    boolean notificationActive, isExpanded;
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
		contextMenu = (ImageView) findViewById(R.id.overflowButton);
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
        randomWords = (RadioButton) findViewById(R.id.radioRandomWords);
        randomWords.setOnClickListener(radioButtonClick);

        randomnessLevelSlider = (SeekBar) findViewById(R.id.randomnessLevelSlider);
        randomnessLevelSlider.setOnSeekBarChangeListener(seekBarChangeListener);

				
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

        VerseDB db = new VerseDB(context);
        db.open();

        passage = db.getVerse(MetaSettings.getVerseId(context));

        if(passage != null) {
            String s = passage.getReference().toString();
            ref.setText(s);
            ver.setText(passage.getText());
        }
        else {
            //look through all "current" lists and get the first verse it finds. If there are none
            //in states 1-4, tell the user to add more
            for(int i = 1; i <= 4; i++) {
                Verses<Passage> allCurrent = db.getStateVerses(i);
                if (allCurrent.size() > 0) {
                    passage = allCurrent.get(0);
                    MetaSettings.putVerseId(context, (int) passage.getId());
                    ref.setText(passage.getReference().toString());
                    ver.setText(passage.getText());
                    break;
                }
                else if(allCurrent.size() == 0 && i == 4) {
                    ref.setText("All verses memorized!");
                    ver.setText("Why don't you try adding some more, or moving some verses back to your current list?");
                }
            }
        }

        db.close();
    }
    
    public void removeFromParent() {
		((ViewGroup)getParent()).removeView(NotificationVerseCard.this);
	}
    
//Public Getters and Setters
//------------------------------------------------------------------------------
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
	 	 	    	((ActionBarActivity) context).overridePendingTransition(R.anim.slide_in_right, 0);
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
                    randomnessLevelSlider.setVisibility(View.GONE);
                    break;
                case R.id.radioDashes:
                    verseDisplayMode = 1;
                    randomnessLevelSlider.setVisibility(View.GONE);
                    break;
                case R.id.radioLetters:
                    verseDisplayMode = 2;
                    randomnessLevelSlider.setVisibility(View.GONE);
                    break;
                case R.id.radioLetteredDashes:
                    verseDisplayMode = 3;
                    randomnessLevelSlider.setVisibility(View.GONE);
                    break;
                case R.id.radioRandomWords:
                    verseDisplayMode = 4;
                    randomnessLevelSlider.setVisibility(View.VISIBLE);
                    break;
            }
            MetaSettings.putVerseDisplayMode(context, verseDisplayMode);
            MetaSettings.putNotificationActive(context, true);

			MainNotification.notify(context).show();
		}
	};

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MetaSettings.putRandomnessLevel(context, seekBar.getProgress() / 1000f);

            if(MetaSettings.getNotificationActive(context)) {
                MainNotification.notify(context).show();
            }
        }
    };
}
