package com.caseybrooks.androidbibletools.data;

import java.util.TreeSet;

/** Class used to store, categorize, and retrieve tags on a Verse. Supports
 * an arbitrary number of unique tags, and these tags can be used to find or sort
 * Verses in a list structure
 */
public class Tags {
    TreeSet<String> tags;

    public Tags() {
        tags = new TreeSet<String>();
    }

    public boolean containsTag(String tag) { return tags.contains(tag); }
    public void addTag(String... tags) {
        for(String tag : tags) {
            this.tags.add(tag);
        }
    }
    public boolean removeTag(String tag) { return tags.remove(tag); }
    public void clear() { tags.clear(); }
    public String[] getTags() {
        String[] tagsArray = new String[tags.size()];
        tags.toArray(tagsArray);
        return tagsArray;
    }


}
