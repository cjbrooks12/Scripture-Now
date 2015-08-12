package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.common.features.NavListItem;
import com.caseybrooks.common.features.NavigationCallbacks;
import com.caseybrooks.common.features.Util;
import com.caseybrooks.scripturememory.BuildConfig;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.nowcards.main.MainSettings;
import com.nirhart.parallaxscroll.views.ParallaxExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NavigationDrawerFragment extends Fragment {
    private NavigationCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ParallaxExpandableListView mDrawerListView;
    private ExpandableListAdapter listAdapter;
    private View mFragmentContainerView;

    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private AppCompatActivity parentActivity;

    public NavigationDrawerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.

        mUserLearnedDrawer = MetaSettings.getUserLearnedDrawer(getActivity());

        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
        }

		Pair<Integer, Integer> pair;
		switch(MetaSettings.getDefaultScreen(getActivity()).first) {
		case 0:
			pair = MetaSettings.getDrawerSelection(parentActivity);
			break;
		case 1:
			pair = new Pair<>(0, 0);
			break;
		case 2:
			pair = new Pair<>(1, 0);
			break;
        case 3:
            pair = new Pair<>(2, 0);
            break;
		case 4:
			pair = new Pair<>(3, MetaSettings.getDefaultScreen(getActivity()).second);
			break;
		case 5:
			pair = new Pair<>(4, MetaSettings.getDefaultScreen(getActivity()).second);
			break;
		case 6:
			if(MainSettings.getWorkingList(getActivity()).first == -1) {
				pair = new Pair<>(0, 0);
			}
			if(MainSettings.getWorkingList(getActivity()).first == VerseListFragment.STATE) {
				pair = new Pair<>(3, MainSettings.getWorkingList(getActivity()).second);
			}
			else {
				pair = new Pair<>(4, MainSettings.getWorkingList(getActivity()).second);
			}
			break;
		default:
			pair = new Pair<>(0, 0);
			break;
		}

		selectItem(pair);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

//create ExpandableListView and populate
//------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView = (ParallaxExpandableListView) view.findViewById(R.id.navListView);

		populateList();

        return view;
    }

    private void populateList() {
        RelativeLayout header = (RelativeLayout) LayoutInflater.from(parentActivity).inflate(R.layout.parallax_drawer_header, null);
        mDrawerListView.addParallaxedHeaderView(header);

        List<String> listDataHeader = new ArrayList<String>();
        HashMap<String, List<NavListItem>> listDataChild = new HashMap<String, List<NavListItem>>();

        //set the headers for top-level navigation
        listDataHeader.add("Dashboard");
        listDataHeader.add("Read");
        listDataHeader.add("Discover");
        listDataHeader.add("Memorization State");
        listDataHeader.add("Tags");
        listDataHeader.add("Settings");
        listDataHeader.add("Help & Feedback");

        // set dashboard subitems (none)
        listDataChild.put(listDataHeader.get(0), new ArrayList<NavListItem>());
        listDataChild.put(listDataHeader.get(1), new ArrayList<NavListItem>());
		listDataChild.put(listDataHeader.get(2), new ArrayList<NavListItem>());
		listDataChild.put(listDataHeader.get(3), new ArrayList<NavListItem>());
		listDataChild.put(listDataHeader.get(4), new ArrayList<NavListItem>());
		listDataChild.put(listDataHeader.get(5), new ArrayList<NavListItem>());
        listDataChild.put(listDataHeader.get(6), new ArrayList<NavListItem>());

		if (BuildConfig.DEBUG) {
			listDataHeader.add("Debug");
			listDataChild.put(listDataHeader.get(7), new ArrayList<NavListItem>());
		}

        listAdapter = new ExpandableListAdapter(parentActivity, listDataHeader, listDataChild);
        mDrawerListView.setAdapter(listAdapter);
		listAdapter.notifyDataSetChanged();

        mDrawerListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
				selectItem(new Pair<>(groupPosition, (int)id));

                return true;
            }
        });

        mDrawerListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if(groupPosition == 0) {
                    selectItem(new Pair<>(groupPosition, 0));
                    return true;
                }
                else if(groupPosition == 1) {
                    if (BuildConfig.DEBUG) {
                        return false;
                    }
                    else {
                        selectItem(new Pair<>(groupPosition, -1));
                        return true;
                    }
                }
                else if(groupPosition == 2) {
                    selectItem(new Pair<>(groupPosition, 0));
                    return true;
                }
                else if(groupPosition == 5) {
                    selectItem(new Pair<>(groupPosition, 0));
                    return true;
                }
                else if(groupPosition == 6) {
                    selectItem(new Pair<>(groupPosition, 0));
                    return true;
                }
                else {
                    return false;
                }
            }
        });
    }

    private void selectItem(Pair<Integer, Integer> item) {
        if (mCallbacks != null) {
            MetaSettings.putDrawerSelection(parentActivity, item.first, item.second);
            if(listAdapter != null) listAdapter.notifyDataSetChanged();

            switch(item.first) {
                case 0:
                    mCallbacks.toDashboard();
                    break;
                case 1:
                    mCallbacks.toBible(item.second);
                    break;
                case 2:
                    mCallbacks.toTopicalBible();
                    break;
                case 3:
                    mCallbacks.toVerseList(VerseListFragment.STATE, item.second);
                    break;
                case 4:
                    mCallbacks.toVerseList(VerseListFragment.TAGS, item.second);
                    break;
                case 5:
                    mCallbacks.toSettings();
                    break;
                case 6:
                    mCallbacks.toHelp();
					break;
				case 7:
					if(item.second == 0)
						mCallbacks.toDebugPreferences();
					else if(item.second == 1)
						mCallbacks.toDebugDatabase();
                    else if(item.second == 2)
                        mCallbacks.toDebugCache();
                    break;
                default:
            }
        }

		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(mFragmentContainerView);
		}
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private List<String> headerItems; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<NavListItem>> childItems;

        public ExpandableListAdapter(
                Context context,
                List<String> headerItems,
                HashMap<String, List<NavListItem>> childItems) {

            this.context = context;
            this.headerItems = headerItems;
            this.childItems = childItems;
        }

        @Override
        public void notifyDataSetChanged() {
            int[] allRibbonIds = new int[] { 0, 1, 2, 3 };

            List<NavListItem> ribbons = new ArrayList<NavListItem>();
            if(allRibbonIds.length > 0) {
                for (int id : allRibbonIds) {
                    NavListItem item = new NavListItem();
                    item.id = id;
                    item.groupPosition = 1;
                    item.name = getResources().getStringArray(R.array.ribbon_names)[id];
                    item.count = 0;

                    switch(id) {
                    case 0:
                        item.color = getResources().getColor(R.color.ribbon_nt);
                        break;
                    case 1:
                        item.color = getResources().getColor(R.color.ribbon_ot);
                        break;
                    case 2:
                        item.color = getResources().getColor(R.color.ribbon_wis);
                        break;
                    case 3:
                        item.color = getResources().getColor(R.color.ribbon_other);
                        break;
                    default:
                        item.color = getResources().getColor(R.color.ribbon_nt);
                    }

                    ribbons.add(item);
                }
            }
            childItems.remove(headerItems.get(1));
            childItems.put(headerItems.get(1), ribbons);

			VerseDB db = new VerseDB(context).open();

			//update all the state information
			int[] allStateIds = new int[] {
					VerseDB.ALL_VERSES,
					VerseDB.CURRENT,
					VerseDB.MEMORIZED,
					VerseDB.CURRENT_ALL,
					VerseDB.CURRENT_MOST,
					VerseDB.CURRENT_SOME,
					VerseDB.CURRENT_NONE };

			List<NavListItem> states = new ArrayList<NavListItem>();
			if(allStateIds.length > 0) {
				for (int id : allStateIds) {
					NavListItem item = new NavListItem();
					item.id = id;
					item.groupPosition = 3;
					item.name = db.getStateName(item.id);
					item.color = db.getStateColor(item.id);
					item.count = db.getStateCount(item.id);

					states.add(item);
				}
			}
			childItems.remove(headerItems.get(3));
			childItems.put(headerItems.get(3), states);

			//update all the tag information
			ArrayList<Tag> allTags = db.getAllTags();
			allTags.add(db.getTag(VerseDB.UNTAGGED));

			List<NavListItem> tags = new ArrayList<NavListItem>();
			for (Tag tag: allTags) {
				NavListItem item = new NavListItem();
				item.id = tag.id;
				item.groupPosition = 4;
				item.name = tag.name;
				item.color = tag.color;
				item.count = tag.count;

				tags.add(item);
			}
			childItems.remove(headerItems.get(4));
			childItems.put(headerItems.get(4), tags);

            db.close();

			//put debug selections if in debug mode
			if(BuildConfig.DEBUG) {
                //Items under the Debug Tab
				List<NavListItem> debugItems = new ArrayList<>();

				NavListItem debugPrefsItem = new NavListItem();
				debugPrefsItem.id = 0;
				debugPrefsItem.groupPosition = 7;
				debugPrefsItem.childPosition = 0;
				debugPrefsItem.name = "All Preferences";
				debugPrefsItem.color = Color.parseColor("#FFC107");
				debugPrefsItem.count = DebugSharedPreferencesFragment.getPrefsCount(context);
				debugItems.add(debugPrefsItem);

				NavListItem debugDatabaseItem = new NavListItem();
				debugDatabaseItem.id = 1;
				debugDatabaseItem.groupPosition = 7;
				debugDatabaseItem.childPosition = 1;
				debugDatabaseItem.name = "Entire Database";
				debugDatabaseItem.color = Color.parseColor("#4CAF50");
				debugDatabaseItem.count = 0;
				debugItems.add(debugDatabaseItem);

				NavListItem debugCacheItem = new NavListItem();
				debugCacheItem.id = 2;
				debugCacheItem.groupPosition = 7;
				debugCacheItem.childPosition = 2;
				debugCacheItem.name = "Cache Contents";
				debugCacheItem.color = Color.parseColor("#607D8B");
				debugCacheItem.count = DebugCacheFragment.getCacheCount(context);
				debugItems.add(debugCacheItem);

                childItems.remove(headerItems.get(7));
                childItems.put(headerItems.get(7), debugItems);
			}

            super.notifyDataSetChanged();
        }

        @Override
        public NavListItem getChild(int groupPosition, int childPosition) {
            return childItems.get(headerItems.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            NavListItem item = getChild(groupPosition, childPosition);
            return item.id;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_drawer_child_view, null);
            }

            NavListItem item = getChild(groupPosition, childPosition);
			item.childPosition = childPosition;

            TextView childText = (TextView) convertView.findViewById(R.id.subitemText);
            childText.setText(item.name);

            ImageView tagCircle = (ImageView) convertView.findViewById(R.id.subitemCircle);
            TextView tagCircleCount = (TextView) convertView.findViewById(R.id.subitemCircleText);

            if(groupPosition == 1) {
                Drawable ribbonIcon = getResources().getDrawable(R.drawable.ic_action_bookmark_light);
                ribbonIcon.mutate();
                ribbonIcon.setColorFilter(new PorterDuffColorFilter(item.color, PorterDuff.Mode.SRC_IN));
                tagCircle.setBackgroundColor(Color.parseColor("#00000000"));
                tagCircle.setImageDrawable(ribbonIcon);
            }
            else {
                tagCircle.setBackgroundDrawable(Util.Drawables.circle(item.color));
                tagCircleCount.setText(Integer.toString(item.count));
            }


            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childItems.get(headerItems.get(groupPosition)).size();
        }

        @Override
        public String getGroup(int groupPosition) {
            return headerItems.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return headerItems.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_drawer_group_view, null);
            }

            String headerTitle = getGroup(groupPosition);
            TextView headerText = (TextView) convertView.findViewById(R.id.navListHeader);
            ImageView headerImage = (ImageView) convertView.findViewById(R.id.header_image);

			if(groupPosition == 4) {
				headerText.setText(headerTitle + " (" + Math.max(getChildrenCount(groupPosition)-1, 0) + ")");
			}
			else {
				headerText.setText(headerTitle);
			}

			TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{
					R.attr.ic_action_home,
                    R.attr.ic_action_book,
					R.attr.ic_action_find_in_page,
					R.attr.ic_action_group_work,
					R.attr.ic_action_tag,
					R.attr.ic_action_settings,
					R.attr.ic_action_help,
					R.attr.ic_action_debug
			});

			Drawable headerDrawable = getResources().getDrawable(a.getResourceId(groupPosition, 0));
			a.recycle();

            int selectedGroup = MetaSettings.getDrawerSelection(context).first;
            TypedArray selectedColorAttrs = context.getTheme().obtainStyledAttributes(new int[]{
					R.attr.colorAccent,
					R.attr.color_text_secondary,
					R.attr.color_background,
					R.attr.color_background_selected });

            int selectedColor = selectedColorAttrs.getColor(0, 0);
            int unselectedColor = selectedColorAttrs.getColor(1, 0);
            int backgroundColor = selectedColorAttrs.getColor(2, 0);
            int selectedBackgroundColor = selectedColorAttrs.getColor(3, 0);
            selectedColorAttrs.recycle();

            if(groupPosition == selectedGroup) {
                headerDrawable.mutate().setColorFilter(new PorterDuffColorFilter(selectedColor, PorterDuff.Mode.SRC_IN));

                headerText.setTextColor(selectedColor);
                convertView.setBackgroundColor(selectedBackgroundColor);
            }
            else {
                headerDrawable.mutate().setColorFilter(new PorterDuffColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN));

                headerText.setTextColor(unselectedColor);
                convertView.setBackgroundColor(backgroundColor);
            }

            headerImage.setImageDrawable(headerDrawable);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }


//Setup this fragment as a NavigationDrawer
//------------------------------------------------------------------------------
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }



    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(AppCompatActivity activity,
                    Toolbar tb,
                    final View parentContainer,
                    DrawerLayout drawerLayout) {
        mFragmentContainerView = parentContainer;
        this.parentActivity = activity;
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = parentActivity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                parentActivity,                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                tb,             /* Toolbar to host navigation in */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                parentActivity.supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    MetaSettings.putUserLearnedDrawer(parentActivity, true);
                }

                listAdapter.notifyDataSetChanged();
                parentActivity.supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationCallbacks.");
        }

        try {
            parentActivity = (AppCompatActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must be an instance of AppCompatActivity.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
