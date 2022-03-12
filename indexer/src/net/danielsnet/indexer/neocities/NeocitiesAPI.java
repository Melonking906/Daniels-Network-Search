package net.danielsnet.indexer.neocities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

public class NeocitiesAPI {
    private static final String SUCCESS = "success";
    public static final DateTimeFormatter NEOCITIES_DATE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

    private String result;
    private NeocitiesInfo info;

    public boolean hasLoaded() {
        return result.equals(SUCCESS);
    }


    public void setResult(String result) {
        this.result = result;
    }

    public NeocitiesInfo getInfo() {
        return info;
    }

    public void setInfo(NeocitiesInfo info) {
        this.info = info;
    }

    @JsonIgnoreProperties({"latest_ipfs_hash"})
    public class NeocitiesInfo {
        private String sitename;
        private int views;
        private int hits;
        private String created_at;
        private String last_updated;
        private String domain;
        private HashSet<String> tags;

        public String getSitename() {
            return sitename;
        }

        public void setSitename(String sitename) {
            this.sitename = sitename;
        }

        public int getViews() {
            return views;
        }

        public void setViews(int views) {
            this.views = views;
        }

        public int getHits() {
            return hits;
        }

        public void setHits(int hits) {
            this.hits = hits;
        }

        public LocalDateTime getCreated_at() {
            return LocalDateTime.parse(created_at, NEOCITIES_DATE_FORMATTER);
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public LocalDateTime getLast_updated() {
            //Fix for some sites that are created and never updated so have a null time
            if(last_updated == null || last_updated.equals("")) {
                return getCreated_at();
            }
            return LocalDateTime.parse(last_updated, NEOCITIES_DATE_FORMATTER);
        }

        public void setLast_updated(String last_updated) {
            this.last_updated = last_updated;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public HashSet<String> getTags() {
            return tags;
        }

        public void setTags(HashSet<String> tags) {
            this.tags = tags;
        }
    }
}