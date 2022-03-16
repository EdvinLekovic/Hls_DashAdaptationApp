package com.example.hls_dashadaptationapp.web;

import com.example.hls_dashadaptationapp.service.DownloadZipService;
import com.example.hls_dashadaptationapp.service.impl.ConversionType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/download-zip")
@CrossOrigin("http://localhost:5000")
public class DownloadZipController {

    private final DownloadZipService downloadZipService;


    public DownloadZipController(DownloadZipService downloadZipService) {
        this.downloadZipService = downloadZipService;
    }

    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = "application/zip")
    public void downloadHlsDashVideos(HttpServletResponse response,
                                      @RequestParam String frameSize,
                                      @RequestParam String fps,
                                      @RequestParam String aspectRatio,
                                      @RequestParam String gopLength,
                                      @RequestParam String videoBitRate,
                                      @RequestParam String audioBitRate,
                                      @RequestParam ConversionType conversionType,
                                      @RequestParam MultipartFile uploadedVideo) throws IOException, InterruptedException {
        System.out.println(uploadedVideo);
        downloadZipService.downloadZippedVideos(response, frameSize, fps, aspectRatio, gopLength, videoBitRate, audioBitRate, conversionType, uploadedVideo);
    }
}
