package com.chuxin.m3u8tpmp4.processor;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author 初心
 * @date 2023/3/24 20:35
 */
@Log4j2
public class ReadStreamInfo implements Runnable{
    final InputStream is;
    public ReadStreamInfo(InputStream is) {
        this.is = is;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                log.info("[FFmpeg] " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
