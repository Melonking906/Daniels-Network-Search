package net.danielsnet.indexer.indexers;

import net.danielsnet.indexer.Settings;
import net.danielsnet.indexer.StaticLists;
import net.danielsnet.indexer.UI;
import net.danielsnet.indexer.Utils;
import net.danielsnet.indexer.objects.Page;
import net.danielsnet.indexer.objects.Site;
import org.jsoup.helper.W3CDom;

import java.util.HashSet;

public class PageIndexer {
    private final Site site;
    private final HashSet<Integer> hashes;
    private final HashSet<Integer> urlHashes;

    public PageIndexer(Site site) {
        this.site = site;
        hashes = new HashSet<>();
        urlHashes = new HashSet<>();
    }

    public static boolean isSiteInSystem(String domain) {
        for (String url : StaticLists.getAllSitesInSystem()) {
            url = url.replace("http://", "");
            url = url.replace("https://", "");
            url = url.replace("/", "");
            if (domain.equals(url)) {
                return true;
            }
        }
        return false;
    }

    public void index() {
        UI.log("PAGE INDEXER: Starting page index for site: " + site.getUrl());
        processPage(site.getUrl());
    }

    //RECURSIVE - Take a url, filter it, load it, store to database and make a page object.
    private void processPage(String url) {
        //Limit pages per site
        if (hashes.size() > Settings.MAX_PAGES_PER_SITE) {
            return;
        }

        //Filter faulty links
        if (url == null || url.equals("")) {
            return;
        }

        //Filter out anything but regular pages
        int hashLocation = url.indexOf('#');
        url = url.substring(0, hashLocation != -1 ? hashLocation : url.length());

        int questionLocation = url.indexOf('?');
        url = url.substring(0, questionLocation != -1 ? questionLocation : url.length());

        //If a url is still stupidly long, discard it.
        if(url.length() > Settings.MAX_URL_LENGTH) {
            return;
        }

        //Check if we have check if we have already tested this URL
        int urlHash = url.hashCode();
        if(urlHashes.contains(urlHash)) {
            return;
        }
        urlHashes.add(urlHash);

        //Filter file types
        String fileType = Utils.getURLFileType(url);
        if (fileType == null || (!fileType.equals("html") && !fileType.equals("htm") && !fileType.equals("php") && !fileType.equals(""))) {
            return;
        }

        //Filter out types of path we dont want..
        for (String thingIDontWant : StaticLists.getUrlPathsToIgnore()) {
            if (url.contains(thingIDontWant)) {
                return;
            }
        }

        //Handle filtering of local links and remote links
        if (!url.contains(site.getUrl())) {
            // Ignore normal neocities links as we handle them elsewhere
            if (url.contains("neocities.org")) {
                return;
            }

            // Add a non local link to the found sites list
            String domain = Utils.getURLDomain(url);
            if (domain != null && !isSiteInSystem(domain)) {
                String proto = "http://";
                if (url.contains("https://")) {
                    proto = "https://";
                }
                site.addFoundSite(proto + domain + "/");
            }
            return;
        }

        //Sleep to simulate normal reading.
        //Utils.threadSleep(Utils.randomNumber(5, 15));

        //Generate page NOTE the page is DOWNLOADED HERE
        Page page = new Page(site, url);
        if (!page.isLoaded()) {
            return;
        }

        //Ensure page is unique and not a copy with another url
        if (hashes.contains(page.getHash())) {
            return;
        }
        hashes.add(page.getHash());

        //WORD INDEX the page?
        new WordIndexer(page).index();
        site.addPage(page);
        UI.log("Page Indexed: " + url + " HASH " + url.toLowerCase().hashCode());

        //Recursive loop to next links
        HashSet<String> links = page.getLinks();
        int loopCount = 0;
        for (String link : links) {
            if(loopCount > Settings.MAX_LINKS_PER_PAGE) {
                break;
            }
            processPage(link);
            loopCount++;
        }

        //Save the index document so it can be processed later for css
        if(page.getUrl().equals(site.getUrl())) {
            try {
                site.setIndexDocument( new W3CDom().fromJsoup(page.getData()) );
            } catch (Exception e) {
                UI.log("PAGE INDEXER: Error converting JSOUP to W3C Document for "+site.getUrl());
            }
        }

        page.unload(); //Unload the page data to save memory space once it's been indexed.
    }
}