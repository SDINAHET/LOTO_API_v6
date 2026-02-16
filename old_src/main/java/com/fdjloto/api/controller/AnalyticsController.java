package com.fdjloto.api.controller;

import com.fdjloto.api.dto.AnalyticsEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

  private static final Logger analyticsLog =
      LoggerFactory.getLogger("ANALYTICS_LOG");

  @PostMapping("/event")
  public void logEvent(@RequestBody AnalyticsEvent evt,
                       HttpServletRequest request) {

    String ip = extractClientIp(request);
    String ua = request.getHeader("User-Agent");

    analyticsLog.info(
      "event={} page={} user={} ip={} ua=\"{}\" data={}",
      evt.getEventType(),
      evt.getPage(),
      "anonymous",
      ip,
      ua,
      evt.getExtra()
    );
  }

  private String extractClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
