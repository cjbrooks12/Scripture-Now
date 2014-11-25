package com.caseybrooks.scripturememory.fragments;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.container.Verses;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.BibleVerseAdapter;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;

public class VerseListFragment extends ListFragment {
//Data members
//------------------------------------------------------------------------------
	Context context;
    ActionBar ab;
    Toolbar tb;

	BibleVerseAdapter bibleVerseAdapter;
	String list;
    int state;

    VerseDB db;
	
//Lifecycle and Initialization
//------------------------------------------------------------------------------
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		getListView().setSelector(new StateListDrawable());
		getListView().setFastScrollEnabled(true);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		context = getActivity();
		Bundle extras = getArguments();
		if(extras.containsKey("KEY_LIST")) {
			list = extras.getString("KEY_LIST");
		}
		else {
			list = "current";
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
        db = new VerseDB(context);
        db.open();

		SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(context,
							   R.array.sort_methods, android.R.layout.simple_spinner_dropdown_item);

		ab = ((ActionBarActivity) context).getSupportActionBar();
		ab.setHomeButtonEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setListNavigationCallbacks(spinnerAdapter, navigationListener);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setSelectedNavigationItem(MetaSettings.getSortBy(context));

		populateBibleVerses();
	}

	@Override
	public void onPause() {
		super.onPause();
		((ActionBarActivity) context).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        db.close();
    }

	//Custom list Adapter
//------------------------------------------------------------------------------
//	public class BibleVerseAdapter extends ArrayAdapter<Passage> {
//	    Context context;
//	    Verses<Passage> verses;
//
//		public BibleVerseAdapter(Context context, Verses<Passage> verses) {
//            super(context, R.layout.list_bible_verse, verses.toArray(new Passage[verses.size()]));
//
//			this.context = context;
//			this.verses = verses;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			View itemView = convertView;
//			if(itemView == null) {
//				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//				itemView = inflater.inflate(R.layout.list_card_new, parent, false);
//			}
//
//            final float scale = getContext().getResources().getDisplayMetrics().density;
//            final CardView cardView = (CardView) itemView.findViewById(R.id.card_view);
//            cardView.setMaxCardElevation(6 * scale + 0.5f);
//            cardView.setCardElevation(1 * scale + 0.5f);
//
//            //Set reference, verse text, and version
//			Passage currentVerse = verses.get(position);
//
//			TextView reference = (TextView) itemView.findViewById(R.id.item_reference);
//            reference.setText(currentVerse.getReference());
//
//			TextView verse = (TextView) itemView.findViewById(R.id.item_verse);
//            verse.setText(currentVerse.getText());
//
//            TextView version = (TextView) itemView.findViewById(R.id.version);
//            version.setText(currentVerse.getVersion().getCode().toUpperCase());
//
//            //set text and color of main circle icon
//            final TextView icon_text = (TextView) itemView.findViewById(R.id.ref_icon_text);
//            String first_letter = currentVerse.getReference().substring(0, 1);
//            if(first_letter.equals("1") || first_letter.equals("2") || first_letter.equals("3"))
//                icon_text.setText(currentVerse.getReference().substring(0, 4));
//            else
//                icon_text.setText(currentVerse.getReference().substring(0, 2));
//
//            //create drawable circle
//            final ImageView icon_background = (ImageView) itemView.findViewById(R.id.ref_icon_background);
//
//            //TODO: Change to use static Util circle method, to hide using the color filter
//            final Drawable circle = context.getResources().getDrawable(R.drawable.circle);
//            int iconColor = db.getStateColor(state);
//            circle.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.MULTIPLY));
//            icon_background.setImageDrawable(circle);//create drawable circle
//            final ImageView icon_background = (ImageView) itemView.findViewById(R.id.ref_icon_background);
//
//            //TODO: Change to use static Util circle method, to hide using the color filter
//            final Drawable circle = context.getResources().getDrawable(R.drawable.circle);
//            int iconColor = db.getStateColor(state);
//            circle.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.MULTIPLY));
//            icon_background.setImageDrawable(circle);
//
//            icon_background.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    final Animation a = AnimationUtils.loadAnimation(context, R.anim.flip_to_middle);
//                    final Animation b = AnimationUtils.loadAnimation(context, R.anim.flip_from_middle);
//
//                    a.setAnimationListener(new Animation.AnimationListener() {
//                        @Override
//                        public void onAnimationStart(Animation arg0) {
//                        }
//
//                        @Override
//                        public void onAnimationRepeat(Animation arg0) {
//                        }
//
//                        @Override
//                        public void onAnimationEnd(Animation arg0) {
//
//                            TypedArray a = context.getTheme().obtainStyledAttributes(R.style.Theme_BaseLight, new int[]{R.attr.colorAccent});
//                            int selectedColor = a.getColor(0, 0);
//                            a.recycle();
//
//                            circle.setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY));
//                            icon_background.setImageDrawable(circle);
//
//                            icon_background.startAnimation(b);
////                            icon_text.startAnimation(b);
//                        }
//                    });
//                    icon_background.startAnimation(a);
////                    icon_text.startAnimation(a);
//
//                    cardView.setCardElevation(4 * scale + 0.5f);
//
//                    getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//                }
//            });
//
//
//            //add tag circles to layout
//            LinearLayout tagsLayout = (LinearLayout) itemView.findViewById(R.id.tags_layout);
//            tagsLayout.removeAllViews();
//            String[] tags = currentVerse.getTags();
//            for(String tag : tags) {
//                int tagColor = db.getTagColor(tag); //tag_cursor.getString(tag_cursor.getColumnIndex(VerseDB.KEY_TAGS_COLOR));
//
//                Drawable tag_circle = context.getResources().getDrawable(R.drawable.circle);
//                tag_circle.setColorFilter(new PorterDuffColorFilter(tagColor, PorterDuff.Mode.MULTIPLY));
//
//                ImageView tagView = new ImageView(context);
//
//
//                int size = (int) (20 * scale + 0.5f);
//                int margin = (int) (2 * scale + 0.5f);
//
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
//                layoutParams.setMargins(0, 0, margin, 0);
//                tagView.setLayoutParams(layoutParams);
//
//
//                tagView.setImageDrawable(tag_circle);
//                tagsLayout.addView(tagView);
//            }
//
//
//            itemView.findViewById(R.id.list_overflow_button).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    PopupMenu popup = new PopupMenu(context, v);
//                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        @Override
//                        public boolean onMenuItemClick(MenuItem menuItem) {
//                            switch (menuItem.getItemId()) {
//                                case R.id.context_list_card_post:
//                                    return true;
//                                case R.id.context_list_card_move:
//                                    return true;
//                                case R.id.context_list_card_edit:
//                                    return true;
//                                case R.id.context_list_card_delete:
//                                    return true;
//                                default:
//                                    return false;
//                            }
//                        }
//                    });
//                    MenuInflater inflater = popup.getMenuInflater();
//                    inflater.inflate(R.menu.context_list_card, popup.getMenu());
//                    popup.show();
//                }
//            });
//
//
//
//			return itemView;
//		}
//
//		@Override
//	    public void add(Passage bv) {
//	        verses.add(bv);
//	        notifyDataSetChanged();
//	    }
//
//		@Override
//	    public void remove(Passage bv) {
//	        verses.remove(bv);
//	        notifyDataSetChanged();
//	    }
//
//		public Passage get(int position) {
//			return verses.get(position);
//		}
//	}
	
	public void onListItemClick(ListView lv, View v, int position, long id) {
		switchToEditFragment((int)bibleVerseAdapter.getItemId(position));
	}

    View.OnClickListener iconClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
	
	private void populateBibleVerses() {
		Verses<Passage> verses;
		VerseDB db = new VerseDB(context);
        state = (list.equals("memorized")) ? 5 : 1;
		
		db.open();
        if(state == 5)
		    verses = db.getStateVerses(state);
        else
            verses = db.getAllCurrentVerses();
		db.close();

        switch(MetaSettings.getSortBy(context)) {
            case 0:
                verses.sortByID();
                break;
            case 1:
                verses.sort();
                break;
            case 2:
                verses.sortAlphabetical();
                break;
        }

		bibleVerseAdapter = new BibleVerseAdapter(context, verses);
		setListAdapter(bibleVerseAdapter);
	}

//ActionBar Spinner
//------------------------------------------------------------------------------
    ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {
        @Override
        public boolean onNavigationItemSelected(int position, long itemId) {

            String[] strings = getActivity().getResources().getStringArray(R.array.sort_methods);

			MetaSettings.putSortBy(context, position);
            populateBibleVerses();

            return true;
        }
    };
	
//Host Activity Interface
//------------------------------------------------------------------------------
	private static onListEditListener listener;
	
	public void switchToEditFragment(int id) {
	    if(listener != null) {
	        listener.toEdit(id);
	    }
	}

	public interface onListEditListener {
	    void toEdit(int id);
    }

	public static void setOnListEditListener(onListEditListener listener) {
	    VerseListFragment.listener = listener;
	}
}