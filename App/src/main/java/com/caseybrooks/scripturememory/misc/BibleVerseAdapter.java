package com.caseybrooks.scripturememory.misc;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.container.Verses;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.VerseDB;

public class BibleVerseAdapter extends BaseAdapter {
    public static class ViewHolder {
        public Passage passage;
        public int position;

        CardView cardview;

        TextView reference;
        TextView verseText;
        TextView version;

        ImageView iconBackground;
        Drawable circle;
        TextView iconText;
        ImageView iconCheck;

        ImageView overflow;
        LinearLayout tagsLayout;

        ViewHolder(View inflater) {
            cardview = (CardView) inflater.findViewById(R.id.verse_list_card_view);

            reference = (TextView) inflater.findViewById(R.id.item_reference);
            verseText = (TextView) inflater.findViewById(R.id.item_verse);
            version = (TextView) inflater.findViewById(R.id.version);

            iconBackground = (ImageView) inflater.findViewById(R.id.ref_icon_background);
            iconText = (TextView) inflater.findViewById(R.id.ref_icon_text);
            iconCheck = (ImageView) inflater.findViewById(R.id.ref_icon_check);

            overflow = (ImageView) inflater.findViewById(R.id.overflow);
            tagsLayout = (LinearLayout) inflater.findViewById(R.id.tags_layout);
        }
    }

    public static interface OnMultiSelectListener {
        void onMultiSelect(View view, int position);
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public static interface OnOverflowClickListener {
        void onOverflowClick(View view, int position);
    }

    Context context;
    ListView lv;
    Verses<Passage> items;
    OnMultiSelectListener multiSelectListener;
    OnItemClickListener itemClickListener;
    OnOverflowClickListener overflowClickListener;

    public BibleVerseAdapter(Context context, Verses<Passage> items, ListView lv) {
        this.context = context;
        this.items = items;
        this.lv = lv;
    }

    public void setOnMultiSelectListener(OnMultiSelectListener listener) {
        this.multiSelectListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnOverflowClickListener(OnOverflowClickListener listener) {
        this.overflowClickListener = listener;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public int getSelectedCount() {
        int count = 0;

        for(Passage passage : items.verses) {
            if(passage.getMetaData().getBoolean(DefaultMetaData.IS_CHECKED)) count++;
        }

        return count;
    }

    public void deselectAll() {
        int firstVisible = lv.getFirstVisiblePosition();
        int lastVisible = lv.getLastVisiblePosition();

        for(int i = 0; i < items.size(); i++) {
            if(items.get(i).getMetaData().getBoolean(DefaultMetaData.IS_CHECKED)) {
                if(i >= firstVisible && i <= lastVisible) {
                    //if this item is visible, animate it as if it were clicked
                    View v = lv.getChildAt(i);
                    iconClick.onClick(v);
                }
                else {
                    //if this item is not visible, simply uncheck it
                    items.get(i).getMetaData().putBoolean(DefaultMetaData.IS_CHECKED, false);
                }
            }
        }
    }

    @Override
    public Passage getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getMetaData().getInt(DefaultMetaData.ID);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        View view = convertView;
        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_card_new, parent, false);
            vh = new ViewHolder(view);
            vh.circle = context.getResources().getDrawable(R.drawable.circle);
            view.setTag(vh);
        }
        else {
            vh = (ViewHolder) view.getTag();
        }

        Passage passage = items.get(position);
        VerseDB db = new VerseDB(context).open();

        //setup bookkeeping information
        vh.position = position;
        vh.passage = passage;
        vh.cardview.setOnLongClickListener(longClick);
        vh.cardview.setOnClickListener(cardClick);
        vh.cardview.setTag(R.id.ref_icon_background, vh);
        vh.cardview.setMaxCardElevation(context.getResources().getDisplayMetrics().density * 4f);


        vh.overflow.setOnClickListener(overflowClick);

        //setup main content of ListItem
        vh.reference.setText(passage.getReference().toString());
        vh.verseText.setText(passage.getText());
        vh.version.setText(passage.getVersion().getCode().toUpperCase());

        //setup icon of ListItem
        String passageBookCode = passage.getVerses()[0].getReference().book.getCode();
        vh.iconText.setText(passageBookCode.replaceFirst("(\\d)", "$1 "));

        if(vh.passage.getMetaData().getBoolean(DefaultMetaData.IS_CHECKED)) {
            TypedArray a = context.getTheme().obtainStyledAttributes(R.style.Theme_BaseLight, new int[]{R.attr.colorAccent});
            int selectedColor = a.getColor(0, 0);
            a.recycle();
            vh.circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
            vh.iconBackground.setImageDrawable(vh.circle);

            vh.iconCheck.setVisibility(View.VISIBLE);
            vh.iconText.setVisibility(View.INVISIBLE);
        }
        else {
            int selectedColor = db.getStateColor(passage.getMetaData().getInt(DefaultMetaData.STATE));
            vh.circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
            vh.iconBackground.setImageDrawable(vh.circle);

            vh.iconCheck.setVisibility(View.INVISIBLE);
            vh.iconText.setVisibility(View.VISIBLE);
        }
        vh.iconBackground.setTag(vh);
        vh.iconBackground.setOnClickListener(iconClick);

        //setup tags of each ListItem
        vh.tagsLayout.removeAllViews();
        final float scale = context.getResources().getDisplayMetrics().density;
        int size = (int) (20 * scale + 0.5f);
        int margin = (int) (2 * scale + 0.5f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        layoutParams.setMargins(0, 0, margin, 0);

        for(String tag : passage.getTags()) {
            int tagColor = db.getTagColor(tag);

            Drawable tag_circle = context.getResources().getDrawable(R.drawable.circle);
            tag_circle.setColorFilter(new PorterDuffColorFilter(tagColor, PorterDuff.Mode.MULTIPLY));

            ImageView tagView = new ImageView(context);
            tagView.setLayoutParams(layoutParams);

            tagView.setImageDrawable(tag_circle);
            vh.tagsLayout.addView(tagView);
        }

        db.close();

        return view;
    }

//Click listeners
//------------------------------------------------------------------------------

    View.OnClickListener cardClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ViewHolder vh = (ViewHolder)v.getTag(R.id.ref_icon_background);
            if(itemClickListener != null) itemClickListener.onItemClick(v, vh.position);
        }
    };

    View.OnClickListener overflowClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ViewHolder vh = (ViewHolder)v.getTag(R.id.ref_icon_background);
            if(overflowClickListener != null) overflowClickListener.onOverflowClick(v, vh.position);
        }
    };

    //enter multi-selection mode by either clicking the icon (primary) or long-pressing card (secondary)
    View.OnLongClickListener longClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            iconClick.onClick(v);

            return true;
        }
    };

    View.OnClickListener iconClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ViewHolder vh = (ViewHolder) v.getTag();

            final Animation a = AnimationUtils.loadAnimation(context, R.anim.flip_to_middle);
            final Animation b = AnimationUtils.loadAnimation(context, R.anim.flip_from_middle);

            if(!vh.passage.getMetaData().getBoolean(DefaultMetaData.IS_CHECKED)) {

                vh.passage.getMetaData().putBoolean(DefaultMetaData.IS_CHECKED, true);
                if(multiSelectListener != null) multiSelectListener.onMultiSelect(vh.iconBackground, vh.position);

                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation arg0) {
                        vh.iconText.setVisibility(View.VISIBLE);
                        vh.iconCheck.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        TypedArray a = context.getTheme().obtainStyledAttributes(R.style.Theme_BaseLight, new int[]{R.attr.colorAccent});
                        int selectedColor = a.getColor(0, 0);
                        a.recycle();

                        vh.circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
                        vh.iconBackground.setImageDrawable(vh.circle);

                        vh.iconText.setVisibility(View.INVISIBLE);
                        vh.iconCheck.setVisibility(View.VISIBLE);

                        vh.iconBackground.startAnimation(b);
                        vh.iconCheck.startAnimation(b);
                    }
                });
                vh.iconBackground.startAnimation(a);
                vh.iconText.startAnimation(a);
                vh.cardview.setCardElevation(context.getResources().getDisplayMetrics().density * 4f);
            }
            else {

                vh.passage.getMetaData().putBoolean(DefaultMetaData.IS_CHECKED, false);
                if(multiSelectListener != null) multiSelectListener.onMultiSelect(vh.iconBackground, vh.position);

                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation arg0) {
                        vh.iconText.setVisibility(View.INVISIBLE);
                        vh.iconCheck.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        VerseDB db = new VerseDB(context).open();
                        int selectedColor = db.getStateColor(vh.passage.getMetaData().getInt(DefaultMetaData.STATE));
                        db.close();

                        vh.circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
                        vh.iconBackground.setImageDrawable(vh.circle);

                        vh.iconText.setVisibility(View.VISIBLE);
                        vh.iconCheck.setVisibility(View.INVISIBLE);

                        vh.iconBackground.startAnimation(b);
                        vh.iconText.startAnimation(b);
                        vh.cardview.setCardElevation(context.getResources().getDisplayMetrics().density * 1f);
                    }
                });
                vh.iconBackground.startAnimation(a);
                vh.iconCheck.startAnimation(a);
            }
        }
    };
}
