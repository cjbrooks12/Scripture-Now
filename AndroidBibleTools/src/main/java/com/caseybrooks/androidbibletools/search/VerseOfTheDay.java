package com.caseybrooks.androidbibletools.search;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.enumeration.Flags;
import com.caseybrooks.androidbibletools.enumeration.Version;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.EnumSet;

public class VerseOfTheDay {
	public static Passage retrieve(Version version) throws IOException {
		Document doc = Jsoup.connect("http://verseoftheday.com").get();

		Elements reference = doc.select("meta[property=og:title]");

		Passage passage = new Passage(version, reference.attr("content").substring(18));
		passage.retrieve();
		passage.setFlags(EnumSet.of(Flags.TEXT_NORMAL));

		return passage;
	}


}
