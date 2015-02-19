package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.androidbibletools.enumeration.Version;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.misc.FlowLayout;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;
import com.caseybrooks.scripturememory.nowcards.main.Main;
import com.caseybrooks.scripturememory.nowcards.main.MainNotification;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.io.IOException;
import java.util.ArrayList;

public class EditVerseFragment extends Fragment {
//Data Members
//------------------------------------------------------------------------------
	Context context;
	View view;
    Passage passage;

	EditText editRef, editVer;
	TextView version;

    TagAdapter tagAdapter;

    FlowLayout tagChipsLayout;
    TextView seekbarText;
    SeekBar seekbar;
	ProgressBar progress;

	NavigationCallbacks mCallbacks;

	Pair<Integer, Integer> displayedList;

	public static Fragment newInstance(int verseId, int listType, int listId) {
        Fragment fragment = new EditVerseFragment();
        Bundle extras = new Bundle();
        extras.putInt("KEY_ID", verseId);
        extras.putInt("KEY_LIST_TYPE", listType);
        extras.putInt("KEY_LIST_ID", listId);
        fragment.setArguments(extras);
        return fragment;
    }

//Lifecycle and Initialization
//------------------------------------------------------------------------------
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_edit_verse, container, false);
        context = getActivity();
        initialize();

        return view;
	}

    @Override
    public void onResume() {
        super.onResume();

		TypedValue typedValue = new TypedValue();
		Resources.Theme theme = context.getTheme();
		theme.resolveAttribute(R.attr.color_toolbar, typedValue, true);

		mCallbacks.setToolBar("Edit Verse", typedValue.data);
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallbacks = (NavigationCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationCallbacks.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

    private void initialize() {
		long id = getArguments().getInt("KEY_ID", 1);
        displayedList = Main.getWorkingList(context);

        VerseDB db = new VerseDB(context).open();
		passage = db.getVerse(id);

        if(passage != null) {
            editRef = (EditText) view.findViewById(R.id.updateReference);
            editRef.setText(passage.getReference().toString());
            editVer = (EditText) view.findViewById(R.id.updateVerse);
            editVer.setText(passage.getText());

			version = (TextView) view.findViewById(R.id.version);
			version.setText(passage.getVersion().getCode().toUpperCase());

            seekbarText = (TextView) view.findViewById(R.id.seekbar_text);
            switch (passage.getMetadata().getInt(DefaultMetaData.STATE)) {
                case VerseDB.CURRENT_NONE:
                    seekbarText.setText("Current - None");
                    break;
                case VerseDB.CURRENT_SOME:
                    seekbarText.setText("Current - Some");
                    break;
                case VerseDB.CURRENT_MOST:
                    seekbarText.setText("Current - Most");
                    break;
                case VerseDB.CURRENT_ALL:
                    seekbarText.setText("Current - All");
                    break;
                case VerseDB.MEMORIZED:
                    seekbarText.setText("Memorized");
                    break;
            }

            seekbar = (SeekBar) view.findViewById(R.id.stateSeekBar);
            seekbar.setProgress(passage.getMetadata().getInt(DefaultMetaData.STATE) - 1);

			ColorFilter filter = new PorterDuffColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
			progress = (ProgressBar) view.findViewById(R.id.progress);
			progress.getProgressDrawable().setColorFilter(filter);
			progress.getIndeterminateDrawable().setColorFilter(filter);

            int color = db.getStateColor(seekbar.getProgress() + 1);

            Drawable line = seekbar.getProgressDrawable();
            line.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            Drawable thumb = getResources().getDrawable(R.drawable.seekbar_thumb);
            thumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            seekbar.setThumb(thumb);

            seekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                    VerseDB db = new VerseDB(context).open();
                    int color = db.getStateColor(progressValue + 1);
                    passage.getMetadata().putInt(DefaultMetaData.STATE, progressValue + 1);
                    db.updateVerse(passage);
                    db.close();

                    Drawable line = seekBar.getProgressDrawable();
                    line.setColorFilter(color, PorterDuff.Mode.SRC_IN);

                    Drawable thumb = getResources().getDrawable(R.drawable.seekbar_thumb);
                    thumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    seekbar.setThumb(thumb);

                    switch (passage.getMetadata().getInt(DefaultMetaData.STATE)) {
                        case VerseDB.CURRENT_NONE:
                            seekbarText.setText("Current - None");
                            break;
                        case VerseDB.CURRENT_SOME:
                            seekbarText.setText("Current - Some");
                            break;
                        case VerseDB.CURRENT_MOST:
                            seekbarText.setText("Current - Most");
                            break;
                        case VerseDB.CURRENT_ALL:
                            seekbarText.setText("Current - All");
                            break;
                        case VerseDB.MEMORIZED:
                            seekbarText.setText("Memorized");
                            break;
                    }

                    //if this verse is the current notification verse and the active list is its state, then
                    //change the active list to be whatever state this verse becomes
                    if(Main.getVerseId(context) == passage.getMetadata().getInt(DefaultMetaData.ID) &&
                            displayedList.first == VerseListFragment.STATE) {
                        Main.putWorkingList(context, VerseListFragment.STATE, passage.getMetadata().getInt(DefaultMetaData.STATE));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            tagChipsLayout = (FlowLayout) view.findViewById(R.id.tagChipLayout);

            Tag[] tags = passage.getTags();
            ArrayList<Tag> tagsList = new ArrayList<>();
            for(Tag tag : tags) {
                tagsList.add(tag);
            }
            tagsList.add(new Tag("Add New Tag", -10, Color.parseColor("#000000"), 0));
            tagAdapter = new TagAdapter(context, tagsList);
            tagChipsLayout.setAdapter(tagAdapter);

            tagChipsLayout.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TagAdapter.ViewHolder vh = (TagAdapter.ViewHolder)v.getTag();
                    if(vh.tag.id == -10) {
                        addNewTag();
                    }
                    else {
                        editTagName(vh.tag);
                    }
                }
            });

            tagChipsLayout.setOnItemLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TagAdapter.ViewHolder vh = ((TagAdapter.ViewHolder) v.getTag());

                    if(vh.tag.id != -10) {
                        removeTag(vh.tag);
                    }
                    return true;
                }
            });
        }

        db.close();

		setHasOptionsMenu(true);
	}

    private class TagAdapter extends BaseAdapter implements ListAdapter {
        Context context;
        ArrayList<Tag> tags;

        public class ViewHolder {
			Tag tag;
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

//Popups for tags
//------------------------------------------------------------------------------
    private void editTagName(final Tag tag) {
        final View view = LayoutInflater.from(context).inflate(R.layout.popup_edit_tag, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        final EditText edit = (EditText) view.findViewById(R.id.edit_text);

		VerseDB db = new VerseDB(context).open();
        edit.setText(tag.name);

		final ColorPicker picker = (ColorPicker) view.findViewById(R.id.color_picker);
		SaturationBar saturationBar = (SaturationBar) view.findViewById(R.id.saturation_bar);
		ValueBar valueBar = (ValueBar) view.findViewById(R.id.value_bar);
		picker.addSaturationBar(saturationBar);
		picker.addValueBar(valueBar);
		picker.setOldCenterColor(tag.color);
		picker.setColor(tag.color);

		db.close();

		TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView saveEditButton = (TextView) view.findViewById(R.id.save_edit_button);
        saveEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = edit.getText().toString().trim();
                if(text.length() > 0) {
                    VerseDB db = new VerseDB(context).open();

					int pickerColor = picker.getColor();
					String colorString = String.format("#%02X%02X%02X", Color.red(pickerColor), Color.green(pickerColor), Color.blue(pickerColor));
                    db.updateTag(tag.id, text, colorString);
                    db.close();
                    tagAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void removeTag(final Tag tag) {
        final View view = LayoutInflater.from(context).inflate(R.layout.popup_remove_tag, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView thisVerseButton = (TextView) view.findViewById(R.id.this_verse_button);
        thisVerseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VerseDB db = new VerseDB(context).open();
                passage.removeTag(tag);
                db.updateVerse(passage);
                db.close();
                tagAdapter.removeTag(tag);
                tagAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        TextView allVersesButton = (TextView) view.findViewById(R.id.all_verses_button);
        allVersesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VerseDB db = new VerseDB(context).open();
                passage.removeTag(tag);
                db.updateVerse(passage);
                db.deleteTag(tag);
                db.close();
                tagAdapter.removeTag(tag);
                tagAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addNewTag() {
//        final View view = LayoutInflater.from(context).inflate(R.layout.popup_new_tag, null);
//        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setView(view);
//
//        final AlertDialog dialog = builder.create();
//
//        final AutoCompleteTextView edit = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
//        VerseDB db = new VerseDB(context).open();
//
//        ArrayList<Tag> tagSuggestions = db.getAllTags();
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                context,
//                android.R.layout.simple_list_item_1,
//                tagSuggestions
//        );
//        edit.setAdapter(adapter);
//
//        TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        TextView addButton = (TextView) view.findViewById(R.id.add_button);
//        addButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String text = edit.getText().toString().trim();
//                if(text.length() > 0) {
//                    VerseDB db = new VerseDB(context).open();
//                    passage.addTag(new Tag(text));
//                    db.updateVerse(passage);
//					int id = (int)db.getTagID(text);
//                    tagAdapter.addTag(new Tag(text, id, db.getTagColor(id), db.getTagCount(id)));
//                    tagAdapter.notifyDataSetChanged();
//                    dialog.dismiss();
//                }
//            }
//        });
//        dialog.show();

		final View view = LayoutInflater.from(context).inflate(R.layout.popup_new_tag, null);
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(view);

		final AlertDialog dialog = builder.create();

		final AutoCompleteTextView edit = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
		VerseDB db = new VerseDB(context).open();

		ArrayList<Tag> tags = db.getAllTags();
		if(tags.size() > 0) {
			ArrayList<String> tagSuggestions = new ArrayList<>();
			for(Tag tag : tags) tagSuggestions.add(tag.name);

			ArrayAdapter<String> adapter = new ArrayAdapter<>(
					context,
					android.R.layout.simple_list_item_1,
					tagSuggestions
			);
			edit.setAdapter(adapter);
		}
		db.close();

		TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		TextView addButton = (TextView) view.findViewById(R.id.add_button);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = edit.getText().toString().trim();
				if(text.length() > 0) {
                    VerseDB db = new VerseDB(context).open();
                    passage.addTag(new Tag(text));
                    db.updateVerse(passage);
					Tag tag = db.getTag(text);
                    tagAdapter.addTag(new Tag(text, tag.id, tag.color, tag.count));
                    tagAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
			}
		});
		dialog.show();
    }

//ActionBar
//------------------------------------------------------------------------------
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater = ((ActionBarActivity) context).getMenuInflater();
	    inflater.inflate(R.menu.menu_edit_verse, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        VerseDB db = new VerseDB(context).open();

        switch (item.getItemId()) {
	    case R.id.menu_edit_set_notification:
            if(passage != null) {
                Main.putVerseId(context, passage.getMetadata().getInt(DefaultMetaData.ID));
                Main.setActive(context, true);
                Main.putWorkingList(context, displayedList.first, displayedList.second);
				MainNotification.getInstance(context).create().show();
                Toast.makeText(context, passage.getReference().toString() + " set as notification", Toast.LENGTH_SHORT).show();
            }
	    	return true;
		case R.id.menu_edit_redownload:
			if(passage != null) {
				new AsyncTask<Void, Void, Boolean>() {

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						progress.setVisibility(View.VISIBLE);
						progress.setIndeterminate(true);
						progress.setProgress(0);
					}

					@Override
					protected void onPostExecute(Boolean aVoid) {
						super.onPostExecute(aVoid);
						progress.setVisibility(View.GONE);

						if(aVoid) {
							editVer.setText(passage.getText());
							version.setText(passage.getVersion().getCode().toUpperCase());
						}
						else {
							Toast.makeText(context, "Could not redownload verse at this time", Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					protected Boolean doInBackground(Void... params) {
						if(Util.isConnected(context)) {
							Version version = passage.getVersion();
							try {
								passage.setVersion(MetaSettings.getBibleVersion(context));
								passage.retrieve();
								return true;
							}
							catch(IOException ioe) {
								ioe.printStackTrace();
								passage.setVersion(version);
								return false;
							}
						}
						else return false;
					}

				}.execute();
			}
			return true;
	    case R.id.menu_edit_save_changes:
            if(passage != null) {
                passage.getMetadata().putInt(DefaultMetaData.STATE, seekbar.getProgress() + 1);
                passage.setText(editVer.getText().toString());
                db.updateVerse(passage);
                db.close();
                Toast.makeText(context, passage.getReference().toString() + " Updated", Toast.LENGTH_SHORT).show();
                ((ActionBarActivity) context).finish();
            }
	    	return true;
	    case R.id.menu_edit_delete:
            if(passage != null) {
                final View view = LayoutInflater.from(context).inflate(R.layout.popup_delete_verse, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(view);

                final AlertDialog dialog = builder.create();

                view.findViewById(R.id.verse_list).setVisibility(View.GONE);

                TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                TextView deleteButton = (TextView) view.findViewById(R.id.delete_button);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        VerseDB db = new VerseDB(context).open();
                        db.deleteVerse(passage);
                        db.close();
                        dialog.dismiss();

                        Toast.makeText(context, passage.getReference().toString() + " deleted", Toast.LENGTH_SHORT).show();
                        ((ActionBarActivity) context).finish();
                    }
                });

                dialog.show();
            }
	    	return true;
	    case R.id.menu_edit_share:
            if(passage != null) {
                String shareMessage = passage.getReference() + " - " + passage.getText();
                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, passage.getReference().toString());
                intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(intent, "Share To..."));
            }
	    	return true;
        default:
            db.close();
            return super.onOptionsItemSelected(item);
	    }
	}
}
