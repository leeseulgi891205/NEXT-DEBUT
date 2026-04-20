package com.java.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * {@code spring.web.resources.add-mappings=false} 일 때 classpath 정적 파일을 쓰는 경로만 직접 연결한다.
	 * (기본 {@code /**} 가 켜 있으면 {@code /main} 이 정적 리소스 조회로 가 Spring Boot 4에서 404가 날 수 있음)
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
		registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
		// webapp/images 를 먼저 조회: 파일만 바꿔도 classpath(빌드 복사) 없이 즉시 반영. 없으면 classpath 정적 복사본 사용.
		registry.addResourceHandler("/images/**")
				.addResourceLocations("/images/", "classpath:/static/images/")
				.setCachePeriod(0);
		registry.addResourceHandler("/audio/**").addResourceLocations("classpath:/static/audio/");
		registry.addResourceHandler("/favicon.svg").addResourceLocations("classpath:/static/");
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/")
				.setCachePeriod(0);
	}

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/login",
                        "/signup",
                        "/signup/form",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/logout",
                        "/api/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/h2-console/**"
                );

        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns(
                        "/boards/*/write",
                        "/boards/*/edit",
                        "/boards/*/delete"
                )
                .excludePathPatterns(
                        "/login",
                        "/signup",
                        "/signup/form",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/logout",
                        "/api/**",        // 중복체크 API 인터셉터 제외
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/h2-console/**"  // H2 콘솔 인터셉터 제외
                );
    }
}
