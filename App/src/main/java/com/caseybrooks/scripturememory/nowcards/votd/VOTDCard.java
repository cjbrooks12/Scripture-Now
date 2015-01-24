package com.caseybrooks.scripturememory.nowcards.votd;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;

public class VOTDCard extends FrameLayout {
    private TextView ref, ver;
    private ProgressBar progress;
    ImageButton overflowButton;
    Context context;

    public VOTDCard(Context context) {
        super(context);
        this.context = context;

        initialize();
    }

    private void initialize() {
        LayoutInflater.from(context).inflate(R.layout.card_votd, this);

        overflowButton = (ImageButton) findViewById(R.id.overflowButton);
        ref = (TextView) findViewById(R.id.votdReference);
        ver = (TextView) findViewById(R.id.votdVerse);
        progress = (ProgressBar) findViewById(R.id.progress);

        overflowButton.setOnClickListener(overflowClick);
        this.setOnClickListener(cardClick);

        setWorking(true);

        update();
    }

    public void removeFromParent() {
        setVisibility(View.GONE);
        ((ViewGroup)getParent()).removeView(VOTDCard.this);
    }

    private OnClickListener overflowClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            final VOTD votd = new VOTD(context);

            if(votd.currentVerse != null) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.overflow_votd_card_remove:
                                removeFromParent();
                                return true;
                            case R.id.overflow_votd_card_redownload:
                                setWorking(true);
                                votd.downloadCurrentVerseAsync();
                                return true;
                            case R.id.overflow_votd_card_save:
                                votd.saveVerse();
                                Toast.makeText(context, votd.currentVerse.getReference().toString() + " has been saved", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.overflow_votd_card_post:
                                votd.setAsNotification();
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

                if(votd.currentVerse.getMetadata().getInt(DefaultMetaData.STATE) == VerseDB.VOTD) {
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
            final VOTD votd = new VOTD(context);

            if(votd.currentVerse != null && votd.currentVerse.getMetadata().getInt(DefaultMetaData.STATE) == VerseDB.VOTD) {
                final View view = LayoutInflater.from(context).inflate(R.layout.popup_add_verse, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(view);

                final AlertDialog dialog = builder.create();
                view.findViewById(R.id.scroll_area).setVisibility(View.GONE);

                TextView verseList = (TextView) view.findViewById(R.id.description);
                verseList.setText("Add " + votd.currentVerse.getReference().toString() + " to your list?");

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
                        Toast.makeText(context, votd.currentVerse.getReference().toString() + " has been saved", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
            else {
                votd.downloadCurrentVerseAsync();
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
        VOTD votd = new VOTD(context);

        if(votd.currentVerse != null) {
            setWorking(false);
            ref.setText(votd.currentVerse.getReference().toString());
            ver.setText(votd.currentVerse.getText());
        }
        else {
            if(Util.isConnected(context)) {
                setWorking(true);
                votd.downloadCurrentVerseAsync();
            }
            else {
                setWorking(false);
                ref.setText("Problem Retrieving Verse");
                ver.setText("Please check your internet connection and click to try again");
            }
        }
    }
}
