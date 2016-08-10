package com.caseybrooks.common.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
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
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.util.HeaderDecoration;
import com.caseybrooks.common.util.Util;

import java.util.List;

public class ExpandableNavigationView extends LinearLayout {
//Data Members
//--------------------------------------------------------------------------------------------------
    RecyclerView recyclerView;
    NavExpandableRecyclerViewAdapter adapter;
    OnExpandableNavigationItemSelectedListener listener;

    DrawerFeature selectedFeature;

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

    public void setDrawerFeatures(List<DrawerFeature> items) {
        adapter = new NavExpandableRecyclerViewAdapter(items);
        recyclerView.setAdapter(adapter);
    }

    public void setExpandableNavigationItemSelectedListener(OnExpandableNavigationItemSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedFeature(DrawerFeature selectedFeature) {
        this.selectedFeature = selectedFeature;
        adapter.notifyDataSetChanged();
    }

//Parent views
//--------------------------------------------------------------------------------------------------

    private class NavParentViewHolder extends ParentViewHolder {
        public View root;
        public ImageView itemIcon;
        public TextView itemText;
        DrawerFeature parent;

        public NavParentViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            itemIcon = (ImageView) itemView.findViewById(R.id.itemIcon);
            itemText = (TextView) itemView.findViewById(R.id.itemText);
        }

        public void onBind(Object object) {
            parent = (DrawerFeature) object;
//            ColorStateList textColor = getResources().getColorStateList(R.color.text_color_primary_light);

            if(parent.equals(selectedFeature))
                root.setSelected(true);
            else
                root.setSelected(false);

            if(!TextUtils.isEmpty(parent.getTitle()))
                itemText.setText(parent.getTitle());
            else
                itemText.setText(null);

            if(parent.getIcon() != 0)
                itemIcon.setImageResource(parent.getIcon());
        }

        @Override
        public void onClick(View v) {
            if(parent.getChildItemList() != null && parent.getChildItemList().size() > 0)
                super.onClick(v);
            else if(listener != null)
                listener.selectFeature(parent);
        }
    }

//Child views
//--------------------------------------------------------------------------------------------------

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
            final DrawerFeature child = (DrawerFeature) object;

            if(child.equals(selectedFeature))
                root.setSelected(true);
            else
                root.setSelected(false);

            if(!TextUtils.isEmpty(child.getTitle()))
                subitemText.setText(child.getTitle());
            else
                subitemText.setText(null);

            if(child.getCount() != 0)
                subitemIconText.setText(Integer.toString(child.getCount()));
            else
                subitemIconText.setText(null);

            if(child.getCount() != 0)
                subitemIcon.setImageResource(child.getCount());
            else
                subitemIcon.setImageResource(R.drawable.ic_circle);

            boolean shouldUseLightFont;
            if(child.getColor() != 0) {
                subitemIcon.setColorFilter(child.getColor(), PorterDuff.Mode.SRC_IN);
                shouldUseLightFont = Util.shouldUseLightFont(child.getColor());
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
                        listener.selectFeature(child);
                }
            });
        }
    }

//Complete Adapter
//--------------------------------------------------------------------------------------------------

    public class NavExpandableRecyclerViewAdapter extends ExpandableRecyclerAdapter<NavParentViewHolder, NavChildViewHolder> {
        public NavExpandableRecyclerViewAdapter(@NonNull List<? extends ParentListItem> parentItemList) {
            super(parentItemList);
        }

        @Override
        public NavParentViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.nav_parent_view, viewGroup, false);
            return new NavParentViewHolder(view);
        }

        @Override
        public NavChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.nav_child_view, viewGroup, false);
            return new NavChildViewHolder(view);
        }

        @Override
        public void onBindParentViewHolder(NavParentViewHolder parentViewHolder, int position, ParentListItem parentListItem) {
            parentViewHolder.onBind(parentListItem);
        }


        @Override
        public void onBindChildViewHolder(NavChildViewHolder navChildViewHolder, int i, Object o) {
            navChildViewHolder.onBind(o);
        }
    }

    public interface OnExpandableNavigationItemSelectedListener {
        void selectFeature(DrawerFeature feature);
    }
}
