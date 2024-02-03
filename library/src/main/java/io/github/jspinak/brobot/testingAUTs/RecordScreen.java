package io.github.jspinak.brobot.testingAUTs;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

@Component
public class RecordScreen {
/*    private final FFmpegFrameGrabber grabber;
    private final FFmpegFrameRecorder recorder;
    private String outputFilePath;

    public RecordScreen() {
        Screen s = new Screen();
        int screenWidth = s.w;
        int screenHeight = s.h;
        int frameRate = 30;
        grabber = new FFmpegFrameGrabber("avfoundation");
        grabber.setFormat("avfoundation");
        grabber.setFrameRate(frameRate);
        grabber.setImageWidth(screenWidth);
        grabber.setImageHeight(screenHeight);

        recorder = new FFmpegFrameRecorder(outputFilePath, screenWidth, screenHeight);
        recorder.setFormat("avfoundation");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_BGR24); // Set the pixel format to BGR24 (adjust as needed)
        recorder.setFrameRate(frameRate);
    }

    public void startRecording(String outputFilePath, long maxRecordingDurationMs) {
        this.outputFilePath = outputFilePath;
        Thread recordingThread = new Thread(() -> {
            try {
                grabber.start();
                recorder.start();

                long startTime = System.currentTimeMillis();
                Frame frame;
                while ((frame = grabber.grabImage()) != null && (System.currentTimeMillis() - startTime < maxRecordingDurationMs)) {
                    recorder.record(frame);
                }

                stopRecording();

            } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        });
        recordingThread.start();
    }

    public void stopRecording() {
        // Stop and release the recorder and grabber
        try {
            recorder.stop();
            recorder.release();
            grabber.stop();
            grabber.release();

            System.out.println("Screen recording saved to: " + outputFilePath);
        } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

 */
}