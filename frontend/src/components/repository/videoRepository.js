const videoService = {
    sendVideoData: (frameSize,
                    fps,
                    aspectRatio,
                    gopLength,
                    videoBitRate,
                    audioBitRate,
                    conversionType,
                    uploadedVideo) => {
        let formData = new FormData();
        formData.append("frameSize", frameSize);
        formData.append("fps", fps);
        formData.append("aspectRatio", aspectRatio);
        formData.append("gopLength", gopLength);
        formData.append("videoBitRate", videoBitRate);
        formData.append("audioBitRate", audioBitRate);
        formData.append("conversionType", conversionType);
        console.log(uploadedVideo[0].mozFullPath);
        formData.append("uploadedVideo", uploadedVideo[0]);

        fetch("http://localhost:8080/download-zip/video", {
            method: "POST",
            body: formData,
            responseType: 'arraybuffer'
        }).then((res) => { return res.blob(); })
            .then((data) => {
                var a = document.createElement("a");
                a.href = window.URL.createObjectURL(data);
                a.download = "video_adaptation";
                a.click();
            });
    }
}

export default videoService;