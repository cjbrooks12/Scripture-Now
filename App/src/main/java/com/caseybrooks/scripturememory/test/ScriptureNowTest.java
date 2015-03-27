package com.caseybrooks.scripturememory.test;

import android.test.InstrumentationTestCase;

public class ScriptureNowTest extends InstrumentationTestCase {

	public void testRandomness() {
//		int numTrials = 0;
//		int testCount = 10000;
//
//		int allVersesCount = 0;
//		int memorizedCount = 0;
//		int currentCount = 0;
//		int currentAllCount = 0;
//		int currentMostCount = 0;
//		int currentSomeCount = 0;
//		int currentNoneCount = 0;
//
//		while(testCount > 0) {
//			Passage passage = ScriptureNow.getNextStateVerse(getInstrumentation().getContext());
//			int state = passage.getMetadata().getInt(DefaultMetaData.STATE);
//
//			switch(state) {
//				case VerseDB.ALL_VERSES: allVersesCount++;
//				case VerseDB.MEMORIZED: memorizedCount++;
//				case VerseDB.CURRENT: currentCount++;
//				case VerseDB.CURRENT_ALL: currentAllCount++;
//				case VerseDB.CURRENT_MOST: currentMostCount++;
//				case VerseDB.CURRENT_SOME: currentSomeCount++;
//				case VerseDB.CURRENT_NONE: currentNoneCount++;
//			}
//
//
//			numTrials++;
//			testCount--;
//		}
//
//		float error = 0.05f;
//
//		assertTrue((allVersesCount / testCount) < (ScriptureNow.allVersesShare - 0.0 + error));
	}

}
