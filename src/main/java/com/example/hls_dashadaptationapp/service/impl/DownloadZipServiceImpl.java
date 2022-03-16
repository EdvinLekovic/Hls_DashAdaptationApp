package com.example.hls_dashadaptationapp.service.impl;

import com.example.hls_dashadaptationapp.service.DownloadZipService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DownloadZipServiceImpl implements DownloadZipService {

    private final String UPLOAD_VIDEO_PATH = "C:/Users/edvin/Videos/hlsAndDash/";
    private static final Semaphore enterWritingVideo = new Semaphore(2);

    @Override
    public void downloadZippedVideos(HttpServletResponse response,
                                     String frameSize,
                                     String fps,
                                     String aspectRatio,
                                     String gopLength,
                                     String videoBitRate,
                                     String audioBitRate,
                                     ConversionType conversionType,
                                     MultipartFile uploadedVideo) throws IOException, InterruptedException {
        String videoLocation = saveVideoFile(uploadedVideo);
        String folderLocation = UPLOAD_VIDEO_PATH + uploadedVideo.getOriginalFilename();
        //wait until you write videos for dash and hls
        writeAndExecuteFfmpegScript(frameSize, fps, aspectRatio, gopLength, videoBitRate, audioBitRate, conversionType, videoLocation, folderLocation);
        enterWritingVideo.acquire(2);
        System.out.println("Finished with writing scripts");
        enterWritingVideo.release(2);
        zipFolder(folderLocation, conversionType);
        enterWritingVideo.acquire(2);
        downloadZipFile(response, folderLocation, conversionType);
        enterWritingVideo.release(2);
        System.out.println("Finishing with zipping the files");
    }

    private String saveVideoFile(MultipartFile uploadedVideo) throws IOException {
        System.out.println(uploadedVideo.getOriginalFilename());
        System.out.println(uploadedVideo.getName());
        File file = new File(UPLOAD_VIDEO_PATH + uploadedVideo.getOriginalFilename());
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new IOException();
            }
            Path path = Paths.get(UPLOAD_VIDEO_PATH + uploadedVideo.getOriginalFilename() + "\\" + uploadedVideo.getOriginalFilename());
            Files.write(path, uploadedVideo.getBytes());
        }
        return UPLOAD_VIDEO_PATH + uploadedVideo.getOriginalFilename() + "/" + uploadedVideo.getOriginalFilename();
    }

    private void writeAndExecuteFfmpegScript(String frameSize,
                                             String fps,
                                             String aspectRatio,
                                             String gopLength,
                                             String videoBitRate,
                                             String audioBitRate,
                                             ConversionType conversionType,
                                             String videoLocation,
                                             String folderLocation) throws IOException, InterruptedException {

        if (frameSize != null) {
            frameSize = String.format(" -s:v:0 %s ", frameSize);
        }
        if (fps != null) {
            fps = String.format(" -r:v:0 %s ", fps);
        }
        if (aspectRatio != null) {
            aspectRatio = String.format(" -aspect:v:0 %s ", aspectRatio);
        }
        if (gopLength != null) {
            gopLength = String.format(" -g %s ", gopLength);
        }
        if (videoBitRate != null) {
            videoBitRate = String.format(" -b:v:0 %sk ", videoBitRate);
        }
        if (audioBitRate != null) {
            audioBitRate = String.format(" -c:a:0 aac -ac 2 -ab %sk ", audioBitRate);
        }

        boolean hls_and_dash = conversionType.toString().equals(ConversionType.HLS_AND_DASH.toString());
        if (conversionType.toString().equals(ConversionType.HLS.toString()) ||
                hls_and_dash) {
            //HLS
            /*ffmpeg -y -i first_video.mp4 -g 48 -sc_threshold 0 -map 0:0 -map 0:1 -c:v:0 libx264 -aspect:v:0 4:3 -r:v:0 24 -s:v:0 640x480 -b:v:0 600k -c:a:0 aac -ac 2 -ab 128k -var_stream_map "v:0,a:0" -master_pl_name hls_master.m3u8 -f hls -hls_time 6 -hls_list_size 0 -hls_segment_filename "video%v/mmm%d.ts" video%v/prog_index.m3u8*/
            String hls_ffmpeg = "ffmpeg -y -i " + videoLocation + gopLength + " -sc_threshold 0 -map 0:0 -map 0:1 -c:v:0 libx264 " + aspectRatio + fps + frameSize + videoBitRate + audioBitRate + "-var_stream_map \"v:0,a:0\" -master_pl_name " + folderLocation + "/hls/hls_master.m3u8 -f hls -hls_time 6 -hls_list_size 0 -hls_segment_filename " + folderLocation + "\"/hls/video%v/mmm%d.ts\" " + folderLocation + "/hls/video%v/prog_index.m3u8";
            //transform the video in hls version
            threadExec(hls_ffmpeg, "hls");
        }
        if (conversionType.toString().equals(ConversionType.DASH.toString()) ||
                hls_and_dash) {
            //DASH
            //ffmpeg -y -i first_video.mp4 -g 48 -sc_threshold 0 -map 0:0 -map 0:1 -c:v:0 libx264 -s:v:0 640x480 -aspect:v:0 4:3 -r:v:0 25 -b:v:0 600k -c:a:0 aac -ac 2 -ab 128k -use_template 1 -window_size 5 -adaptation_sets "id=0,streams=v id=1,streams=a" -f dash video1/dash_master.mpd
            File file = new File(folderLocation + "/dash");
            if (!file.exists()) {
                file.mkdirs();
            }
            String dash_ffmpeg = "ffmpeg -y -i " + videoLocation + gopLength + " -sc_threshold 0 -map 0:0 -map 0:1 -c:v:0 libx264 " + frameSize + aspectRatio + fps + videoBitRate + audioBitRate + " -window_size 5 -adaptation_sets \"id=0,streams=v id=1,streams=a\" -f dash " + folderLocation + "/dash/dash_master.mpd";
            //transform the video in dash version
            threadExec(dash_ffmpeg, "dash");
        }
    }

    private void threadExec(String ffmpegCommand, String videoType) throws IOException, InterruptedException {
        enterWritingVideo.acquire();
        Process p2 = Runtime.getRuntime().exec(ffmpegCommand);
        new Thread(() -> {

            Scanner sc = new Scanner(p2.getErrorStream());

            // Find duration
            Pattern durPattern = Pattern.compile("(?<=Duration: )[^,]*");
            String dur = sc.findWithinHorizon(durPattern, 0);
            if (dur == null)
                throw new RuntimeException("Could not parse duration.");
            String[] hms = dur.split(":");
            double totalSecs = Integer.parseInt(hms[0]) * 3600
                    + Integer.parseInt(hms[1]) * 60
                    + Double.parseDouble(hms[2]);
            System.out.println("Total duration: " + totalSecs + " seconds.");

            // Find time as long as possible.
            Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
            String[] matchSplit;
            String match;
            while (null != (match = sc.findWithinHorizon(timePattern, 0))) {
                matchSplit = match.split(":");
                double progress = (Integer.parseInt(matchSplit[0]) * 3600 +
                        Integer.parseInt(matchSplit[1]) * 60 +
                        Double.parseDouble(matchSplit[2])) / totalSecs;
                System.out.printf("Progress %s: %.2f%%%n", videoType, progress * 100);
            }
            //Finish with writing the video chunks for appropriate video type
            enterWritingVideo.release();
        }).start();
    }

    private void downloadZipFile(HttpServletResponse response, String folderLocation, ConversionType conversionType) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=hls_dash_videos.zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            zipFolders(folderLocation, conversionType, zipOutputStream);
            zipOutputStream.finish();
        } catch (IOException e) {
            System.out.println("Can't zip the file");
        }
    }

    private void zipFolder(String folderLocation, ConversionType conversionType) throws IOException, InterruptedException {
        boolean hls_and_dash = conversionType.toString().equals(ConversionType.HLS_AND_DASH.toString());
        System.out.println(folderLocation);
        if (hls_and_dash || conversionType.toString().equals(ConversionType.DASH.toString())) {
            String filesLocation = folderLocation + "/dash/";
            String zipFile = "tar.exe -a -c -f " + folderLocation + "/dash.zip " + filesLocation + "*.m4s " + filesLocation + "*.mpd";
            Process p = Runtime.getRuntime().exec(zipFile);
            p.waitFor();
        }
        if (hls_and_dash || conversionType.toString().equals(ConversionType.HLS.toString())) {
            String filesLocation = folderLocation + "/hls/video0/";
            String zipFile = "tar.exe -a -c -f " + folderLocation + "/hls.zip " + filesLocation + "*.ts " + filesLocation + "*.m3u8";
            Process p = Runtime.getRuntime().exec(zipFile);
            p.waitFor();
        }
    }

    private void zipFolders(String folderLocation, ConversionType conversionType, ZipOutputStream zipOutputStream) throws IOException {
        List<FileSystemResource> zipFiles = new ArrayList<>();
        boolean hls_and_dash = conversionType.toString().equals(ConversionType.HLS_AND_DASH.toString());
        if (hls_and_dash || conversionType.toString().equals(ConversionType.DASH.toString())) {
            zipFiles.add(new FileSystemResource(folderLocation + "/dash.zip"));
        }
        if (hls_and_dash || conversionType.toString().equals(ConversionType.HLS.toString())) {
            zipFiles.add(new FileSystemResource(folderLocation + "/hls.zip"));
        }

        for (FileSystemResource f : zipFiles) {
            try {
                ZipEntry zipEntry = new ZipEntry(f.getFilename());
                zipEntry.setSize(f.contentLength());
                zipEntry.setTime(System.currentTimeMillis());
                zipOutputStream.putNextEntry(zipEntry);
                StreamUtils.copy(f.getInputStream(), zipOutputStream);
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                System.out.println("Can't zip the files");
            }
        }
    }
}
