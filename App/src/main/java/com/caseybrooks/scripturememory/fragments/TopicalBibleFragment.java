package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.androidbibletools.providers.openbible.OpenBibleInfo;
import com.caseybrooks.common.features.NavigationCallbacks;
import com.caseybrooks.common.features.Util;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;

import java.io.IOException;
import java.util.ArrayList;

public class TopicalBibleFragment extends Fragment {
    Context context;

    AutoCompleteTextView searchEditText;
    ArrayAdapter<String> suggestionsAdapter;

    ListView listView;
    OpenBibleAdapter adapter;
    View headerView;

    ActionMode mActionMode;
    NavigationCallbacks mCallbacks;
    ProgressBar progress;
    ImageButton searchButton;

    public static Fragment newInstance() {
        Fragment fragment = new TopicalBibleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

		mCallbacks.setToolBar("Topical Bible", context.getResources().getColor(R.color.open_bible_brown));
        mCallbacks.expandToolbarWIthView(headerView);
		MetaSettings.putDrawerSelection(context, 2, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mActionMode != null) mActionMode.finish();

        mCallbacks.collapseExpandedToolbar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the main layout for this fragment
        View view = inflater.inflate(R.layout.fragment_topical_bible, container, false);

        this.context = getActivity();

        ColorFilter filter = new PorterDuffColorFilter(getResources().getColor(R.color.forest_green), PorterDuff.Mode.SRC_IN);
        progress = (ProgressBar) view.findViewById(R.id.progress);
        progress.getProgressDrawable().setColorFilter(filter);
        progress.getIndeterminateDrawable().setColorFilter(filter);

        listView = (ListView) view.findViewById(R.id.listview);

        headerView = inflater.inflate(R.layout.parallax_open_bible_header, null);

        searchEditText = (AutoCompleteTextView) headerView.findViewById(R.id.discoverEditText);
        searchButton = (ImageButton) headerView.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Util.isConnected(context)) {
                    String text = searchEditText.getText().toString();
                    if (text.length() > 1) {
                        new SearchVerseAsync().execute(text);
                    }
                }
                else {
                    Toast.makeText(context, "Cannot search, no internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(Util.isConnected(context)) {
                        String text = searchEditText.getText().toString();
                        if (text.length() > 1) {
                            new SearchVerseAsync().execute(text);
                            return true;
                        }
                    }
                    else {
                        Toast.makeText(context, "Cannot search, no internet connection", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                return false;
            }
        });
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    searchButton.setVisibility(View.GONE);
                }
                else {
                    searchButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        suggestionsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        searchEditText.setAdapter(suggestionsAdapter);
        searchEditText.addTextChangedListener(new TextWatcher() {
            Character searchedChar;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                if(searchedChar == null || (s.length() > 0 && s.charAt(0) != searchedChar)) {
                    if(Util.isConnected(context)) {
                        new GetSuggestionsAsync().execute(s.charAt(0));
                        searchedChar = s.charAt(0);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String s = suggestionsAdapter.getItem(position);
                if(Util.isConnected(context)) {
                    new SearchVerseAsync().execute(s);
                }
                else {
                    Toast.makeText(context, "Cannot search, no internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //setup adapter for parallax listview
        adapter = new OpenBibleAdapter(context, new ArrayList<ABSPassage>(), listView);
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<ABSPassage> singleItem = new ArrayList<ABSPassage>();
                singleItem.add(((ViewHolder)view.getTag()).passage);
                save(singleItem);
            }
        });
        adapter.setOnItemMultiselectListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mActionMode == null) {
                    mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    mActionMode.setTitle(adapter.getSelectedCount() + "");
                }
                else if(adapter.getSelectedCount() == 0) {
                    mActionMode.finish();
                }
                else {
                    mActionMode.setTitle(adapter.getSelectedCount() + "");
                }
            }
        });
        adapter.setOnItemOverflowClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
        listView.setAdapter(adapter);

        return view;
    }

//ActionMode callbacks to handle CAB lifecycle and menu pressed on it
//------------------------------------------------------------------------------

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_open_bible, menu);
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
                case R.id.contextual_open_bible_select_all:
                    ArrayList<ABSPassage> items = adapter.getItems();

                    int firstPosition = listView.getFirstVisiblePosition();
                    int lastPosition = firstPosition + listView.getChildCount() - 1;

                    for(Passage passage : items) {
                        int position = passage.getMetadata().getInt("LIST_POSITION");

                        if(!passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) {
                            if (position >= firstPosition && position <= lastPosition) {
                                ViewHolder vh = (ViewHolder) listView.getChildAt(position - firstPosition).getTag();
                                if (vh != null) {
                                    vh.multiSelect();
                                }
                                else {
                                    Toast.makeText(context, "Position [" + position + "] has no tag: " + passage.getReference().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                passage.getMetadata().putBoolean(DefaultMetaData.IS_CHECKED, true);
                            }
                        }
                    }

                    //update count in toolbar
                    mActionMode.setTitle(adapter.getSelectedCount() + "");

                    //just to ensure that all verses correctly reflect their selected state in case of issues
                    adapter.notifyDataSetChanged();

                    return true;
//				case R.id.contextual_open_bible_redownload:
//					ABSPassage[] passagesArray = new ABSPassage[adapter.getSelectedItems().size()];
//					adapter.getSelectedItems().toArray(passagesArray);
//					new RedownloadAsync().execute(passagesArray);
//					return true;
                case R.id.contextual_open_bible_save:
                    save(adapter.getSelectedItems());
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
            ArrayList<ABSPassage> selectedItems = adapter.getSelectedItems();

            int firstPosition = listView.getFirstVisiblePosition();
            int lastPosition = firstPosition + listView.getChildCount() - 1;

            for(Passage passage : selectedItems) {
                int position = passage.getMetadata().getInt("LIST_POSITION");
                if( position >= firstPosition && position <= lastPosition) {

                    View view = listView.getChildAt(position - firstPosition);
                    if(view != null) {
                        ViewHolder vh = (ViewHolder) listView.getChildAt(position - firstPosition).getTag();
                        if (vh != null) {
                            vh.multiSelect();
                        }
                        else {
                            Toast.makeText(context, "Position [" + position + "] has no tag: " + passage.getReference().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    passage.getMetadata().putBoolean(DefaultMetaData.IS_CHECKED, false);
                }
            }
            //just to ensure that all verses correctly reflect their selected state in case of issues
            adapter.notifyDataSetChanged();
        }
    };

//TODO: merge the ViewHolder for this class with the ViewHolder for the VerseListFragment and use the one adapter for either one
//Adapter for the listview in this fragment
//------------------------------------------------------------------------------
    public class OpenBibleAdapter extends BaseAdapter {
        Context context;
        ListView lv;
        ArrayList<ABSPassage> items;

        AdapterView.OnItemClickListener cardClick;
        AdapterView.OnItemClickListener overflowClick;
        AdapterView.OnItemClickListener iconClick;

        public OpenBibleAdapter(Context context, ArrayList<ABSPassage> items, ListView lv) {
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

        public ArrayList<ABSPassage> getItems() {
            return items;
        }

        public int getSelectedCount() {
            int count = 0;

            for (Passage passage : items) {
                if (passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) count++;
            }

            return count;
        }

        public ArrayList<ABSPassage> getSelectedItems() {
            ArrayList<ABSPassage> selectedItems = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                ABSPassage passage = items.get(i);
                passage.getMetadata().putInt("LIST_POSITION", i);
                if (passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) {
                    selectedItems.add(passage);
                }
            }

            return selectedItems;
        }

        @Override
        public Passage getItem(int position) {
            Passage passage = items.get(position);
            passage.getMetadata().putInt("LIST_POSITION", position);
            return passage;
        }

        //items do not yet exist in the database, so do not have ids
        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void removeItem(ABSPassage item) {
            if (items.contains(item)) {
                items.remove(item);
            }
        }

        public void add(ABSPassage item) {
            this.items.add(item);
        }

        public void add(ABSPassage item, int index) {
            this.items.add(index, item);
        }

        public void addAll(ArrayList<ABSPassage> items) {
            this.items = items;
            for(int i = 0; i < this.items.size(); i++) {
                this.items.get(i).getMetadata().putInt("LIST_POSITION", i);
            }

            notifyDataSetChanged();
        }

        public void clear() {
            items.clear();
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.list_open_bible, parent, false);
                vh = new ViewHolder(context, view);
                view.setTag(vh);
            } else {
                vh = (ViewHolder) view.getTag();
            }

            vh.initialize(items.get(position));

            vh.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder vh = (ViewHolder) v.getTag();
                    if (iconClick != null)
                        cardClick.onItemClick(lv, vh.view, vh.getPosition(), vh.getId());
                }
            });
            vh.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder vh = (ViewHolder) v.getTag();
                    if (iconClick != null)
                        overflowClick.onItemClick(lv, vh.overflow, vh.getPosition(), vh.getId());
                }
            });
            vh.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ViewHolder vh = (ViewHolder) v.getTag();
                    vh.multiSelect();
                    if (iconClick != null)
                        iconClick.onItemClick(lv, vh.iconBackground, vh.getPosition(), vh.getId());
                    return true;
                }
            });
            vh.iconBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder vh = (ViewHolder) v.getTag();
                    vh.multiSelect();
                    if (iconClick != null)
                        iconClick.onItemClick(lv, vh.iconBackground, vh.getPosition(), vh.getId());

                }
            });

            view.setTag(vh);
            vh.iconBackground.setTag(vh);
            vh.view.setTag(vh);
            vh.overflow.setTag(vh);

            return view;
        }
    }

    public static class ViewHolder {
        final Context context;
        final Animation a_selected;
        final Animation a_not_selected;
        final Animation b;
        final Drawable circle;

        public ABSPassage passage;

        View view;
        TextView reference;
        TextView verseText;
        TextView version;
        TextView upcount;

        ImageView iconBackground;
        TextView iconText;
        ImageView iconCheck;

        ImageView overflow;

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
                    circle.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.open_bible_green), PorterDuff.Mode.MULTIPLY));
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
                    circle.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.open_bible_brown), PorterDuff.Mode.MULTIPLY));
                    iconBackground.setImageDrawable(circle);

                    iconText.setVisibility(View.VISIBLE);
                    iconCheck.setVisibility(View.INVISIBLE);

                    iconBackground.startAnimation(b);
                    iconText.startAnimation(b);
                }
            });

            view = inflater.findViewById(R.id.list_open_bible_view);

            reference = (TextView) inflater.findViewById(R.id.item_reference);
            verseText = (TextView) inflater.findViewById(R.id.item_verse);
            version = (TextView) inflater.findViewById(R.id.item_version);
            upcount = (TextView) inflater.findViewById(R.id.item_upcount);

            iconBackground = (ImageView) inflater.findViewById(R.id.icon_background);
            iconText = (TextView) inflater.findViewById(R.id.icon_text);
            iconCheck = (ImageView) inflater.findViewById(R.id.icon_check);

            overflow = (ImageView) inflater.findViewById(R.id.overflow);
        }

        private int getPosition() {
            return passage.getMetadata().getInt("LIST_POSITION");
        }

        private int getId() {
            return passage.getMetadata().getInt(DefaultMetaData.ID);
        }

        private void initialize(ABSPassage passage) {
            this.passage = passage;

            reference.setText(passage.getReference().toString());
            verseText.setText(passage.getText());
            version.setText(passage.getBible().getAbbreviation().toUpperCase());
            upcount.setText(passage.getMetadata().getInt("UPVOTES") + " helpful votes");

            iconText.setText(passage.getMetadata().getInt("UPVOTES") + "");

            if(passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) {
                circle.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.open_bible_green), PorterDuff.Mode.SRC_IN));
                iconBackground.setImageDrawable(circle);

                iconCheck.setVisibility(View.VISIBLE);
                iconText.setVisibility(View.INVISIBLE);
            }
            else {
                circle.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.open_bible_brown), PorterDuff.Mode.SRC_IN));
                iconBackground.setImageDrawable(circle);

                iconCheck.setVisibility(View.INVISIBLE);
                iconText.setVisibility(View.VISIBLE);
            }
        }

        public void multiSelect() {
            if(!passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) {

                passage.getMetadata().putBoolean(DefaultMetaData.IS_CHECKED, true);

                iconBackground.startAnimation(a_not_selected);
                iconText.startAnimation(a_not_selected);
            }
            else {
                passage.getMetadata().putBoolean(DefaultMetaData.IS_CHECKED, false);

                iconBackground.startAnimation(a_selected);
                iconCheck.startAnimation(a_selected);
            }
        }
    }

//asynchronously perform tasks to get suggestions and verses from search topic
//------------------------------------------------------------------------------

    private class GetSuggestionsAsync extends AsyncTask<Character, Void, Void> {
        String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);
            progress.setProgress(0);
            suggestionsAdapter.clear();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            int threshold = searchEditText.getThreshold();
            searchEditText.setThreshold(1);
            searchEditText.showDropDown();
            searchEditText.setThreshold(threshold);
            suggestionsAdapter.notifyDataSetChanged();
			progress.setVisibility(View.GONE);
		}

        @Override
        protected Void doInBackground(Character... params) {
            try {
                if(Util.isConnected(context)) {
                    for(Character character : params) {
                        ArrayList<String> suggestions = OpenBibleInfo.getSuggestions(character);

                        for(String string : suggestions) {
                            suggestionsAdapter.add(string);
                        }
                    }
                    message = "Finished";
                }
                else {
                    message = "Cannot search, no internet connection";
                }
            }
            catch(IOException e2) {
                message = "Error while retrieving verse";
            }

            return null;
        }
    }

    private class SearchVerseAsync extends AsyncTask<String, ABSPassage, ArrayList<ABSPassage>> {
        String message;
        int count;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            count = 0;

            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);
            progress.setProgress(0);
        }

        @Override
        protected ArrayList<ABSPassage> doInBackground(String... params) {
            try {
                if(Util.isConnected(context)) {
					ArrayList<Passage> downloadedPassages = OpenBibleInfo.getVersesFromTopic(params[0]);
					ArrayList<ABSPassage> absPassages = new ArrayList<>();

					for(Passage passage : downloadedPassages) {
						ABSPassage newPassage = new ABSPassage(
								context.getResources().getString(R.string.bibles_org),
								passage.getReference()
						);
						newPassage.setText(passage.getText());
						newPassage.setMetadata(passage.getMetadata());
						newPassage.setTags(passage.getTags());

						absPassages.add(newPassage);
					}
					return absPassages;
                }
                else {
                    message = "Cannot search, no internet connection";
                }
            }
            catch(IOException e2) {
                message = "Error while retrieving verse";
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<ABSPassage> aVoid) {
            super.onPostExecute(aVoid);

            progress.setVisibility(View.GONE);

            adapter.addAll(aVoid);
        }
    }

//Perform actions of groups of verses selected from the list of retrieved verses
//------------------------------------------------------------------------------

//    private class RedownloadAsync extends AsyncTask<ABSPassage, ABSPassage, Void> {
//
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//			progress.setVisibility(View.VISIBLE);
//			progress.setIndeterminate(true);
//			progress.setProgress(0);
//		}
//
//		@Override
//		protected void onPostExecute(Void aVoid) {
//			super.onPostExecute(aVoid);
//			adapter.notifyDataSetChanged();
//			progress.setVisibility(View.GONE);
//		}
//
//        @Override
//        protected void onProgressUpdate(ABSPassage... values) {
//            super.onProgressUpdate(values);
//            adapter.notifyDataSetChanged();
//			progress.setIndeterminate(false);
//			progress.setProgress(progress.getProgress() + 1);
//        }
//
//        @Override
//        protected Void doInBackground(ABSPassage... params) {
////			progress.post(new Runnable() {
////                @Override
////                public void run() {
////                    progress.setMax(params.length);
////                }
////            };
//
//            for(ABSPassage passage : params) {
//                try {
//                    passage.setBible(MetaSettings.getBibleVersion(context));
//                    passage.parseDocument(passage.getDocument());
//					publishProgress(passage);
//                }
//                catch(IOException ioe) {
//                    ioe.printStackTrace();
//                }
//            }
//
//            return null;
//        }
//    }

    //TODO: get helpful feedback about which verses to add in the popup and a count of verses added in a toast
    public void save(final ArrayList<ABSPassage> verses) {
        final View view = LayoutInflater.from(context).inflate(R.layout.popup_add_verse, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        TextView verseList = (TextView) view.findViewById(R.id.verse_list);
        String message = "";
        for(Passage passage : verses) {
            message += passage.getReference().toString() + "\n";
        }
        verseList.setText(message.trim());

        TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        TextView addVerseButton = (TextView) view.findViewById(R.id.add_verse_button);
        addVerseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VerseDB db = new VerseDB(context).open();
                for(Passage passage : verses) {
                    passage.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.CURRENT_NONE);
                    passage.addTag(new Tag(passage.getMetadata().getString("SEARCH_TERM")));
                    db.insertVerse(passage);
                }
                dialog.cancel();
                if(mActionMode != null) mActionMode.finish();
            }
        });

        dialog.show();
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
