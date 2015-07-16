package com.caseybrooks.scripturememory.nowcards.input;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.androidbibletools.widget.EditVerse;
import com.caseybrooks.androidbibletools.widget.ReferencePicker;
import com.caseybrooks.androidbibletools.widget.ReferencePickerListener;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.VerseDB;

import java.util.ArrayList;

public class VerseInputCard extends FrameLayout {
//Data Members
//------------------------------------------------------------------------------
	Context context;

	ImageButton contextMenu;

	ReferencePicker referencePicker;
	EditVerse verseView;
	MultiAutoCompleteTextView editTags;

	ImageView checkReference;
	ImageView lookupText;
	ImageView saveVerse;
	ImageView showTags;

//Constructors and Initialization
//------------------------------------------------------------------------------
	public VerseInputCard(Context context) {
		super(context);
		this.context = context;

		initialize();
	}

	public VerseInputCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		initialize();
	}

	void initialize() {

		LayoutInflater.from(context).inflate(R.layout.card_verse_input, this);

		contextMenu = (ImageButton) findViewById(R.id.overflowButton);
    	contextMenu.setOnClickListener(contextMenuClick);

		referencePicker = (ReferencePicker) findViewById(R.id.reference_picker);
		referencePicker.setListener(new ReferencePickerListener() {
			@Override
			public void onPreParse(String textToParse) {

			}

			@Override
			public void onParseCompleted(Reference parsedReference, boolean wasSuccessful) {
				if(wasSuccessful) {
					final ABSPassage passage = new ABSPassage(
							getResources().getString(R.string.bibles_org_key),
							parsedReference
					);

					verseView.loadSelectedBible();
					verseView.setVerse(passage);
					verseView.tryCacheOrDownloadText();
				}
			}
		});

		verseView = (EditVerse) findViewById(R.id.verse_view);

		editTags = (MultiAutoCompleteTextView) findViewById(R.id.edit_tags);

		ArrayList<String> tags = new ArrayList<>();
		VerseDB db = new VerseDB(context).open();
		for(Tag tag : db.getAllTags()) {
			tags.add(tag.name);
		}
		db.close();

		ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(
				context,
				android.R.layout.simple_expandable_list_item_1,
				tags
		);
		editTags.setAdapter(tagsAdapter);
		editTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

		checkReference = (ImageView) findViewById(R.id.check_reference_button);
		checkReference.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				referencePicker.checkReference();
			}
		});

		lookupText = (ImageView) findViewById(R.id.lookup_text_button);
		lookupText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				referencePicker.checkReference();
			}
		});

		saveVerse = (ImageView) findViewById(R.id.save_verse_button);
		saveVerse.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Reference ref = referencePicker.getReference();

				VerseDB db = new VerseDB(context).open();
				if(db.getVerseId(ref) > 0) {
					referencePicker.setError("Verse is already saved");
				}
				else {
					Passage passage = new Passage(ref);
					passage.setText(verseView.getText().toString());
					db.insertVerse(passage);
					Toast.makeText(context, ref.toString() + " saved", Toast.LENGTH_SHORT).show();
					referencePicker.setText("");
					verseView.setText("");
				}

				db.close();
			}
		});

		showTags = (ImageView) findViewById(R.id.show_tags_button);
		showTags.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(editTags.getVisibility() == View.GONE) {
					editTags.setVisibility(View.VISIBLE);
				}
				else {
					editTags.setVisibility(View.GONE);
				}
			}
		});
	}

	public void removeFromParent() {
        setVisibility(View.GONE);
		((ViewGroup)getParent()).removeView(VerseInputCard.this);
	}

//Public Getters and Setters
//------------------------------------------------------------------------------
	public void setReference(String reference) {
	}

	public void setVerse(String verse) {
    }

//Context Menu
//------------------------------------------------------------------------------
	private OnClickListener contextMenuClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			removeFromParent();
		}
	};
}

