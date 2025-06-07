package com.chuxin.m3u8tpmp4.controller;

import com.chuxin.m3u8tpmp4.domain.VideoParamsDTO;
import com.chuxin.m3u8tpmp4.processor.HlsToMp4Processor;
import com.chuxin.m3u8tpmp4.processor.VideoPusher;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * @author 初心
 * @date 2023/3/24 20:37
 */
@RestController
@Log4j2
public class M3u8ToMp4Controller {
    @PostMapping("convert_to_mp4/")
    public void convertToMp4(@RequestBody VideoParamsDTO dataVO) {
        String sourceVideoUrl = dataVO.getSourceVideoUrl();
        Assert.notNull(sourceVideoUrl, "视频源不能为空");

        // 将m3u8格式视频转为mp4本地文件（用于转换格式的中间文件）
        String destFileName = HlsToMp4Processor.process(sourceVideoUrl, dataVO.getVideoName());
        if (StringUtils.isEmpty(destFileName)) {
            log.error("操作失败");
        }

        // 推送流
        if(StringUtils.isNotEmpty(dataVO.getDestVideoPath())) {
            try {
                new VideoPusher().from(destFileName).to(dataVO.getDestVideoPath() + destFileName).go();
            }finally {
                // 删除中间文件
                new File(destFileName).deleteOnExit();
            }
        }
    }
}
