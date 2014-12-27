package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;
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

    RelativeLayout header;

    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private ActionBarActivity parentActivity;

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

        // Select either the default item (0, 0) or the last selected item.

        NavListItem item = new NavListItem();
        item.groupPosition = 0;
        item.childPosition = 0;
        selectItem(item);
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

    public static class NavListItem {
        public int groupPosition;
        public int childPosition;
        public String name;
        public int id;
        public int count;
        public int color;
    }

    private void populateList() {
        header = (RelativeLayout) LayoutInflater.from(parentActivity).inflate(R.layout.nav_drawer_header, null);

        mDrawerListView.addParallaxedHeaderView(header);

        List<String> listDataHeader = new ArrayList<String>();
        HashMap<String, List<Integer>> listDataChild = new HashMap<String, List<Integer>>();

        //set the headers for top-level navigation
        listDataHeader.add("Dashboard");
        listDataHeader.add("Discover");
        listDataHeader.add("Memorization State");
        listDataHeader.add("Tags");
        listDataHeader.add("Settings");
        listDataHeader.add("Help & Feedback");

        // set dashboard subitems (none)
        VerseDB db = new VerseDB(parentActivity).open();
        listDataChild.put(listDataHeader.get(0), new ArrayList<Integer>());

        // set Discover subitems (Topical Bible and Import Verses)
        List<Integer> discoverItems = new ArrayList<Integer>();
        discoverItems.add(0);
        discoverItems.add(1);
        listDataChild.put(listDataHeader.get(1), discoverItems);

        // set Memorization State subitems (each state, plus all current and all verses
        List<Integer> states = new ArrayList<Integer>();
        states.add(VerseDB.ALL_VERSES);
        states.add(VerseDB.CURRENT);
        states.add(VerseDB.MEMORIZED);
        states.add(VerseDB.CURRENT_ALL);
        states.add(VerseDB.CURRENT_MOST);
        states.add(VerseDB.CURRENT_SOME);
        states.add(VerseDB.CURRENT_NONE);
        listDataChild.put(listDataHeader.get(2), states);

        // set Tags subitems (each tag, plus untagged)
        //TODO: set way to get all untagged verses as a list
        int[] tagIds = db.getAllTagIds();
        List<Integer> tags = new ArrayList<Integer>();
        if(tagIds != null && tagIds.length > 0) {
            for (int id : tagIds) {
                tags.add(id);
            }
        }
        listDataChild.put(listDataHeader.get(3), tags);

        // set Settings subitems (none)
        listDataChild.put(listDataHeader.get(4), new ArrayList<Integer>());

        //set Help subitems (none)
        listDataChild.put(listDataHeader.get(5), new ArrayList<Integer>());

        db.close();

        listAdapter = new ExpandableListAdapter(parentActivity, listDataHeader, listDataChild);

        // setting list adapter
        mDrawerListView.setAdapter(listAdapter);
        mDrawerListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                selectItem((NavListItem)parent.getExpandableListAdapter().getChild(groupPosition, childPosition));

                return false;
            }
        });

        mDrawerListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                if(groupPosition == 0 || groupPosition == 4 || groupPosition == 5) {
                    NavListItem item = new NavListItem();
                    item.name = (String)parent.getExpandableListAdapter().getGroup(groupPosition);
                    item.groupPosition = groupPosition;
                    selectItem(item);
                    return true;
                }

                return false;
            }
        });
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private List<String> headerItems; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<Integer>> childItems;

        public ExpandableListAdapter(
                Context context,
                List<String> headerItems,
                HashMap<String, List<Integer>> childItems) {

            this.context = context;
            this.headerItems = headerItems;
            this.childItems = childItems;
        }

        @Override
        public void notifyDataSetChanged() {
            VerseDB db = new VerseDB(context).open();
            int[] tagIds = db.getAllTagIds();
            List<Integer> tags = new ArrayList<Integer>();
            if(tagIds != null && tagIds.length > 0) {
                for (int id : tagIds) {
                    tags.add(id);
                }
            }
            childItems.remove(headerItems.get(3));
            childItems.put(headerItems.get(3), tags);
            db.close();

            super.notifyDataSetChanged();
        }

        @Override
        public NavListItem getChild(int groupPosition, int childPosition) {
            int id = childItems.get(headerItems.get(groupPosition))
                    .get(childPosition);

            NavListItem item = new NavListItem();
            item.id = id;
            item.groupPosition = groupPosition;
            item.childPosition = childPosition;
            VerseDB db = new VerseDB(parentActivity).open();
            if(groupPosition == 0) {
                item.name = headerItems.get(groupPosition);
            }
            else if(groupPosition == 1) {
                if(childPosition == 0) {
                    item.name = "Topical Bible";
                    item.color = getResources().getColor(R.color.open_bible_brown);
                }
                else {
                    item.name = "Import Verses";
                    item.color = getResources().getColor(R.color.all_verses);
                }
            }
            else if(groupPosition == 2) {
                item.name = db.getStateName(item.id);
                item.color = db.getStateColor(item.id);
                item.count = db.getStateCount(item.id);
            }
            else if(groupPosition == 3) {
                item.name = db.getTagName(item.id);
                item.color = db.getTagColor(db.getTagName(item.id));
                item.count = db.getTagCount(item.id);
            }
            else if(groupPosition == 4) {
                item.name = headerItems.get(groupPosition);
            }

            db.close();

            return item;
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
                LayoutInflater infalInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.nav_list_subitem, null);
            }

            NavListItem item = getChild(groupPosition, childPosition);

            TextView childText = (TextView) convertView.findViewById(R.id.subitemText);
            childText.setText(item.name);

            ImageView tagCircle = (ImageView) convertView.findViewById(R.id.subitemCircle);
            tagCircle.setBackgroundDrawable(Util.Drawables.circle(item.color));

            TextView tagCircleCount = (TextView) convertView.findViewById(R.id.subitemCircleText);
            if(groupPosition == 1) {
                tagCircleCount.setText("");
            }
            else {
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
                convertView = LayoutInflater.from(context).inflate(R.layout.nav_list_header, null);
            }

            String headerTitle = getGroup(groupPosition);
            TextView header = (TextView) convertView.findViewById(R.id.navListHeader);
            if(groupPosition == 3) {
                header.setText(headerTitle + " (" + getChildrenCount(groupPosition) + ")");
            }
            else {
                header.setText(headerTitle);
            }

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
    public void setUp(ActionBarActivity activity,
                    Toolbar tb,
                    View parentContainer,
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

    private void selectItem(NavListItem item) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(item);
        }
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
            parentActivity = (ActionBarActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must be an instance of ActionBarActivity.");
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
