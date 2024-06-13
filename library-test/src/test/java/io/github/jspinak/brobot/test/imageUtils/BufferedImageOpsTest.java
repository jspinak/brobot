package io.github.jspinak.brobot.test.imageUtils;

import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BufferedImageOpsTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void getBuffImgFromFile() {
        BufferedImage bufferedImage = BufferedImageOps.getBuffImgFromFile("topLeft");
        assertNotNull(bufferedImage);
    }

    @Test
    void getBuffImgDirectly() {
    }

    @Test
    void getBuffImgFromScreen() {
    }

    @Test
    void getBuffImgsFromScreen() {
    }

    @Test
    void convertTo3ByteBGRType() {
    }

    @Test
    void convert() {
    }

    @Test
    void testConvert() {
    }

    @Test
    void fromMat() {
    }

    @Test
    void toByteArray() {
    }

    @Test
    void fromByteArray() {
    }

    @Test
    void getSubImage() {
    }

    @Test
    void testGetSubImage() {
    }

    @Test
    void encodeImage() {
    }

    @Test
    void base64StringToImage() {
    }
}