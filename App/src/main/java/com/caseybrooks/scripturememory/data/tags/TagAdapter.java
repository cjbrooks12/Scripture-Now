package com.caseybrooks.scripturememory.data.tags;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.common.Util;
import com.caseybrooks.scripturememory.data.VerseDB;

import java.util.ArrayList;

//TODO: replace with Recyclerview adapter
public class TagAdapter extends BaseAdapter implements ListAdapter {
	Context context;
	ArrayList<Tag> tags;

	public class ViewHolder {
		public Tag tag;
		int position;
		TextView nameText;
		ImageView tagCircle;
		ImageView tagIcon;

		ViewHolder(View inflater) {
			nameText = (TextView) inflater.findViewById(R.id.chip_tag_name);
			tagCircle = (ImageView) inflater.findViewById(R.id.chip_tag_circle);
			tagIcon = (ImageView) inflater.findViewById(R.id.chip_tag_icon);
		}
	}

	public TagAdapter(Context context, ArrayList<Tag> tags) {
		this.context = context;
		this.tags = tags;
	}

	@Override
	public int getCount() {
		return tags.size();
	}

	@Override
	public Tag getItem(int position) {
		return tags.get(position);
	}

	@Override
	public long getItemId(int position) {
		return tags.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		View view = convertView;
		if(view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.list_tag_chip, parent, false);
			vh = new ViewHolder(view);
			view.setTag(vh);
		}
		else {
			vh = (ViewHolder) view.getTag();
		}

		//setup bookkeeping information
		Tag tag = getItem(position);
		vh.tag = tag;
		vh.nameText.setText(tag.name);
		vh.position = position;
		Drawable circle = Util.Drawables.circle(tag.color);
		vh.tagCircle.setBackgroundDrawable(circle);

		if(position == tags.size() - 1) {
			vh.tagIcon.setImageResource(R.drawable.ic_action_add_dark);
		}

		return view;
	}

	public void addTag(Tag tag) {
		if(!tags.contains(tag)) {
			tags.add(0, tag);
			notifyDataSetChanged();
		}
	}

	public void removeTag(Tag tagId) {
		if(tags.contains(tagId)) {
			tags.remove(tagId);
			notifyDataSetChanged();
		}
	}

	@Override
	public void notifyDataSetChanged() {
		VerseDB db = new VerseDB(context).open();

		for(Tag tag : tags) {
			if(tag.id != -10) {
				Tag updatedTag = db.getTag(tag.id);
				tag.name = updatedTag.name;
				tag.color = updatedTag.color;
			}
		}
		db.close();

		super.notifyDataSetChanged();
	}
}
