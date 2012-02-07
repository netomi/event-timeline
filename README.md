EVENT TIMELINE
==============

The Event Timeline (http://vaadin.com/directory#addon/event-timeline) is a widget for the vaadin web-development framework. 
It is inspired by the NASA OSTPV (Onboard Short Term Plan Viewer) application to display mission plans and timelines.

The widget is derived from the Vaadin Timeline (http://vaadin.com/directory#addon/vaadin-timeline), and modified to support events rather than data graphs. 

Features
--------

+ Support for multiple independent event bands
+ Overlapping events in different stripes
+ Adjust band heights dynamically
+ Custom styling of event labels with CSS
+ Easy to use time-line browser with a draggable and zoomable interface
+ Customizable zoom levels

Usage
-----

To use the Event Timeline, just add a TimelineEventProvider as an event band: 


``` java
import java.util.Calendar;
import java.util.Date;

import com.spaceapplications.vaadin.addon.eventtimeline.EventTimeline;
import com.spaceapplications.vaadin.addon.eventtimeline.event.BasicEvent;
import com.spaceapplications.vaadin.addon.eventtimeline.event.BasicEventProvider;
import com.spaceapplications.vaadin.addon.eventtimeline.event.TimelineEventProvider;
import com.vaadin.Application;
import com.vaadin.ui.Window;

public class SimpleEventTimelineDemo extends Application {
  @Override
  public void init() {
    Window mainWindow = new Window("Simple EventTimeline Demo");

    setMainWindow(mainWindow);

    // create the timeline
    EventTimeline timeline = new EventTimeline("Our event timeline");
    timeline.setHeight("500px");
    timeline.setWidth("100%");

    // set the visible time range
    Calendar cal = Calendar.getInstance();
    Date start = cal.getTime();
    cal.add(Calendar.HOUR_OF_DAY, 24);
    Date end = cal.getTime();
    timeline.setVisibleDateRange(start, end);

    // add our data sources
    timeline.addEventBand("Band", createEventProvider(end));

    // Add some zoom levels
    timeline.addZoomLevel("Hour", 60 * 60 * 1000L);
    timeline.addZoomLevel("Day", 86400000L);

    mainWindow.addComponent(timeline);
  }

  public TimelineEventProvider createEventProvider(final Date end) {
    BasicEventProvider provider = new BasicEventProvider();

    // get events for a whole day
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, 1);
    int idx = 0;
    while (cal.getTime().before(end)) {
      // create a simple event
      BasicEvent event = new BasicEvent();
      event.setEventId(String.valueOf(idx));
      
      // set the timestamp property
      event.setStart(cal.getTime());
      cal.add(Calendar.MINUTE, 10);
      event.setEnd(cal.getTime());
      // set the caption
      event.setCaption("Event");
      // style the event
      event.setStyleName("color1");

      provider.addEvent(event);
      cal.add(Calendar.MINUTE, 30);      
    }

    return provider;
  }  
}
```

Copyright and license
---------------------

Copyright 2009-2011 Vaadin Ltd., 2011 Space Applications Services NV/SA.

This program is available under GNU Affero General Public License 
(version 3 or later at your option).

See the file licensing.txt distributed with this software for more
information about licensing.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.