package com.caseybrooks.androidbibletools.search;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.enumeration.Version;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;

public class VerseOfTheDay {
	public static Passage retrieve(Version version) throws IOException {
		Document doc = Jsoup.connect("http://verseoftheday.com").get();

		Elements reference = doc.select("meta[property=og:title]");

        try {
            Passage passage = new Passage(reference.attr("content").substring(18));
            passage.setVersion(version);
            passage.retrieve();
            return passage;
        }
        catch(ParseException e) {
            e.printStackTrace();
            return null;
        }
	}


}
