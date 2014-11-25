package com.caseybrooks.scripturememory.data;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.container.Verses;
import com.caseybrooks.scripturememory.R;

public class BibleVerseAdapter extends BaseAdapter {
    private static class ViewHolder {
        Passage passage;
        int position;

        TextView reference;
        TextView verseText;
        TextView version;

        ImageView iconBackground;
        Drawable circle;
        TextView iconText;

        ImageView overflow;
        LinearLayout tagsLayout;
    }

    Context context;
    Verses<Passage> items;

    public BibleVerseAdapter(Context context, Verses<Passage> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Passage getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        View view = convertView;
        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_card_new, parent, false);
            vh = new ViewHolder();

            vh.reference = (TextView) view.findViewById(R.id.item_reference);
            vh.verseText = (TextView) view.findViewById(R.id.item_verse);
            vh.version = (TextView) view.findViewById(R.id.version);

            vh.iconBackground = (ImageView) view.findViewById(R.id.ref_icon_background);
            vh.iconText = (TextView) view.findViewById(R.id.ref_icon_text);
            vh.circle = context.getResources().getDrawable(R.drawable.circle);

            vh.overflow = (ImageView) view.findViewById(R.id.overflow);
            vh.tagsLayout = (LinearLayout) view.findViewById(R.id.tags_layout);
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

        //setup main content of ListItem
        vh.reference.setText(passage.getReference());
        vh.verseText.setText(passage.getText());
        vh.version.setText(passage.getVersion().getCode().toUpperCase());

        //setup icon of ListItem
        String passageBookCode = passage.getVerses()[0].getBook().getCode();
        String first_letter = passageBookCode.substring(0, 1);
        if(first_letter.equals("1") || first_letter.equals("2") || first_letter.equals("3")) {
            vh.iconText.setText(passageBookCode.replaceFirst("(\\d)", "$1 "));
        }
        else {
            vh.iconText.setText(passageBookCode);
        }

        if(vh.passage.isChecked()) {
            TypedArray a = context.getTheme().obtainStyledAttributes(R.style.Theme_BaseLight, new int[]{R.attr.colorAccent});
            int selectedColor = a.getColor(0, 0);
            a.recycle();
//            Log.e("INITIALIZE PASSAGE", passage.getReference() + " checked");
            vh.circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
            vh.iconBackground.setImageDrawable(vh.circle);
        }
        else {
            int selectedColor = db.getStateColor(passage.getState());
//            Log.e("INITIALIZE PASSAGE", passage.getReference() + " not checked");
            vh.circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
            vh.iconBackground.setImageDrawable(vh.circle);

        }
        vh.iconBackground.setTag(R.id.ref_icon_background, vh);
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

    View.OnClickListener iconClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ViewHolder vh = (ViewHolder)v.getTag(R.id.ref_icon_background);
            Log.i("ICON CLICKED POSITION", "" + vh.position);

            final Animation a = AnimationUtils.loadAnimation(context, R.anim.flip_to_middle);
            final Animation b = AnimationUtils.loadAnimation(context, R.anim.flip_from_middle);

            if(!vh.passage.isChecked()) {
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation arg0) {
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

                        vh.iconBackground.startAnimation(b);
                        vh.iconText.startAnimation(b);
                    }
                });
            }
            else {
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation arg0) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        VerseDB db = new VerseDB(context).open();
                        int selectedColor = db.getStateColor(vh.passage.getState());
                        db.close();

                        vh.circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
                        vh.iconBackground.setImageDrawable(vh.circle);

                        vh.iconBackground.startAnimation(b);
                        vh.iconText.startAnimation(b);
                    }
                });
            }

            vh.iconBackground.startAnimation(a);
            vh.iconText.startAnimation(a);

            //toggle checked state of this view
            vh.passage.toggle();
        }
    };
}
