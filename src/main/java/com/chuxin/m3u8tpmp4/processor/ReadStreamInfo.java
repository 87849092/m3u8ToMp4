package com.chuxin.m3u8tpmp4.processor;

import java.io.InputStream;

/**
 *  在用Runtime.getRuntime().exec()或ProcessBuilder(array).start()创建子进程Process之后，
 *  一定要及时取走子进程的输出信息和错误信息，否则输出信息流和错误信息流很可能因为信息太多导致被填满，
 *  最终导致子进程阻塞住，然后执行不下去。
 * @author 初心
 * @date 2023/3/24 20:35
 */
public class ReadStreamInfo extends Thread{
    InputStream is = null;
    public ReadStreamInfo(InputStream is) {
        this.is = is;
    }

    @Override
    public void run() {
        try {
            while(true) {
                int ch = is.read();
                if(ch != -1) {
                    System.out.print((char)ch);
                } else {
                    break;
                }
            }
            if (is != null) {
                is.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
