package com.caseybrooks.scripturememory.nowcards.votd;

import android.app.AlertDialog;
import android.content.Context;
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
import com.caseybrooks.scripturememory.R;

public class VOTDCard extends FrameLayout {
    private TextView ref, ver;
    private ProgressBar progress;
    ImageButton overflowButton;
    Context context;

//LIfecycle and Initialization
//------------------------------------------------------------------------------
    public VOTDCard(Context context) {
        super(context);
        this.context = context;
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(context).inflate(R.layout.card_votd, this);

		ref = (TextView) findViewById(R.id.votdReference);
		ver = (TextView) findViewById(R.id.votdVerse);

        overflowButton = (ImageButton) findViewById(R.id.overflowButton);
		overflowButton.setOnClickListener(overflowButtonClick);
		this.setOnClickListener(cardClick);

        progress = (ProgressBar) findViewById(R.id.progress);

		update();
    }

	public void update() {
		setWorking(false);
		Passage currentPassage = VOTD.getPassage(context);

		if(currentPassage != null) {
			ref.setText(currentPassage.getReference().toString());
			ver.setText(currentPassage.getText());
		}
		else {
			ref.setText("Trouble Displaying Verse");
			ver.setText("Click here to try again");
		}
	}

	public void removeFromParent() {
		setVisibility(View.GONE);
		((ViewGroup)getParent()).removeView(VOTDCard.this);
	}

//Click Listeners
//------------------------------------------------------------------------------
	private View.OnClickListener overflowButtonClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			PopupMenu popup = new PopupMenu(context, v);
			popup.setOnMenuItemClickListener(menuItemClick);
			MenuInflater inflater = popup.getMenuInflater();
			inflater.inflate(R.menu.overflow_votd_card, popup.getMenu());

			popup.show();
		}
	};

	private PopupMenu.OnMenuItemClickListener menuItemClick = new PopupMenu.OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {

			switch (item.getItemId()) {
			case R.id.overflow_votd_card_redownload:
				new VOTD.Redownload(context).execute();
				setWorking(true);
			default:
				return false;
			}
		}
	};











//    private OnClickListener overflowClick = new OnClickListener() {
//
//        @Override
//        public void onClick(View v) {
//            if(votd.currentVerse != null) {
//                PopupMenu popup = new PopupMenu(context, v);
//                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
////                            case R.id.overflow_votd_card_remove:
////                                removeFromParent();
////                                return true;
//                            case R.id.overflow_votd_card_redownload:
//                                setWorking(true);
//                                votd.redownload();
//                                return true;
////                            case R.id.overflow_votd_card_save:
////                                votd.saveVerse();
////                                Toast.makeText(context, votd.currentVerse.getReference().toString() + " has been saved", Toast.LENGTH_SHORT).show();
////                                return true;
////                            case R.id.overflow_votd_card_post:
////                                votd.setAsNotification();
////                                return true;
////                            case R.id.overflow_votd_card_browser:
////                                Intent i = new Intent(Intent.ACTION_VIEW);
////                                i.setData(Uri.parse("http://www.verseoftheday.com/"));
////                                context.startActivity(i);
////                                Toast.makeText(context, "Opening browser...", Toast.LENGTH_SHORT).show();
////                                return true;
//                            default:
//                                return false;
//                        }
//                    }
//                });
//                MenuInflater inflater = popup.getMenuInflater();
//                inflater.inflate(R.menu.overflow_votd_card, popup.getMenu());
//
////                if(votd.isSaved()) {
////                    popup.getMenu().findItem(R.id.overflow_votd_card_save).setVisible(false);
////                }
////                else {
////                    popup.getMenu().findItem(R.id.overflow_votd_card_save).setVisible(true);
////                }
////
//                popup.show();
//            }
//        }
//    };
//
    private OnClickListener cardClick = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if(VOTD.getPassage(context) != null && !VOTD.isSaved(context)) {
                final View view = LayoutInflater.from(context).inflate(R.layout.popup_add_verse, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(view);

                final AlertDialog dialog = builder.create();
                view.findViewById(R.id.scroll_area).setVisibility(View.GONE);

                TextView verseList = (TextView) view.findViewById(R.id.description);
                verseList.setText("Add " + VOTD.getPassage(context).getReference().toString() + " to your list?");

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
                        VOTD.saveVerse(context);
                        Toast.makeText(context, VOTD.getPassage(context).getReference().toString() + " has been saved", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
            else {
                new VOTD.Redownload(context).execute();
				setWorking(true);
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
}
