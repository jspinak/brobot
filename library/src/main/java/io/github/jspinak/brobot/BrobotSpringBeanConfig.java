package io.github.jspinak.brobot;

import org.sikuli.script.ImagePath;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class BrobotSpringBeanConfig {

    public BrobotSpringBeanConfig() {
        // Set the system property to enable debug logging for org.bytedeco.javacpp
        //System.setProperty("org.bytedeco.javacpp.logger.debug", "true");
        //new Pattern(); // loads OpenCV from SikuliX
        ImagePath.setBundlePath("images"); // Brobot's default bundle path
        // set screen dimensions
        //Screen screen = new Screen();
        //ScreenOps.w = screen.w;
        //ScreenOps.h = screen.h;
    }
}
