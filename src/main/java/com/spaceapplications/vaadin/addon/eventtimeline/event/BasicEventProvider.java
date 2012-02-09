/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>
 * Simple implementation of {@link TimelineEventProvider}. Use {@link #addEvent(BasicEvent)} and
 * {@link #removeEvent(BasicEvent)} to add / remove events.
 * </p>
 * 
 * <p>
 * {@link TimelineEventProvider.EventSetChangeNotifier EventSetChangeNotifier} and
 * {@link TimelineEvent.EventChangeListener EventChangeListener} are also implemented, so the
 * Calendar is notified when an event is added, changed or removed.
 * </p>
 */
public class BasicEventProvider implements TimelineEventProvider,
    TimelineEventProvider.EventSetChangeNotifier, TimelineEvent.EventChangeListener {

  private static final long serialVersionUID = 1L;

  protected Set<TimelineEvent> eventList = new TreeSet<TimelineEvent>(new EventComparator());

  private static class EventComparator implements Comparator<TimelineEvent>, Serializable {

    private static final long serialVersionUID = -3838480424724696804L;

    @Override
    public int compare(TimelineEvent o1, TimelineEvent o2) {
      int diff = o1.getStart().compareTo(o2.getStart());
      if (diff != 0) {
        return diff;
      } else {
        diff = o1.getEnd().compareTo(o2.getEnd());
        if (diff != 0) {
          return diff;
        } else {
          return o1.getEventId().compareTo(o2.getEventId());
        }
      }
    }
  }

  private List<EventSetChangeListener> listeners = new ArrayList<EventSetChangeListener>();

  /**
   * {@inheritDoc}
   */
  public List<TimelineEvent> getEvents(Date startDate, Date endDate) {
    ArrayList<TimelineEvent> activeEvents = new ArrayList<TimelineEvent>();

    for (TimelineEvent ev : eventList) {
      long from = startDate.getTime();
      long to = endDate.getTime();

      long f = ev.getStart().getTime();
      long t = ev.getEnd().getTime();
      // Select only events that overlaps with startDate and
      // endDate.
      if ((f <= to && f >= from) || (t >= from && t <= to) || (f <= from && t >= to)) {
        activeEvents.add(ev);
      }
    }

    return activeEvents;
  }

  /**
   * Returns all events and ignores any date ranges.
   */
  public List<TimelineEvent> getEvents() {
    ArrayList<TimelineEvent> activeEvents = new ArrayList<TimelineEvent>(eventList);
    return activeEvents;
  }

  /**
   * Return the event with the given event Id if it is available.
   */
  public TimelineEvent getEvent(String eventId) {
    for (TimelineEvent ev : eventList) {
      if (eventId.equals(ev.getEventId())) {
        return ev;
      }
    }
    return null;
  }

  public void addEvent(BasicEvent event) {
    eventList.add(event);
    event.addListener(this);
    fireEventSetChange();
  }

  public void removeEvent(BasicEvent event) {
    eventList.remove(event);
    event.removeListener(this);
    fireEventSetChange();
  }

  public boolean containsEvent(BasicEvent event) {
    return eventList.contains(event);
  }

  /**
   * {@inheritDoc}
   */
  public void addListener(EventSetChangeListener listener) {
    listeners.add(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void removeListener(EventSetChangeListener listener) {
    listeners.remove(listener);
  }

  protected void fireEventSetChange() {
    EventSetChange event = new EventSetChange(this);

    for (EventSetChangeListener listener : listeners) {
      listener.eventSetChange(event);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void eventChange(TimelineEvent.EventChange changeEvent) {
    // naive implementation
    fireEventSetChange();
  }
}
