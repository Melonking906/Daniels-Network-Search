package net.danielsnet.indexer;

import net.danielsnet.indexer.indexers.IndexRunnable;
import net.danielsnet.indexer.neocities.NeocitiesScrapeRunnable;
import net.danielsnet.indexer.scrapers.TildeClub;
import net.danielsnet.indexer.scrapers.TildeTown;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

public class Main {
    private static final DatabaseConnect database = new DatabaseConnect();

    public static void main(String[] args) {
        UI.log("Daniels Net Indexer is coming online!");

        database.connect();
        UI.log(database.getVersion());

        if (database.test()) {
            UI.log("The database is online!");
        } else {
            UI.log("The database is offline!");
            return;
        }

        disableSSLCerts();

        UI.log("Starting Interface Thread");
        new Thread(new UI()).start();

       UI.log("Starting Indexer Threads");
       new Thread(new IndexRunnable()).start();

       UI.log("Starting Neocities Harvest Thread");
       new Thread(new NeocitiesScrapeRunnable()).start();

       UI.log("Starting Tilde.Town Harvest Thread");
       new Thread(new TildeTown()).start();

        UI.log("Starting Tilde.Club Harvest Thread");
        new Thread(new TildeClub()).start();
    }

    public static DatabaseConnect getDatabase() {
        return database;
    }

    private static void disableSSLCerts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
        }
    }
}
