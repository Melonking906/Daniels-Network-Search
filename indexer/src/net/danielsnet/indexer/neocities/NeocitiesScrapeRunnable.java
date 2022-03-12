package net.danielsnet.indexer.neocities;

import net.danielsnet.indexer.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;

public class NeocitiesScrapeRunnable implements Runnable {
    private int preCrashPage;

    @Override
    public void run() {
        while (true) {
            if (isItTimeToScrape()) {
                scrapeSites(1, 1);
                addNewSitesToIndexList();
                updateLastScrapeToNow();
                UI.log("Neocities Site Scraper DONE!");
            } else {
                UI.log("Checking Neocities Scraper - Its not time to scrape yet!");
            }
            Utils.threadSleep(3600); //1 hour
        }
    }

    private void scrapeSites(int startPage, int startPositionCount) {
        try {
            Document firstPage = Jsoup.connect("https://neocities.org/browse?page=1").get();
            Elements pagesBar = firstPage.select(".pagination a:nth-last-child(2)");
            int lastPage = Integer.parseInt(pagesBar.get(0).text());

            for (int i = startPage; i <= lastPage; i++) {
                UI.log("Reading Neocities Page: " + i);
                preCrashPage = i;

                Document page = Jsoup.connect("https://neocities.org/browse?page=" + i).get();
                Elements sites = page.select(".website-Gallery li");

                for (Element site : sites) {
                    String username = site.attr("id").replace("username_", "");
                    String url = site.select(".title a").get(0).attr("href") + "/";
                    NeocitiesSite neoSite = new NeocitiesSite(username, url, startPositionCount);

                    neoSite.saveToDatabase();
                    UI.log("Saved Neocities Site of " + neoSite.getUsername());
                    startPositionCount++;

                    //Utils.threadSleep(5); // Wait a little before going to the next site.
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            UI.log("Neocities Runnable CRASH at page " + preCrashPage + " attempting restart on next page.");
            scrapeSites(preCrashPage++, startPositionCount++);
        }
    }

    private void addNewSitesToIndexList() {
        // Only process sites with 50 updates and 2 followers - Profile disabled sites wont be included.
        String selectSql = "SELECT * FROM neocities_Sites WHERE sID = -1 AND updates >= " + Settings.MIN_UPDATES_TO_APPROVE_NC_SITE + " AND followers >= " + Settings.MIN_FOLLOWERS_TO_APPROVE_NC_SITE + ";";
        HashSet<HashMap<String, String>> sites = Main.getDatabase().query(selectSql, true);

        for (HashMap<String, String> site : sites) {
            String catagory = StaticLists.pickSiteCatagory(site.get("tags").split(","));
            String url = site.get("url");
            String sqlInsert = "INSERT INTO `data_Sites` (url, categorie) VALUES ('" + url + "', '" + catagory + "');";
            Main.getDatabase().query(sqlInsert, false);

            String sqlGetId = "SELECT sID FROM data_Sites WHERE url = '" + url + "';";
            String sID = DatabaseConnect.getOnlyValue(sqlGetId, "sID");

            String sqlUpdateNCSite = "UPDATE neocities_Sites SET sID = " + sID + " WHERE url = '" + url + "';";
            Main.getDatabase().query(sqlUpdateNCSite, false);
            UI.log("Added new Neocities site to main index: " + site.get("username"));
        }
    }

    private boolean isItTimeToScrape() {
        String sql = "SELECT lastNeocitiesScrape FROM meta_Info;";
        String dateString = DatabaseConnect.getOnlyValue(sql, "lastNeocitiesScrape");
        LocalDate lastScrape = LocalDateTime.parse(dateString).toLocalDate();
        return lastScrape.plusDays(Settings.DAYS_BETWEEN_NEOCITIES_SCRAPES).isBefore(LocalDate.now());
    }

    private void updateLastScrapeToNow() {
        String sql = "UPDATE meta_Info SET lastNeocitiesScrape = '"+Utils.dateToSQL(LocalDateTime.now())+"';";
        Main.getDatabase().query(sql, false);
    }
}

