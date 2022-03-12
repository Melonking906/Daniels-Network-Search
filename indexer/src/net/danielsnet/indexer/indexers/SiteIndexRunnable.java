package net.danielsnet.indexer.indexers;

import net.danielsnet.indexer.DatabaseConnect;
import net.danielsnet.indexer.Main;
import net.danielsnet.indexer.UI;
import net.danielsnet.indexer.neocities.NeocitiesAPI;
import net.danielsnet.indexer.neocities.NeocitiesConnect;
import net.danielsnet.indexer.objects.Page;
import net.danielsnet.indexer.objects.Site;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class SiteIndexRunnable implements Runnable {
    private final Site site;
    private final IndexRunnable.ThreadCount threadCounter;

    public SiteIndexRunnable(Site site, IndexRunnable.ThreadCount threadCounter) {
        this.site = site;
        this.threadCounter = threadCounter;
    }

    //Index everything
    public void run() {
        threadCounter.addOne();

        UI.log("SITE INDEXER: Starting site index for: " + site.getUrl());
        site.setLastIndexStart(LocalDateTime.now());

        //Neocities API integration.
        String sqlGetNeocitiesInfo = "SELECT * FROM neocities_Sites WHERE sID = " + site.getId() + ";";
        HashMap<String, String> neocitiesData = DatabaseConnect.getFirstRow(sqlGetNeocitiesInfo);
        if (neocitiesData != null) {
            String neocitiesUser = neocitiesData.get("username");
            UI.log("SITE INDEXER: Neocities site found: " + neocitiesUser);
            NeocitiesAPI ncAPI = new NeocitiesConnect(neocitiesUser).getApi();
            if (ncAPI != null && ncAPI.hasLoaded()) {
                if (!site.getLastIndexComplete().isBefore(ncAPI.getInfo().getLast_updated())) {
                    UI.log("SITE INDEXER: Neocities site not updated, Skipping: " + neocitiesUser);
                    site.setLastIndexComplete(LocalDateTime.now());
                    threadCounter.subtractOne();
                    return;
                }
            }
        }

        //Index site pages
        new PageIndexer(site).index();

        if (site.getPages().size() < 1) {
            UI.log("SITE INDEXER: Index ERROR no pages found for: " + site.getUrl());
            site.setLastIndexComplete(LocalDateTime.now());
            dropSitePages();
            site.setPageCount(0);
            threadCounter.subtractOne();
            return;
        }

        //Index CSS and save the results
        CSSIndexer siteCSS = new CSSIndexer(site);
        if(siteCSS.hasLoaded()) {
            saveSiteCSS(siteCSS);
        }

        //Update info about site in database.
        dropSitePages(); //Delete old pages related to the site
        saveSitePages();
        HashMap<String, String> pageIDs = getPageIDs(); // Set an pID on any page found in the system.
        for (Page page : site.getPages()) {
            for (String pageUrl : pageIDs.keySet()) {
                if (page.getUrl().equalsIgnoreCase(pageUrl)) {
                    page.setId(pageIDs.get(pageUrl));
                    break;
                }
            }
        }
        saveWordReferences();
        if(site.getFoundURLs().size() > 0) {
            saveFoundSites();
        }
        site.setPageCount(pageIDs.size());
        site.setLastIndexComplete(LocalDateTime.now());
        UI.log("SITE INDEXER: Index completed for: " + site.getUrl() + " found " + site.getPageCount() + " pages!");

        threadCounter.subtractOne();
    }

    private HashMap<String, String> getPageIDs() {
        String sql = "SELECT pID, url FROM data_Pages WHERE sID = " + site.getId() + ";";
        HashSet<HashMap<String, String>> rows = Main.getDatabase().query(sql, true);
        return rows.stream().collect(Collectors.toMap(row -> row.get("url"), row -> row.get("pID"), (a, b) -> b, HashMap::new));
    }

    private void dropSitePages() {
        String sql = "DELETE FROM data_Pages WHERE sID = " + site.getId() + ";";
        Main.getDatabase().query(sql, false);
    }

    private void saveSitePages() {
        final StringBuilder sqlInsert = new StringBuilder("INSERT INTO data_Pages (sID, url, title, hash) VALUES ");
        final StringJoiner insertSQLJoiner = new StringJoiner(",");
        for (Page page : site.getPages()) {
            final StringJoiner pageRow = new StringJoiner(",", "(", ")");
            pageRow.add(site.getId());
            pageRow.add("'" + safeText(page.getUrl()) + "'");
            pageRow.add("'" + safeText(page.getTitle()) + "'");
            pageRow.add("'" + page.getHash() + "'");
            insertSQLJoiner.add(pageRow.toString());
        }
        sqlInsert.append(insertSQLJoiner).append(";");
        Main.getDatabase().query(sqlInsert.toString(), false);
    }

    private void saveFoundSites() {
        StringBuilder sql = new StringBuilder("INSERT IGNORE INTO submit_FoundSites (finder_sID, url) VALUES ");
        StringJoiner sqlJoiner = new StringJoiner(",");
        for (String url : site.getFoundURLs()) {
            sqlJoiner.add("('" + site.getId() + "','" + safeText(url) + "')");
        }
        sql.append(sqlJoiner).append(";");
        Main.getDatabase().query(sql.toString(), false);
    }

    private void saveSiteCSS(CSSIndexer css) {
        UI.log("SITE INDEXER: Saving found CSS for: " + site.getUrl());
        StringBuilder sql = new StringBuilder();
        sql.append("REPLACE INTO data_SiteCSS (sID, textColour, backgroundColour, backgroundImage) VALUES (");
        sql.append(site.getId()).append(",");
        sql.append("'").append(safeText(css.getTextColour())).append("',");
        sql.append("'").append(safeText(css.getBackgroundColour())).append("',");
        sql.append("'").append(safeText(css.getBackgroundImage())).append("');");
        Main.getDatabase().query(sql.toString(), false);
    }

    private void saveWordReferences() {
        UI.log("SITE INDEXER: Starting SAVE TO DATABASE for: " + site.getUrl());
        // Make a list of all unique words in the site
        final HashSet<String> allSiteWords = new HashSet<>();
        for (Page page : site.getPages()) {
            allSiteWords.addAll(page.getBodyWords());
            allSiteWords.addAll(page.getTitleWords());
        }

        if(allSiteWords.size() < 1) {
            UI.log("    SITE INDEXER: Site had no words! No references saved for " + site.getUrl());
            return;
        }

        // Store unique words to the database
        UI.log("    SITE INDEXER: Registering words to dictionary from: " + site.getUrl());
        final StringBuilder sqlInsertWords = new StringBuilder("INSERT IGNORE INTO word_Dictionary (word) VALUES ");
        final StringJoiner insertSQLJoiner = new StringJoiner(",");
        allSiteWords.stream().map(word -> new StringBuilder().append("('").append(word).append("')")).forEach(insertSQLJoiner::add);
        sqlInsertWords.append(insertSQLJoiner).append(';');
        Main.getDatabase().query(sqlInsertWords.toString(), false);

        // Get IDs for all words.
        UI.log("    SITE INDEXER: Words registered processing word IDs for: " + site.getUrl());
        final StringBuilder sqlSelectWords = new StringBuilder("SELECT word, wID FROM word_Dictionary WHERE word IN ");
        final StringJoiner selectSQLJoiner = new StringJoiner(",", "(", ")");
        allSiteWords.stream().map(word -> new StringBuilder().append("'").append(word).append("'")).forEach(selectSQLJoiner::add);
        sqlSelectWords.append(selectSQLJoiner).append(";");
        final HashSet<HashMap<String, String>> wordData = Main.getDatabase().query(sqlSelectWords.toString(), true);
        final HashMap<String, String> wordToIdMap = wordData.stream()
                .collect(Collectors.toMap(row -> row.get("word"), row -> row.get("wID"), (a, b) -> b, HashMap::new));

        // Write all Body References
        UI.log("    SITE INDEXER: Saving body text references for " + site.getUrl());
        StringBuilder sqlInsertBodyRefrences = new StringBuilder("INSERT INTO index_WordOccurrences (pID, wID) VALUES ");
        final StringJoiner insertBodyRefrencesSQLJoiner = new StringJoiner(",");
        for (Page page : site.getPages()) {
            for (String pageWord : page.getBodyWords()) {
                wordToIdMap.keySet().stream().filter(databaseWord -> databaseWord.equalsIgnoreCase(pageWord))
                        .findFirst().ifPresent(databaseWord -> insertBodyRefrencesSQLJoiner
                                .add("(" + page.getId() + "," + wordToIdMap.get(databaseWord) + ")"));
            }
        }
        //Check if anything has been added to the joiner before finishing the statement.
        if(insertBodyRefrencesSQLJoiner.length() > 0) {
            sqlInsertBodyRefrences.append(insertBodyRefrencesSQLJoiner).append(";");
            Main.getDatabase().query(sqlInsertBodyRefrences.toString(), false);
        }

        // Write all Title References
        UI.log("    SITE INDEXER: Saving title text references for " + site.getUrl());
        StringBuilder sqlInsertTitleRefrences = new StringBuilder("INSERT INTO index_TitleOccurrences (pID, wID) VALUES ");
        final StringJoiner insertTitleRefrencesSQLJoiner = new StringJoiner(",");
        for (Page page : site.getPages()) {
            for (String pageWord : page.getTitleWords()) {
                wordToIdMap.keySet().stream().filter(databaseWord -> databaseWord.equalsIgnoreCase(pageWord))
                        .findFirst().ifPresent(databaseWord -> insertTitleRefrencesSQLJoiner
                                .add("(" + page.getId() + "," + wordToIdMap.get(databaseWord) + ")"));
            }
        }
        //Check if anything has been added to the joiner before finishing the statement.
        if(insertTitleRefrencesSQLJoiner.length() > 0) {
            sqlInsertTitleRefrences.append(insertTitleRefrencesSQLJoiner).append(";");
            Main.getDatabase().query(sqlInsertTitleRefrences.toString(), false);
        }
    }

    private String safeText(String text) {
        return text.replace( "'", "''" ).replace( "\\", "\\\\" );
    }
}
