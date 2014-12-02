package com.caseybrooks.androidbibletools.basic;

import com.caseybrooks.androidbibletools.enumeration.Flags;
import com.caseybrooks.androidbibletools.enumeration.Version;

import java.io.IOException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.TreeSet;

public abstract class AbstractVerse implements Comparable<AbstractVerse> {
	//Data Members
//------------------------------------------------------------------------------
	protected Version version;
	protected long id;
	protected TreeSet<String> tags;
	protected EnumSet<Flags> flags;
    protected long millis;
    protected int state;
    protected boolean checked;

	public AbstractVerse() {
		version = Version.KJV;
		id = 0;
		tags = new TreeSet<String>();
		flags = EnumSet.of(Flags.TEXT_NORMAL);
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

	public AbstractVerse setFlags(EnumSet<Flags> flags) {
		this.flags = flags;
		return this;
	}

	public AbstractVerse setFlag(Flags flag, boolean status) {
		//Whether to include the verse number
		if (flag.equals(Flags.PRINT_VERSE_NUMBER)) {
			if (status) {
				flags.add(Flags.PRINT_VERSE_NUMBER);
			}
			else {
				flags.remove(Flags.PRINT_VERSE_NUMBER);
			}
		}

		//To format how the number is shown
		if (flag.equals(Flags.NUMBER_PLAIN)
					|| flag.equals(Flags.NUMBER_DOT)
					|| flag.equals(Flags.NUMBER_PARENTHESIS)
					|| flag.equals(Flags.NUMBER_DOUBLE_PARENTHESIS)) {

			if(status) {
				flags.remove(Flags.NUMBER_PLAIN);
				flags.remove(Flags.NUMBER_DOT);
				flags.remove(Flags.NUMBER_PARENTHESIS);
				flags.remove(Flags.NUMBER_DOUBLE_PARENTHESIS);

				flags.add(flag);
			}
			else {
				flags.remove(flag);
			}
		}

		//To format how the getText is shown
		if (flag.equals(Flags.TEXT_NORMAL)
					|| flag.equals(Flags.TEXT_DASHES)
					|| flag.equals(Flags.TEXT_LETTERS)
					|| flag.equals(Flags.TEXT_DASHED_LETTERS)) {

			if(status) {
				flags.remove(Flags.TEXT_NORMAL);
				flags.remove(Flags.TEXT_DASHES);
				flags.remove(Flags.TEXT_LETTERS);
				flags.remove(Flags.TEXT_DASHED_LETTERS);

				flags.add(flag);
			}
			else {
				flags.remove(flag);
			}
		}

		//Whether to include a newline character after the verse getText
		if (flag.equals(Flags.PRINT_NEWLINE)) {
			if (status) {
				flags.add(Flags.PRINT_NEWLINE);
			}
			else {
				flags.remove(Flags.PRINT_NEWLINE);
			}
		}

		return this;
	}

	public EnumSet<Flags> getFlags() {
		return flags;
	}

//Abstract Methods
//------------------------------------------------------------------------------
	public abstract String getReference();
	public abstract String getText();
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
			return lhs.getReference().compareToIgnoreCase(rhs.getReference());
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