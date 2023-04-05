package net.nussi;

import kong.unirest.*;
import com.fazecast.jSerialComm.*;

import java.util.Scanner;

public class Main {
    public static final String titleText =
            "  _      ____  _         _____      _                _             \n" +
                    " | |    / __ \\| |       / ____|    | |              | |            \n" +
                    " | |   | |  | | |      | (___   ___| |__   ___   ___| | _____ _ __ \n" +
                    " | |   | |  | | |       \\___ \\ / __| '_ \\ / _ \\ / __| |/ / _ \\ '__|\n" +
                    " | |___| |__| | |____   ____) | (__| | | | (_) | (__|   <  __/ |   \n" +
                    " |______\\____/|______| |_____/ \\___|_| |_|\\___/ \\___|_|\\_\\___|_|   \n" +
                    "                                                                   \n" +
                    "                                                                   ";

    public static String activePlayerName;
    public static boolean activePlayerNameLoop = true;

    public static boolean mainLoop = true;

    public static SummonerStats summonerStats;
    public static SummonerStats oldSummonerStats;

    public static SerialPort serialPort;

    public static void main(String[] args) throws InterruptedException {

        Unirest.config().verifySsl(false);
        Unirest.config().connectTimeout(1000);


        serialPort = getSerialPort();
        serialPort.openPort();


        while (true) {



            printDashboard();

            StepRetrivePlayerName();

            StepRetrievePlayerStats();

            activePlayerNameLoop = true;
            mainLoop = true;
            summonerStats = null;
            oldSummonerStats = null;
        }


    }

    private static void StepRetrievePlayerStats() {


        while (mainLoop) {
            printDashboard();

            try {
                Unirest.get("https://127.0.0.1:2999/liveclientdata/playerscores?summonerName=" + activePlayerName)
                        .asJson()
                        .ifFailure(failureResponse -> {
                            System.out.print("Waiting for game...");
                        })
                        .ifSuccess(successResponse -> {
                            try {
                                int deaths = successResponse.getBody().getObject().getInt("deaths");

                                int assists = successResponse.getBody().getObject().getInt("assists");

                                int kills = successResponse.getBody().getObject().getInt("kills");


                                int creepScore = successResponse.getBody().getObject().getInt("creepScore");

                                double wardScore = successResponse.getBody().getObject().getDouble("wardScore");

                                oldSummonerStats = summonerStats;
                                summonerStats = new SummonerStats(
                                        assists,
                                        creepScore,
                                        deaths,
                                        kills,
                                        wardScore
                                );
                                if (oldSummonerStats == null) oldSummonerStats = summonerStats;

                                if (summonerStats.assists() > oldSummonerStats.assists()) {
                                    onAssist();
                                }
                                if (summonerStats.kills() > oldSummonerStats.kills()) {
                                    onKill();
                                }
                                if (summonerStats.deaths() > oldSummonerStats.deaths()) {
                                    onDeath();
                                }


                            } catch (Exception e) {
                                mainLoop = false;
                                System.out.println("Failed to parse json stats!");
                            }
                        });
            } catch (Exception e) {
                mainLoop = false;
                System.out.println("Game end!");
            }

        }

    }

    public static void printDashboard() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            builder.append("\n");
        }


        builder.append(titleText + "\n");

        builder.append("Summoner: " + activePlayerName + "\n");
        builder.append("---------------------------------------\n");

        if (summonerStats == null) {
            builder.append("Waiting for stats...\n");
        } else {
            builder.append("Kills:   " + summonerStats.kills() + "\n");
            builder.append("Deaths:  " + summonerStats.deaths() + "\n");
            builder.append("Assists: " + summonerStats.assists() + "\n");

            builder.append("CS:      " + summonerStats.creepScore() + "\n");
            builder.append("Vision:  " + summonerStats.wardScore() + "\n");
        }

        builder.append("---------------------------------------\n");

        System.out.println(builder);
    }

    public static SerialPort getSerialPort() {

        int i = 0;
        for(SerialPort serialPort : SerialPort.getCommPorts()) {
            System.out.println("[" + i + "] " + serialPort);
        }


        System.out.println("Type the index number of the comport:");
        Scanner scanner = new Scanner(System.in);
        String inputLine = scanner.nextLine();
        int input = Integer.parseInt(inputLine);
        scanner.close();
        System.out.println("OK");
        return SerialPort.getCommPorts()[input];
    }

    public static void onDeath() {

        serialPort.writeBytes(new byte[]{0x00}, 1);


        System.out.println("OnDeath");
    }

    public static void onAssist() {

    }

    public static void onKill() {
        System.out.println("OnKill");
    }


    public static void StepRetrivePlayerName() throws InterruptedException {

        System.out.println("Fetching player name...");
        while (activePlayerNameLoop) {
            try {
                Unirest.get("https://127.0.0.1:2999/liveclientdata/activeplayername")
                        .asString()
                        .ifFailure(failureResponse -> {
                        }).ifSuccess(successResponse -> {
                            activePlayerName = successResponse.getBody();
                            activePlayerName = activePlayerName.substring(1, activePlayerName.length() - 1);

                            if (activePlayerName.trim().length() > 4) {
                                activePlayerNameLoop = false;
                            }
                        });
            } catch (Exception e) {
                System.out.print("\rWaiting for round to begin...");
                Thread.sleep(300);
            }
        }

        System.out.println();
        System.out.println("Active Player: " + activePlayerName);
    }

}