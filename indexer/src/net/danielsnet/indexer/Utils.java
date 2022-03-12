package net.danielsnet.indexer;

import org.apache.commons.io.FilenameUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Utils {
    //public static final String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter SQL_DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public static String getURLDomain(String url) {
        URI urlObject = URIFromString(url);
        if (urlObject == null) {
            return null;
        }
        if (urlObject.getPort() == -1 || urlObject.getPort() == 80) {
            return urlObject.getHost();
        }
        return urlObject.getHost() + ":" + urlObject.getPort();
    }

    public static String getURLFileType(String url) {
        URI urlObject = URIFromString(url);
        if (urlObject == null) {
            return null;
        }
        return FilenameUtils.getExtension(urlObject.getPath());
    }

    public static String getURLFileName(String url) {
        URI urlObject = URIFromString(url);
        if (urlObject == null) {
            return null;
        }
        return FilenameUtils.getName(urlObject.getPath());
    }

    public static URI URIFromString(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    public static String filterNonAscii(String text) {
        return text.replaceAll("[^a-zA-Z0-9]", "");
    }

    public static String filterEmoji(String text) {
        return text.replace("/[\u2190-\u21FF]|[\u2600-\u26FF]|[\u2700-\u27BF]|[\u3000-\u303F]|[\u1F300-\u1F64F]|[\u1F680-\u1F6FF]/g", "");
    }

    public static String dateToSQL(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "1994-01-01 01:01:01";
        }
        return dateTime.format(SQL_DATE_FORMATTER);
    }

    public static LocalDateTime dateFromSQL(String sqlDateTime) {
        return LocalDateTime.parse(sqlDateTime, SQL_DATE_FORMATTER);
    }

    public static int randomNumber(int min, int max) {
        Random rand = new Random();
        return rand.nextInt(max) + min;
    }

    public static void threadSleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isSiteType(String type) {
        for (String knownType : StaticLists.getCategories().keySet()) {
            if (type.equalsIgnoreCase(knownType)) {
                return true;
            }
        }
        return false;
    }
}