package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.misc.FlowLayout;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;
import com.caseybrooks.scripturememory.notifications.MainNotification;
import com.caseybrooks.scripturememory.views.TagChip;

import java.util.Arrays;

public class EditVerseFragment extends Fragment {
//Data Members
//------------------------------------------------------------------------------
	Context context;
	View view;
    Passage passage;
    NavigationCallbacks mCallbacks;
	
	ActionBar ab;
	EditText editRef, editVer;

    FlowLayout tagChipsLayout;
    SeekBar seekbar;

    public static Fragment newInstance(int id) {
        Fragment fragment = new EditVerseFragment();
        Bundle extras = new Bundle();
        extras.putInt("KEY_ID", id);
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

        ActionBar ab = ((MainActivity) context).getSupportActionBar();
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.color_toolbar, typedValue, true);
        int color = typedValue.data;
        ColorDrawable colorDrawable = new ColorDrawable(color);
        ab.setBackgroundDrawable(colorDrawable);
        ab.setTitle("Edit");
    }

    private void initialize() {
		long id = getArguments().getInt("KEY_ID", 1);

        VerseDB db = new VerseDB(context).open();
		passage = db.getVerse(id);

        if(passage != null) {
            editRef = (EditText) view.findViewById(R.id.updateReference);
            editRef.setText(passage.getReference().toString());
            editVer = (EditText) view.findViewById(R.id.updateVerse);
            editVer.setText(passage.getText());

            seekbar = (SeekBar) view.findViewById(R.id.stateSeekBar);
            seekbar.setProgress(passage.getState() - 1);

            int color = db.getStateColor(seekbar.getProgress() + 1);

            Drawable line = seekbar.getProgressDrawable();
            line.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            if(Build.VERSION.SDK_INT >= 16) {
                Drawable thumb = seekbar.getThumb();
                thumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }

            seekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                    VerseDB db = new VerseDB(context).open();
                    int color = db.getStateColor(progressValue + 1);
                    passage.setState(progressValue + 1);
                    db.updateVerse(passage);
                    db.close();

                    Drawable line = seekBar.getProgressDrawable();
                    line.setColorFilter(color, PorterDuff.Mode.SRC_IN);

                    if(Build.VERSION.SDK_INT >= 16) {
                        Drawable thumb = seekBar.getThumb();
                        thumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
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

            String[] tags = passage.getTags();

            for(String tag : tags) {
                final TagChip tagChip = new TagChip(context);
                int tagId = (int)db.getTagID(tag);
                tagChip.setMode(0);
                tagChip.setTag(tagId);
                tagChip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        if(((TagChip)v).getMode() == 2) {
                            Toast.makeText(context, "Add new tag", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            final EditText input = new EditText(context);
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder
                                    .setTitle("Change Tag Name")
                                    .setMessage("Change the name of this tag")
                                    .setView(input)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            String value = input.getText().toString();
                                            ((TagChip) v).changeName(value);
                                        }
                                    })

                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // Canceled.
                                        }
                                    });

                            builder.show();
                        }
                    }
                });

                tagChip.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder
                            .setTitle("Delete Tag")
                            .setMessage("Remove this tag from all verses?")
                            .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    TagChip chip = (TagChip) v;
                                    passage.removeTag(chip.getTagName());
                                    chip.deleteTag();
                                    tagChipsLayout.removeView(v);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            });
                        builder.show();

                        return false;
                    }
                });

                tagChipsLayout.addView(tagChip);
            }

            TagChip tagChip = new TagChip(context);
            tagChip.setMode(2);

            tagChip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final EditText input = new EditText(context);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder
                            .setTitle("New Tag")
                            .setMessage("Enter the name for this new tag")
                            .setView(input)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String value = input.getText().toString().trim();
                                    VerseDB db = new VerseDB(context).open();
                                    passage.addTag(value);
                                    db.updateVerse(passage);

                                    int[] tags = db.getAllTagIds();
                                    Arrays.sort(tags);

                                    TagChip newTagChip = new TagChip(context);
                                    newTagChip.setMode(0);
                                    newTagChip.setTag(tags[tags.length-1]);

                                    tagChipsLayout.addView(newTagChip, tagChipsLayout.getChildCount() - 1);
                                    db.close();
                                }
                            })

                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });

                    builder.show();
                }

            });

            tagChipsLayout.addView(tagChip);
        }

        db.close();


		setHasOptionsMenu(true);
        setupActionBar();
	}
	
//ActionBar
//------------------------------------------------------------------------------
	private void setupActionBar() {
		ab = ((ActionBarActivity) context).getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
	}

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(passage != null) {
            if (MetaSettings.getVerseId(context) == passage.getId()) {
                menu.removeItem(R.id.menu_edit_delete);
                menu.removeItem(R.id.menu_edit_change_list);
            }
            if (passage.getState() == 4) {
                menu.removeItem(R.id.menu_edit_set_notification);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater = ((ActionBarActivity) context).getMenuInflater();
	    inflater.inflate(R.menu.menu_edit_verse, menu);
        if(passage != null) {
            if (passage.getState() != 5) {
                menu.findItem(R.id.menu_edit_change_list).setTitle("Move to Memorized");
            } else {
                menu.findItem(R.id.menu_edit_change_list).setTitle("Move to Current");
            }
        }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        VerseDB db = new VerseDB(context).open();

        switch (item.getItemId()) {
	    case android.R.id.home:
	    	((ActionBarActivity) context).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	    	return true;
	    case R.id.menu_edit_set_notification:
            if(passage != null) {
                MetaSettings.putVerseId(context, (int)passage.getId());
                MainNotification.notify(context).show();
                Toast.makeText(context, "Notification Set", Toast.LENGTH_SHORT).show();
            }
	    	return true;
	    case R.id.menu_edit_save_changes:
            if(passage != null) {
                passage.setState(seekbar.getProgress() + 1);
                passage.setText(editVer.getText().toString());
                db.updateVerse(passage);
                db.close();
                Toast.makeText(context, "Verse Updated", Toast.LENGTH_SHORT).show();
            }
	    	return true;
	    case R.id.menu_edit_delete:
            if(passage != null) {
                passage.setState(6);
                db.updateVerse(passage);
                db.close();
            }
	    	return true;
	    case R.id.menu_edit_change_list:
//            if(passage != null) {
//                if (passage.getState() != 5) {
//                    passage.setState(5);
//                    db.updateVerse(passage);
//                }
//                else {
//                    passage.setState(1+(int)(Math.random()*4));
//                    db.updateVerse(passage);
//                }
//                db.close();
//                Toast.makeText(context, "Verse Updated", Toast.LENGTH_SHORT).show();
//            }
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
	
//Host Activity Interface
//------------------------------------------------------------------------------
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
}
