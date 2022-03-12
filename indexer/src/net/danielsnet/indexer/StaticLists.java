package net.danielsnet.indexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class StaticLists {
    // Words that are spelled differently and should be corrected.
    private static final HashMap<String, String> CORRECTED_WORDS = createCorrectedWords();
    // Words that should be filtered from most indexes
    private static final HashSet<String> FILTERED_WORDS = createFilteredWords();
    // Site categories
    private static final HashMap<String, String[]> CATEGORIES = createSiteCategories();
    // If the indexer sees one of these in a url it will discard the url.
    private static final HashSet<String> URL_PATHS_TO_IGNORE = createPathsToIgnore();
    // A complete list of all sites accepted, on neocities or in the submit bin.
    private static final HashSet<String> ALL_SITES_IN_SYSTEM = createAllSitesInSystem();

    private static HashMap<String, String> createCorrectedWords() {
        HashMap<String, String> correctedWords = new HashMap<>();
        String sql = "SELECT fromWord, toWord FROM meta_SpellingFilter;";
        HashSet<HashMap<String, String>> words = Main.getDatabase().query(sql, true);
        for (HashMap<String, String> word : words) {
            correctedWords.put(word.get("fromWord"), word.get("toWord"));
        }
        return correctedWords;
    }

    private static HashMap<String, String[]> createSiteCategories() {
        // Download Catagories and convert them to a list.
        HashMap<String, String[]> catagories = new HashMap<>();
        String sql = "SELECT * FROM meta_Catagories";
        HashSet<HashMap<String, String>> sqlCatagorys = Main.getDatabase().query(sql, true);
        for (HashMap<String, String> sqlCatagory : sqlCatagorys) {
            String masterWord = sqlCatagory.get("catagory");
            String[] otherWords = sqlCatagory.get("otherWords").split(",");
            catagories.put(masterWord, otherWords);
        }
        return catagories;
    }

    private static HashSet<String> createAllSitesInSystem() {
        String sql = "(SELECT url FROM data_Sites) UNION (SELECT url FROM submit_Sites) UNION (SELECT url FROM neocities_Sites)";
        HashSet<HashMap<String, String>> sqlURLs = Main.getDatabase().query(sql, true);
        return sqlURLs.stream().map(sqlURL -> sqlURL.get("url")).collect(Collectors.toCollection(HashSet::new));
    }

    private static HashSet<String> createPathsToIgnore() {
        HashSet<String> toIgnore = new HashSet<>();
        toIgnore.add("apidocs");
        toIgnore.add("policy");
        toIgnore.add("statement");
        toIgnore.add("terms");
        toIgnore.add("/m.");
        toIgnore.add("api");
        toIgnore.add("/vb/");
        toIgnore.add("forum");
        toIgnore.add("viewforum.php");
        toIgnore.add("bandcamp.com");
        toIgnore.add("google.com");
        toIgnore.add("github.com");
        toIgnore.add("/http"); //No URLS within urls
        return toIgnore;
    }

    private static HashSet<String> createFilteredWords() {
        HashSet<String> filteredWords = new HashSet<>();
        filteredWords.add("the");
        filteredWords.add("from");
        filteredWords.add("your");
        filteredWords.add("and");
        filteredWords.add("have");
        filteredWords.add("that");
        filteredWords.add("for");
        filteredWords.add("not");
        filteredWords.add("with");
        filteredWords.add("you");
        filteredWords.add("did");
        filteredWords.add("its");
        filteredWords.add("use");
        filteredWords.add("all");
        filteredWords.add("any");
        filteredWords.add("are");
        filteredWords.add("does");
        filteredWords.add("where");
        filteredWords.add("one");
        filteredWords.add("but");
        filteredWords.add("last");
        filteredWords.add("this");
        filteredWords.add("page");
        filteredWords.add("was");
        filteredWords.add("what");
        filteredWords.add("when");
        filteredWords.add("her");
        filteredWords.add("him");
        filteredWords.add("itself");
        filteredWords.add("most");
        filteredWords.add("here");
        filteredWords.add("can");
        filteredWords.add("also");
        filteredWords.add("sure");
        filteredWords.add("want");
        filteredWords.add("come");
        filteredWords.add("thats");
        filteredWords.add("youre");
        filteredWords.add("into");
        filteredWords.add("see");
        filteredWords.add("get");
        filteredWords.add("came");
        filteredWords.add("set");
        filteredWords.add("url");
        filteredWords.add("has");
        filteredWords.add("try");
        filteredWords.add("well");
        filteredWords.add("whats");
        filteredWords.add("need");
        filteredWords.add("youll");
        filteredWords.add("just");
        filteredWords.add("these");
        filteredWords.add("there");
        filteredWords.add("would");
        filteredWords.add("going");
        filteredWords.add("via");
        filteredWords.add("very");
        filteredWords.add("how");
        filteredWords.add("will");
        filteredWords.add("which");
        filteredWords.add("given");
        filteredWords.add("own");
        filteredWords.add("they");
        filteredWords.add("then");
        filteredWords.add("way");
        filteredWords.add("else");
        filteredWords.add("maybe");
        filteredWords.add("them");
        return filteredWords;
    }

    public static HashMap<String, String> getCorrectedWords() {
        return CORRECTED_WORDS;
    }

    public static HashSet<String> getFilteredWords() {
        return FILTERED_WORDS;
    }

    public static HashSet<String> getUrlPathsToIgnore() {
        return URL_PATHS_TO_IGNORE;
    }

    public static HashSet<String> getAllSitesInSystem() {
        return ALL_SITES_IN_SYSTEM;
    }

    public static HashMap<String, String[]> getCategories() {
        return CATEGORIES;
    }

    //Correct a word or return it unchanged.
    public static String correctWord(String word) {
        if (CORRECTED_WORDS.containsKey(word)) {
            return CORRECTED_WORDS.get(word);
        }
        return word;
    }

    //Determine if a word should be filtered out.
    public static boolean isFilterWord(String word) {
        //Skip words that are too long or short
        if (word.length() < Settings.MIN_WORD_LENGTH || word.length() > Settings.MAX_WORD_LENGTH) {
            return true;
        }
        return FILTERED_WORDS.contains(word);
    }

    public static String pickSiteCatagory(String[] words) {
        for (String word : words) {
            word = word.trim();
            word = Utils.filterNonAscii(word);
            if(word.equalsIgnoreCase("")) {
                continue;
            }
            for (String category : StaticLists.getCategories().keySet()) {
                if (word.equalsIgnoreCase(category)) {
                    return category; // A direct tag to category match, return right away!
                }
                for (String otherWord : StaticLists.getCategories().get(category)) {
                    if (word.equalsIgnoreCase(otherWord)) {
                        return category; // A tag matches a category other word!
                    }
                }
            }
        }
        return Settings.DEFAULT_CATEGORY;
    }
}