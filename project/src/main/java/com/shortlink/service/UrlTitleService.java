package com.shortlink.service;

/**
 * @author LYT0905
 * @date 2024/03/06/21:36
 */
public interface UrlTitleService {

    /**
     * 获取链接标题
     * @param url
     * @return
     */
    public String getTitleByUrl(String url);
}
