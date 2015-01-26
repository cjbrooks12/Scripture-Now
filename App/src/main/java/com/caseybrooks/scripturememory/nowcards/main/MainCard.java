package com.caseybrooks.scripturememory.nowcards.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.util.Pair;
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

import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;

public class MainCard extends FrameLayout {
//Data Members
//------------------------------------------------------------------------------
    Context context;
    NavigationCallbacks mCallbacks;

    TextView ref, ver;
    ImageView contextMenu;
    RelativeLayout expandedSection;
    View expandedhline;

    RadioButton normal, dashes, letters, letteredDashes, randomWords;
    SeekBar randomnessLevelSlider;

    View activeListSidebar;
    TextView activeListText;
    boolean isExpanded;

    //Constructors and Initialization
//------------------------------------------------------------------------------
    public MainCard(Context context) {
        super(context);
        this.context = context;

        initialize();
    }

    public MainCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        initialize();
    }

    void initialize() {
        LayoutInflater.from(context).inflate(R.layout.card_notification_verse, this);

        ref = (TextView) findViewById(R.id.notificationReference);
        ver = (TextView) findViewById(R.id.notificationVerse);
        contextMenu = (ImageView) findViewById(R.id.overflowButton);
        contextMenu.setOnClickListener(contextMenuClick);
        this.setOnClickListener(expandCardClick);

        activeListSidebar = findViewById(R.id.active_list_sidebar);
        activeListText = (TextView) findViewById(R.id.active_list_text);

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

        TypedArray a = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.colorAccent});
        int accentColor = a.getColor(0, 0);
        a.recycle();

        Drawable line = randomnessLevelSlider.getProgressDrawable();
        line.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);

        Drawable thumb = getResources().getDrawable(R.drawable.seekbar_thumb);
        thumb.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN);
        randomnessLevelSlider.setThumb(thumb);

        expandedSection = (RelativeLayout) findViewById(R.id.expandedLayout);
        expandedhline = findViewById(R.id.expandedhline);
        expandedSection.setVisibility(View.GONE);
        expandedhline.setVisibility(View.GONE);
        isExpanded = false;

        update();
    }

    public void update() {
        MainVerse mv = new MainVerse(context);

        if(mv.passage != null) {
            //set the radio buttons and text based on user selection and whether the card is expanded
            switch (MetaSettings.getVerseDisplayMode(context)) {
                case 0: ((RadioButton) findViewById(R.id.radioNormal)).setChecked(true); break;
                case 1: ((RadioButton) findViewById(R.id.radioDashes)).setChecked(true); break;
                case 2: ((RadioButton) findViewById(R.id.radioLetters)).setChecked(true); break;
                case 3: ((RadioButton) findViewById(R.id.radioLetteredDashes)).setChecked(true); break;
                case 4:
                    ((RadioButton) findViewById(R.id.radioRandomWords)).setChecked(true);
                    randomnessLevelSlider.setVisibility(View.VISIBLE);
                    randomnessLevelSlider.setProgress(((int) (MetaSettings.getRandomnessLevel(context) * 1000)));
                    break;
                default: ((RadioButton) findViewById(R.id.radioNormal)).setChecked(true); break;
            }

            if (isExpanded) {
                mv.setPassageFormatted();
            }
            else {
                mv.setPassageNormal();
            }
            ref.setText(mv.passage.getReference().toString());
            ver.setText(mv.passage.getText());

            //set the active list
            VerseDB db = new VerseDB(context);
            Pair<Integer, Integer> activeList = MetaSettings.getActiveList(context);
            if(activeList.first == VerseListFragment.STATE) {
                db.open();
                activeListSidebar.setVisibility(View.VISIBLE);
                activeListSidebar.setBackgroundColor(db.getStateColor(activeList.second));

                activeListText.setVisibility(View.VISIBLE);
                activeListText.setText("In state list: " + db.getStateName(activeList.second));
                db.close();
            }
            else if(activeList.first == VerseListFragment.TAGS) {
                db.open();
                activeListSidebar.setVisibility(View.VISIBLE);
                activeListSidebar.setBackgroundColor(db.getTagColor(activeList.second));

                activeListText.setVisibility(View.VISIBLE);
                activeListText.setText("In tag list: " + db.getTagName(activeList.second));
                db.close();
            }
            else {
                activeListSidebar.setVisibility(View.GONE);
                activeListText.setVisibility(View.GONE);
            }
        }
        else {
            activeListSidebar.setVisibility(View.GONE);
            activeListText.setVisibility(View.GONE);
            ref.setText("No Verse Set!");
            ver.setText("Why don't you try adding some more verses, or start memorizing a different list?");
        }
    }

    public void removeFromParent() {
        ((ViewGroup)getParent()).removeView(MainCard.this);
    }

    //Public Getters and Setters
//------------------------------------------------------------------------------
    public boolean isExpanded() {
        return isExpanded;
    }

    //Expand card to show display options for notification
//------------------------------------------------------------------------------
    public void expandCard() {
        expandedhline.setVisibility(View.VISIBLE);
        expandedSection.setVisibility(View.VISIBLE);
        isExpanded = true;
        update();
    }

    public void shrinkCard() {
        expandedhline.setVisibility(View.GONE);
        expandedSection.setVisibility(View.GONE);
        isExpanded = false;
        update();
    }

    //Click Listeners
//------------------------------------------------------------------------------
    private View.OnClickListener contextMenuClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MainVerse mv = new MainVerse(context);
            if(mv.passage != null) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.setOnMenuItemClickListener(menuItemClick);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.context_notification_card, popup.getMenu());

                if(MetaSettings.getVerseDisplayMode(context) == 4) {
                    popup.getMenu().findItem(R.id.context_notification_card_scramble_random).setVisible(true);
                    popup.getMenu().findItem(R.id.context_notification_card_reset_random).setVisible(true);
                }
                else {
                    popup.getMenu().findItem(R.id.context_notification_card_scramble_random).setVisible(false);
                    popup.getMenu().findItem(R.id.context_notification_card_reset_random).setVisible(false);
                }

                popup.show();
            }
        }
    };

    private View.OnClickListener expandCardClick = new View.OnClickListener() {
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

    private PopupMenu.OnMenuItemClickListener menuItemClick = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            MainVerse mv = new MainVerse(context);

            if (mv.passage != null) {
                switch (item.getItemId()) {
                    case R.id.context_notification_card_edit:
                        try {
                            mCallbacks = (NavigationCallbacks) context;
                            mCallbacks.toVerseEdit(mv.passage.getMetadata().getInt(DefaultMetaData.ID));
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                        return true;
                    case R.id.context_notification_card_toggle:
                        if (MetaSettings.getNotificationActive(context)) {
                            MainNotification.getInstance(context).dismiss();
                            MetaSettings.putNotificationActive(context, false);
                        }
                        else {
							MainNotification.getInstance(context).create().show();
                            MetaSettings.putNotificationActive(context, true);
                        }
                        return true;
                    case R.id.context_notification_card_scramble_random:
                        MetaSettings.putRandomSeedOffset(context, (int) System.currentTimeMillis());

                        update();
						MainNotification.getInstance(context).create().show();
                        MetaSettings.putNotificationActive(context, true);
                        return true;
                    case R.id.context_notification_card_reset_random:
                        MetaSettings.putRandomSeedOffset(context, 0);

                        update();
						MainNotification.getInstance(context).create().show();
                        MetaSettings.putNotificationActive(context, true);
                        return true;
                    default:
                        return false;
                }
            }
            return false;
        }
    };

    private View.OnClickListener radioButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int verseDisplayMode = 0;

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
			MetaSettings.putTextIsFull(context, false);

            update();

			MainNotification.getInstance(context).create().show();
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        int last;
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(Math.abs(progress - last) >= 10) {
                last = progress;

                MetaSettings.putRandomnessLevel(context, seekBar.getProgress() / 1000f);
                MainVerse mv = new MainVerse(context);
                mv.setPassageFormatted();
                ver.setText(mv.passage.getText());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MetaSettings.putRandomnessLevel(context, seekBar.getProgress() / 1000f);
            MainVerse mv = new MainVerse(context);
            mv.setPassageFormatted();
            ver.setText(mv.passage.getText());

            if(MetaSettings.getNotificationActive(context)) {
				MainNotification.getInstance(context).create().show();
            }
        }
    };
}
