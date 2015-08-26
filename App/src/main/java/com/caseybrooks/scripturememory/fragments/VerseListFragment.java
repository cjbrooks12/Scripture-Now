package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Metadata;
import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.basic.Verse;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.common.features.NavigationCallbacks;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.misc.BibleVerseAdapter;
import com.caseybrooks.scripturememory.nowcards.main.Main;
import com.caseybrooks.scripturememory.nowcards.main.MainSettings;
import com.caseybrooks.scripturememory.nowcards.workingverse.WorkingVerse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class VerseListFragment extends ListFragment {
//enums for creating new fragment
//------------------------------------------------------------------------------
    public static final int TAGS = 0;
    public static final int STATE = 1;

	private PopulateBibleVerses loaderThread;

    public static Fragment newInstance(int type, int id) {
        Fragment fragment = new VerseListFragment();
        Bundle data = new Bundle();
        data.putInt("KEY_LIST_TYPE", type);
        data.putInt("KEY_LIST_ID", id);
        fragment.setArguments(data);
        return fragment;
    }

//Data members
//------------------------------------------------------------------------------
	Context context;
    ActionMode mActionMode;
    NavigationCallbacks mCallbacks;

	BibleVerseAdapter bibleVerseAdapter;
	int listType;
    int listId;

//Lifecycle and Initialization
//------------------------------------------------------------------------------
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        float density = getResources().getDisplayMetrics().density;

        getListView().setDivider(null);
		getListView().setDividerHeight(0);
		getListView().setSelector(new StateListDrawable());
		getListView().setFastScrollEnabled(true);
        getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
        getListView().setPadding((int)(8*density), 0, (int)(8*density), 0);

		context = getActivity();
		Bundle extras = getArguments();
        listType = extras.getInt("KEY_LIST_TYPE");
        listId = extras.getInt("KEY_LIST_ID");
	}

	@Override
	public void onResume() {
		super.onResume();

        setHasOptionsMenu(true);

        String title;
        int color;

        VerseDB db = new VerseDB(context).open();
        if(listType == TAGS) {
			Tag tag = db.getTag(listId);
            if(tag == null) {
                mCallbacks.toDashboard();
                return;
            }
            title = tag.name;
            color = tag.color;
            MetaSettings.putDrawerSelection(context, 4, listId);
        }
        else {
            title = db.getStateName(listId);
            color = db.getStateColor(listId);
            MetaSettings.putDrawerSelection(context, 3, listId);
        }
        db.close();

        mCallbacks.setToolBar(title, color);

		loaderThread = new PopulateBibleVerses();
		loaderThread.execute();
	}

	@Override
	public void onPause() {
        if(mActionMode != null) mActionMode.finish();
		if(loaderThread != null) loaderThread.cancel(true);
        super.onPause();
    }

    AdapterView.OnItemClickListener iconClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mActionMode == null) {
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            }
            if(bibleVerseAdapter.getSelectedCount() == 0) {
                mActionMode.finish();
            }
            else {
                mActionMode.setTitle(bibleVerseAdapter.getSelectedCount() + "");
            }
        }
    };

    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            WorkingVerse.setWorkingVerseId(context, (int) id);

            mCallbacks.toVerseEdit();
        }
    };

    AdapterView.OnItemClickListener overflowClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BibleVerseAdapter.ViewHolder vh = (BibleVerseAdapter.ViewHolder) view.getTag();
            final ArrayList<Passage> listOfOne = new ArrayList<>();
            listOfOne.add(vh.passage);

            PopupMenu popup = new PopupMenu(context, view);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
					case R.id.context_list_post:
						MainSettings.setActive(context, true);
						MainSettings.putWorkingList(context, listType, listId);
						new Main(context).setMainPassage(vh.passage);
						Toast.makeText(context, vh.passage.getReference().toString() + " set as notification", Toast.LENGTH_SHORT).show();
						return true;
					case R.id.context_list_add_tag:
						addTag(listOfOne);
						return true;
					case R.id.context_list_share:
						String shareMessage = vh.passage.getReference() + " - " + vh.passage.getText();
						Intent intent = new Intent();
						intent.setType("text/plain");
						intent.setAction(Intent.ACTION_SEND);
						intent.putExtra(Intent.EXTRA_SUBJECT, vh.passage.getReference().toString());
						intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
						startActivity(Intent.createChooser(intent, "Share To..."));
						return true;
					case R.id.context_list_delete:
						delete(listOfOne);
						return true;
					default:
						return false;
                    }
                }
            });
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.overflow_list, popup.getMenu());

			popup.show();
        }
    };

	private class PopulateBibleVerses extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			VerseDB db = new VerseDB(context).open();
			if(listType == TAGS) {
				Tag tag = db.getTag(listId);
				mCallbacks.setToolBar(tag.name, tag.color);
			}
			else if(listType == STATE) {
				if(listId != 0) {
					mCallbacks.setToolBar(db.getStateName(listId), db.getStateColor(listId));
				}
				else {
					mCallbacks.setToolBar("All", getResources().getColor(R.color.all_verses));
				}
			}
			else {
				mCallbacks.setToolBar("All", getResources().getColor(R.color.all_verses));
			}
			db.close();

			setListAdapter(bibleVerseAdapter);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<Passage> verses;

			VerseDB db = new VerseDB(context).open();
			if(listType == TAGS) {
				verses = db.getTaggedVerses(listId);
			}
			else if(listType == STATE) {
				if(listId != 0) {
					verses = db.getStateVerses(listId);
				}
				else {
					verses = db.getAllCurrentVerses();
				}
			}
			else {
				verses = db.getAllCurrentVerses();
			}

			db.close();

			if(isCancelled()) return null;

			Comparator comparator;

			switch(MetaSettings.getSortBy(context)) {
			case 0:
				comparator = new Metadata.Comparator(DefaultMetaData.TIME_CREATED);
				break;
			case 1:
				comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE);
				break;
			case 2:
				comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE_ALPHABETICAL);
				break;
			case 3:
				comparator = new Metadata.Comparator(DefaultMetaData.STATE);
				break;
			default:
				comparator = new Metadata.Comparator(DefaultMetaData.STATE);
				break;
			}

			Collections.sort(verses, comparator);

			if(isCancelled()) return null;

			bibleVerseAdapter = new BibleVerseAdapter(context, verses, getListView());
			bibleVerseAdapter.setOnItemClickListener(itemClick);
			bibleVerseAdapter.setOnItemMultiselectListener(iconClick);
			bibleVerseAdapter.setOnItemOverflowClickListener(overflowClick);

			return null;
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

//ActionBar items
//------------------------------------------------------------------------------
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_list_sort_date:
                MetaSettings.putSortBy(context, 0);
                loaderThread = new PopulateBibleVerses();
				loaderThread.execute();
                return true;
//            case R.id.menu_list_sort_canonical:
//                MetaSettings.putSortBy(context, 1);
//                loaderThread = new PopulateBibleVerses();
//				loaderThread.execute();
//                return true;
            case R.id.menu_list_sort_alphabetically:
                MetaSettings.putSortBy(context, 2);
                loaderThread = new PopulateBibleVerses();
				loaderThread.execute();
                return true;
            case R.id.menu_list_sort_mem_state:
                MetaSettings.putSortBy(context, 3);
                loaderThread = new PopulateBibleVerses();
				loaderThread.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//Contextual ActionMode for multi-selection
//------------------------------------------------------------------------------
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_list, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                //clicked back button to close the CAB
                case android.R.id.home:
                    mode.finish();
                    return true;
                //select all verses in the list
                case R.id.contextual_list_select_all:
                    //deselect all items
                    ArrayList<Passage> items = bibleVerseAdapter.getItems();

					int firstPosition = getListView().getFirstVisiblePosition();
					int lastPosition = firstPosition + getListView().getChildCount() - 1;

                    for(Passage passage : items) {
                        if(!passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) {

							int position = passage.getMetadata().getInt("LIST_POSITION");

							if(position >= firstPosition && position <= lastPosition) {
								View view = getListView().getChildAt(position - firstPosition);
								if(view != null) {
									BibleVerseAdapter.ViewHolder vh = (BibleVerseAdapter.ViewHolder) view.getTag();
									vh.multiSelect();
								}
							}
							else {
								passage.getMetadata().putBoolean(DefaultMetaData.IS_CHECKED, true);
							}
                        }
					}

                    //update count in toolbar
                    mActionMode.setTitle(bibleVerseAdapter.getSelectedCount() + "");

                    //just to ensure that all verses correctly reflect their selected state in case of issues
					bibleVerseAdapter.notifyDataSetChanged();

                    return true;

                //delete all selected verses
                case R.id.contextual_list_delete:
                    delete(bibleVerseAdapter.getSelectedItems());
                    return true;

                //export selected verses to XML file and save to SD card
                case R.id.contextual_list_export:
                    new ExportVerses().showPopup(bibleVerseAdapter.getSelectedItems());
                    return true;

                //add a tag to all selected verses
                case R.id.contextual_list_add_tag:
                    addTag(bibleVerseAdapter.getSelectedItems());
                    return true;

                //change the memorization state of all selected verses
                case R.id.contextual_list_change_state:
                    changeState(bibleVerseAdapter.getSelectedItems());
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;

            //deselect all items
            ArrayList<Passage> selectedItems = bibleVerseAdapter.getSelectedItems();

            int firstPosition = getListView().getFirstVisiblePosition();
            int lastPosition = firstPosition + getListView().getChildCount() - 1;

            for(Passage passage : selectedItems) {
                int position = passage.getMetadata().getInt("LIST_POSITION");

                if( position >= firstPosition && position <= lastPosition) {

                    View view = getListView().getChildAt(position - firstPosition);
                    if(view != null) {
                        BibleVerseAdapter.ViewHolder vh = (BibleVerseAdapter.ViewHolder) view.getTag();
                        vh.multiSelect();
                    }
                }
                else {
                    passage.getMetadata().putBoolean(DefaultMetaData.IS_CHECKED, false);
                }
            }
            //just to ensure that all verses correctly reflect their selected state in case of issues
            bibleVerseAdapter.notifyDataSetChanged();
        }
    };

//Actions to perform on verses within the listview. Size-generic, so one call for both single and multiples
//------------------------------------------------------------------------------
    private void delete(final ArrayList<Passage> items) {
        final View view = LayoutInflater.from(context).inflate(R.layout.popup_delete_verse, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        TextView verseList = (TextView) view.findViewById(R.id.verse_list);
        String message = "";
        for(Passage passage : items) {
            message += passage.getReference().toString() + "\n";
        }
        verseList.setText(message.trim());

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
                for(Passage passage : items) {
                    db.deleteVerse(passage);
                    bibleVerseAdapter.removeItem(passage);
                }
                db.close();
                dialog.dismiss();
                if(mActionMode != null) mActionMode.finish();
                bibleVerseAdapter.notifyDataSetChanged();

                String toastMessage = "";
                if(items.size() > 1) toastMessage += items.size() + " verses deleted";
                else toastMessage += items.get(0).getReference().toString() + " deleted";

                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private class ExportVerses extends AsyncTask<Void, Void, Void> {

        View view;
        AlertDialog dialog;
        ArrayList<Passage> passages;
        File file;

        public void showPopup(ArrayList<Passage> passages) {
            this.passages = passages;
            view = LayoutInflater.from(context).inflate(R.layout.popup_export_verses, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);

            dialog = builder.create();

            final EditText editText = (EditText) view.findViewById(R.id.edit_text);

            view.findViewById(R.id.export_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(editText.getText().toString().length() > 0) {
                        String pathA = Environment.getExternalStorageDirectory().getPath() + "/scripturememory";
                        file = new File(pathA, editText.getText().toString().trim().replaceAll("\\..*", "").replaceAll("\\s", "_") + ".xml");
                        execute();
                    }
                }
            });
            view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            view.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            view.findViewById(R.id.description).setVisibility(View.GONE);
            view.findViewById(R.id.export_button).setVisibility(View.GONE);
            view.findViewById(R.id.edit_text).setVisibility(View.GONE);
            view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel(true);
                }
            });
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            dialog.dismiss();
            if(mActionMode != null) mActionMode.finish();
            Toast.makeText(context, "Verses exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                //print verses to an XML file
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                org.w3c.dom.Document doc = builder.newDocument();
                org.w3c.dom.Element root = doc.createElement("verses");
                root.setAttribute("name", file.getName().replaceAll("_", " ").replaceAll("\\.xml", ""));
                doc.appendChild(root);

                for (int i = 0; i < passages.size(); i++) {
                    if (isCancelled()) break;
                    org.w3c.dom.Element passageElement = doc.createElement("passage");
                    root.appendChild(passageElement);

                    org.w3c.dom.Element r = doc.createElement("R");
                    r.appendChild(doc.createTextNode(passages.get(i).getReference().toString()));
                    passageElement.appendChild(r);

                    org.w3c.dom.Element q = doc.createElement("Q");
                    q.appendChild(doc.createTextNode(passages.get(i).getBible().getAbbreviation()));
                    passageElement.appendChild(q);

                    org.w3c.dom.Element t = doc.createElement("T");
                    passageElement.appendChild(t);
                    for (Tag tag : passages.get(i).getTags()) {
                        org.w3c.dom.Element tagItem = doc.createElement("item");
                        tagItem.appendChild(doc.createTextNode(tag.name));
                        t.appendChild(tagItem);
                    }

                    org.w3c.dom.Element p = doc.createElement("P");
                    p.appendChild(doc.createTextNode(passages.get(i).getText()));
                    passageElement.appendChild(p);
                }

                if (isCancelled()) return null;

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
                return null;
            } catch (ParserConfigurationException pce) {
                pce.printStackTrace();
            } catch (TransformerConfigurationException tce) {
                tce.printStackTrace();
            } catch (TransformerException te) {
                te.printStackTrace();
            }
            return null;
        }
    }

    private void changeState(final ArrayList<Passage> items) {
        final View view = LayoutInflater.from(context).inflate(R.layout.popup_change_state, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        final TextView seekbarText = (TextView) view.findViewById(R.id.seekbar_text);
        switch (items.get(0).getMetadata().getInt(DefaultMetaData.STATE)) {
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

        final SeekBar seekbar = (SeekBar) view.findViewById(R.id.stateSeekBar);
        seekbar.setProgress(items.get(0).getMetadata().getInt(DefaultMetaData.STATE) - 1);

        VerseDB db = new VerseDB(context).open();
        int color = db.getStateColor(seekbar.getProgress() + 1);
        db.close();

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
                db.close();

                Drawable line = seekBar.getProgressDrawable();
                line.setColorFilter(color, PorterDuff.Mode.SRC_IN);

                Drawable thumb = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) ?
                        seekbar.getThumb() :
                        context.getResources().getDrawable(R.drawable.seekbar_thumb);
                thumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                seekbar.setThumb(thumb);

                switch (progressValue + 1) {
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
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView saveStateButton = (TextView) view.findViewById(R.id.save_state_button);
        saveStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VerseDB db = new VerseDB(context).open();
                int progress = seekbar.getProgress() + 1;
                for(Passage passage : items) {
                    passage.getMetadata().putInt(DefaultMetaData.STATE, progress);
                    db.updateVerse(passage);

                    //if this verse is the current notification verse and the active list is its state, then
                    //change the active list to be whatever state this verse becomes
                    if(MainSettings.getMainId(context) == passage.getMetadata().getInt(DefaultMetaData.ID) &&
                            listType == VerseListFragment.STATE) {
                        MainSettings.putWorkingList(context, VerseListFragment.STATE, passage.getMetadata().getInt(DefaultMetaData.STATE));
                    }
                }
                db.close();
                if(mActionMode != null) mActionMode.finish();
                dialog.dismiss();
				if(loaderThread == null) {
					loaderThread = new PopulateBibleVerses();
				}
				loaderThread.execute();
            }
        });
        dialog.show();
    }

    private void addTag(final ArrayList<Passage> items) {
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
                    for(Passage passage : items) {
                        passage.addTag(new Tag(text));
                        db.updateVerse(passage);
                    }
                    db.close();
                    dialog.dismiss();
                    if(mActionMode != null) mActionMode.finish();
                    bibleVerseAdapter.notifyDataSetChanged();

                    String toastMessage = "Tag '" + text + "' added to ";
                    if(items.size() > 1) toastMessage += items.size() + " verses";
                    else toastMessage += items.get(0).getReference().toString();

                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

	private void splitVerse(Passage passage) {
		VerseDB db = new VerseDB(context).open();
		db.deleteVerse(passage);


	}

	private class SplitVerse extends AsyncTask<Void, Void, Void> {
		Passage passage;
		ArrayList<Passage> splitVerses;

		public SplitVerse(Passage passage) {
			this.passage = passage;
			splitVerses = new ArrayList<>();
		}


		@Override
		protected void onPreExecute() {
			super.onPreExecute();


		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			VerseDB db = new VerseDB(context).open();
			db.deleteVerse(passage);

			for(Passage splitVerse : splitVerses) {
				db.insertVerse(splitVerse);
			}

			if(loaderThread == null) {
				loaderThread = new PopulateBibleVerses();
			}
			loaderThread.execute();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Verse[] verses = passage.getVerses();

				for(Verse verse : verses) {
					ABSPassage newPassage = new ABSPassage(
							context.getResources().getString(R.string.bibles_org),
							verse.getReference()
					);

					newPassage.setBible(passage.getBible());
					newPassage.setMetadata(passage.getMetadata());
					for(Tag tag : passage.getTags()) {
						newPassage.addTag(tag);
					}
					newPassage.addTag(new Tag(passage.getReference().toString()));

					newPassage.parseDocument(newPassage.getDocument());
				}

				return null;
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return null;
			}
		}
	}
}