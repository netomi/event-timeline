/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.vaadin.terminal.gwt.client.Util;

/**
 * The widget to display a single VEvent.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 */
public class VEventLabel extends Label {
  
  private VEvent event;
  
  public VEventLabel(VEvent event, float w, float h) {
    this.event = event;
    
    setStylePrimaryName(VEventTimelineDisplay.CLASSNAME_EVENT);

    final Element caption = DOM.createDiv();
    // FIXME: the limits to show the caption are hard-coded by now
    if (w > 45 && h > 20) {
      caption.setInnerText(Util.escapeHTML(event.getCaption()));
    } else {
      caption.setInnerHTML("");
    }

    caption.addClassName(VEventTimelineDisplay.CLASSNAME_EVENT_CAPTION);
    getElement().appendChild(caption);

    Element eventContent = DOM.createDiv();
    eventContent.addClassName(VEventTimelineDisplay.CLASSNAME_EVENT_CONTENT);
    eventContent.setInnerHTML(event.getDescription());
    getElement().appendChild(eventContent);

    if (event.getStyleName() != null && event.getStyleName().length() > 0) {
      addStyleName(event.getStyleName());
    }

    setWidth(w + "px");
    setHeight(h + "px");
  }
  
  public VEvent getEvent() {
    return event;
  }
}
