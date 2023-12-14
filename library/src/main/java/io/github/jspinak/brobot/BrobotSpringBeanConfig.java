package io.github.jspinak.brobot;

import org.sikuli.script.Pattern;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class BrobotSpringBeanConfig {

    public BrobotSpringBeanConfig() {
        new Pattern(); // loads OpenCV from SikuliX
    }
}
