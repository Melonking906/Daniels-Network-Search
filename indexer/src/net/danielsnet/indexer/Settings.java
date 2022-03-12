package net.danielsnet.indexer;

public class Settings {
    public static final String USER_AGENT = "Mozilla/5.0";
    public static final String DEFAULT_CATEGORY = "other";

    // Neocities Scraper
    public static final int DAYS_BETWEEN_NEOCITIES_SCRAPES = 6;
    public static final int MIN_UPDATES_TO_APPROVE_NC_SITE = 50;
    public static final int MIN_FOLLOWERS_TO_APPROVE_NC_SITE = 2;

    // Other Scrapers
    public static final int DAYS_BETWEEN_TILDETOWN_SCRAPES = 6;

    // Indexers
    public static final int DAYS_BETWEEN_SITE_INDEXES = 2;
    public static final int SECONDS_BETWEEN_INDEXES = 5;
    public static final int TARGET_NUMBER_OF_SITES_TO_INDEX_AT_ONCE = 5;
    public static final int PAGE_DOWNLOAD_TIMEOUT = 5000;
    public static final int MAX_PAGES_PER_SITE = 127;
    public static final int MAX_WORDS_PER_PAGE = 3000;
    public static final int MAX_LINKS_PER_PAGE = 100;
    public static final int MAX_URL_LENGTH = 256;

    // Words
    public static final int MAX_WORD_LENGTH = 27;
    public static final int MIN_WORD_LENGTH = 3;

    // Flags - DO NOT EDIT
    public static boolean Flag_DoIndex = true;
}
