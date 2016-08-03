package com.caseybrooks.common.features.verses;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.AbstractVerse;
import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.providers.simple.SimplePassage;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.fragment.AppFeature;
import com.caseybrooks.common.app.fragment.FragmentBase;
import com.caseybrooks.common.widget.FlowLayoutManager;

public class EditVerseFragment extends FragmentBase {
    public static EditVerseFragment newInstance(AbstractVerse passage) {
        EditVerseFragment fragment = new EditVerseFragment();
        Bundle args = new Bundle();
        String ref = passage.getReference().toString();
        String text = passage.getFormattedText();
        args.putString("reference", ref);
        args.putString("text", text);
        args.putString("id", passage.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Edit, 0);
    }


//Data Members
//--------------------------------------------------------------------------------------------------
    SimplePassage passage;
    TextView reference;
    EditText verseText;

    RecyclerView tagsList;
    TagAdapter tagAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getArguments() != null) {
            Reference ref = new Reference.Builder().parseReference(getArguments().getString("reference")).create();
            passage = new SimplePassage(ref);
            passage.setText(getArguments().getString("text"));
            passage.setId(getArguments().getString("id"));
        }

        View view = inflater.inflate(R.layout.fragment_editverse, container, false);

        reference = (TextView) view.findViewById(R.id.reference);
        verseText = (EditText) view.findViewById(R.id.verseText);

        reference.setText(passage.getReference().toString());
        verseText.setText(passage.getText());

        for(int i = 0; i < 20; i++) {
            passage.addTag(new Tag("Tag " + i));
        }

        tagsList = (RecyclerView) view.findViewById(R.id.tagsList);
        tagsList.setLayoutManager(new FlowLayoutManager(getContext()));
        tagAdapter = new TagAdapter();
        tagsList.setAdapter(tagAdapter);

        return view;
    }


//Multiple Choice Adapter
//--------------------------------------------------------------------------------------------------
    private class TagViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public TagViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.chip);
        }

        public void onBind(final Tag tag) {
            text.setText(tag.name);
        }
    }

    private class TagAdapter extends RecyclerView.Adapter<TagViewHolder> {

        public TagAdapter() {
        }

        @Override
        public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chip, null, false);
            return new TagViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(TagViewHolder holder, int position) {
            holder.onBind(passage.getTags()[position]);
        }

        @Override
        public int getItemCount() {
            return passage.getTags().length;
        }
    }
}
