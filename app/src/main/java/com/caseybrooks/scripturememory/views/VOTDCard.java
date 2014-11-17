package com.caseybrooks.scripturememory.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.OnTaskCompletedListener;
import com.caseybrooks.scripturememory.data.VOTDGetTask;
import com.caseybrooks.scripturememory.data.VerseDB;

import java.util.Calendar;

public class VOTDCard extends FrameLayout {
//Data Members
//------------------------------------------------------------------------------
	Context context;

	private TextView ref, ver;
    private ProgressBar progress;
    ImageButton removeView;
    LinearLayout layout;

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
    
    public VOTDCard(Context context, AttributeSet attrs) {
        super(context, attrs);
		this.context = context;
		
		LayoutInflater.from(context).inflate(R.layout.card_votd, this);
        
        initialize();
	}
    
    void initialize() {
        status = 0;

        removeView = (ImageButton) findViewById(R.id.contextMenu);
        removeView.setOnClickListener(votdRemove);
        ref = (TextView) findViewById(R.id.votdReference);
        ver = (TextView) findViewById(R.id.votdVerse);
        layout = (LinearLayout) findViewById(R.id.votdLayout);
        progress = new ProgressBar(context);
        progress.setIndeterminate(true);

        this.setOnClickListener(votdAdd);
    }

    public void removeFromParent() {
        setVisibility(View.GONE);
        ((ViewGroup)getParent()).removeView(VOTDCard.this);
    }

    public void retrieve() {
        status = 1;

        layout.addView(progress, 1);
        ref.setVisibility(View.GONE);
        ver.setVisibility(View.GONE);
		try {
			new VOTDGetTask(context, MetaSettings.getBibleVersion(context), new OnTaskCompletedListener() {
				@Override
				public void onTaskCompleted(Object param) {
					if (param != null) {
						status = 2;

						Passage passage = (Passage) param;
						layout.removeView(progress);
						ref.setVisibility(View.VISIBLE);
						ver.setVisibility(View.VISIBLE);

						ref.setText(passage.getReference());
						ver.setText(passage.getText());
					} else {
						status = 3;

						layout.removeView(progress);
						ref.setVisibility(View.VISIBLE);
						ver.setVisibility(View.VISIBLE);

						ref.setText("Problem Retrieving Verse");
						ver.setText("Please check your internet connection and click to try again");
					}
				}
			}).execute();
		}
		catch(Exception e) {
			status = 3;

			layout.removeView(progress);
			ref.setVisibility(View.VISIBLE);
			ver.setVisibility(View.VISIBLE);

			ref.setText("Problem Retrieving Verse");
			ver.setText(e.getMessage());

		}
	}

//Helper Methods
//------------------------------------------------------------------------------
	public void saveVerse() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(true);
			builder.setTitle("Verse of the Day");
			builder.setMessage("Save " + ref.getText().toString() + " to verse list?");
			builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					boolean success;
					try {
                        Passage newVerse = new Passage(ref.getText().toString());
                        newVerse.setText(ver.getText().toString());
                        newVerse.setVersion(MetaSettings.getBibleVersion(context));
                        newVerse.setState(1);
                        newVerse.setMillis(Calendar.getInstance().getTimeInMillis());
                        VerseDB db = new VerseDB(context);
                        db.open();
                        db.insertVerse(newVerse);
                        db.close();
                        success = true;
					}
					catch(Exception ex) {
						success = false;
						Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
					}
					if(success) {
						Toast.makeText(context, "Verse Saved", Toast.LENGTH_SHORT).show();
					}
				    else {
					    Toast.makeText(context, "Something went wrong while saving verse", Toast.LENGTH_LONG).show();
				    }
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					
				}
			});
			
		AlertDialog dialog = builder.create();
		dialog.show();	
	}
    
//Click Listeners
//------------------------------------------------------------------------------     
    private OnClickListener votdRemove = new OnClickListener() {
 		@Override
 		public void onClick(final View v) {
 			removeFromParent();
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
