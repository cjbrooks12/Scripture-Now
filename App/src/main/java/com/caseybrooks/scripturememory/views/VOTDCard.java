package com.caseybrooks.scripturememory.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VOTDService;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.notifications.MainNotification;

public class VOTDCard extends FrameLayout {
//Data Members
//------------------------------------------------------------------------------
	Context context;

	private TextView ref, ver;
    private ProgressBar progress;
    ImageButton overflowButton;
    LinearLayout layout;

    Passage currentVerse;

    //status of Verse of the Day.
    //0: No attempt has been made to retrieve the Verse
    //1: We are currently trying to retrieve the Verse
    //2: The Verse has been successfully retrieved
    //3: Attempt to retrieve the Verse has failed
    int status;

//Constructors and Initialization
//------------------------------------------------------------------------------    
    public VOTDCard(Context context) {
        super(context);
		this.context = context;
        
		LayoutInflater.from(context).inflate(R.layout.card_votd, this);
        
        initialize();
    }
    
    void initialize() {
        status = 0;

        overflowButton = (ImageButton) findViewById(R.id.overflowButton);
        overflowButton.setOnClickListener(votdRemove);
        ref = (TextView) findViewById(R.id.votdReference);
        ver = (TextView) findViewById(R.id.votdVerse);
        layout = (LinearLayout) findViewById(R.id.votdLayout);
        progress = new ProgressBar(context);
        progress.setIndeterminate(true);

        this.setOnClickListener(votdAdd);

        retrieve();
    }

    public void removeFromParent() {
        setVisibility(View.GONE);
        ((ViewGroup)getParent()).removeView(VOTDCard.this);
    }

    public void retrieve() {
        status = 1;
        currentVerse = VOTDService.getCurrentVerse(context);

        //if verse is old, delete it from database (no need to keep it around, its not in any lists),
        // and set currentVerse to null so that we download it again
        if(currentVerse != null && !currentVerse.getMetadata().getBoolean("IS_CURRENT")) {
            if(currentVerse.getMetadata().getInt(DefaultMetaData.STATE) == VerseDB.VOTD) {
                VerseDB db = new VerseDB(context).open();
                db.deleteVerse(currentVerse);
                db.close();
            }
            currentVerse = null;
        }

        if(currentVerse == null) {
            new VOTDService.GetVOTD(context, new VOTDService.GetVerseListener() {

                @Override
                public void onPreDownload() {
                    layout.addView(progress, 1);
                    ref.setVisibility(View.GONE);
                    ver.setVisibility(View.GONE);
                }

                @Override
                public void onVerseDownloaded(Passage passage) {
                    if(passage != null) {
                        currentVerse = passage;

                        layout.removeView(progress);
                        ref.setVisibility(View.VISIBLE);
                        ver.setVisibility(View.VISIBLE);

                        ref.setText(currentVerse.getReference().toString());
                        ver.setText(currentVerse.getText());
                        status = 2;
                    }
                    else {
                        layout.removeView(progress);
                        ref.setVisibility(View.VISIBLE);
                        ver.setVisibility(View.VISIBLE);

                        ref.setText("Problem Retrieving Verse");
						ver.setText("Please check your internet connection and click to try again");
                        status = 3;
                    }
                }
            }).execute();
        }
        else {
            ref.setText(currentVerse.getReference().toString());
            ver.setText(currentVerse.getText());
            status = 2;
        }
	}

//Helper Methods
//------------------------------------------------------------------------------
    public void saveVerse() {
        final View view = LayoutInflater.from(context).inflate(R.layout.popup_add_verse, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        ScrollView scrollArea = (ScrollView) view.findViewById(R.id.scroll_area);
        scrollArea.setVisibility(View.GONE);

        TextView verseList = (TextView) view.findViewById(R.id.description);
        verseList.setText("Add " + currentVerse.getReference().toString() + " to your list?");

        TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        TextView addVerseButton = (TextView) view.findViewById(R.id.add_verse_button);
        addVerseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VerseDB db = new VerseDB(context).open();
                currentVerse.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.CURRENT_NONE);
                currentVerse.addTag("VOTD");
                db.updateVerse(currentVerse);
                dialog.cancel();
            }
        });

        dialog.show();
    }
    
//Click Listeners
//------------------------------------------------------------------------------     
    private OnClickListener votdRemove = new OnClickListener() {
 		@Override
 		public void onClick(final View v) {
            PopupMenu popup = new PopupMenu(context, v);
            popup.setOnMenuItemClickListener(menuItemClick);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.context_votd_card, popup.getMenu());
            popup.show();
        }
 	};

    private PopupMenu.OnMenuItemClickListener menuItemClick = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.context_votd_card_remove:
                    removeFromParent();
                    return true;
                case R.id.context_votd_card_redownload:
                    if(currentVerse != null) {
                        VerseDB db = new VerseDB(context).open();
                        db.deleteVerse(currentVerse);
                        db.close();
                    }
                    retrieve();
                    return true;
                case R.id.context_votd_card_save:
                    saveVerse();
                    Toast.makeText(context, currentVerse.getReference().toString() + " has been saved", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.context_votd_card_post:
                    VerseDB db = new VerseDB(context).open();
                    currentVerse.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.CURRENT_NONE);
                    currentVerse.addTag("VOTD");
                    db.updateVerse(currentVerse);

                    MetaSettings.putVerseId(context, currentVerse.getMetadata().getInt(DefaultMetaData.ID));
                    MetaSettings.putNotificationActive(context, true);
                    MetaSettings.putActiveList(context, VerseListFragment.TAGS, (int)db.getTagID("VOTD"));
                    MainNotification.notify(context).show();
                    Toast.makeText(context, currentVerse.getReference().toString() + " has been saved and set as notification", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.context_votd_card_browser:
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("http://www.verseoftheday.com/"));
                    context.startActivity(i);
                    Toast.makeText(context, "Opening browser...", Toast.LENGTH_SHORT).show();
                    return true;
                default:
                    return false;
            }
        }
    };
 	
 	private OnClickListener votdAdd = new OnClickListener() {
 		@Override
 		public void onClick(final View v) {
            switch(status) {
                case 0: retrieve(); break;
                case 1: break;
                case 2: saveVerse(); break;
                case 3: retrieve(); break;
            }
 		}
 	};
}
