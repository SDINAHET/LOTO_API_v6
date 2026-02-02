package com.fdjloto.api.dto;

import java.util.Map;

public class AnalyticsEvent {

  private String eventType;
  private String page;
  private String ts;
  private Map<String, Object> extra;

  public String getEventType() { return eventType; }
  public void setEventType(String eventType) { this.eventType = eventType; }

  public String getPage() { return page; }
  public void setPage(String page) { this.page = page; }

  public String getTs() { return ts; }
  public void setTs(String ts) { this.ts = ts; }

  public Map<String, Object> getExtra() { return extra; }
  public void setExtra(Map<String, Object> extra) { this.extra = extra; }
}
