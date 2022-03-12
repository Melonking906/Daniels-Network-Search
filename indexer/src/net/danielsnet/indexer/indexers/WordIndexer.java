package net.danielsnet.indexer.indexers;

import net.danielsnet.indexer.Settings;
import net.danielsnet.indexer.StaticLists;
import net.danielsnet.indexer.UI;
import net.danielsnet.indexer.Utils;
import net.danielsnet.indexer.objects.Page;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.HashSet;

public class WordIndexer {
    private final Page page;

    public WordIndexer(Page page) {
        this.page = page;
    }

    public boolean index() {
        UI.log("WORD INDEXER: Starting word index for: " + page.getUrl());

        //Generate word list
        page.setBodyWords(uniqueWords(page.getText()));
        page.setTitleWords(uniqueWords(page.getTitle()));

        return true;
    }

    //Take a block of text and strip it down to a set of unique words.
    private HashSet<String> uniqueWords(String text) {
        UrlValidator urlValidator = UrlValidator.getInstance();
        HashSet<String> uniqueWords = new HashSet<>();
        String[] words = text.split(" ");
        int wordCount = 0;

        for (String word : words) {
            //Stop if page has too many words!
            if (wordCount > Settings.MAX_WORDS_PER_PAGE) {
                return uniqueWords;
            }
            wordCount++;

            //Skip urls/
            if (urlValidator.isValid(word)) {
                continue;
            }

            //Skip refrences to files. -- eek could be missing stuff but better not to spam system.
            if (word.contains(".")) {
                continue;
            }

            //Process the word
            word = word.trim();
            word = word.toLowerCase();
            word = Utils.filterNonAscii(word);

            // Skip words that only contain numbers
            if (word.matches("[0-9]+")) {
                continue;
            }

            //Remove words that should not be indexed.
            if (StaticLists.isFilterWord(word)) {
                continue;
            }

            //Convert common spelling differences
            word = StaticLists.correctWord(word);

            //Add the word
            uniqueWords.add(word);
        }
        return uniqueWords;
    }
}