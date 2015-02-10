package com.caseybrooks.androidbibletools.basic;

public class Tag implements Comparable<Tag> {
	public String name;
	public int id;
	public int color;
	public int count;

	public Tag() {
		this.name = null;
		this.id = 0;
		this.color = 0;
		this.count = 0;
	}

	public Tag(String name) {
		if(name != null && name.length() > 0) this.name = name;
		this.id = 0;
		this.color = 0;
		this.count = 0;
	}

	public Tag(String name, int id, int color, int count) {
		if(name != null && name.length() > 0) this.name = name;
		this.id = id;
		this.color = color;
		this.count = count;
	}

	@Override
	public int compareTo(Tag another) {
		return this.name.compareToIgnoreCase(another.name);
	}
}
