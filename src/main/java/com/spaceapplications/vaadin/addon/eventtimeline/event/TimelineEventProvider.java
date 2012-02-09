/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.event;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Interface for querying events.
 */
public interface TimelineEventProvider extends Serializable {

  /**
   * <p>
   * Gets all available events in the target date range between startDate and
   * endDate.
   * </p>
   * 
   * <p>
   * For example, if you set the date range to be monday 22.2.2010 - wednesday
   * 24.2.2000, the used Event Provider will be queried for events between
   * monday 22.2.2010 00:00 and sunday 28.2.2010 23:59. Generally you can
   * expect the date range to be expanded to whole days and whole weeks.
   * </p>
   * 
   * @param startDate
   *          Start date
   * @param endDate
   *          End date
   * @return List of events
   */
  public List<TimelineEvent> getEvents(Date startDate, Date endDate);
  
  public void addListener(EventSetChangeListener listener);

  public void removeListener(EventSetChangeListener listener);

  public TimelineEvent getEvent(final String eventId);
  
  /**
   * Event to signal that the set of events has changed and the calendar
   * should refresh its view from the {@link TimelineEventProvider}.
   */
  public class EventSetChange implements Serializable {
    private static final long serialVersionUID = 1L;

    private TimelineEventProvider source;

    public EventSetChange(TimelineEventProvider source) {
      this.source = source;
    }

    /**
     * @return the {@link TimelineEventProvider} that has changed
     */
    public TimelineEventProvider getProvider() {
      return source;
    }
  }

  /**
   * Listener for EventSetChange events.
   */
  public interface EventSetChangeListener extends Serializable {
    /**
     * Called when the set of Events has changed.
     */
    public void eventSetChange(EventSetChange changeEvent);
  }

  /**
   * Notifier interface for EventSetChange events.
   */
  public interface EventSetChangeNotifier extends Serializable {
    void addListener(EventSetChangeListener listener);

    void removeListener(EventSetChangeListener listener);
  }
}
