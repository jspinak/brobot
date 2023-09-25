package io.github.jspinak.brobot.testingAUTs;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Component;

/**
 * For uploading files to a Render filesystem. The testing application should upload
 * MP4 and PNG files after completing a test run. Avoid uploading PNG files more than once
 * by checking the files on disk before uploading.
 */
@Component
public class RenderFileUploader {

    public void upload(String localFilePath, String remoteFilePath) {
        String host = "your-render-instance-hostname";
        String username = "your-username";
        String privateKeyPath = "path-to-your-private-key";

        JSch jsch = new JSch();
        Session session;

        try {
            jsch.addIdentity(privateKeyPath);
            session = jsch.getSession(username, host);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Specify local and remote file paths
            //String localFilePath = "path/to/local/file.txt";
            //String remoteFilePath = "path/to/render/disk/file.txt";

            // Upload the file
            channelSftp.put(localFilePath, remoteFilePath);

            channelSftp.disconnect();
            session.disconnect();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        }
    }
}

