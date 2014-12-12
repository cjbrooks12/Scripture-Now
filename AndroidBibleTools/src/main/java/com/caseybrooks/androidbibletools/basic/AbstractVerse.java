package com.caseybrooks.androidbibletools.basic;

import com.caseybrooks.androidbibletools.enumeration.Version;

import java.io.IOException;
import java.util.Comparator;
import java.util.TreeSet;

public abstract class AbstractVerse implements Comparable<AbstractVerse> {
	//Data Members
//------------------------------------------------------------------------------
	protected Version version;
    protected Reference reference;
    protected Formatter formatter;
	protected long id;

	protected TreeSet<String> tags;
    protected long millis;
    protected int state;
    protected boolean checked;

	public AbstractVerse(Reference reference) {
		version = Version.KJV;
        this.reference = reference;
        formatter = new DefaultFormatter();

		id = 0;
		tags = new TreeSet<String>();
	}

	//Defined methods
//------------------------------------------------------------------------------
	public AbstractVerse setVersion(Version version) {
		this.version = version;
		return this;
	}

	public long getMillis() {
		return millis;
	}

    public AbstractVerse setMillis(long millis) {
        this.millis = millis;
        return this;
    }

    public int getState() {
        return state;
    }

    public AbstractVerse setState(int state) {
        this.state = state;
        return this;
    }

    public AbstractVerse setFormatter(Formatter formatter) {
        this.formatter = formatter;
        return this;
    }

    public boolean isChecked() {
        return checked;
    }

    public AbstractVerse setChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    public AbstractVerse toggle() {
        setChecked(!checked);
        return this;
    }

    public Version getVersion() {
        return version;
    }

	public AbstractVerse setId(long id) {
		this.id = id;
		return this;
	}

	public long getId() {
		return id;
	}

    public AbstractVerse removeAllTags() {
        tags.clear();

        return this;
    }

	public AbstractVerse setTags(String... tags) {
		for (String item : tags) {
			this.tags.add(item);
		}
		return this;
	}

	public AbstractVerse addTag(String tag) {
		this.tags.add(tag);
		return this;
	}

	public boolean containsTag(String tag) {
		return this.tags.contains(tag);
	}

	public String[] getTags() {
		String[] tags = new String[this.tags.size()];
		this.tags.toArray(tags);
		return tags;
	}

//Abstract Methods
//------------------------------------------------------------------------------
	public Reference getReference() { return reference; };
	public abstract String getText();
    public abstract String getURL();
	public abstract AbstractVerse retrieve() throws IOException;


//Comparison methods
//------------------------------------------------------------------------------
	public abstract int compareTo(AbstractVerse verse);
	public abstract boolean equals(AbstractVerse verse);

	public static class IDComparator implements Comparator<AbstractVerse> {
		@Override
		public int compare(AbstractVerse lhs, AbstractVerse rhs) {
			return (int)(lhs.getId() - rhs.getId());
		}
	}

	public static class AlphabeticalReferenceComparator implements Comparator<AbstractVerse> {
		@Override
		public int compare(AbstractVerse lhs, AbstractVerse rhs) {
			return lhs.getReference().toString().compareToIgnoreCase(rhs.getReference().toString());
		}
	}

	//Sorts Verses by their tags. Verses without tags will always be first,
	//  Verses with tags will be sorted by their first tag, which is already
	//  sorted to be the least tag.
	public static class TagComparator implements Comparator<AbstractVerse> {
		@Override
		public int compare(AbstractVerse lhs, AbstractVerse rhs) {
			if(lhs.getTags().length == 0) {
				return -2147483647;
			}
			else if(rhs.getTags().length == 0) {
				return -2147483647;
			}
			else {
				return lhs.getTags()[0].compareTo(rhs.getTags()[0]);
			}
		}
	}
}