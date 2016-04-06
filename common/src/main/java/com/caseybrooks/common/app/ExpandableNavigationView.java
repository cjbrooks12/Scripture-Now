package com.caseybrooks.common.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
import com.caseybrooks.common.util.HeaderDecoration;
import com.caseybrooks.common.R;
import com.caseybrooks.common.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ExpandableNavigationView extends LinearLayout {
//Data Members
//--------------------------------------------------------------------------------------------------
    RecyclerView recyclerView;
    NavExpandableRecyclerViewAdapter adapter;
    OnExpandableNavigationItemSelectedListener listener;

    AppFeature selectedFeature;
    int selectedFeatureId;

//Constructors and Initialization
//--------------------------------------------------------------------------------------------------
    public ExpandableNavigationView(Context context) {
        super(context);

        initialize(null);
    }

    public ExpandableNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize(attrs);
    }

    public void initialize(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.expandable_navigationview, this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), VERTICAL, false));

        HeaderDecoration.Builder builder = new HeaderDecoration.Builder(getContext());
        builder.dropShadowDp(2);
        builder.inflate(R.layout.drawer_header_view);
        builder.parallax(0.5f);
        builder.scrollsHorizontally(false);

        recyclerView.addItemDecoration(builder.build());
    }

    public void setContent(List<NavParentItem> items) {
        List<ParentObject> objects = new ArrayList<>();
        objects.addAll(items);
        adapter = new NavExpandableRecyclerViewAdapter(getContext(), objects);

        recyclerView.setAdapter(adapter);
    }

    public void setExpandableNavigationItemSelectedListener(OnExpandableNavigationItemSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedFeature(AppFeature selectedFeature, int selectedFeatureId) {
        this.selectedFeature = selectedFeature;
        this.selectedFeatureId = selectedFeatureId;
        adapter.notifyDataSetChanged();
    }

    //Parent views
//--------------------------------------------------------------------------------------------------

    public static class NavParentItem implements ParentObject {
        public String itemText;
        public int itemIconResId;
        public List<Object> children;
        public AppFeature appFeature;

        @Override
        public List<Object> getChildObjectList() {
            return children;
        }

        @Override
        public void setChildObjectList(List<Object> list) {
            children = list;
        }

        public void setNavChildItemList(List<NavChildItem> list) {
            children = new ArrayList<>();
            children.addAll(list);
        }
    }

    private class NavParentViewHolder extends ParentViewHolder {
        public View root;
        public ImageView itemIcon;
        public TextView itemText;
        NavParentItem parent;

        public NavParentViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            itemIcon = (ImageView) itemView.findViewById(R.id.itemIcon);
            itemText = (TextView) itemView.findViewById(R.id.itemText);
        }

        public void onBind(Object object) {
            parent = (NavParentItem) object;
            ColorStateList textColor = getResources().getColorStateList(R.color.text_color_primary_light);
//            itemText.setTextColor(textColor);




            if(parent.appFeature == selectedFeature)
                root.setSelected(true);
            else
                root.setSelected(false);

            if(!TextUtils.isEmpty(parent.itemText))
                itemText.setText(parent.itemText);
            else
                itemText.setText(null);

            if(parent.itemIconResId != 0)
                itemIcon.setImageResource(parent.itemIconResId);
        }

        @Override
        public void onClick(View v) {
            if(parent.appFeature.hasChildren())
                super.onClick(v);
            else if(listener != null)
                listener.onParentSelected(parent.appFeature);
        }
    }

//Child views
//--------------------------------------------------------------------------------------------------

    public static class NavChildItem {
        public String subitemText;
        public int subitemCount;
        public int subitemIcon;
        public int subitemIconColor;
        public AppFeature appFeature;
        public int appFeatureId;
    }

    public class NavChildViewHolder extends ChildViewHolder {
        public View root;
        public TextView subitemText;
        public TextView subitemIconText;
        public ImageView subitemIcon;

        public NavChildViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            subitemText = (TextView) itemView.findViewById(R.id.subitemText);
            subitemIconText = (TextView) itemView.findViewById(R.id.subitemCircleText);
            subitemIcon = (ImageView) itemView.findViewById(R.id.subitemCircle);
        }

        public void onBind(Object object) {
            final NavChildItem child = (NavChildItem) object;

            if(child.appFeature == selectedFeature && child.appFeatureId == selectedFeatureId)
                root.setSelected(true);
            else
                root.setSelected(false);

            if(!TextUtils.isEmpty(child.subitemText))
                subitemText.setText(child.subitemText);
            else
                subitemText.setText(null);

            if(child.subitemCount != 0)
                subitemIconText.setText(Integer.toString(child.subitemCount));
            else
                subitemIconText.setText(null);

            if(child.subitemIcon != 0)
                subitemIcon.setImageResource(child.subitemIcon);
            else
                subitemIcon.setImageResource(R.drawable.ic_circle);

            boolean shouldUseLightFont;
            if(child.subitemIconColor != 0) {
                subitemIcon.setColorFilter(child.subitemIconColor, PorterDuff.Mode.SRC_IN);
                shouldUseLightFont = Util.shouldUseLightFont(child.subitemIconColor);
            }
            else {
                subitemIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                shouldUseLightFont = true;
            }

            if(shouldUseLightFont) {
                ColorStateList textColor = getResources().getColorStateList(R.color.text_color_primary_dark);
                subitemIconText.setTextColor(textColor);
            }
            else {
                ColorStateList textColor = getResources().getColorStateList(R.color.text_color_primary_light);
                subitemIconText.setTextColor(textColor);
            }

            root.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                        listener.onChildSelected(child.appFeature, child.appFeatureId);
                }
            });
        }


    }

//Complete Adapter
//--------------------------------------------------------------------------------------------------

    public class NavExpandableRecyclerViewAdapter extends ExpandableRecyclerAdapter<NavParentViewHolder, NavChildViewHolder> {
        public NavExpandableRecyclerViewAdapter(Context context, List<ParentObject> parentItemList) {
            super(context, parentItemList);
        }

        @Override
        public NavParentViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.nav_parent_view, viewGroup, false);
            return new NavParentViewHolder(view);
        }

        @Override
        public NavChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.nav_child_view, viewGroup, false);
            return new NavChildViewHolder(view);
        }

        @Override
        public void onBindParentViewHolder(NavParentViewHolder navParentViewHolder, int i, Object o) {
            navParentViewHolder.onBind(o);
        }

        @Override
        public void onBindChildViewHolder(NavChildViewHolder navChildViewHolder, int i, Object o) {
            navChildViewHolder.onBind(o);
        }
    }

    public interface OnExpandableNavigationItemSelectedListener {
        void onParentSelected(AppFeature feature);
        void onChildSelected(AppFeature feature, int childId);
    }
}
