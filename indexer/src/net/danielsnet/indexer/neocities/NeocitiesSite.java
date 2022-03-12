package net.danielsnet.indexer.neocities;

import net.danielsnet.indexer.DatabaseConnect;
import net.danielsnet.indexer.Main;
import net.danielsnet.indexer.UI;
import net.danielsnet.indexer.Utils;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;

import java.time.LocalDateTime;
import java.util.HashSet;

public class NeocitiesSite {
    private final String username;
    private final String url;
    private final int position;
    HashSet<String> tags;
    //Scraped info
    private int views;
    private int hits;
    private int followers;
    private int updates;
    private LocalDateTime lastUpdated;
    private LocalDateTime dateCreated;

    public NeocitiesSite(String username, String url, int position) {
        this.username = username;
        this.url = url;
        this.position = position;
        scrapeSiteInfo();
    }

    private static int tryAndGetSiteId(String url) {
        String sql = "SELECT sID FROM data_Sites WHERE url = '" + url + "'";
        String id = DatabaseConnect.getOnlyValue(sql, "sID");
        if (id == null) {
            return -1;
        }
        try {
            return Integer.parseInt(id);
        } catch (Exception e) {
            return -1;
        }
    }

    private void scrapeSiteInfo() {
        // Get site info from the neocities profile page
        try {
            Document page = Jsoup.connect("https://neocities.org/site/" + username).get();
            followers = page.select(".follower-list a").size();
            updates = Integer.parseInt(page.select(".stats .stat:nth-last-child(2) strong").get(0).text().replace(",", ""));
        } catch (Exception exception) {
            UI.log("Neocities Scraper: Error scraping profile info for " + username);
        }

        // Get additional site info from the neocities api
        NeocitiesAPI api = new NeocitiesConnect(username).getApi();
        if (!api.hasLoaded()) {
            UI.log("Neocities Scraper: Error scraping api info for " + username);
        }
        views = api.getInfo().getViews();
        hits = api.getInfo().getHits();
        lastUpdated = api.getInfo().getLast_updated();
        dateCreated = api.getInfo().getCreated_at();
        tags = api.getInfo().getTags();
    }

    public void saveToDatabase() {
        // Check for an existing site
        String sqlSelectUsername = "SELECT url FROM neocities_Sites WHERE username = '" + this.getUsername() + "';";
        String url = DatabaseConnect.getOnlyValue(sqlSelectUsername, "url");
        if (url != null) {
            // Update an existing site
            String sql = "UPDATE neocities_Sites SET";
            sql += " url = '" + this.getUrl() + "',"; // Basic support for url changing, however it does not connect to main index yet.
            sql += " position = " + this.getPosition() + ",";
            sql += " views = " + this.getViews() + ",";
            sql += " hits = " + this.getHits() + ",";
            sql += " followers = " + this.getFollowers() + ",";
            sql += " updates = " + this.getUpdates() + ",";
            sql += " lastUpdated = '" + Utils.dateToSQL(this.getLastUpdated()) + "',";
            sql += " tags = '" + StringUtil.join(this.getTags(), ",") + "'";
            sql += " WHERE username = '" + this.getUsername() + "';";
            Main.getDatabase().query(sql, false);
        } else {
            // Add the site if its new.
            String sql = "";
            sql += "INSERT INTO neocities_Sites (username, sID, url, position, views, hits, followers, updates, lastUpdated, dateCreated, tags)";
            sql += " VALUES(";
            sql += "'" + this.getUsername() + "',";
            sql += tryAndGetSiteId(this.getUrl()) + ",";
            sql += "'" + this.getUrl() + "',";
            sql += this.getPosition() + ",";
            sql += this.getViews() + ",";
            sql += this.getHits() + ",";
            sql += this.getFollowers() + ",";
            sql += this.getUpdates() + ",";
            sql += "'" + Utils.dateToSQL(this.getLastUpdated()) + "',";
            sql += "'" + Utils.dateToSQL(this.getDateCreated()) + "',";
            sql += "'" + StringUtil.join(this.getTags(), ",") + "'";
            sql += ");";
            Main.getDatabase().query(sql, false);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }

    public int getPosition() {
        return position;
    }

    public int getViews() {
        return views;
    }

    public int getHits() {
        return hits;
    }

    public int getFollowers() {
        return followers;
    }

    public int getUpdates() {
        return updates;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public HashSet<String> getTags() {
        return tags;
    }
}
