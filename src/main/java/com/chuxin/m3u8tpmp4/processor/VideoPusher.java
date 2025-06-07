package com.chuxin.m3u8tpmp4.processor;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author 初心
 * @date 2023/3/24 20:36
 */
public class VideoPusher {
    /** 采集器 **/
    private FFmpegFrameGrabber grabber;
    /** 录制器 **/
    private FFmpegFrameRecorder recorder;

    static final String DEST_VIDEO_TYPE = ".mp4";

    public VideoPusher() {
        // 在FFmpegFrameGrabber.start()之前设置FFmpeg日志级别
        avutil.av_log_set_level(avutil.AV_LOG_INFO);
        FFmpegLogCallback.set();
    }

    /**
     * 处理视频源
     * 输入流和输出地址必须有一个是有效输入
     * @param inputStream   输入流
     * @param inputAddress  输入地址
     * @return
     */
    public VideoPusher from(InputStream inputStream, String inputAddress) {
        if (inputStream != null) {
            grabber = new FFmpegFrameGrabber(inputStream);
        } else if (StringUtils.isNotEmpty(inputAddress)) {
            grabber = new FFmpegFrameGrabber(inputAddress);
        } else {
            throw new RuntimeException("视频源为空错误，请确定输入有效视频源");
        }

        // 开始采集
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    public VideoPusher from(InputStream inputStream) {
        return from(inputStream, null);
    }

    public VideoPusher from(String inputAddress) {
        return from(null, inputAddress);
    }

    /**
     * 设置输出
     *
     * @param outputStream
     * @param outputAddress
     * @return
     */
    public VideoPusher to(OutputStream outputStream, String outputAddress) {
        if (outputStream != null) {
            recorder = new FFmpegFrameRecorder(outputStream, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
        } else if (StringUtils.isNotEmpty(outputAddress)) {
            recorder = new FFmpegFrameRecorder(outputAddress, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
        } else {
            throw new RuntimeException("输入路径为空错误，请指定正确输入路径或输出流");
        }
        // 设置格式
        recorder.setFormat("mp4");

        //recorder.setOption("method", "POST");
        recorder.setOption("movflags", "frag_keyframe+empty_moov");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

        // 开始录制
        try {
            recorder.start(grabber.getFormatContext());
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public VideoPusher to(OutputStream outputStream) {
        return to(outputStream, null);
    }

    public VideoPusher to(String outputAddress) {
        return to(null, outputAddress);
    }


    /**
     * 转封装，推送流
     */
    public void go() {
        AVPacket pkt;
        try {
            while ((pkt = grabber.grabPacket()) != null) {
                recorder.recordPacket(pkt);
            }
        } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
            e.printStackTrace();
        }finally {
            // 确保资源一定被关闭
            close();
        }

    }

    public void close() {
        try {
            if (recorder != null) {
                // 确保写入 moov atom
                recorder.stop();
                recorder.close();
            }
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (grabber != null) {
                    // 因为grabber的close调用了stop和release，而stop也是调用了release，为了防止重复调用，直接使用release
                    grabber.release();
                }
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }
    }
}
