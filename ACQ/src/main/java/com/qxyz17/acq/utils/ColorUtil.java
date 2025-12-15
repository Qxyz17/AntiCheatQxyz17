package com.qxyz17.acq.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class ColorUtil {
    
    public static final String[] GRADIENT_BLUE_PINK = {"#4A90E2", "#6A5ACD", "#9370DB", "#DA70D6", "#FF69B4"};
    public static final String[] GRADIENT_PURPLE_PINK = {"#8A2BE2", "#9932CC", "#BA55D3", "#DA70D6", "#FF69B4"};
    public static final String[] GRADIENT_OCEAN = {"#00B4DB", "#0083B0", "#005C97", "#003D73", "#001F4D"};
    public static final String[] GRADIENT_SUNSET = {"#FF512F", "#DD2476", "#FF5F6D", "#FFC371", "#FFAFBD"};
    
    public static String createGradient(String text, String[] colors) {
        if (text == null || text.isEmpty()) return text;
        
        text = ChatColor.stripColor(text);
        StringBuilder result = new StringBuilder();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            double progress = (double) i / (length - 1);
            String color = interpolateColor(colors, progress);
            result.append(net.md_5.bungee.api.ChatColor.of(color))
                  .append(text.charAt(i));
        }
        
        return result.toString();
    }
    
    private static String interpolateColor(String[] colors, double progress) {
        int segments = colors.length - 1;
        double segment = progress * segments;
        int segmentIndex = (int) Math.floor(segment);
        double segmentProgress = segment - segmentIndex;
        
        if (segmentIndex >= segments) {
            return colors[segments];
        }
        
        String color1 = colors[segmentIndex];
        String color2 = colors[segmentIndex + 1];
        
        return interpolateHexColor(color1, color2, segmentProgress);
    }
    
    private static String interpolateHexColor(String color1, String color2, double progress) {
        int r1 = Integer.parseInt(color1.substring(1, 3), 16);
        int g1 = Integer.parseInt(color1.substring(3, 5), 16);
        int b1 = Integer.parseInt(color1.substring(5, 7), 16);
        
        int r2 = Integer.parseInt(color2.substring(1, 3), 16);
        int g2 = Integer.parseInt(color2.substring(3, 5), 16);
        int b2 = Integer.parseInt(color2.substring(5, 7), 16);
        
        int r = (int) (r1 + (r2 - r1) * progress);
        int g = (int) (g1 + (g2 - g1) * progress);
        int b = (int) (b1 + (b2 - b1) * progress);
        
        return String.format("#%02X%02X%02X", r, g, b);
    }
    
    public static String createAnimatedText(String text, String[] colors, int offset) {
        StringBuilder result = new StringBuilder();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            int colorIndex = (i + offset) % colors.length;
            result.append(net.md_5.bungee.api.ChatColor.of(colors[colorIndex]))
                  .append(text.charAt(i));
        }
        
        return result.toString();
    }
    
    public static List<String> createGradientList(List<String> lines) {
        List<String> result = new ArrayList<>();
        int totalLines = lines.size();
        
        for (int i = 0; i < totalLines; i++) {
            double progress = (double) i / (totalLines - 1);
            String color = interpolateColor(GRADIENT_PURPLE_PINK, progress);
            result.add(net.md_5.bungee.api.ChatColor.of(color) + lines.get(i));
        }
        
        return result;
    }
}
