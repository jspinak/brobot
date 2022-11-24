package io.github.jspinak.brobot.reports;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class Output {

    // special characters
    public static String check = "✓"; //'\u2713';
    public static String fail = "✘"; //'\u2718';

    public static void printColor(String message, String... colors) {
        //System.out.print("| ");
        Arrays.stream(colors).forEach(System.out::print);
        System.out.print(message + ANSI.RESET);
    }

    public static void printColorLn(String message, String... colors) {
        printColor(message, colors);
        System.out.println();
    }

}
