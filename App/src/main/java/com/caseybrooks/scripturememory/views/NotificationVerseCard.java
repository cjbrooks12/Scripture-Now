package com.caseybrooks.scripturememory.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
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

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.data.Formatter;
import com.caseybrooks.androidbibletools.defaults.DefaultFormatter;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;
import com.caseybrooks.scripturememory.notifications.MainNotification;

public class NotificationVerseCard extends FrameLayout {
    //Data Members
//------------------------------------------------------------------------------
    Context context;

    TextView ref, ver;
    ImageView contextMenu;
    RelativeLayout expandedSection;
    View expandedhline;
    View root;

    RadioButton normal, dashes, letters, letteredDashes, randomWords;
    SeekBar randomnessLevelSlider;
    NavigationCallbacks mCallbacks;

    Passage passage;
    View activeListSidebar;
    TextView activeListText;
    boolean notificationActive, isExpanded;
    int verseDisplayMode;

//Constructors and Initialization
//------------------------------------------------------------------------------    
    public NotificationVerseCard(Context context) {
        super(context);
        this.context = context;

        initialize();
    }

    public NotificationVerseCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        initialize();
    }

    void initialize() {
        root = LayoutInflater.from(context).inflate(R.layout.card_notification_verse, this);

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

        refresh();
    }

    public void refresh() {
        VerseDB db = new VerseDB(context).open();
        passage = db.getVerse(MetaSettings.getVerseId(context));

        if(passage != null) {
            verseDisplayMode = MetaSettings.getVerseDisplayMode(context);
            Formatter formatter;

            switch (verseDisplayMode) {
                case 0:
                    ((RadioButton) findViewById(R.id.radioNormal)).setChecked(true);
                    formatter = new DefaultFormatter.Normal();
                    break;
                case 1:
                    ((RadioButton) findViewById(R.id.radioDashes)).setChecked(true);
                    formatter = new DefaultFormatter.Dashes();
                    break;
                case 2:
                    ((RadioButton) findViewById(R.id.radioLetters)).setChecked(true);
                    formatter = new DefaultFormatter.FirstLetters();
                    break;
                case 3:
                    ((RadioButton) findViewById(R.id.radioLetteredDashes)).setChecked(true);
                    formatter = new DefaultFormatter.DashedLetter();
                    break;
                case 4:
                    ((RadioButton) findViewById(R.id.radioRandomWords)).setChecked(true);
                    formatter = new DefaultFormatter.RandomWords(MetaSettings.getRandomnessLevel(context));
                    randomnessLevelSlider.setVisibility(View.VISIBLE);
                    randomnessLevelSlider.setProgress(((int) (MetaSettings.getRandomnessLevel(context) * 1000)));
                    break;
                default:
                    ((RadioButton) findViewById(R.id.radioNormal)).setChecked(true);
                    formatter = new DefaultFormatter.Normal();
                    break;
            }

            if (isExpanded) {
                passage.setFormatter(formatter);
            }
            else {
                passage.setFormatter(new DefaultFormatter.Normal());
            }
            ref.setText(passage.getReference().toString());
            ver.setText(passage.getText());

            Pair<Integer, Integer> activeList = MetaSettings.getActiveList(context);
            if(activeList.first == VerseListFragment.STATE) {
                activeListSidebar.setVisibility(View.VISIBLE);
                activeListSidebar.setBackgroundColor(db.getStateColor(activeList.second));

                activeListText.setVisibility(View.VISIBLE);
                activeListText.setText("In state list: " + db.getStateName(activeList.second));
            }
            else if(activeList.first == VerseListFragment.TAGS) {
                activeListSidebar.setVisibility(View.VISIBLE);
                activeListSidebar.setBackgroundColor(db.getTagColor(activeList.second));

                activeListText.setVisibility(View.VISIBLE);
                activeListText.setText("In tag list: " + db.getTagName(activeList.second));
            }
            else {
                activeListSidebar.setVisibility(View.GONE);
                activeListText.setVisibility(View.GONE);
            }
        }
        else {
            activeListSidebar.setVisibility(View.GONE);
            activeListText.setVisibility(View.GONE);
            ref.setText("All verses memorized!");
            ver.setText("Why don't you try adding some more, or start memorizing a different list?");
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
        expandedhline.setVisibility(View.VISIBLE);
        expandedSection.setVisibility(View.VISIBLE);
        isExpanded = true;
        refresh();
    }

    public void shrinkCard() {
        expandedhline.setVisibility(View.GONE);
        expandedSection.setVisibility(View.GONE);
        isExpanded = false;
        refresh();
    }

    //Click Listeners
//------------------------------------------------------------------------------      	
    private OnClickListener contextMenuClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(passage != null) {
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

    private OnClickListener expandCardClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!isExpanded() && passage != null) {
                expandCard();
            }
            else {
                shrinkCard();
            }
        }
    };

    private OnMenuItemClickListener menuItemClick = new OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (passage != null) {
                switch (item.getItemId()) {
                    case R.id.context_notification_card_edit:
                        try {
                            mCallbacks = (NavigationCallbacks) context;
                            mCallbacks.toVerseEdit(passage.getMetadata().getInt(DefaultMetaData.ID));
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                        return true;
                    case R.id.context_notification_card_toggle:
                        notificationActive = MetaSettings.getNotificationActive(context);
                        if (notificationActive) {
                            MainNotification.getInstance().dismiss();
                            notificationActive = false;

                            MetaSettings.putNotificationActive(context, false);
                        } else {
                            MainNotification.notify(context).show();
                            notificationActive = true;

                            MetaSettings.putNotificationActive(context, true);
                        }
                        return true;
                    case R.id.context_notification_card_scramble_random:
                        float level = MetaSettings.getRandomnessLevel(context);
                        int seedOffset = (int) System.currentTimeMillis();
                        MetaSettings.putRandomSeedOffset(context, seedOffset);
                        passage.setFormatter(new DefaultFormatter.RandomWords(level, seedOffset));
                        ver.setText(passage.getText());
                        MainNotification.notify(context).show();
                        MetaSettings.putNotificationActive(context, true);
                        return true;
                    case R.id.context_notification_card_reset_random:
                        level = MetaSettings.getRandomnessLevel(context);
                        MetaSettings.putRandomSeedOffset(context, 0);
                        passage.setFormatter(new DefaultFormatter.RandomWords(level, 0));
                        ver.setText(passage.getText());
                        MainNotification.notify(context).show();
                        MetaSettings.putNotificationActive(context, true);
                        return true;
                    default:
                        return false;
                }
            }
            return false;
        }
    };

    private OnClickListener radioButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.radioNormal:
                    verseDisplayMode = 0;
                    randomnessLevelSlider.setVisibility(View.GONE);
                    passage.setFormatter(new DefaultFormatter.Normal());
                    ver.setText(passage.getText());
                    break;
                case R.id.radioDashes:
                    verseDisplayMode = 1;
                    randomnessLevelSlider.setVisibility(View.GONE);
                    passage.setFormatter(new DefaultFormatter.Dashes());
                    ver.setText(passage.getText());
                    break;
                case R.id.radioLetters:
                    verseDisplayMode = 2;
                    randomnessLevelSlider.setVisibility(View.GONE);
                    passage.setFormatter(new DefaultFormatter.FirstLetters());
                    ver.setText(passage.getText());
                    break;
                case R.id.radioLetteredDashes:
                    verseDisplayMode = 3;
                    randomnessLevelSlider.setVisibility(View.GONE);
                    passage.setFormatter(new DefaultFormatter.DashedLetter());
                    ver.setText(passage.getText());
                    break;
                case R.id.radioRandomWords:
                    verseDisplayMode = 4;
                    randomnessLevelSlider.setVisibility(View.VISIBLE);
                    passage.setFormatter(new DefaultFormatter.RandomWords(MetaSettings.getRandomnessLevel(context)));
                    ver.setText(passage.getText());
                    break;
            }
            MetaSettings.putVerseDisplayMode(context, verseDisplayMode);
            MetaSettings.putNotificationActive(context, true);

            MainNotification.notify(context).show();
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        int last;
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(Math.abs(progress - last) >= 10) {
                last = progress;
                MetaSettings.putRandomnessLevel(context, seekBar.getProgress() / 1000f);
                passage.setFormatter(new DefaultFormatter.RandomWords(MetaSettings.getRandomnessLevel(context)));
                ver.setText(passage.getText());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MetaSettings.putRandomnessLevel(context, seekBar.getProgress() / 1000f);
            passage.setFormatter(new DefaultFormatter.RandomWords(MetaSettings.getRandomnessLevel(context)));
            ver.setText(passage.getText());

            if(MetaSettings.getNotificationActive(context)) {
                MainNotification.notify(context).show();
            }
        }
    };
}
