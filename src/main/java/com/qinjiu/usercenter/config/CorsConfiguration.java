package com.qinjiu.usercenter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局跨域配置
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {



    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                //允许哪些域访问
                .allowedOriginPatterns(
                        "https://match.qin-jiu.icu:8001/"
                        ,"https://match.qin-jiu.icu:3001/"
                        ,"http://localhost:4173/"
                        ,"http://localhost:3001/"
                        ,"http://localhost:8001/"
                        ,"https://match.qin-jiu.icu/"
                        )
                //允许哪些方法访问
                .allowedMethods("GET","POST","PUT","DELETE","HEAD","OPTIONS")
                //是否允许携带cookie
                .allowCredentials(true)
                //设置浏览器询问的有效期
                .maxAge(3600)
                .allowedHeaders("*");



    }
}
