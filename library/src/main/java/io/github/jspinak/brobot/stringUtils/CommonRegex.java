package io.github.jspinak.brobot.stringUtils;

public class CommonRegex {

    public static boolean isNumeric(String str){
        return str != null && str.matches("[0-9.]+");
    }

}
