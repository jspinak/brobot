package reports;

import org.springframework.stereotype.Component;

@Component
public class Output {

    // special characters
    public static char check = '\u2713';
    public static char fail = '\u2718';

    public static void printColor(String message, String color) {
        System.out.print("| " + color + message + ANSI.RESET);
    }

    public static void printColorLn(String message, String color) {
        System.out.println("| " + color + message + ANSI.RESET);
    }

}
