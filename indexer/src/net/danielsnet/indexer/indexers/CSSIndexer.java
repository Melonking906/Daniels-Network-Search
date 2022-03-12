package net.danielsnet.indexer.indexers;

import cz.vutbr.web.css.*;
import net.danielsnet.indexer.Settings;
import net.danielsnet.indexer.UI;
import net.danielsnet.indexer.objects.Site;

import java.net.URL;

public class CSSIndexer {
    private boolean hasLoaded = false;
    private Site site;
    private String backgroundColour = "";
    private String backgroundImage = "";
    private String textColour = "";
    private final MediaSpec mediaSpec = new MediaSpec("screen");

    public CSSIndexer(Site site) {
        this.site = site;

        if(site.getIndexDocument() == null) {
            UI.log("CSS INDEXER: Site had no index document: " + site.getUrl());
            return;
        }

        try {
            URL url = new URL(site.getUrl());
            StyleSheet css = CSSFactory.getUsedStyles(site.getIndexDocument(), null, url, mediaSpec);

            for (RuleBlock<?> rule : css) {
                if (rule instanceof RuleSet) {
                    RuleSet set = (RuleSet) rule;
                    for (CombinedSelector selector : set.getSelectors()) {
                        if (selector.toString().equalsIgnoreCase("body")) {
                            for (Declaration decl : set) {
                                if (decl.getProperty().equalsIgnoreCase("color")) {
                                    textColour = safeCSS(decl.get(0).toString());
                                }
                                if (decl.getProperty().equalsIgnoreCase("background-color")) {
                                    backgroundColour = safeCSS(decl.get(0).toString());
                                }
                                if (decl.getProperty().equalsIgnoreCase("background-image")) {
                                    backgroundImage = processCSSUrlTag(decl.get(0).toString());
                                }
                            }
                        }
                    }
                }
            }

            //If any value was recovered then flag the css as available.
            if( !backgroundColour.equals("") || !textColour.equals("") || !backgroundImage.equals("") ) {
                hasLoaded = true;
            }

        } catch (Exception e) {
            //e.printStackTrace();
            UI.log("CSS INDEXER: Error loading CSS for " + site.getUrl());
        }
    }

    private String safeCSS(String css) {
        return css.replace("none", "");
    }

    private String processCSSUrlTag(String cssURL) {
        if(cssURL.equalsIgnoreCase("none")) {
            return "";
        }

        if(cssURL.length() > Settings.MAX_URL_LENGTH) {
            return "";
        }

        String url = cssURL.replace("url(", "");
        url = url.replace(")", "");
        url = url.replace("\\", "");
        url = url.replace("'", "");
        url = url.replace("\"", "");
        if (url.contains("http") || url.contains("https")) {
            return url;
        }
        return site.getUrl() + url;
    }

    public boolean hasLoaded() {
        return hasLoaded;
    }

    public String getBackgroundColour() {
        return backgroundColour;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public String getTextColour() {
        return textColour;
    }
}
