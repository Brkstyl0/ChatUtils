package com.chatutils.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtil {

    private static final Pattern TIME_PATTERN = Pattern.compile("(?i)^(\\d+)\\s*([smhdw]|sn|dk|sa|gun|gün)?$");

    private TimeUtil() {}

    /**
     * Süre dizgisini (örn: 10m, 2h, 1d, 30s, kalici) milisaniyeye dönüştürür.
     * @param input Süre dizgisi
     * @return Milisaniye cinsinden süre. Kalıcı ise -1L döner. Geçersiz ise null döner.
     */
    public static Long parseDuration(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String lower = input.trim().toLowerCase();
        if (lower.equals("kalici") || lower.equals("kalıcı") || lower.equals("perm") || lower.equals("permanent") || lower.equals("-1") || lower.equals("sonsuz")) {
            return -1L;
        }

        Matcher matcher = TIME_PATTERN.matcher(lower);
        if (!matcher.matches()) {
            // Eğer sadece sayı girilmişse dakika olarak varsayalım
            try {
                long num = Long.parseLong(lower);
                return num * 60 * 1000L;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        try {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            if (unit == null || unit.isEmpty() || unit.equals("m") || unit.equals("dk")) {
                return value * 60 * 1000L; // Dakika
            }

            switch (unit.toLowerCase()) {
                case "s":
                case "sn":
                    return value * 1000L;
                case "h":
                case "sa":
                    return value * 60 * 60 * 1000L;
                case "d":
                case "gun":
                case "gün":
                    return value * 24 * 60 * 60 * 1000L;
                case "w":
                    return value * 7 * 24 * 60 * 60 * 1000L;
                default:
                    return value * 60 * 1000L;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Milisaniyeyi Türkçe okunabilir süreye çevirir (Örn: 2 gün 5 saat, 15 dakika).
     */
    public static String formatDuration(long millis) {
        if (millis < 0) {
            return "Kalıcı";
        }
        if (millis == 0) {
            return "0 saniye";
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" gün ");
        }
        if (hours > 0) {
            sb.append(hours).append(" saat ");
        }
        if (minutes > 0 && days == 0) { // Gün varsa dakikayı gizleyip sade tutabiliriz
            sb.append(minutes).append(" dakika ");
        }
        if (seconds > 0 && days == 0 && hours == 0) {
            sb.append(seconds).append(" saniye");
        }

        String result = sb.toString().trim();
        return result.isEmpty() ? "1 saniyeden az" : result;
    }

    /**
     * Bitiş zamanına göre kalan süreyi hesaplar.
     */
    public static String formatRemaining(long endTimestamp) {
        if (endTimestamp <= -1L) {
            return "Kalıcı";
        }
        long diff = endTimestamp - System.currentTimeMillis();
        if (diff <= 0) {
            return "Süresi Doldu";
        }
        return formatDuration(diff);
    }

    /**
     * Zaman damgasını biçimlendirilmiş tarih metnine çevirir.
     */
    public static String formatDate(long timestamp, String pattern) {
        if (timestamp <= 0) return "-";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern != null ? pattern : "dd.MM.yyyy HH:mm:ss");
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return new Date(timestamp).toString();
        }
    }
}
