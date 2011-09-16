/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.spaceapplications.vaadin.addon.eventtimeline.gwt.style.client.ComputedStyle;

/**
 * Represents a single event in the client-side widget.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 */
public class VEvent implements Serializable, Comparable<VEvent> {
  private static final long serialVersionUID = 1L;
  
  private String id;
  private String caption;
  private Date start, end;
  private String styleName;
  private long startTime, endTime;
  private String description;
  private int slotIndex = -1;
  private int height = -1;
  private String backgroundColor;
  private String strokeColor;

  public String getID() {
    return id;
  }

  public void setID(String id) {
    this.id = id;
  }
  
  public String getStyleName() {
    return styleName;
  }

  public Date getStart() {
    return start;
  }

  public void setStyleName(String style) {
    styleName = style;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public int getSlotIndex() {
    return slotIndex;
  }

  public void setSlotIndex(int i) {
    slotIndex = i;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public String getBackgroundColor() {
    if (backgroundColor == null || backgroundColor.length() == 0) {
      Element elem = DOM.createDiv();
      elem.addClassName(getStyleName());
      backgroundColor = ComputedStyle.getBackgroundColor(elem);
    }
    
    return backgroundColor;
  }

  public void setBackgroundColor(String backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public String getStrokeColor() {
    if (strokeColor == null || strokeColor.length() == 0) {
      Element elem = DOM.createDiv();
      elem.addClassName(getStyleName());
      strokeColor = ComputedStyle.getColor(elem);
    }
    
    return strokeColor;
  }
  
  @Override
  public boolean equals(Object other) {
    if (this == other)
      return true;
    if (!(other instanceof VEvent)) return false;
    return hasEqualState((VEvent) other);
  }

  private boolean hasEqualState(VEvent other) {
    // note VEvent treatment for possibly-null fields
    return objectEquals(this.id, other.id)
        && objectEquals(this.caption, other.caption)
        && objectEquals(this.styleName, other.styleName)
        && objectEquals(this.startTime, other.startTime)
        && objectEquals(this.endTime, other.endTime);
  }

  private boolean objectEquals(Object o1, Object o2) {
    return (o1 == null ? o2 == null : o1.equals(o2));
  }

  @Override
  public int hashCode() {
    int result = 23;
    result = HashCodeUtil.hash(result, id);
    result = HashCodeUtil.hash(result, caption);
    result = HashCodeUtil.hash(result, styleName);
    result = HashCodeUtil.hash(result, startTime);
    result = HashCodeUtil.hash(result, endTime);
    return result;
  }

  @Override
  public int compareTo(VEvent o) {
    return (int) (startTime - o.startTime);
  }
}