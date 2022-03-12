package net.danielsnet.indexer.indexers;

import net.danielsnet.indexer.*;
import net.danielsnet.indexer.objects.Site;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndexRunnable implements Runnable {
    private final ExecutorService executor;
    private final DatabaseConnect database;
    private final ThreadCount threadCounter;

    public IndexRunnable() {
        this.database = Main.getDatabase();
        this.executor = Executors.newCachedThreadPool();
        this.threadCounter = new ThreadCount();
    }

    @Override
    public void run() {
        while (Settings.Flag_DoIndex) {
            UI.log("INDEXER: Checking for Sites...");
            HashSet<Site> sites = getSitesToUpdate(
                    Settings.TARGET_NUMBER_OF_SITES_TO_INDEX_AT_ONCE - threadCounter.getThreadCount());
            if (sites.size() > 0) {
                UI.log("INDEXER: Sites selected: " + sites.size());
                for (Site site : sites) {
                    //Start indexing thread
                    executor.execute(new SiteIndexRunnable(site, threadCounter));
                    //Wait to offset indexing
                    Utils.threadSleep(Utils.randomNumber(1, 5));
                }
            } else {
                UI.log("INDEXER: No sites selected this time: " + threadCounter.getThreadCount() + " threads active!");
            }
            Utils.threadSleep(Settings.SECONDS_BETWEEN_INDEXES);
        }
    }

    private HashSet<Site> getSitesToUpdate(int limit) {
        HashSet<Site> sites = new HashSet<>();
        if (limit < 1) {
            return sites;
        }
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(Settings.DAYS_BETWEEN_SITE_INDEXES);
        StringBuilder sql = new StringBuilder("SELECT * FROM data_Sites WHERE");
        sql.append(" lastIndexComplete < '").append(beforeDate).append("'");
        sql.append(" AND lastIndexStart < '").append(beforeDate).append("'");
        sql.append(" ORDER BY rand() LIMIT ").append(limit).append(";");
        HashSet<HashMap<String, String>> siteSQLRows = database.query(sql.toString(), true);
        if (siteSQLRows == null) {
            return sites;
        }
        siteSQLRows.forEach(siteSQLRow -> sites.add(new Site(siteSQLRow)));
        return sites;
    }

    protected class ThreadCount {
        private int threadCounter = 0;

        public void addOne() {
            threadCounter++;
        }

        public void subtractOne() {
            threadCounter--;
        }

        public int getThreadCount() {
            return threadCounter;
        }
    }
}