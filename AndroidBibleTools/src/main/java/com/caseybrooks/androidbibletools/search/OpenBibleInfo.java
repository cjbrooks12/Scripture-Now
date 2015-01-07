package com.caseybrooks.androidbibletools.search;

import com.caseybrooks.androidbibletools.basic.Passage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class OpenBibleInfo {
    public static ArrayList<Passage> getVersesFromTopic(String topic) throws IOException {
        ArrayList<Passage> verses = new ArrayList<Passage>();

        String query = "http://www.openbible.info/topics/" + topic.trim().replaceAll(" ", "_");

        Document doc = Jsoup.connect(query).get();
        Elements passages = doc.select(".verse");

        for(Element element : passages) {
            try {
                Passage passage = new Passage(element.select(".bibleref").first().ownText());
                passage.setText(element.select("p").get(1).text());

                String notesString = element.select(".note").get(0).ownText();
                passage.getMetadata().putInt("UPVOTES", Integer.parseInt(notesString.replaceAll("\\D", "")));
                passage.getMetadata().putString("SEARCH_TERM", topic.trim());

                verses.add(passage);
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return verses;
    }

    public static  ArrayList<String> getSuggestions(char letter) throws IOException {
        ArrayList<String> verses = new ArrayList<String>();

        String query = "http://www.openbible.info/topics/" + letter;

        Document doc = Jsoup.connect(query).get();
        Elements passages = doc.select("li");

        for (Element element : passages) {
            verses.add(element.text());
        }

        return verses;
    }
}
