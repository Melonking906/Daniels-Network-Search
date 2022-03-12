package net.danielsnet.indexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UI implements Runnable {
    public static void log(String text) {
        System.out.println(text);
    }

    @Override
    public void run() {
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try {
                processCommand(br.readLine());
            } catch (IOException ex) {
                log("Console error!");
            }
        }
    }

    private void processCommand(String command) {
        if (command == null) {
            return;
        }

        String[] parts = command.split(" ");
        if (parts.length < 1) {
            log(" ");
            return;
        }

        if (parts[0].equals("stop")) {
            Settings.Flag_DoIndex = false;
            UI.log("Stopping the index.");
        } else {
            log("That command is unknown!");
        }
    }
}