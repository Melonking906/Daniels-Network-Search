package net.danielsnet.indexer.objects;

import net.danielsnet.indexer.Main;
import net.danielsnet.indexer.Utils;
import org.w3c.dom.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Site {
    private final String id;
    private final String url;
    private final String category;
    private LocalDateTime lastIndexStart;
    private LocalDateTime lastIndexComplete;
    //private final LocalDateTime dateAdded;
    private ArrayList<Page> pages;
    private int pageCount;
    private Document indexDocument;
    private HashSet<String> foundURLs;

    public Site(HashMap<String, String> sqlSiteRow) {
        this.id = sqlSiteRow.get("sID");
        this.url = sqlSiteRow.get("url");
        this.category = sqlSiteRow.get("category");
        this.lastIndexStart = Utils.dateFromSQL(sqlSiteRow.get("lastIndexStart"));
        this.lastIndexComplete = Utils.dateFromSQL(sqlSiteRow.get("lastIndexComplete"));
        //this.dateAdded = Utils.dateFromSQL(sqlSiteRow.get("dateAdded"));
        this.pages = new ArrayList<>();
        this.pageCount = Integer.parseInt(sqlSiteRow.get("pageCount"));
        foundURLs = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getCategory() {
        return category;
    }

    public LocalDateTime getLastIndexStart() {
        return lastIndexStart;
    }

    public void setLastIndexStart(LocalDateTime lastIndexStart) {
        this.lastIndexStart = lastIndexStart;
        Main.getDatabase().query("UPDATE data_Sites SET lastIndexStart = '"
                + Utils.dateToSQL(lastIndexStart) + "' WHERE sID = " + getId() + ";", false);
    }

    //public LocalDateTime getDateAdded() {
    //    return dateAdded;
    //}

    public LocalDateTime getLastIndexComplete() {
        return lastIndexComplete;
    }

    public void setLastIndexComplete(LocalDateTime lastIndexComplete) {
        this.lastIndexComplete = lastIndexComplete;
        Main.getDatabase().query("UPDATE data_Sites SET lastIndexComplete = '"
                + Utils.dateToSQL(lastIndexComplete) + "' WHERE sID = " + getId() + ";", false);
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
        Main.getDatabase().query("UPDATE data_Sites SET pageCount = " + pageCount + " WHERE sID = " + getId() + ";", false);
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

    public void addPage(Page page) {
        this.pages.add(page);
    }

    public HashSet<String> getFoundURLs() {
        return foundURLs;
    }

    public void addFoundSite(String url) {
        this.foundURLs.add(url);
    }

    public Document getIndexDocument() {
        return indexDocument;
    }

    public void setIndexDocument(Document indexDocument) {
        this.indexDocument = indexDocument;
    }
}
