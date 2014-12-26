package com.caseybrooks.androidbibletools.container;

import com.caseybrooks.androidbibletools.basic.AbstractVerse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/** Simple container for Verse objects. Groups multiple verses together and
 *  prints them all with the same flags, but carries no other restrictions. Will
 *  eventually become base class for more specialized containers.
 */
public class Verses<T extends AbstractVerse> {
//Data Members
//------------------------------------------------------------------------------
    protected ArrayList<T> verses;

//Constructors
//------------------------------------------------------------------------------
    public Verses() {
        this.verses = new ArrayList<T>();
    }



//Getters and Setters
//------------------------------------------------------------------------------
    public boolean add(T t) {
        return verses.add(t);
    }

    public boolean addAll(Collection<T> collection) {
        return verses.addAll(collection);
    }

    public void clear() {
        verses.clear();
    }

    public boolean contains(T t) {
        return verses.contains(t);
    }

    public boolean containsAll(Collection<T> collection) {
        return verses.containsAll(collection);
    }

    public boolean isEmpty() {
        return verses.isEmpty();
    }

    public Iterator<T> iterator() {
        return verses.iterator();
    }

    public boolean removeAll(Collection<T> collection) {
        return false;
    }

    public boolean retainAll(Collection<T> collection) {
        return verses.retainAll(collection);
    }

    public boolean remove(T t) {
        return verses.remove(t);
    }

    public T get(int i) {
        return verses.get(i);
    }

    public int size() {
        return verses.size();
    }

    public T[] toArray(T[] array) {
		return verses.toArray(array);
    }

//Sorting methods
    public void sort() {
        Collections.sort(verses);
    }

    public void sortByTag() {
        Collections.sort(verses, new T.TagComparator());
    }

    public void sortAlphabetical() {
        Collections.sort(verses, new T.AlphabeticalReferenceComparator());
    }

    public void sortByID() {
        Collections.sort(verses, new T.IDComparator());
    }
}
