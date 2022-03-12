package net.danielsnet.indexer.objects;

import net.danielsnet.indexer.Settings;
import net.danielsnet.indexer.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;

public class Page {
    private final String url;
    private final Site site;
    private String id;
    private String title;
    private HashSet<String> bodyWords;
    private HashSet<String> titleWords;
    private int hash;

    //Loading Data
    private Document data;
    private boolean isLoaded = false;

    public Page(Site site, String url) {
        this.url = url;
        this.site = site;
        this.bodyWords = new HashSet<>();
        this.titleWords = new HashSet<>();
        load();
    }

    public boolean load() {
        try {
            data = Jsoup.connect(url).userAgent(Settings.USER_AGENT)
                    .timeout(Settings.PAGE_DOWNLOAD_TIMEOUT)
                    .get();
        } catch (IOException ex) {
            return false;
        }
        isLoaded = true;
        title = getTitle();
        hash = data.body().text().hashCode() + title.hashCode(); //Unique id of the page
        return true;
    }

    public void unload() {
        this.data = null;
        this.isLoaded = false;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        if (this.title != null) {
            return this.title;
        }
        if (!isLoaded()) {
            return null;
        }
        String title = data.title();
        //Trim it to fit in the database if its too long
        if (title.length() > 64) {
            return title.substring(0, 63);
        }
        return title;
    }

    public String getText() {
        if (!isLoaded()) {
            return null;
        }
        if (!data.body().hasText()) { //BE SURE TO TEST
            return "";
        }
        return data.body().text();
    }

    public HashSet<String> getLinks() {
        if (!isLoaded()) {
            return null;
        }
        HashSet<String> links = new HashSet<>();
        //Get all links
        Elements linkElements = data.select("a[href]");
        for (Element linkElement : linkElements) {
            links.add(linkElement.attr("abs:href"));
        }
        //Get linked frames
        Elements frameElements = data.select("frame[src]");
        for (Element frameElement : frameElements) {
            links.add(frameElement.attr("abs:src"));
        }
        return links;
    }

    public String getUrl() {
        return url;
    }

    public Site getSite() {
        return site;
    }

    public HashSet<String> getBodyWords() {
        return bodyWords;
    }

    public void setBodyWords(HashSet<String> bodyWords) {
        this.bodyWords = bodyWords;
    }

    public HashSet<String> getTitleWords() {
        return titleWords;
    }

    public void setTitleWords(HashSet<String> titleWords) {
        this.titleWords = titleWords;
    }

    public int getHash() {
        return hash;
    }

    public Document getData() {
        return data;
    }
}
