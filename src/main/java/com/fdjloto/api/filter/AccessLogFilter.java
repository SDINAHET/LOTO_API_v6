package com.fdjloto.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AccessLogFilter extends OncePerRequestFilter {

  private static final Logger accessLog =
      LoggerFactory.getLogger("ACCESS_LOG");

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {

    long start = System.currentTimeMillis();

    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - start;

      accessLog.info(
          "method={} path={} status={} ms={} ip={} ua=\"{}\"",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          duration,
          extractClientIp(request),
          request.getHeader("User-Agent")
      );
    }
  }

  private String extractClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
