/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.spaceapplications.vaadin.addon.eventtimeline.event.TimelineEvent;
import com.spaceapplications.vaadin.addon.eventtimeline.event.TimelineEventProvider;
import com.spaceapplications.vaadin.addon.eventtimeline.event.TimelineEventProvider.EventSetChange;
import com.spaceapplications.vaadin.addon.eventtimeline.event.TimelineEventProvider.EventSetChangeListener;
import com.spaceapplications.vaadin.addon.eventtimeline.gwt.client.VEventTimelineWidget;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.ClientWidget.LoadStyle;

/**
 * EventTimeline implementation, based on original version from vaadin-timeline.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 * @author John Ahlroos / IT Mill Oy Ltd 2010
 */
@ClientWidget(value = VEventTimelineWidget.class, loadStyle = LoadStyle.EAGER)
@SuppressWarnings({ "serial" })
public class EventTimeline extends AbstractComponent implements EventSetChangeListener {
 
  // The style name
  private static final String STYLENAME = "v-eventtimeline";

  // Event providers
  protected List<TimelineEventProvider> eventProviders = new ArrayList<TimelineEventProvider>();

  // Event band captions
  protected List<String> bandCaptions = new ArrayList<String>();

  // Initialization is done
  private boolean initDone = false;

  // Should the time limits be sent
  private boolean sendTimeLimits = false;

  // Should the captions for the zoom etc. be sent
  private boolean sendUICaptions = false;

  // Should the band captions be sent
  private boolean sendBandCaptions = false;

  // Should a refresh command be sent
  private boolean sendRefreshRequest = false;

  // Should the date range be sent. (Maximum and Minimum dates be sent)
  private boolean sendDateRange = false;

  // Activate change events
  private boolean sendChangeEventAvailable = false;

  // Should the component visibilities be sent
  private boolean sendComponentVisibilities = false;

  // Should the zoom levels be sent
  private boolean sendZoomLevels = false;

  // Should the graph grid color be sent
  private boolean sendGridColor = false;

  // Should the date format info be sent
  private boolean sendDateFormatInfo = false;

  // Is the browser bar locked to the selection
  protected boolean selectionBarLocked = true;

  // The events to send in the next refresh
  private Date eventsStartTime = null;
  private Date eventsEndTime = null;
  private final Map<Integer, List<TimelineEvent>> eventsToSend =
    new HashMap<Integer, List<TimelineEvent>>();

  /**
   * The zoom levels to send in the next refresh.<br/>
   * key = Caption of the zoom level<br/>
   * value = The time in milliseconds of the zoom level<br/>
   */
  private final Map<String, Long> zoomLevels = new LinkedHashMap<String, Long>();

  /*
   * We need to keep count of the date range listeners so we can turn off the feature if not needed
   */
  private int dateRangeListenerCounter = 0;

  // The minimum date of all the graphs
  private Date minDate = null;

  // The maximum data of all the graphs
  private Date maxDate = null;

  // Selection minimum date
  private Date selectedStartDate = null;

  // Selection maximum date
  private Date selectedEndDate = null;

  // Is the browser visible
  protected boolean browserIsVisible = true;

  // Is the zoom levels visible
  protected boolean zoomIsVisible = true;

  // The caption of the zoom levels
  protected String zoomLevelCaption = "Zoom";

  // Is the date select visible
  protected boolean dateSelectVisible = true;

  // Is the date select enabled
  protected boolean dateSelectEnabled = true;

  // Is the legend visible
  protected boolean legendVisible = false;

  // The graph grid color (as CSS3 style rgba string)
  protected String gridColor = "rgba(192,192,192,1)";

  // Date formats
  protected final DateFormatInfo dateFormatInfo = new DateFormatInfo();

  /** Date format that will be used in the UIDL for dates. */
  protected DateFormat df_date = new SimpleDateFormat("yyyy-MM-dd");

  /** Time format that will be used in the UIDL for time. */
  protected DateFormat df_time = new SimpleDateFormat("HH:mm:ss");

  /** Date format that will be used in the UIDL for both date and time. */
  protected DateFormat df_date_time = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

  /**
   * Date range changed event.<br/>
   * The date range changed event represents a change in the time span.
   */
  public class DateRangeChangedEvent extends Component.Event {

    private Date startDate;

    private Date endDate;

    /**
     * Default constructor.<br/>
     * See {@link Component.Event} for more details.
     * 
     * @param source
     *          The source of the event
     */
    public DateRangeChangedEvent(Component source) {
      super(source);
    }

    /**
     * See {@link Component.Event} for more details.
     * 
     * @param source
     *          The source of the event
     * @param start
     *          The start date of the range
     * @param end
     *          The end date of the range
     */
    public DateRangeChangedEvent(Component source, Date start, Date end) {
      super(source);
      startDate = start;
      endDate = end;
    }

    /**
     * Returns the start date of the range
     * 
     * @return The start date
     */
    public Date getStartDate() {
      return startDate;
    }

    /**
     * Returns the end date of the range
     * 
     * @return The end date
     */
    public Date getEndDate() {
      return endDate;
    }
  }

  /**
   * Event button click event. This event is sent when a user clicks an event button in the graph.
   * 
   */
  public class EventButtonClickEvent extends Component.Event {

    private Object id;

    /**
     * See {@link Component.Event} for details.
     * 
     * @param source
     *          The source of the event
     */
    public EventButtonClickEvent(Component source) {
      super(source);
    }

    /**
     * See {@link Component.Event} for more details.
     * 
     * @param source
     *          The source of the event
     * @param itemIds
     *          The item id:s in the event data source which are related to the event
     */
    public EventButtonClickEvent(Component source, Object itemId) {
      super(source);
      id = itemId;
    }

    /**
     * Gets the item id:s in the event data source which are related to the event
     * 
     * @return The item id:s related to the event
     */
    public Object getItemId() {
      return id;
    }
  }

  /**
   * Describes the date formats used by the EventTimeline.
   */
  public class DateFormatInfo implements Serializable {

    private String dateSelectDisplaySimpleDateFormat = "MMM d, y";
    private String dateSelectEditSimpleDateFormat = "dd/MM/yyyy";
    private String shortYearFormat = "''yy";
    private String longYearFormat = "yyyy";
    private String shortMonthFormat = "MMM yyyy";
    private String longMonthFormat = "MMMM yyyy";
    private String shortDayFormat = "MMM d, yyyy";
    private String longDayFormat = "MMMM d, yyyy";
    private String shortTimeFormat = "HH:mm";
    private String longTimeFormat = "HH:mm:ss";

    private static final char DELIM = '|';

    /**
     * Get the date format which is used to display the selected date range in the top right corner.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public String getDateSelectDisplaySimpleDateFormat() {
      return dateSelectDisplaySimpleDateFormat;
    }

    /**
     * Set the date format used to display the selected date range in the top right corner.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @param format
     *          A format describing how the date should be formatted
     */
    public void setDateSelectDisplaySimpleDateFormat(String format) {
      if (dateSelectDisplaySimpleDateFormat != null) {
        this.dateSelectDisplaySimpleDateFormat = format;
        sendDateFormatInfo = true;
        if (initDone) {
          requestRepaint();
        }
      }
    }

    /**
     * Get the date format which is used to edit the selected date range in the top right corner
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public String getDateSelectEditSimpleDateFormat() {
      return dateSelectEditSimpleDateFormat;
    }

    /**
     * Set the date format used to display the selected date range in the top right corner.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @param format
     *          A format describing how the date should be formatted
     */
    public void setDateSelectEditSimpleDateFormat(String format) {
      if (dateSelectEditSimpleDateFormat != null) {
        this.dateSelectEditSimpleDateFormat = format;
        sendDateFormatInfo = true;
        if (initDone) {
          requestRepaint();
        }
      }
    }

    /**
     * Get the date format which is displayed in the horizontal scale when the scale has a
     * year-resolution and only a little amount of space is available.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public String getShortYearFormat() {
      return shortYearFormat;
    }

    /**
     * Set the date format which is displayed in the horizontal scale when the scale has a
     * year-resolution and only a little amount of space is available.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public void setShortYearFormat(String shortYearFormat) {
      if (shortYearFormat != null) {
        this.shortYearFormat = shortYearFormat;
        sendDateFormatInfo = true;
        if (initDone) {
          requestRepaint();
        }
      }
    }

    /**
     * Get the date format which is displayed in the horizontal scale when the scale has a
     * year-resolution.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public String getLongYearFormat() {
      return longYearFormat;
    }

    /**
     * Get the date format which is displayed in the horizontal scale when the scale has a
     * year-resolution.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public void setLongYearFormat(String longYearFormat) {
      if (longYearFormat != null) {
        this.longYearFormat = longYearFormat;
        sendDateFormatInfo = true;
        if (initDone) {
          requestRepaint();
        }
      }
    }

    /**
     * Get the date format which is displayed in the horizontal scale when the scale has a
     * month-resolution and only a little amount of space is available.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public String getShortMonthFormat() {
      return shortMonthFormat;
    }

    /**
     * Set the date format which is displayed in the horizontal scale when the scale has a
     * month-resolution and only a little amount of space is available.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public void setShortMonthFormat(String shortMonthFormat) {
      if (shortMonthFormat != null) {
        this.shortMonthFormat = shortMonthFormat;
        sendDateFormatInfo = true;
        if (initDone) {
          requestRepaint();
        }
      }
    }

    /**
     * Get the date format which is displayed in the horizontal scale when the scale has a
     * month-resolution.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public String getLongMonthFormat() {
      return longMonthFormat;
    }

    /**
     * Set the date format which is displayed in the horizontal scale when the scale has a
     * month-resolution.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public void setLongMonthFormat(String longMonthFormat) {
      if (longMonthFormat != null) {
        this.longMonthFormat = longMonthFormat;
        sendDateFormatInfo = true;
        if (initDone) {
          requestRepaint();
        }
      }
    }

    /**
     * Get the date format which is displayed in the horizontal scale when the scale has a
     * day-resolution and only a little amount of space is available.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public String getShortDayFormat() {
      return shortDayFormat;
    }

    /**
     * Set the date format which is displayed in the horizontal scale when the scale has a
     * day-resolution and only a little amount of space is available.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public void setShortDayFormat(String shortDayFormat) {
      if (shortDayFormat != null) {
        this.shortDayFormat = shortDayFormat;
        sendDateFormatInfo = true;
        if (initDone) {
          requestRepaint();
        }
      }
    }

    /**
     * Get the date format which is displayed in the horizontal scale when the scale has a
     * day-resolution.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public String getLongDayFormat() {
      return longDayFormat;
    }

    /**
     * Set the date format which is displayed in the horizontal scale when the scale has a
     * day-resolution.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public void setLongDayFormat(String longDayFormat) {
      if (longDayFormat != null) {
        this.longDayFormat = longDayFormat;
        sendDateFormatInfo = true;
        if (initDone) {
          requestRepaint();
        }
      }
    }

    /**
     * Get the date format which is displayed in the horizontal scale when the scale is displaying
     * time and only a little amount of space is available.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public String getShortTimeFormat() {
      return shortTimeFormat;
    }

    /**
     * Set the date format which is displayed in the horizontal scale when the scale is displaying
     * time and only a little amount of space is available.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public void setShortTimeFormat(String shortTimeFormat) {
      if (shortTimeFormat != null) {
        this.shortTimeFormat = shortTimeFormat;
        sendDateFormatInfo = true;
        if (initDone) {
          requestRepaint();
        }
      }
    }

    /**
     * Get the date format which is displayed in the horizontal scale when the scale is displaying
     * time.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public String getLongTimeFormat() {
      return longTimeFormat;
    }

    /**
     * Set the date format which is displayed in the horizontal scale when the scale is displaying
     * time.
     * 
     * See {@link SimpleDateFormat} for format details.
     * 
     * @return A format describing how the date should be formatted
     */
    public void setLongTimeFormat(String longTimeFormat) {
      if (longTimeFormat != null) {
        this.longTimeFormat = longTimeFormat;
        sendDateFormatInfo = true;
        if (initDone) {
          requestRepaint();
        }
      }
    }

    /**
     * Serializes the date formats into a string which can be sent to the client.
     * 
     * @return
     */
    public String serialize() {
      return dateSelectDisplaySimpleDateFormat + DELIM + dateSelectEditSimpleDateFormat + DELIM
          + shortYearFormat + DELIM + longYearFormat + DELIM + shortMonthFormat + DELIM
          + longMonthFormat + DELIM + shortDayFormat + DELIM + longDayFormat + DELIM
          + shortTimeFormat + DELIM + longTimeFormat;
    }

    @Override
    public String toString() {
      return serialize();
    }
  }

  /**
   * The date range listener interface
   */
  public interface DateRangeListener {
    public void dateRangeChanged(DateRangeChangedEvent event);
  }

  /**
   * The event click listener interface
   */
  public interface EventClickListener {
    public void eventClick(EventButtonClickEvent event);
  }

  /*
   * Event methods
   */
  private static final Method DATERANGE_CHANGED_METHOD;
  private static final Method EVENT_CLICK_METHOD;

  static {
    try {
      DATERANGE_CHANGED_METHOD =
        DateRangeListener.class.getDeclaredMethod("dateRangeChanged",
            new Class[] { DateRangeChangedEvent.class });

      EVENT_CLICK_METHOD =
        EventClickListener.class.getDeclaredMethod("eventClick",
            new Class[] { EventButtonClickEvent.class });

    } catch (final java.lang.NoSuchMethodException e) {
      // This should never happen
      throw new java.lang.RuntimeException("Internal error finding methods in EventTimeline");
    }
  }

  /**
   * Default constructor
   */
  public EventTimeline() {
    setStyleName(STYLENAME);

    // Default size
    setWidth("400px");
    setHeight("300px");
  }

  /**
   * Constructor
   * 
   * @param caption
   *          The caption of the graph
   */
  public EventTimeline(String caption) {
    this();
    setCaption(caption);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.vaadin.ui.AbstractComponent#changeVariables(java.lang.Object, java.util.Map)
   */
  @Override
  public void changeVariables(Object source, @SuppressWarnings("rawtypes") Map variables) {

    // Initialization data requested from the client side (refresh occurred)
    if (variables.containsKey("init")) {
      initDataFlags();
      requestRepaint();
    }

    // The client need some events to display
    if (variables.containsKey("events")) {
      Object[] indexes = (Object[]) variables.get("events");
      Date start = new Date(Long.parseLong(indexes[0].toString()));
      Date end = new Date(Long.parseLong(indexes[1].toString()));

      if (eventsStartTime == null) {
        eventsStartTime = start;
        eventsEndTime = end;
      } else {
        if (start.before(eventsStartTime)) {
          eventsStartTime = start;
        }

        if (end.after(eventsEndTime)) {
          eventsEndTime = end;
        }

        eventsToSend.clear();
      }

      eventsToSend.putAll(getEvents(eventsStartTime, eventsEndTime));
    }

    // Send the data to the client
    if (variables.containsKey("send")) {
      requestRepaint();
    }

    // Date range changed event
    if (variables.containsKey("drce")) {
      Object[] values = (Object[]) variables.get("drce");
      selectedStartDate = new Date(Long.parseLong(values[0].toString()));
      selectedEndDate = new Date(Long.parseLong(values[1].toString()));

      fireDateRangeChangedEvent(selectedStartDate, selectedEndDate);
    }

    // Event button click event received
    if (variables.containsKey("ebce")) {
      String itemId = (String) variables.get("ebce");
      fireEventButtonClickEvent(itemId);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.vaadin.ui.AbstractComponent#paintContent(com.vaadin.terminal.PaintTarget )
   */
  @Override
  public void paintContent(PaintTarget target) throws PaintException {

    // Superclass writes any common attributes in the paint target.
    super.paintContent(target);

    // Send the selection lock
    target.addAttribute("lock", selectionBarLocked);

    // Always send change event available flag
    target.addAttribute("e1", sendChangeEventAvailable);

    // Always sending locale
    final Locale l = getLocale();
    if (l != null) {
      target.addAttribute("locale", l.toString());
    }

    // Add the events
    if (eventsToSend.size() > 0) {
      target.startTag("events");
      target.addAttribute("start", eventsStartTime.getTime());
      target.addAttribute("end", eventsEndTime.getTime());

      for (Map.Entry<Integer, List<TimelineEvent>> entry : eventsToSend.entrySet()) {
        target.startTag("band");
        target.addAttribute("bandid", entry.getKey());
        //target.addAttribute("events", getEventsAsStringArray(entry.getValue()));
        for (TimelineEvent event : entry.getValue()) {
          target.startTag("event");
          paintEvent(event, target);
          target.endTag("event");
        }
        target.endTag("band");
      }
      target.endTag("events");
      eventsToSend.clear();
      eventsStartTime = null;
      eventsEndTime = null;
    }

    // Send time limits
    if (sendTimeLimits && minDate != null && maxDate != null) {
      target.addAttribute("startDate", minDate.getTime());
      target.addAttribute("endDate", maxDate.getTime());
      sendTimeLimits = false;
    }

    // Send refresh request
    if (sendRefreshRequest) {
      target.addAttribute("refresh", true);
      sendRefreshRequest = false;
    }

    // Send new selected date range
    if (sendDateRange && selectedStartDate != null && selectedEndDate != null) {
      target.addVariable(this, "selectStart", selectedStartDate.getTime());
      target.addVariable(this, "selectEnd", selectedEndDate.getTime());

      sendDateRange = false;
    }

    // Send the component visibilities
    if (sendComponentVisibilities) {
      target.addAttribute("browserVisibility", browserIsVisible);
      target.addAttribute("zoomVisibility", zoomIsVisible);
      target.addAttribute("dateSelectVisibility", dateSelectVisible);
      target.addAttribute("dateSelectEnabled", dateSelectEnabled);
      target.addAttribute("legendVisibility", legendVisible);
      sendComponentVisibilities = false;
    }

    // Send zoom levels
    if (sendZoomLevels) {
      // put them into an array of objects to keep the order
      String[] levels = new String[zoomLevels.size()];
      int idx = 0;
      for (Map.Entry<String, Long> entry : zoomLevels.entrySet()) {
        levels[idx++] = entry.getKey() + "," + entry.getValue();
      }
      target.addAttribute("zoomLevels", levels);
      sendZoomLevels = false;
    }

    // Send the graph grid color if it has changed
    if (sendGridColor) {
      // target
      target.addAttribute("gridColor", gridColor == null ? "" : gridColor);
      sendGridColor = false;
    }

    // Send the UI captions if they have changed
    if (sendUICaptions) {
      target.addAttribute("zlvlcaption", zoomLevelCaption);
      sendUICaptions = false;
    }

    if (sendBandCaptions) {
      target.addAttribute("numBands", bandCaptions.size());
      target.addAttribute("bcaption", bandCaptions.toArray());
      sendBandCaptions = false;
    }

    // Send date formats if set
    if (sendDateFormatInfo) {
      target.addAttribute("dateformats", dateFormatInfo.serialize());
      sendDateFormatInfo = false;
    }
  }

  /**
   * Paints single timeline event to UIDL. Override this method to add custom attributes for events.
   * 
   * @param i
   *          Index of target Timeline.Event
   * @param target
   *          PaintTarget
   */
  protected void paintEvent(TimelineEvent e, PaintTarget target) throws PaintException {
    target.addAttribute(VEventTimelineWidget.ATTR_ID, e.getEventId());
    target.addAttribute(VEventTimelineWidget.ATTR_CAPTION,
        (e.getCaption() == null ? "" : e.getCaption()));
    target.addAttribute(VEventTimelineWidget.ATTR_DATEFROM, df_date.format(e.getStart()));
    target.addAttribute(VEventTimelineWidget.ATTR_DATETO, df_date.format(e.getEnd()));
    target.addAttribute(VEventTimelineWidget.ATTR_TIMEFROM, e.getStart().getTime());
    target.addAttribute(VEventTimelineWidget.ATTR_TIMETO, e.getEnd().getTime());
    target.addAttribute(VEventTimelineWidget.ATTR_DESCRIPTION,
        e.getDescription() == null ? "" : e.getDescription());
    target.addAttribute(VEventTimelineWidget.ATTR_STYLE,
        e.getStyleName() == null ? "" : e.getStyleName());
  }

  protected String[] getEventsAsStringArray(final List<TimelineEvent> events) throws PaintException {
    String[] result = new String[events.size()];
    
    int idx = 0;
    for (TimelineEvent e : events) {
      StringBuilder sb = new StringBuilder();
      sb.append(e.getEventId());
      sb.append(";");
      sb.append(e.getCaption());
      sb.append(";");
      sb.append(df_date.format(e.getStart()));
      sb.append(";");
      sb.append(df_date.format(e.getEnd()));
      sb.append(";");
      sb.append(e.getStart().getTime());
      sb.append(";");
      sb.append(e.getEnd().getTime());
      sb.append(";");
      sb.append(e.getDescription());
      sb.append(";");
      sb.append(e.getStyleName());
      result[idx++] = sb.toString();
    }
    
    return result;
  }
  
  /**
   * Add another event band.
   * 
   * @param caption
   *          The caption for this band
   * @param provider
   *          The provider for events displayed in this band
   */
  public void addEventBand(final String caption, final TimelineEventProvider provider) {
    eventProviders.add(provider);
    bandCaptions.add(caption);

    sendBandCaptions = true;

    provider.addListener(this);
    
    if (initDone) {
      requestRepaint();
    }
  }

  @Override
  public void eventSetChange(EventSetChange changeEvent) {
    TimelineEventProvider provider = changeEvent.getProvider();
    List<TimelineEvent> events = provider.getEvents(minDate, maxDate);
    int idx = 0;
    for (TimelineEventProvider p : eventProviders) {
      if (p.equals(provider)) {
        break;
      }
      idx++;
    }
    
    eventsToSend.put(idx, events);
    eventsStartTime = minDate;
    eventsEndTime = maxDate;
    requestRepaint();
  }

  /**
   * Sets the displayed time range.<br/>
   * The displayed time is the time range selected in the browser and displayed in the main area of
   * the component. The displayed time range cannot be set until some data source has been added to
   * the component.
   * 
   * @param start
   *          The start date
   * @param end
   *          The end data
   */
  public void setVisibleDateRange(Date start, Date end) {
    minDate = start;
    maxDate = end;

    // Do consistency check
    if (start.equals(end) || end.after(start)) {
      selectedStartDate = start;
      selectedEndDate = end;
      sendDateRange = true;
      if (initDone) {
        requestRepaint();
      }

    } else {
      throw new IllegalArgumentException("End date must come after the start date.");
    }
  }

  /**
   * Makes the whole graph visible and selected in the browser.
   */
  public void selectFullRange() {
    setVisibleDateRange(minDate, maxDate);
  }

  /**
   * Adds a date range listener.<br/>
   * This is triggered when the date range is changed.
   * 
   * @param listener
   *          The listener to be added
   */
  public void addListener(DateRangeListener listener) {
    addListener(DateRangeChangedEvent.class, listener, DATERANGE_CHANGED_METHOD);
    sendChangeEventAvailable = true;
    dateRangeListenerCounter++;
  }

  /**
   * Add a button click listener
   * 
   * @param listener
   *          The listener to be added
   */
  public void addListener(EventClickListener listener) {
    addListener(EventButtonClickEvent.class, listener, EVENT_CLICK_METHOD);
  }

  /**
   * Remove a date range listener
   * 
   * @param listener
   *          The listener to be removed
   */
  public void removeListener(DateRangeListener listener) {
    removeListener(DateRangeChangedEvent.class, listener);
    dateRangeListenerCounter--;

    if (dateRangeListenerCounter == 0) {
      sendChangeEventAvailable = false;
    }
  }

  /**
   * Remove a event button click listener
   * 
   * @param listener
   *          The listener to be removed
   */
  public void removeListener(EventClickListener listener) {
    removeListener(EventButtonClickEvent.class, listener);
  }

  /**
   * Fires a date range changed event
   * 
   * @param start
   *          The start date of the range
   * @param end
   *          The end date of the range
   */
  protected void fireDateRangeChangedEvent(Date start, Date end) {
    fireEvent(new EventTimeline.DateRangeChangedEvent(this, start, end));
  }

  /**
   * Fires a event button click event which occurs when the user presses an event button in the
   * graph
   * 
   * @param itemId
   *          The item id in the Event container which represents the event
   */
  protected void fireEventButtonClickEvent(String itemId) {
    fireEvent(new EventTimeline.EventButtonClickEvent(this, itemId));
  }

  /**
   * Shows or hides the browser.<br/>
   * The browser is the timeline browser in the bottom of the component. With the browser you can
   * move or zoom in time.
   * 
   * @param visible
   *          If true then the browser is visible
   */
  public void setBrowserVisible(boolean visible) {
    browserIsVisible = visible;
    sendComponentVisibilities = true;

    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Is the browser visible
   */
  public boolean isBrowserVisible() {
    return browserIsVisible;
  }

  /**
   * Show the zoom levels.<br/>
   * Zoom levels are predefined time ranges which are displayed in the top left corner of the
   * Timeline component.
   * 
   * @param visible
   *          If true then the zoom levels are visible
   */
  public void setZoomLevelsVisible(boolean visible) {
    zoomIsVisible = visible;
    sendComponentVisibilities = true;

    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Are the zoom levels visible.<br/>
   * Zoom levels are predefined time ranges which are displayed in the top left corner of the
   * Timeline component.
   */
  public boolean isZoomLevelsVisible() {
    return zoomIsVisible;
  }

  /**
   * Sets the caption of the zoom levels
   * 
   * @param caption
   * 
   */
  public void setZoomLevelsCaption(String caption) {
    if (caption == null) {
      caption = "";
    }

    this.zoomLevelCaption = caption;
    sendUICaptions = true;
    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Get the caption of the zoom levels
   * 
   * @return
   */
  public String getZoomLevelsCaption() {
    return this.zoomLevelCaption;
  }

  /**
   * Show the date select.<br/>
   * The date select is the text fields in the top right corner of the component which shows the
   * currently selected date range.
   * 
   * @param visible
   *          Should the data select be visible
   */
  public void setDateSelectVisible(boolean visible) {
    dateSelectVisible = visible;
    sendComponentVisibilities = true;

    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Is the date select visible.<br/>
   * The date select is the text fields in the top right corner of the component which shows the
   * currently selected date range.
   */
  public boolean isDateSelectVisible() {
    return dateSelectVisible;
  }

  /**
   * Enable manual editing of selected date range.<br/>
   * The date select is the text fields in the top right corner of the component which shows the
   * currently selected date range.<br/>
   * The date range can be used to either just display the currently selected date range or one can
   * allow the used to manually edit the selected date range by clicking on the dates by setting
   * enabled to true.
   * 
   * @param enabled
   *          Is the date selected manually editable
   */
  public void setDateSelectEnabled(boolean enabled) {
    dateSelectEnabled = enabled;
    sendComponentVisibilities = true;

    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Is the date select enabled.<br/>
   * The date select is the text fields in the top right corner of the component which shows the
   * currently selected date range.<br/>
   * The date range can be used to either just display the currently selected date range or one can
   * allow the used to manually edit the selected date range by clicking on the dates by setting
   * enabled to true.
   */
  public boolean isDateSelectEnabled() {
    return dateSelectEnabled;
  }

  /**
   * Set legend visibility.<br/>
   * The legend displays labels for each graph
   * 
   * @param visible
   *          The legend visibility
   */
  public void setGraphLegendVisible(boolean visible) {
    legendVisible = visible;
    sendComponentVisibilities = true;
    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Is the legend visible
   */
  public boolean isGraphLegendVisible() {
    return legendVisible;
  }

  /**
   * Add a zoom level.<br/>
   * Adds a custom zoom level. Zoom levels are defined as milliseconds and are added to the top left
   * of the Timeline component.
   * 
   * @param caption
   *          The title of the zoom level
   * @param time
   *          The timespan of the zoom level
   */
  public void addZoomLevel(String caption, Long time) {
    zoomLevels.put(caption, time);
    sendZoomLevels = true;
    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Remove a zoom level.<br/>
   * Zoom levels are defined as milliseconds and are added to the top left of the Timeline
   * component.
   * 
   * @param caption
   *          The title of the zoom level
   */
  public void removeZoomLevel(String caption) {
    zoomLevels.remove(caption);
    sendZoomLevels = true;
    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Sets the color of the grid.<br/>
   * Setting the color to NULL will remove the grid.
   * 
   * @param color
   *          The color (as CSS3-style rgba string) of the grid or Null to remove the grid
   */
  public void setGridColor(String color) {
    gridColor = color;
    sendGridColor = true;
    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Gets the grid color used to draw the vertical and horizontal scale grids.<br/>
   * Returns null if no grid is drawn in the graph
   * 
   * @return The color of the graph
   */
  public String getGridColor() {
    return gridColor;
  }
  
  /**
   * When using dynamically updating graphs the updates may cause the selection bar to move when new
   * items are added to the data source and the graph changes. To lock the browser bar so it stays
   * still and instead the selection changes when new items are added set this to false. By default
   * the selection bar is locked to the selection. <br/>
   * Please note that when the selection lock is unlocked then the selection range will change with
   * each update.
   * 
   * @param locked
   *          Should the selection range be locked to the selected range or should the selection
   *          change when new items are added to the data source
   */
  public void setBrowserSelectionLock(boolean locked) {
    selectionBarLocked = locked;
    if (initDone) {
      requestRepaint();
    }
  }

  private Map<Integer, List<TimelineEvent>> getEvents(Date start, Date end) {
    Map<Integer, List<TimelineEvent>> events = new HashMap<Integer, List<TimelineEvent>>();

    int idx = 0;
    for (TimelineEventProvider provider : eventProviders) {
      events.put(idx++, provider.getEvents(start, end));
    }

    return events;
  }

  private void initDataFlags() {
    initDone = true;
    sendTimeLimits = true;
    sendDateRange = true;
    sendComponentVisibilities = true;
    sendGridColor = true;
    sendZoomLevels = true;
    sendBandCaptions = true;
    sendUICaptions = true;
  }

  /**
   * Gets an object describing the date formats.
   * 
   * To use your own date formats retrieve the formats using this method and use the setters to
   * customize the date formats.
   * 
   * @return
   */
  public DateFormatInfo getDateFormats() {
    return this.dateFormatInfo;
  }
}
