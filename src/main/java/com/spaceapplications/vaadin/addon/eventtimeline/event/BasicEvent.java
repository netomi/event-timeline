/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Simple implementation of {@link TimelineEvent}. Has setters for all required fields and fires
 * events when this event is changed.
 */
public class BasicEvent implements TimelineEvent, TimelineEvent.EventChangeNotifier {
  private static final long serialVersionUID = 1L;

  private String eventId;
  private String caption;
  private String description;
  private Date end;
  private Date start;
  private String styleName;
  private List<EventChangeListener> listeners = new ArrayList<EventChangeListener>();

  // property getters from interface

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  /**
   * {@inheritDoc}
   */
  public String getCaption() {
    return caption;
  }

  /**
   * {@inheritDoc}
   */
  public String getDescription() {
    return description;
  }

  /**
   * {@inheritDoc}
   */
  public Date getEnd() {
    return end;
  }

  /**
   * {@inheritDoc}
   */
  public Date getStart() {
    return start;
  }

  /**
   * {@inheritDoc}
   */
  public String getStyleName() {
    return styleName;
  }

  // setters for properties

  public void setCaption(String caption) {
    if (this.caption == null || !this.caption.equals(caption)) {
      this.caption = caption;
      fireEventChange();
    }
  }

  public void setDescription(String description) {
    if (this.description == null || !this.description.equals(description)) {
      this.description = description;
      fireEventChange();
    }
  }

  public void setEnd(Date end) {
    if (this.end == null || !this.end.equals(end)) {
      this.end = end;
      fireEventChange();
    }
  }

  public void setStart(Date start) {
    if (this.start == null || !this.start.equals(start)) {
      this.start = start;
      fireEventChange();
    }
  }

  public void setStyleName(String styleName) {
    if (this.styleName == null || !this.styleName.equals(styleName)) {
      this.styleName = styleName;
      fireEventChange();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addListener(EventChangeListener listener) {
    listeners.add(listener);

  }

  /**
   * {@inheritDoc}
   */
  public void removeListener(EventChangeListener listener) {
    listeners.remove(listener);
  }

  protected void fireEventChange() {
    EventChange event = new EventChange(this);

    for (EventChangeListener listener : listeners) {
      listener.eventChange(event);
    }
  }
}
