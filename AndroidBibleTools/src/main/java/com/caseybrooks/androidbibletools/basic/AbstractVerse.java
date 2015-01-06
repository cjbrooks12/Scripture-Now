package com.caseybrooks.androidbibletools.basic;

import com.caseybrooks.androidbibletools.data.Formatter;
import com.caseybrooks.androidbibletools.data.Metadata;
import com.caseybrooks.androidbibletools.data.Reference;
import com.caseybrooks.androidbibletools.defaults.DefaultFormatter;
import com.caseybrooks.androidbibletools.enumeration.Version;

import java.io.IOException;
import java.text.ParseException;
import java.util.TreeSet;

public abstract class AbstractVerse implements Comparable<AbstractVerse> {
	//Data Members
//------------------------------------------------------------------------------
	protected Version version;
    protected final Reference reference;
    protected Formatter formatter;
    protected Metadata metadata;
	protected TreeSet<String> tags;

	public AbstractVerse(Reference reference) {
		this.version = Version.KJV;
        this.reference = reference;
        this.formatter = new DefaultFormatter();
        this.metadata = new Metadata();
		this.tags = new TreeSet<String>();
	}

    public AbstractVerse(String reference) throws ParseException {
        this.version = Version.KJV;
        this.reference = new Reference(reference);
        this.formatter = new DefaultFormatter();
        this.metadata = new Metadata();
        this.tags = new TreeSet<String>();
    }

//Defined methods
//------------------------------------------------------------------------------
    public Version getVersion() { return version; }
    public void setVersion(Version version) { this.version = version; }
    public Reference getReference() { return reference; }
    public Formatter getFormatter() { return formatter; }
    public void setFormatter(Formatter formatter) { this.formatter = formatter; }
    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    public AbstractVerse removeAllTags() {
        tags.clear();

        return this;
    }

    public AbstractVerse removeTag(String tag) {
        if(tags.contains(tag)) tags.remove(tag);

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
//	public Reference getReference() { return reference; };
	public abstract String getText();
    public abstract String getURL();
	public abstract AbstractVerse retrieve() throws IOException;

//Comparison methods
//------------------------------------------------------------------------------
	public abstract int compareTo(AbstractVerse verse);
	public abstract boolean equals(AbstractVerse verse);
}