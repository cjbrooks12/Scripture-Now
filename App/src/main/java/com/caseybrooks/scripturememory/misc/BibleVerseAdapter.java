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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.VerseDB;

import java.util.ArrayList;

public class BibleVerseAdapter extends BaseAdapter {
    Context context;
    ListView lv;
    ArrayList<Passage> items;

    AdapterView.OnItemClickListener cardClick;
    AdapterView.OnItemClickListener overflowClick;
    AdapterView.OnItemClickListener iconClick;

    public BibleVerseAdapter(Context context, ArrayList<Passage> items, ListView lv) {
        this.context = context;
        this.items = items;
        this.lv = lv;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        cardClick = listener;
    }

    public void setOnItemMultiselectListener(AdapterView.OnItemClickListener listener) {
        iconClick = listener;
    }

    public void setOnItemOverflowClickListener(AdapterView.OnItemClickListener listener) {
        overflowClick = listener;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public ArrayList<Passage> getItems() {
        return items;
    }

    public int getSelectedCount() {
        int count = 0;

        for(Passage passage : items) {
            if(passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) count++;
        }

        return count;
    }

    public ArrayList<Passage> getSelectedItems() {
        ArrayList<Passage> selectedItems = new ArrayList<Passage>();

        for(int i = 0; i < items.size(); i++) {
            Passage passage = items.get(i);
            passage.getMetadata().putInt("LIST_POSITION", i);
            if(passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) selectedItems.add(passage);
        }

        return selectedItems;
    }

    @Override
    public Passage getItem(int position) {
        Passage passage = items.get(position);
        passage.getMetadata().putInt("LIST_POSITION", position);
        return passage;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getMetadata().getInt(DefaultMetaData.ID);
    }

    public void removeItem(Passage item) {
        if(items.contains(item)) {
            items.remove(item);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        View view = convertView;
        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_card_new, parent, false);
            vh = new ViewHolder(context, view);
            view.setTag(vh);
        }
        else {
            vh = (ViewHolder) view.getTag();
        }

        vh.initialize(items.get(position));

        vh.iconBackground.setTag(vh);
        vh.cardview.setTag(vh);
        vh.overflow.setTag(vh);

        vh.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder vh = (ViewHolder) v.getTag();
                if(iconClick != null) cardClick.onItemClick(lv, vh.cardview, vh.getPosition(), vh.getId());
            }
        });
        vh.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder vh = (ViewHolder) v.getTag();
                if(iconClick != null) overflowClick.onItemClick(lv, vh.overflow, vh.getPosition(), vh.getId());
            }
        });
        vh.cardview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ViewHolder vh = (ViewHolder) v.getTag();
                vh.multiSelect();
                if(iconClick != null) iconClick.onItemClick(lv, vh.iconBackground, vh.getPosition(), vh.getId());
                return true;
            }
        });
        vh.iconBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder vh = (ViewHolder) v.getTag();
                vh.multiSelect();
                if(iconClick != null) iconClick.onItemClick(lv, vh.iconBackground, vh.getPosition(), vh.getId());

            }
        });

        return view;
    }

//View Holder class
//------------------------------------------------------------------------------
    public static class ViewHolder {
        final Context context;
        final Animation a_selected;
        final Animation a_not_selected;
        final Animation b;
        final Drawable circle;

        public Passage passage;

        CardView cardview;
        TextView reference;
        TextView verseText;
        TextView version;

        ImageView iconBackground;
        TextView iconText;
        ImageView iconCheck;

        ImageView overflow;
        LinearLayout tagsLayout;

        ViewHolder(final Context context, View inflater) {
            this.context = context;
            circle = context.getResources().getDrawable(R.drawable.circle);
            a_selected = AnimationUtils.loadAnimation(context, R.anim.flip_to_middle);
            a_not_selected = AnimationUtils.loadAnimation(context, R.anim.flip_to_middle);
            b = AnimationUtils.loadAnimation(context, R.anim.flip_from_middle);

            a_not_selected.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {
                    iconText.setVisibility(View.VISIBLE);
                    iconCheck.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.colorAccent});
                    int selectedColor = a.getColor(0, 0);
                    a.recycle();

                    circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
                    iconBackground.setImageDrawable(circle);

                    iconText.setVisibility(View.INVISIBLE);
                    iconCheck.setVisibility(View.VISIBLE);

                    iconBackground.startAnimation(b);
                    iconCheck.startAnimation(b);
                }
            });

            a_selected.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {
                    iconText.setVisibility(View.INVISIBLE);
                    iconCheck.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    VerseDB db = new VerseDB(context).open();
                    int selectedColor = db.getStateColor(passage.getMetadata().getInt(DefaultMetaData.STATE));
                    db.close();

                    circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
                    iconBackground.setImageDrawable(circle);

                    iconText.setVisibility(View.VISIBLE);
                    iconCheck.setVisibility(View.INVISIBLE);

                    iconBackground.startAnimation(b);
                    iconText.startAnimation(b);
                }
            });

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

        private int getPosition() {
            return passage.getMetadata().getInt("LIST_POSITION");
        }

        private int getId() {
            return passage.getMetadata().getInt(DefaultMetaData.ID);
        }

        private void initialize(Passage passage) {
            this.passage = passage;

            reference.setText(passage.getReference().toString());
            verseText.setText(passage.getText());
            version.setText(passage.getVersion().getCode().toUpperCase());

            String passageBookCode = passage.getVerses()[0].getReference().book.getCode();
            iconText.setText(passageBookCode.replaceFirst("(\\d)", "$1 "));

            VerseDB db = new VerseDB(context).open();
            if(passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) {
                TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.colorAccent});
                int selectedColor = a.getColor(0, 0);
                a.recycle();
                circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
                iconBackground.setImageDrawable(circle);

                iconCheck.setVisibility(View.VISIBLE);
                iconText.setVisibility(View.INVISIBLE);
            }
            else {
                int selectedColor = db.getStateColor(passage.getMetadata().getInt(DefaultMetaData.STATE));
                circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
                iconBackground.setImageDrawable(circle);

                iconCheck.setVisibility(View.INVISIBLE);
                iconText.setVisibility(View.VISIBLE);
            }

            tagsLayout.removeAllViews();
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
                tagsLayout.addView(tagView);
            }

            db.close();
        }

        public void multiSelect() {
            if(!passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) {

                passage.getMetadata().putBoolean(DefaultMetaData.IS_CHECKED, true);

                iconBackground.startAnimation(a_not_selected);
                iconText.startAnimation(a_not_selected);
                cardview.setCardElevation(context.getResources().getDisplayMetrics().density * 4f);
            }
            else {
                passage.getMetadata().putBoolean(DefaultMetaData.IS_CHECKED, false);

                iconBackground.startAnimation(a_selected);
                iconCheck.startAnimation(a_selected);
                cardview.setCardElevation(context.getResources().getDisplayMetrics().density * 2f);
            }
        }
    }
}
