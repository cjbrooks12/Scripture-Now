package com.caseybrooks.scripturememory.nowcards.votd;

import android.content.Context;
import android.support.v7.app.AlertDialog;
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

import com.caseybrooks.androidbibletools.basic.AbstractVerse;
import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.widget.IVerseViewListener;
import com.caseybrooks.androidbibletools.widget.LoadState;
import com.caseybrooks.androidbibletools.widget.VerseView;
import com.caseybrooks.scripturememory.R;

public class VOTDCard extends FrameLayout implements IVerseViewListener {
    private TextView ref;
	private VerseView ver;
    private ProgressBar progress;
    ImageButton overflowButton;
    Context context;

	VOTD votd;

//Lifecycle and Initialization
//------------------------------------------------------------------------------
    public VOTDCard(Context context) {
        super(context);
        this.context = context;
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(context).inflate(R.layout.card_votd, this);

		ref = (TextView) findViewById(R.id.votdReference);
		ver = (VerseView) findViewById(R.id.votdVerse);

        overflowButton = (ImageButton) findViewById(R.id.overflowButton);
		overflowButton.setOnClickListener(overflowButtonClick);
		this.setOnClickListener(cardClick);

        progress = (ProgressBar) findViewById(R.id.progress);

		setWorking(true);

		votd = new VOTD(context);
		votd.setListener(this);
//		ver.setListener(this);
		votd.loadTodaysVerse();
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
			case R.id.overflow_votd_card_remove:
				removeFromParent();
				return true;
			case R.id.overflow_votd_card_redownload:
				setWorking(true);
				votd.workerThread.post(votd.getReferenceRunnable);
			case R.id.overflow_votd_card_save:
				votd.saveVerse();
				Toast.makeText(context, votd.getVerse().getReference().toString() + " has been saved", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.overflow_votd_card_post:
				votd.setAsNotification();
				return true;
			default:
				return false;
			}
		}
	};



    private OnClickListener cardClick = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if(!votd.isVerseSaved()) {
                final View view = LayoutInflater.from(context).inflate(R.layout.popup_add_verse, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(view);

                final AlertDialog dialog = builder.create();
                view.findViewById(R.id.scroll_area).setVisibility(View.GONE);

                TextView verseList = (TextView) view.findViewById(R.id.description);
                verseList.setText("Add " + votd.getVerse().getReference().toString() + " to your list?");

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
                        votd.saveVerse();
                        Toast.makeText(context, votd.getVerse().getReference().toString() + " has been saved", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                dialog.show();
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

	@Override
	public boolean onBibleLoaded(Bible bible, LoadState loadState) {
		return false;
	}

	@Override
	public boolean onVerseLoaded(final AbstractVerse abstractVerse, final LoadState loadState) {
		ref.post(new Runnable() {
			@Override
			public void run() {
				if(loadState == LoadState.Failed) {
					ref.setText(abstractVerse.getReference().toString());

					if(ver.getText().equals("Failed to download verse")){
						setWorking(false);
					}
					else {
						ver.setText("Failed to download verse");
						votd.workerThread.post(votd.getRandomVerseRunnable);
					}
				}
				else {
					ref.setText(abstractVerse.getReference().toString());
					ver.setText(abstractVerse.getText());
					setWorking(false);
				}
			}
		});

		return true;
	}
}
