package Application;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private static Locale currentLocale = Locale.FRENCH;

    public static Locale getCurrentLocale() { return currentLocale; }
    public static void setCurrentLocale(Locale locale) { currentLocale = locale; }
    public static ResourceBundle getBundle() { return ResourceBundle.getBundle("messages", currentLocale); }
    public static String getString(String key) { return getString(key, key); }
    public static String getString(String key, String defaultValue) {
        try { return getBundle().getString(key); }
        catch (Exception e) { return defaultValue; }
    }
}
