/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.event;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Event in the timeline. Customize your own event by implementing this
 * interface.
 * </p>
 * 
 * <li>Start and end fields are mandatory.</li>
 */
public interface TimelineEvent extends Serializable {
  /**
   * Gets the unique event identifier.
   * 
   * @return event identifier.
   */
  public String getEventId();
  
  /**
   * Gets start date of event.
   * 
   * @return Start date.
   */
  public Date getStart();

  /**
   * Get end date of event.
   * 
   * @return End date;
   */
  public Date getEnd();

  /**
   * Gets caption of event.
   * 
   * @return Caption text
   */
  public String getCaption();

  /**
   * Gets description of event. Shown as a tooltip over the event.
   * 
   * @return Description text.
   */
  public String getDescription();

  /**
   * <p>
   * Gets style name of event. In the client, style name will be set to the
   * event's element class name and can be styled by CSS
   * </p>
   * Styling example:</br> <code>Java code: </br>
   * event.setStyleName("color1");
   * </br></br>
   * CSS:</br>
   * .v-eventtimeline-event-color1 {</br>
   * &nbsp;&nbsp;&nbsp;background-color: #9effae;</br>}</code>
   * 
   * @return Style name.
   */
  public String getStyleName();

  /**
   * Event to signal that an event has changed.
   */
  public class EventChange implements Serializable {
    private static final long serialVersionUID = 1L;

    private TimelineEvent source;

    public EventChange(TimelineEvent source) {
      this.source = source;
    }

    /**
     * @return the {@link TimelineEvent} that has changed
     */
    public TimelineEvent getTimelineEvent() {
      return source;
    }
  }

  /**
   * Listener for EventSetChange events.
   */
  public interface EventChangeListener extends Serializable {
    /**
     * Called when an Event has changed.
     */
    public void eventChange(EventChange changeEvent);
  }

  /**
   * Notifier interface for EventChange events.
   */
  public interface EventChangeNotifier extends Serializable {
    void addListener(EventChangeListener listener);
    void removeListener(EventChangeListener listener);
  }
}
