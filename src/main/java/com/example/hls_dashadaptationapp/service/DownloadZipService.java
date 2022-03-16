package com.example.hls_dashadaptationapp.service;

import com.example.hls_dashadaptationapp.service.impl.ConversionType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface DownloadZipService {
    void downloadZippedVideos(HttpServletResponse response,
                              String frameSize,
                              String fps,
                              String aspectRatio,
                              String gopLength,
                              String videoBitRate,
                              String audioBitRate,
                              ConversionType conversionType,
                              MultipartFile uploadedVideo) throws IOException, InterruptedException;
}
