package com.cms.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

@Configuration
public class XssFilter {

    @Bean
    public FilterRegistrationBean<Filter> xssFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssRequestFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("xssFilter");
        registration.setOrder(1);
        return registration;
    }

    static class XssRequestFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
        }
    }

    static class XssRequestWrapper extends HttpServletRequestWrapper {
        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return value != null ? HtmlUtils.htmlEscape(value) : null;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] escaped = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                escaped[i] = values[i] != null ? HtmlUtils.htmlEscape(values[i]) : null;
            }
            return escaped;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            if (value == null) return null;
            if ("Authorization".equalsIgnoreCase(name) || "Content-Type".equalsIgnoreCase(name)) {
                return value;
            }
            return HtmlUtils.htmlEscape(value);
        }
    }
}
