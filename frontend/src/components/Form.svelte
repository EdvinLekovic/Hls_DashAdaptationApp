<script>
    import videoService from "./repository/videoRepository";

    let frameSize = null;
    let fps = null;
    let aspectRatio = null;
    let gopLength = null;
    let videoBitRate = null;
    let audioBitRate = null;
    let conversionType = null;
    let uploadedVideo = null;

    $:console.log(frameSize);
    $:console.log(aspectRatio);
    $:console.log(uploadedVideo);

    let submitData = () => {
        videoService.sendVideoData(
            frameSize,
            fps,
            aspectRatio,
            gopLength,
            videoBitRate,
            audioBitRate,
            conversionType,
            uploadedVideo);
    }
</script>


<div class="d-flex flex-column align-items-center rounded">
    <form on:submit|preventDefault={submitData}
          class="d-flex flex-column bg-light shadow-lg rounded align-items-center border p-3 mt-4"
          style="width: 689px;">
        <h1 class="text-center">Adapt your video to HLS and DASH</h1>

        <div class="d-flex flex-column mt-3 w-75">
            <label>Frame size (WxH):</label>
            <div class="d-flex flex-column">
                <div class="btn-group" role="group" aria-label="Basic radio toggle button group">
                    <input type="radio"
                           class="btn-check"
                           id="btncheck1"
                           autocomplete="off"
                           value="1920x1080"
                           bind:group={frameSize}>
                    <label class="btn btn-outline-primary" for="btncheck1">1920x1080</label>
                    <input type="radio"
                           class="btn-check"
                           id="btncheck2"
                           autocomplete="off"
                           value="1280x720"
                           bind:group={frameSize}>
                    <label class="btn btn-outline-primary" for="btncheck2">1280x720</label>
                    <input type="radio"
                           class="btn-check"
                           id="btncheck3"
                           autocomplete="off"
                           value="720x480"
                           bind:group={frameSize}>
                    <label class="btn btn-outline-primary" for="btncheck3">720x480</label>
                    <input type="radio"
                           class="btn-check"
                           id="btncheck4"
                           autocomplete="off"
                           value="640x480"
                           bind:group={frameSize}>
                    <label class="btn btn-outline-primary" for="btncheck4">640x480</label>
                </div>
            </div>
        </div>
        <div class="mt-3 w-75">
            <label>Frames per second (fps):</label>
            <input type="number" name="fps" class="form-control" placeholder="Enter Frames per seconds"
                   bind:value={fps}>
        </div>
        <div class="d-flex flex-column mt-3 w-75">
            <label>Aspect ratio:</label>
            <div class="d-flex flex-column">
                <div class="btn-group" role="group" aria-label="Basic checkbox toggle button group">
                    <input type="radio"
                           class="btn-check"
                           id="btn_check1"
                           autocomplete="off"
                           value="4:3"
                           bind:group={aspectRatio}>
                    <label class="btn btn-outline-primary" for="btn_check1">4:3</label>
                    <input type="radio"
                           class="btn-check"
                           id="btn_check2"
                           autocomplete="off"
                           value="16:9"
                           bind:group={aspectRatio}>
                    <label class="btn btn-outline-primary" for="btn_check2">16:9</label>
                    <input type="radio"
                           class="btn-check"
                           id="btn_check3"
                           autocomplete="off"
                           value="5:4"
                           bind:group={aspectRatio}>
                    <label class="btn btn-outline-primary" for="btn_check3">5:4</label>
                    <input type="radio"
                           class="btn-check"
                           id="btn_check4"
                           autocomplete="off"
                           value="6:5"
                           bind:group={aspectRatio}>
                    <label class="btn btn-outline-primary" for="btn_check4">6:5</label>
                </div>
            </div>
        </div>
        <div class="mt-3 w-75">
            <label>GOP length:</label>
            <input type="number" name="gop" class="form-control" placeholder="Enter GOB length" bind:value={gopLength}>
        </div>
        <div class="mt-3 w-75">
            <label>Bit rate for the video:</label>
            <input type="number" name="videoBitRate" class="form-control"
                   placeholder="Enter the bit rate for video" bind:value={videoBitRate}>
        </div>
        <div class="mt-3 w-75">
            <label>Bit rate for the audio:</label>
            <input type="number" name="audioBitRate" class="form-control"
                   placeholder="Enter the bit rate for audio" bind:value={audioBitRate}>
        </div>
        <div class="mt-3 w-75">
            <label>Conversion type:</label>
            <select class="form-select" name="conversionType" bind:value={conversionType}>
                <option value="HLS_AND_DASH">HLS and DASH</option>
                <option value="HLS">HLS</option>
                <option value="DASH">DASH</option>
            </select>
        </div>
        <div class="mt-3 w-75">
            <label>Upload video:</label>
            <input type="file" bind:files={uploadedVideo}/>
        </div>
        <div class="mt-3 w-75">
            <button type="submit" class="btn btn-primary w-100">Convert video</button>
        </div>
    </form>
</div>