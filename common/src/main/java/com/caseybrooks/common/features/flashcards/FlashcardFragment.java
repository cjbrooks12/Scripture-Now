package com.caseybrooks.common.features.flashcards;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.fragment.ActivityBaseFragment;
import com.caseybrooks.common.app.fragment.AppFeature;
import com.caseybrooks.common.app.fragment.FragmentBase;
import com.caseybrooks.common.app.FeatureConfiguration;
import com.wenchao.cardstack.CardStack;

public class FlashcardFragment extends FragmentBase implements CardStack.CardEventListener {
    public static FlashcardFragment newInstance() {
        FlashcardFragment fragment = new FlashcardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static FeatureConfiguration getConfiguration() {
        return new FeatureConfiguration() {
            @Override
            public Pair<AppFeature, Integer> getFragmentFeature() {
                return new Pair<>(AppFeature.Flashcards, 0);
            }

            @Override
            public Class<? extends ActivityBaseFragment> getFragmentClass() {
                return FlashcardFragment.class;
            }

            @Override
            public String getTitle() {
                return "Flashcards";
            }
        };
    }

    public FeatureConfiguration getInstanceConfiguration() {
        return getConfiguration();
    }

// Data Members
//--------------------------------------------------------------------------------------------------

    private static class Flashcard {
        public String frontSide;
        public String backSide;
        public boolean isFlipped;
    }


    CardStack cardStack;
    CardsAdapter cardsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcards, container, false);

        cardStack = (CardStack) view.findViewById(R.id.cardStack);

        cardStack.setContentResource(R.layout.view_cardview);
        cardStack.setStackMargin(20);

        cardsAdapter = new CardsAdapter(getContext());

        for(int i = 0; i < 10; i++) {
            Flashcard flashcard = new Flashcard();
            flashcard.frontSide = "Item " + i;
            flashcard.backSide = "Longer text on the backside of item " + i;
            flashcard.isFlipped = false;

            cardsAdapter.add(flashcard);
        }

        cardStack.setAdapter(cardsAdapter);
        cardStack.setListener(this);

        return view;
    }


    public class CardsAdapter extends ArrayAdapter<Flashcard> {

        public CardsAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, final View contentView, ViewGroup parent) {
            FrameLayout layout = (FrameLayout) contentView.findViewById(R.id.main_content);

            TextView tv = new TextView(getContext());

            Flashcard flashcard = getItem(position);
            if(!flashcard.isFlipped) {
                tv.setText(flashcard.frontSide);
            }
            else {
                tv.setText(flashcard.backSide);
            }

            layout.addView(tv);

            return contentView;
        }
    }

    @Override
    public boolean swipeEnd(int direction, float distance) {
        return distance > (cardStack.getWidth() / 4);
    }

    @Override
    public boolean swipeStart(int direction, float distance) {

        return true;
    }

    @Override
    public boolean swipeContinue(int direction, float distanceX, float distanceY) {

        return true;
    }

    @Override
    public void discarded(int mIndex, int direction) {
        Flashcard flashcard = cardsAdapter.getItem(mIndex);

        if(direction == 0 || direction == 2) {
            Toast.makeText(getContext(), "Dismissed " + flashcard.frontSide + " to the left", Toast.LENGTH_SHORT).show();
        }
        else if(direction == 1 || direction == 3) {
            Toast.makeText(getContext(), "Dismissed " + flashcard.frontSide + " to the right", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void topCardTapped() {
        Flashcard flashcard = cardsAdapter.getItem(cardStack.getCurrIndex());
        flashcard.isFlipped = !flashcard.isFlipped;
        cardsAdapter.notifyDataSetChanged();
    }
}
