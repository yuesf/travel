package com.travel.config;

import com.travel.common.Result;
import com.travel.common.ResultCode;
import com.travel.mapper.AdminUserMapper;
import com.travel.mapper.UserMapper;
import com.travel.util.JwtUtil;
import com.travel.service.AdminAuthService;
import com.travel.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Spring Security配置
 * 
 * @author travel-platform
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    @Lazy
    private AdminAuthService adminAuthService;
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    @Lazy
    private com.travel.service.UserAuthService userAuthService;
    
    @Autowired
    private com.travel.config.WechatConfig wechatConfig;
    
    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Security过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF
            .csrf(csrf -> csrf.disable())
            // 禁用Session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置请求授权（注意：Spring Security 按顺序匹配，先匹配的规则优先）
            .authorizeHttpRequests(auth -> auth
                // 允许匿名访问的 API 接口（必须先配置，否则会被下面的 /api/** 拦截）
                .requestMatchers("/api/v1/admin/auth/login").permitAll()
                .requestMatchers("/api/v1/admin/init/**").permitAll()
                .requestMatchers("/api/v1/miniprogram/auth/**").permitAll()
                .requestMatchers("/api/v1/miniprogram/home/**").permitAll()
                .requestMatchers("/api/v1/miniprogram/config/**").permitAll()
                .requestMatchers("/api/v1/miniprogram/categories").permitAll()
                .requestMatchers("/api/v1/miniprogram/attractions").permitAll()
                .requestMatchers("/api/v1/miniprogram/attractions/**").permitAll()
                .requestMatchers("/api/v1/miniprogram/hotels").permitAll()
                .requestMatchers("/api/v1/miniprogram/hotels/**").permitAll()
                .requestMatchers("/api/v1/miniprogram/products").permitAll()
                .requestMatchers("/api/v1/miniprogram/products/**").permitAll()
                // 前端静态资源允许匿名访问（JS、CSS、图片等）
                .requestMatchers("/assets/**").permitAll()
                .requestMatchers("/*.js", "/*.css", "/*.ico", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif", "/*.svg", 
                               "/*.woff", "/*.woff2", "/*.ttf", "/*.eot", "/*.map").permitAll()
                // 静态资源访问允许匿名访问（上传的文件）
                .requestMatchers("/uploads/**").permitAll()
                // 小程序静态资源路径
                .requestMatchers("/static/**").permitAll()
                // SpringDoc OpenAPI文档允许匿名访问
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/doc.html").permitAll()
                // 健康检查允许匿名访问
                .requestMatchers("/health", "/actuator/health").permitAll()
                // 其他 API 接口需要认证
                .requestMatchers("/api/**").authenticated()
                // 所有其他请求（包括前端路由）允许匿名访问，由前端路由处理认证
                .anyRequest().permitAll()
            )
            // 添加小程序Session过滤器（先执行，处理小程序接口，使用session机制）
            .addFilterBefore(new MiniprogramSessionAuthenticationFilter(userAuthService, userMapper, wechatConfig), 
                           UsernamePasswordAuthenticationFilter.class)
            // 添加后台管理JWT过滤器（后执行，处理后台管理接口）
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, adminAuthService, adminUserMapper, roleService), 
                           UsernamePasswordAuthenticationFilter.class)
            // 异常处理
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    PrintWriter writer = response.getWriter();
                    Result<?> result = Result.error(ResultCode.UNAUTHORIZED);
                    writer.write(objectMapper.writeValueAsString(result));
                    writer.flush();
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    PrintWriter writer = response.getWriter();
                    Result<?> result = Result.error(ResultCode.FORBIDDEN);
                    writer.write(objectMapper.writeValueAsString(result));
                    writer.flush();
                })
            );
        
        return http.build();
    }
}
