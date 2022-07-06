package io.github.jspinak.brobot.reports;

import io.github.jspinak.brobot.primatives.enums.StateEnum;

/**
 * Tests can be used for mocks or for live execution.
 * An example of an application that would benefit from
 *   live testing is a companion app that doesn't necessarily automate anything
 *   but watches a screen and gives indications of expected and unexpected, or
 *   successful and unsuccessful events. With live testing, most other console output should be
 *   deactivated since it will clutter up the console and push test output
 *   off the screen more quickly.
 */
public class TestOutput {

    public static void printValueComparison(String value1, String value2, String color) {
        System.out.print(value1 + ":" + color + value2 + " " + ANSI.RESET);
    }

    private static boolean assertError(int size) {
        if (size == 0) {
            Output.printColorLn("no values to compare", ANSI.YELLOW);
            return true;
        }
        if (size % 2 == 1) {
            Output.printColorLn("odd number of comparison values", ANSI.YELLOW);
            return true;
        }
        return false;
    }

    /**
     *
     * @param message A message to show what is being compared.
     * @param valuesToCompare Actual values should follow expected values.
     * @return true if the results match the expected values.
     */
    public static boolean assertTrue(String message, int... valuesToCompare) {
        String[] values = new String[valuesToCompare.length];
        for (int i=0; i<valuesToCompare.length; i++) values[i] = Integer.toString(valuesToCompare[i]);
        return assertTrue(message, values);
    }

    public static boolean assertTrue(String message, double... valuesToCompare) {
        String[] values = new String[valuesToCompare.length];
        for (int i=0; i<valuesToCompare.length; i++) values[i] = Double.toString(valuesToCompare[i]);
        return assertTrue(message, values);
    }

    public static boolean assertTrue(String message, boolean... valuesToCompare) {
        String[] values = new String[valuesToCompare.length];
        for (int i=0; i<valuesToCompare.length; i++) values[i] = Boolean.toString(valuesToCompare[i]);
        return assertTrue(message, values);
    }

    public static boolean assertTrue(String message, StateEnum... valuesToCompare) {
        String[] values = new String[valuesToCompare.length];
        for (int i=0; i<valuesToCompare.length; i++) values[i] = valuesToCompare[i].toString();
        return assertTrue(message, values);
    }

    public static boolean assertTrue(String message, String... valuesToCompare) {
        if (assertError(valuesToCompare.length)) return false;
        String color = ANSI.GREEN;
        int i = 0;
        while (i < valuesToCompare.length) {
            if (!valuesToCompare[i].equals(valuesToCompare[i+1])) {
                color = ANSI.RED;
                printValueComparison(valuesToCompare[i], valuesToCompare[i+1], ANSI.RED);
            } else {
                printValueComparison(valuesToCompare[i], valuesToCompare[i + 1], ANSI.GREEN);
            }
            i += 2;
        }
        Output.printColorLn(message, color);
        return color.equals(ANSI.GREEN);
    }
}
