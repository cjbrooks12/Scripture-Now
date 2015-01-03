package com.caseybrooks.androidbibletools.basic;

import android.util.Log;

import com.caseybrooks.androidbibletools.data.Formatter;
import com.caseybrooks.androidbibletools.data.MetaData;
import com.caseybrooks.androidbibletools.data.Reference;
import com.caseybrooks.androidbibletools.defaults.DefaultFormatter;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
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
    protected MetaData metaData;
	protected TreeSet<String> tags;

	public AbstractVerse(Reference reference) {
		this.version = Version.KJV;
        this.reference = reference;
        this.formatter = new DefaultFormatter();
        this.metaData = new MetaData();
		this.tags = new TreeSet<String>();
	}

//Defined methods
//------------------------------------------------------------------------------
    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public boolean isChecked() {
        return metaData.getBoolean(DefaultMetaData.IS_CHECKED);
    }

    public void setChecked(boolean checked) {
        metaData.putBoolean(DefaultMetaData.IS_CHECKED, checked);
    }

    public AbstractVerse toggle() {
        setChecked(!isChecked());
        return this;
    }

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