package com.spaceapplications.vaadin.addon.eventtimeline.demo;

import java.util.Calendar;
import java.util.Date;

import com.spaceapplications.vaadin.addon.eventtimeline.EventTimeline;
import com.spaceapplications.vaadin.addon.eventtimeline.EventTimeline.EventButtonClickEvent;
import com.spaceapplications.vaadin.addon.eventtimeline.event.BasicEvent;
import com.spaceapplications.vaadin.addon.eventtimeline.event.BasicEventProvider;
import com.spaceapplications.vaadin.addon.eventtimeline.event.TimelineEventProvider;
import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class MyEventTimelineDemo extends Application {
  @Override
  public void init() {
    Window mainWindow = new Window("EventTimeline Demo Application");
    Label label = new Label("Hello Vaadin user");
    mainWindow.addComponent(label);
    setMainWindow(mainWindow);

    // Create the timeline
    EventTimeline timeline = new EventTimeline("Our event timeline");
    timeline.setHeight("500px");
    timeline.setWidth("100%");

    Calendar cal = Calendar.getInstance();
    Date start = cal.getTime();
    cal.add(Calendar.DAY_OF_WEEK, 1);
    Date end = cal.getTime();
    
    // Create the data sources
    final TimelineEventProvider eventProvider1 = createEventProvider1(end);
    final TimelineEventProvider eventProvider2 = createEventProvider2(end);
    final TimelineEventProvider eventProvider3 = createEventProvider3(end);

    // Add our data sources
    timeline.addEventBand("Band 1", eventProvider1);
    timeline.addEventBand("Band 2", eventProvider2);
    timeline.addEventBand("Band 3", eventProvider3);

    timeline.setVisibleDateRange(start, end);

    // Add some zoom levels
    timeline.addZoomLevel("Hour", 60 * 60 * 1000L);
    timeline.addZoomLevel("3Hour", 3 * 60 * 60 * 1000L);
    timeline.addZoomLevel("6Hour", 6 * 60 * 60 * 1000L);
    timeline.addZoomLevel("12Hour", 12 * 60 * 60 * 1000L);
    timeline.addZoomLevel("Day", 86400000L);
    //timeline.addZoomLevel("Week", 7 * 86400000L);
    // timeline.addZoomLevel("Month", 2629743830L);

    mainWindow.addComponent(timeline);
    
    // Listen to click events from events
    timeline.addListener(new EventTimeline.EventClickListener() {
      public void eventClick(EventButtonClickEvent event) {
        getMainWindow().showNotification("clicked event " + event.getItemId());
        
        BasicEvent e = (BasicEvent) eventProvider1.getEvent((String) event.getItemId());
        if (e != null) {
          e.setCaption("Test");
        }
      }
    });
  }

  public TimelineEventProvider createEventProvider1(final Date end) {
    BasicEventProvider provider = new BasicEventProvider();
    
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, 6);
    int idx = 0;
    while (cal.getTime().before(end)) {
      // Create a point in time
      BasicEvent event = new BasicEvent();
      event.setEventId(String.valueOf(idx));
      
      // Set the timestamp property
      event.setStart(cal.getTime());
      cal.add(Calendar.MINUTE, 3);
      event.setEnd(cal.getTime());
      event.setCaption("Event");

      provider.addEvent(event);
      
      if (idx++ % 2 == 0) {
        cal.add(Calendar.MINUTE, 5);
        event.setStyleName("color1");
      } else {
        cal.add(Calendar.MINUTE, -2);
        event.setStyleName("color2");
      }
    }

    return provider;
  }
  
  public TimelineEventProvider createEventProvider2(final Date end) {
    BasicEventProvider provider = new BasicEventProvider();
    
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR_OF_DAY, 2);
    int idx = 0;
    while (cal.getTime().before(end)) {
      // Create a point in time
      BasicEvent event = new BasicEvent();
      event.setEventId(String.valueOf(idx++));
      
      // Set the timestamp property
      event.setStart(cal.getTime());
      cal.add(Calendar.MINUTE, 10);
      event.setEnd(cal.getTime());
      event.setCaption("Task");
      event.setStyleName("color4");

      provider.addEvent(event);
      cal.add(Calendar.HOUR, 1);
    }

    return provider;
  }
  
  public TimelineEventProvider createEventProvider3(final Date end) {
    BasicEventProvider provider = new BasicEventProvider();
    
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, 6);
    int idx = 0;
    while (cal.getTime().before(end)) {
      // Create a point in time
      BasicEvent event = new BasicEvent();
      event.setEventId(String.valueOf(idx));
      
      // Set the timestamp property
      event.setStart(cal.getTime());
      cal.add(Calendar.MINUTE, 30);
      event.setEnd(cal.getTime());
      event.setCaption("Event");
      event.setDescription("Test");

      provider.addEvent(event);
      
      idx++;
      if (idx % 3 == 0) {
        cal.add(Calendar.MINUTE, 30);
        event.setStyleName("color3");
      } else if (idx % 3 == 1){
        cal.add(Calendar.MINUTE, -20);
        event.setStyleName("color4");
      } else {
        cal.add(Calendar.MINUTE, -8);
        event.setStyleName("color2");
      }
    }

    return provider;
  }  
}