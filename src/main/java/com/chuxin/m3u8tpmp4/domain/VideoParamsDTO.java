package com.chuxin.m3u8tpmp4.domain;

import lombok.*;

import java.io.Serializable;

/**
 * @author 初心
 * @date 2023/3/24 20:38
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class VideoParamsDTO implements Serializable {
    /** 视频源地址 .m3u8格式 **/
    private String sourceVideoUrl;

    /** 推送目标地址 **/
    private String destVideoPath;
}
