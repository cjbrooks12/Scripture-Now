package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.common.features.NavigationCallbacks;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.SNApplication;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.data.tags.TagAdapter;
import com.caseybrooks.scripturememory.misc.FlowLayout;

import java.util.ArrayList;

public class EditVerseFragment extends Fragment {
//Data Members
//------------------------------------------------------------------------------
	Context context;
	View view;

	EditText editRef, editVer;
	TextView version;

    TagAdapter tagAdapter;

    FlowLayout tagChipsLayout;
    TextView seekbarText;
    SeekBar seekbar;
	ProgressBar progress;

	NavigationCallbacks mCallbacks;

//	Pair<Integer, Integer> displayedList;

	public static Fragment newInstance() {
        Fragment fragment = new EditVerseFragment();
        Bundle extras = new Bundle();
//        extras.putInt("KEY_LIST_TYPE", listType);
//        extras.putInt("KEY_LIST_ID", listId);
        fragment.setArguments(extras);
        return fragment;
    }

//Lifecycle and Initialization
//------------------------------------------------------------------------------
	public SNApplication getApplication() {
		return (SNApplication) getActivity().getApplication();
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = getActivity();

		view = inflater.inflate(R.layout.fragment_edit_verse, container, false);

		editRef = (EditText) view.findViewById(R.id.updateReference);
		editVer = (EditText) view.findViewById(R.id.updateVerse);
		version = (TextView) view.findViewById(R.id.version);

		seekbarText = (TextView) view.findViewById(R.id.seekbar_text);
		seekbar = (SeekBar) view.findViewById(R.id.stateSeekBar);
		progress = (ProgressBar) view.findViewById(R.id.progress);

		tagChipsLayout = (FlowLayout) view.findViewById(R.id.tagChipLayout);
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
        if(getApplication().getActivePassage() != null) {
            editRef.setText(getApplication().getActivePassage().getReference().toString());
            editVer.setText(getApplication().getActivePassage().getText());
			version.setText(getApplication().getActivePassage().getBible().getAbbr().toUpperCase());

            switch (getApplication().getActivePassage().getMetadata().getInt(DefaultMetaData.STATE)) {
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

            seekbar.setProgress(getApplication().getActivePassage().getMetadata().getInt(DefaultMetaData.STATE) - 1);

			ColorFilter filter = new PorterDuffColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
			progress.getProgressDrawable().setColorFilter(filter);
			progress.getIndeterminateDrawable().setColorFilter(filter);

			VerseDB db = new VerseDB(context).open();

			int color = db.getStateColor(seekbar.getProgress() + 1);
            Drawable line = seekbar.getProgressDrawable();
            line.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            Drawable thumb = getResources().getDrawable(R.drawable.seekbar_thumb);
            thumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            seekbar.setThumb(thumb);

//            seekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
//                @Override
//                public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
//                    VerseDB db = new VerseDB(context).open();
//                    int color = db.getStateColor(progressValue + 1);
//					getApplication().getActivePassage().getMetadata().putInt(DefaultMetaData.STATE, progressValue + 1);
//                    db.updateVerse(getApplication().getActivePassage());
//                    db.close();
//
//                    Drawable line = seekBar.getProgressDrawable();
//                    line.setColorFilter(color, PorterDuff.Mode.SRC_IN);
//
//                    Drawable thumb = getResources().getDrawable(R.drawable.seekbar_thumb);
//                    thumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
//                    seekbar.setThumb(thumb);
//
//                    switch (getApplication().getActivePassage().getMetadata().getInt(DefaultMetaData.STATE)) {
//                        case VerseDB.CURRENT_NONE:
//                            seekbarText.setText("Current - None");
//                            break;
//                        case VerseDB.CURRENT_SOME:
//                            seekbarText.setText("Current - Some");
//                            break;
//                        case VerseDB.CURRENT_MOST:
//                            seekbarText.setText("Current - Most");
//                            break;
//                        case VerseDB.CURRENT_ALL:
//                            seekbarText.setText("Current - All");
//                            break;
//                        case VerseDB.MEMORIZED:
//                            seekbarText.setText("Memorized");
//                            break;
//                    }
//
//                    //if this verse is the current notification verse and the active list is its state, then
//                    //change the active list to be whatever state this verse becomes
//                    if(Main.getVerseId(context) == passage.getMetadata().getInt(DefaultMetaData.ID) &&
//                            displayedList.first == VerseListFragment.STATE) {
//                        Main.putWorkingList(context, VerseListFragment.STATE, passage.getMetadata().getInt(DefaultMetaData.STATE));
//                    }
//                }
//
//                @Override
//                public void onStartTrackingTouch(SeekBar seekBar) {
//
//                }
//
//                @Override
//                public void onStopTrackingTouch(SeekBar seekBar) {
//                }
//            });
//
//
            Tag[] tags = getApplication().getActivePassage().getTags();
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
//                        addNewTag();
                    }
                    else {
//                        editTagName(vh.tag);
                    }
                }
            });

            tagChipsLayout.setOnItemLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TagAdapter.ViewHolder vh = ((TagAdapter.ViewHolder) v.getTag());

                    if(vh.tag.id != -10) {
//                        removeTag(vh.tag);
                    }
                    return true;
                }
            });
        }
//
//        db.close();

		setHasOptionsMenu(true);
	}



//Popups for tags
//------------------------------------------------------------------------------
//    private void editTagName(final Tag tag) {
//        final View view = LayoutInflater.from(context).inflate(R.layout.popup_edit_tag, null);
//        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setView(view);
//
//        final AlertDialog dialog = builder.create();
//
//        final EditText edit = (EditText) view.findViewById(R.id.edit_text);
//
//		VerseDB db = new VerseDB(context).open();
//        edit.setText(tag.name);
//
//		final ColorPicker picker = (ColorPicker) view.findViewById(R.id.color_picker);
//		SaturationBar saturationBar = (SaturationBar) view.findViewById(R.id.saturation_bar);
//		ValueBar valueBar = (ValueBar) view.findViewById(R.id.value_bar);
//		picker.addSaturationBar(saturationBar);
//		picker.addValueBar(valueBar);
//		picker.setOldCenterColor(tag.color);
//		picker.setColor(tag.color);
//
//		db.close();
//
//		TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        TextView saveEditButton = (TextView) view.findViewById(R.id.save_edit_button);
//        saveEditButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                String text = edit.getText().toString().trim();
//                if(text.length() > 0) {
//                    VerseDB db = new VerseDB(context).open();
//
//					int pickerColor = picker.getColor();
//					String colorString = String.format("#%02X%02X%02X", Color.red(pickerColor), Color.green(pickerColor), Color.blue(pickerColor));
//                    db.updateTag(tag.id, text, colorString);
//                    db.close();
//                    tagAdapter.notifyDataSetChanged();
//                    dialog.dismiss();
//                }
//            }
//        });
//        dialog.show();
//    }

//    private void removeTag(final Tag tag) {
//        final View view = LayoutInflater.from(context).inflate(R.layout.popup_remove_tag, null);
//        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setView(view);
//
//        final AlertDialog dialog = builder.create();
//
//        TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        TextView thisVerseButton = (TextView) view.findViewById(R.id.this_verse_button);
//        thisVerseButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                VerseDB db = new VerseDB(context).open();
//				getApplication().getActivePassage().removeTag(tag);
//                db.updateVerse(getApplication().getActivePassage());
//                db.close();
//                tagAdapter.removeTag(tag);
//                tagAdapter.notifyDataSetChanged();
//                dialog.dismiss();
//            }
//        });
//        TextView allVersesButton = (TextView) view.findViewById(R.id.all_verses_button);
//        allVersesButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                VerseDB db = new VerseDB(context).open();
//				getApplication().getActivePassage().removeTag(tag);
//                db.updateVerse(getApplication().getActivePassage());
//                db.deleteTag(tag);
//                db.close();
//                tagAdapter.removeTag(tag);
//                tagAdapter.notifyDataSetChanged();
//                dialog.dismiss();
//            }
//        });
//
//        dialog.show();
//    }

//    private void addNewTag() {
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
//
//		final View view = LayoutInflater.from(context).inflate(R.layout.popup_new_tag, null);
//		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//		builder.setView(view);
//
//		final AlertDialog dialog = builder.create();
//
//		final AutoCompleteTextView edit = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
//		VerseDB db = new VerseDB(context).open();
//
//		ArrayList<Tag> tags = db.getAllTags();
//		if(tags.size() > 0) {
//			ArrayList<String> tagSuggestions = new ArrayList<>();
//			for(Tag tag : tags) tagSuggestions.add(tag.name);
//
//			ArrayAdapter<String> adapter = new ArrayAdapter<>(
//					context,
//					android.R.layout.simple_list_item_1,
//					tagSuggestions
//			);
//			edit.setAdapter(adapter);
//		}
//		db.close();
//
//		TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
//		cancelButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				dialog.dismiss();
//			}
//		});
//
//		TextView addButton = (TextView) view.findViewById(R.id.add_button);
//		addButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				String text = edit.getText().toString().trim();
//				if(text.length() > 0) {
//                    VerseDB db = new VerseDB(context).open();
//					getApplication().getActivePassage().addTag(new Tag(text));
//                    db.updateVerse(getApplication().getActivePassage());
//					Tag tag = db.getTag(text);
//                    tagAdapter.addTag(new Tag(text, tag.id, tag.color, tag.count));
//                    tagAdapter.notifyDataSetChanged();
//                    dialog.dismiss();
//                }
//			}
//		});
//		dialog.show();
//    }

//ActionBar
//------------------------------------------------------------------------------
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater = ((ActionBarActivity) context).getMenuInflater();
//	    inflater.inflate(R.menu.menu_edit_verse, menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//        VerseDB db = new VerseDB(context).open();
//
//        switch (item.getItemId()) {
//	    case R.id.menu_edit_set_notification:
//            if(getApplication().getActivePassage() != null) {
//				getApplication().setCurrentPassage(getApplication().getActivePassage());
//                Main.setActive(context, true);
//                Main.putWorkingList(context, displayedList.first, displayedList.second);
//				MainNotification.getInstance(context).create().show();
//                Toast.makeText(context, getApplication().getCurrentPassage().getReference().toString() + " set as notification", Toast.LENGTH_SHORT).show();
//            }
//	    	return true;
//		case R.id.menu_edit_redownload:
//			if(passage != null) {
//				new AsyncTask<Void, Void, Boolean>() {
//					ABSPassage absPassage;
//
//					@Override
//					protected void onPreExecute() {
//						super.onPreExecute();
//						progress.setVisibility(View.VISIBLE);
//						progress.setIndeterminate(true);
//						progress.setProgress(0);
//						absPassage = new ABSPassage(
//							getResources().getString(R.string.bibles_org),
//								passage.getReference()
//						);
//					}
//
//					@Override
//					protected void onPostExecute(Boolean aVoid) {
//						super.onPostExecute(aVoid);
//						progress.setVisibility(View.GONE);
//
//						if(aVoid) {
//							editVer.setText(absPassage.getText());
//							version.setText(absPassage.getBible().getAbbr().toUpperCase());
//						}
//						else {
//							Toast.makeText(context, "Could not redownload verse at this time", Toast.LENGTH_SHORT).show();
//						}
//					}
//
//					@Override
//					protected Boolean doInBackground(Void... params) {
//						if(Util.isConnected(context)) {
//							try {
//								absPassage.setBible(MetaSettings.getBibleVersion(context));
//
//								absPassage.parseDocument(absPassage.getDocument());
//								return true;
//							}
//							catch(IOException ioe) {
//								ioe.printStackTrace();
//								absPassage = null;
//								return false;
//							}
//						}
//						else return false;
//					}
//
//				}.execute();
//			}
//			return true;
//	    case R.id.menu_edit_save_changes:
//            if(passage != null) {
//                passage.getMetadata().putInt(DefaultMetaData.STATE, seekbar.getProgress() + 1);
//                passage.setText(editVer.getText().toString());
//                db.updateVerse(passage);
//                db.close();
//                Toast.makeText(context, passage.getReference().toString() + " Updated", Toast.LENGTH_SHORT).show();
//                ((ActionBarActivity) context).finish();
//            }
//	    	return true;
//	    case R.id.menu_edit_delete:
//            if(passage != null) {
//                final View view = LayoutInflater.from(context).inflate(R.layout.popup_delete_verse, null);
//                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setView(view);
//
//                final AlertDialog dialog = builder.create();
//
//                view.findViewById(R.id.verse_list).setVisibility(View.GONE);
//
//                TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
//                cancelButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//                TextView deleteButton = (TextView) view.findViewById(R.id.delete_button);
//                deleteButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        VerseDB db = new VerseDB(context).open();
//                        db.deleteVerse(passage);
//                        db.close();
//                        dialog.dismiss();
//
//                        Toast.makeText(context, passage.getReference().toString() + " deleted", Toast.LENGTH_SHORT).show();
//                        ((ActionBarActivity) context).finish();
//                    }
//                });
//
//                dialog.show();
//            }
//	    	return true;
//	    case R.id.menu_edit_share:
//            if(passage != null) {
//                String shareMessage = passage.getReference() + " - " + passage.getText();
//                Intent intent = new Intent();
//                intent.setType("text/plain");
//                intent.setAction(Intent.ACTION_SEND);
//                intent.putExtra(Intent.EXTRA_SUBJECT, passage.getReference().toString());
//                intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
//                startActivity(Intent.createChooser(intent, "Share To..."));
//            }
//	    	return true;
//        default:
//            db.close();
//            return super.onOptionsItemSelected(item);
//	    }
//	}
}
