package net.danielsnet.indexer.scrapers;

import net.danielsnet.indexer.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class TildeTown implements Runnable {

    private static final String LIST_URL = "https://tilde.town/users.html";
    private static final String DEFAULT_SITE_TITLE = "welcome to my ~ page";
    private static final String DATABASE_INFO_UPDATE = "lastTildeTownScrape";
    private static final int MIN_WORDS_TO_APPROVE = 35;
    private static final int SECONDS_BETWEEN_PAGE_LOADS = 3;

    @Override
    public void run() {
        while (true) {
            if (isItTimeToScrape()) {
                scrapeSites();
                updateLastScrapeToNow();
                UI.log("SCRAPER: Tilde.Town Site Scraper DONE!");
            } else {
                UI.log("Checking Tilde.Town Scraper - Its not time to scrape yet!");
            }
            Utils.threadSleep(3600); //1 hour
        }
    }

    private void scrapeSites() {
        final HashSet<String> siteURLs = new HashSet<>();

        try {
            Document usersPage = Jsoup.connect(LIST_URL).get();
            Elements userLinks = usersPage.select("body a");

            for(Element userLink : userLinks) {
                String title = userLink.text();
                String url = userLink.attr("href") + "/";
                // Skip sites with the default title as they are prob empty
                if(title.equalsIgnoreCase(DEFAULT_SITE_TITLE)) {
                    continue;
                }
                siteURLs.add(url);
            }

            filterExistingURLs(siteURLs).forEach(this::processSite);

        } catch (IOException exception) {
            exception.printStackTrace();
            UI.log("SCRAPER: Tilde.Town CRASH");
        }
    }

    private HashSet<String> filterExistingURLs(HashSet<String> urls) {
        final StringBuilder sql = new StringBuilder("SELECT url FROM data_Sites WHERE url IN ");
        final StringJoiner sqlJoiner = new StringJoiner(",", "(", ")");
        urls.stream().map(word -> new StringBuilder().append("'").append(word).append("'")).forEach(sqlJoiner::add);
        sql.append(sqlJoiner).append(";");
        Set<String> existingURLs  = Main.getDatabase().query(sql.toString(), true)
                .stream().flatMap(url -> url.values().stream())
                .collect(Collectors.toSet());
        urls.removeAll(existingURLs);
        return urls;
    }

    private void processSite(String url) {
        try {
            Document data = Jsoup.connect(url).userAgent(Settings.USER_AGENT)
                    .timeout(Settings.PAGE_DOWNLOAD_TIMEOUT)
                    .get();

            String[] pageWords = (data.body().text() + " " + data.title()).split(" ");
            HashSet<String> correctedWords = new HashSet<>();
            for(String word : pageWords) {
                word = word.trim();
                word = Utils.filterNonAscii(word);
                if(!word.equalsIgnoreCase("")) {
                    correctedWords.add(word);
                }
            }
            if(correctedWords.size() < MIN_WORDS_TO_APPROVE) {
                return;
            }
            String category = StaticLists.pickSiteCatagory(correctedWords.toArray(new String[correctedWords.size()]));

            String sqlInsert = "INSERT INTO `data_Sites` (url, categorie) VALUES ('" + url + "', '" + category + "');";
            Main.getDatabase().query(sqlInsert, false);
            UI.log("SCRAPER: Added Tilde.Town site " + url);

        } catch (IOException ex) {
            UI.log("SCRAPER: Tilde.town site failed to load for " + url);
        }

        Utils.threadSleep(SECONDS_BETWEEN_PAGE_LOADS);
    }

    private boolean isItTimeToScrape() {
        String sql = "SELECT "+DATABASE_INFO_UPDATE+" FROM meta_Info;";
        String dateString = DatabaseConnect.getOnlyValue(sql, DATABASE_INFO_UPDATE);
        LocalDate lastScrape = LocalDateTime.parse(dateString).toLocalDate();
        return lastScrape.plusDays(Settings.DAYS_BETWEEN_TILDETOWN_SCRAPES).isBefore(LocalDate.now());
    }

    private void updateLastScrapeToNow() {
        String sql = "UPDATE meta_Info SET "+DATABASE_INFO_UPDATE+" = '"+Utils.dateToSQL(LocalDateTime.now())+"';";
        Main.getDatabase().query(sql, false);
    }
}
