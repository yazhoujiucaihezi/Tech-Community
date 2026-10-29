package com.box.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 天气服务：对外只暴露「给经纬度，返回天气」这一件事。
 */
public interface WeatherService {

    /**
     * 查询指定坐标的实况与逐小时预报
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 精简后的天气数据，包含 location / now / hourly
     */
    JsonNode getWeather(String longitude, String latitude);
}
