package com.caseybrooks.scripturememory.test;

import android.test.InstrumentationTestCase;

import com.caseybrooks.androidbibletools.basic.Verse;
import com.caseybrooks.androidbibletools.data.Metadata;
import com.caseybrooks.androidbibletools.data.Reference;
import com.caseybrooks.androidbibletools.enumeration.Book;

public class MetadataClassText extends InstrumentationTestCase {
    private class ComparableClass implements Comparable<ComparableClass> {
        public int value;

        public ComparableClass(int value) { this.value = value; }


        @Override
        public int compareTo(ComparableClass rhs) {
            return this.value - rhs.value;
        }
    }

    private class NonComparableClass {
        public int value;

        public NonComparableClass(int value) { this.value = value; }

        public int compareTo(ComparableClass rhs) {
            return this.value - rhs.value;
        }
    }


    public void testAddingKeys() throws Throwable {
        Verse verseA = new Verse(new Reference(Book.John, 3, 16));
        Metadata metadataA = new Metadata();
        metadataA.put("STRING", "value a"); //String is a comparable type
        metadataA.put("INTEGER", 0); //int is comparable
        metadataA.put("LONG", 0l); //long is comparable
        metadataA.put("BOOLEAN", true); //boolean is comparable
        metadataA.put("COMPARABLE_CLASS", new ComparableClass(0));
        try {
            //I expect this to throw an exception. If it does, catch it and continue.
            //If if does not, throw something that cannot be caught to indicate an error
            metadataA.put("NON_COMPARABLE_CLASS", new NonComparableClass(5));
            throw new Throwable();
        }
        catch(IllegalArgumentException iae) {

        }
        verseA.setMetadata(metadataA);

        //create a second Metadata object to test comparison of all types
        Verse verseB = new Verse(new Reference(Book.John, 3, 17));
        Metadata metadataB = new Metadata();
        metadataB.putString("STRING", "value b"); //String is a comparable type
        metadataB.putInt("INTEGER", 1); //int is comparable
        metadataB.putLong("LONG", 1l); //long is comparable
        metadataB.putBoolean("BOOLEAN", true); //boolean is comparable
        metadataB.put("COMPARABLE_CLASS", new ComparableClass(1));
        verseB.setMetadata(metadataB);


        //create a third Metadata object which has the same keys but different classes with those keys
        //to ensure that it won't compare Objects of different type
        Verse verseC = new Verse(new Reference(Book.John, 3, 18));
        Metadata metadataC = new Metadata();
        metadataC.putString("COMPARABLE_CLASS", "value c"); //String is a comparable type
        metadataC.putInt("STRING", 2); //int is comparable
        metadataC.putLong("INTEGER", 2l); //long is comparable
        metadataC.putBoolean("LONG", true); //boolean is comparable
        metadataC.put("BOOLEAN", new ComparableClass(2));
        verseC.setMetadata(metadataC);


        //Test comparison of these verses to see if it works correctly with arbitrary keys
        int testValue;

        testValue = new Metadata.Comparator("STRING").compare(verseA, verseB);
        if(testValue >= 0) throw new Throwable();
        testValue = new Metadata.Comparator("INTEGER").compare(verseA, verseB);
        if(testValue >= 0) throw new Throwable();
        testValue = new Metadata.Comparator("LONG").compare(verseA, verseB);
        if(testValue >= 0) throw new Throwable();
        testValue = new Metadata.Comparator("BOOLEAN").compare(verseA, verseB);
        if(testValue != 0) throw new Throwable();
        testValue = new Metadata.Comparator("COMPARABLE_CLASS").compare(verseA, verseB);
        if(testValue >= 0) throw new Throwable();

        try {
            //STRING key exists in both, but are of different types. I expect an exception to be
            //thrown, but if it doesn't, throw Throwable to ensure I detect the error
            testValue = new Metadata.Comparator("STRING").compare(verseA, verseC);
            throw new Throwable();
        }
        catch(ClassCastException cce) {

        }
        try {
            testValue = new Metadata.Comparator("INTEGER").compare(verseA, verseC);
            throw new Throwable();
        }
        catch(ClassCastException cce) {

        }
        try {
            testValue = new Metadata.Comparator("LONG").compare(verseA, verseC);
            throw new Throwable();
        }
        catch(ClassCastException cce) {

        }
        try {
            testValue = new Metadata.Comparator("BOOLEAN").compare(verseA, verseC);
            throw new Throwable();
        }
        catch(ClassCastException cce) {

        }
        try {
            testValue = new Metadata.Comparator("COMPARABLE_CLASS").compare(verseA, verseC);
            throw new Throwable();
        }
        catch(ClassCastException cce) {

        }
    }
}
