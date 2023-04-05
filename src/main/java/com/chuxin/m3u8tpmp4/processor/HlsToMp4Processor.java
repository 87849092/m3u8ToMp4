package com.chuxin.m3u8tpmp4.processor;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.Loader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.chuxin.m3u8tpmp4.processor.VideoPusher.DEST_VIDEO_TYPE;

/**
 * @author 初心
 * @date 2023/3/24 20:32
 */
@Log4j2
public class HlsToMp4Processor {
    public static final String VIDEO_TYPE_MP4 = ".mp4";
    static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
    /**
     * 方法入口
     *
     * @param sourceVideoPath   视频源路径
     * @return
     */
    public static String process(String sourceVideoPath) {
        log.info("开始进行格式转换");
        if (!checkContentType(sourceVideoPath)) {
            log.info("请输入.m3u8格式的文件");
            return "";
        }
        // 获取文件名
        String destVideoPath = getFileName(sourceVideoPath)
                + "_" + SDF.format(new Date()) + DEST_VIDEO_TYPE;
        // 执行转换逻辑
        return processToMp4(sourceVideoPath, destVideoPath) ? destVideoPath : "";
    }

    private static String getFileName(String sourceVideoPath) {
        return sourceVideoPath.substring(sourceVideoPath.contains("/") ?
                        sourceVideoPath.lastIndexOf("/") + 1 : sourceVideoPath.lastIndexOf("\\") + 1,
                sourceVideoPath.lastIndexOf("."));
    }

    /**
     * 执行转换逻辑
     * @author saodisheng_liuxingwu
     * @modifyDate 2022/1/9
     */
    private static boolean processToMp4(String sourceVideoPath, String destVideoPath) {
        long startTime = System.currentTimeMillis();

        List<String> command = new ArrayList<String>();
        //获取JavaCV中的ffmpeg本地库的调用路径
        String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
        command.add(ffmpeg);
        // 设置支持的网络协议
        command.add("-protocol_whitelist");
        command.add("concat,file,http,https,tcp,tls,crypto");
        command.add("-i");
        command.add(sourceVideoPath);
        command.add(destVideoPath);
        try {
            Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
            fixedThreadPool.execute(new ReadStreamInfo(videoProcess.getErrorStream()));
            fixedThreadPool.execute(new ReadStreamInfo(videoProcess.getInputStream()));
            videoProcess.waitFor();

            log.info("中间转换已完成，生成文件：" + destVideoPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("用时:" + (int)((endTime - startTime) / 1000) + "秒");
        }
    }

    /**
     * 检验是否为m3u8文件
     * @author saodisheng_liuxingwu
     * @modifyDate 2022/1/9
     */
    private static boolean checkContentType(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return false;
        }
        String type = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        return "m3u8".equals(type);
    }
}
