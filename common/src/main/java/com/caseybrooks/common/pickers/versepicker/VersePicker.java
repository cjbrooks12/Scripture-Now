package com.caseybrooks.common.pickers.versepicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.caseybrooks.common.pickers.referencepicker.ReferencePicker;

public class VersePicker extends RelativeLayout {
//Data Members
//------------------------------------------------------------------------------
	Context context;

	ReferencePicker referencePicker;
	VerseView verseView;

	ImageView checkReference;
	ImageView lookupText;
	ImageView saveVerse;
	ImageView showVersions;

//Constructors and Initialization
//------------------------------------------------------------------------------
	public VersePicker(Context context) {
		super(context);
		this.context = context;

		initialize();
	}

	public VersePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		initialize();
	}
	public void initialize() {
//		LayoutInflater.from(context).inflate(R.layout.verse_picker, this);

//		referencePicker = (ReferencePicker) findViewById(R.id.reference_picker);
//		verseView = (VerseView) findViewById(R.id.verse_view);
//
//		checkReference = (ImageView) findViewById(R.id.check_reference_button);
//		checkReference.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				referencePicker.checkReference();
//			}
//		});
//
//		lookupText = (ImageView) findViewById(R.id.lookup_text_button);
//		lookupText.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if(referencePicker.checkReference()) {
//					referencePicker.getSelectedBible();
//					Reference ref = referencePicker.getReference();
//					ABSPassage passage = new ABSPassage(
//							getResources().getString(R.string.bibles_org_key),
//							ref
//					);
//					verseView.getSelectedBible();
//					verseView.setVerse(passage);
//					verseView.tryCacheOrDownloadText();
//				}
//			}
//		});
//
//		saveVerse = (ImageView) findViewById(R.id.save_verse_button);
//		saveVerse.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Passage passage = new Passage(referencePicker.getReference());
//				passage.setText(verseView.getText().toString());
//
//			}
//		});
//
//		showVersions = (ImageView) findViewById(R.id.show_versions_button);
//		showVersions.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				BiblePickerDialog.create(context).show();
//			}
//		});
	}
}
