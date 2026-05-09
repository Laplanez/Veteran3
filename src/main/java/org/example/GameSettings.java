package org.example;

public class GameSettings {
    public static boolean fullscreen = false;
    public static double matchSpeed = 1.0;   // 0.5 = yavaş, 2.0 = hızlı
    public static boolean autoScrollLogs = true;
    public static boolean showGoalAnimations = true;


    public static void sleep(long baseMs) {
        try {
            long ms = (long) (baseMs / Math.max(0.1, matchSpeed));
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
