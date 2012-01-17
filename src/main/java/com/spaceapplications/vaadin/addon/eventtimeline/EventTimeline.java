/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;

/**
 * EventTimeline implementation, based on original version from vaadin-timeline.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 * @author Florian Pirchner (add / remove bands)
 * @author John Ahlroos / IT Mill Oy Ltd 2010
 */
@ClientWidget(value = VEventTimelineWidget.class, loadStyle = LoadStyle.EAGER)
public class EventTimeline extends AbstractComponent implements EventSetChangeListener {

  private static final long serialVersionUID = 6595058445231789530L;

  // The style name
  private static final String STYLENAME = "v-eventtimeline";

  // Event band information
  private final List<BandInfo> bandInfos = new ArrayList<BandInfo>();
  private final List<BandInfo> bandsToBeRemoved = new ArrayList<EventTimeline.BandInfo>();
  private final List<BandInfo> bandsToBeAdded = new ArrayList<EventTimeline.BandInfo>();
  private int lastBandId = -1;

  // The number of event bands visible in the event page area
  private int eventBandPageSize = -1;

  // Initialization is done
  private boolean initDone = false;

  // Should the time limits be sent
  private boolean sendTimeLimits = false;

  // Should the captions for the zoom etc. be sent
  private boolean sendUICaptions = false;

  // Should the bands information be sent
  private boolean sendBands = false;

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

  // Should a the page size be sent
  private boolean sendEventBandPageSize = false;

  // Should the band height be sent
  private boolean sendBandHeight = false;
  
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

  // The captions of the band paging navigation
  private PageNavigationCaptions pagingCaption = 
    new PageNavigationCaptions("Pages", "next", "previous");

  // Is the date select visible
  protected boolean dateSelectVisible = true;

  // Is the date select enabled
  protected boolean dateSelectEnabled = true;

  // Is the legend visible
  protected boolean legendVisible = false;

  // Is the page navigation visible
  protected boolean pageNavigationVisible;

  // Is the band selection enabled
  protected boolean bandSelectionEnabled;

  // The graph grid color (as CSS3 style rgba string)
  protected String gridColor = "rgba(192,192,192,1)";

  // The height of a band, -1 indicates automatic sizing
  protected int bandHeight = -1;
  
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

    private static final long serialVersionUID = -5424380516338748718L;

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

    private static final long serialVersionUID = 1215106616175652769L;
    
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
   * Band selection event. This event is sent when a user clicks a band in the graph.
   */
  public class BandSelectionEvent extends Component.Event {

    private static final long serialVersionUID = -621449416684203285L;
    
    private int id;

    /**
     * See {@link Component.Event} for details.
     * 
     * @param source
     *          The source of the event
     */
    public BandSelectionEvent(Component source) {
      super(source);
    }

    /**
     * See {@link Component.Event} for more details.
     * 
     * @param source
     *          The source of the event
     * @param id
     *          The band id.
     */
    public BandSelectionEvent(Component source, int id) {
      super(source);
      this.id = id;
    }

    /**
     * Returns the id of the selected band.
     * 
     * @return The band id.
     */
    public int getBandId() {
      return id;
    }
  }

  /**
   * Page navigation event. This event is sent when a user selected the next / previous page button.
   */
  public class PageNavigationEvent extends Component.Event {

    private static final long serialVersionUID = -5391363403702750953L;
    
    private final int page;

    /**
     * 
     * @param source
     *          The source of the event
     * @param page
     *          If true, then the next band is requested. False otherwise.
     */
    public PageNavigationEvent(Component source, int page) {
      super(source);

      this.page = page;
    }

    /**
     * Returns the page number of the bands area.
     * 
     * @return the page
     */
    public int getPage() {
      return page;
    }

  }

  /**
   * Describes the date formats used by the EventTimeline.
   */
  public class DateFormatInfo implements Serializable {

    private static final long serialVersionUID = -3103432458378549206L;
    
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

  /**
   * The band selected listener interface
   */
  public interface BandSelectionListener {
    public void bandSelected(BandSelectionEvent event);
  }

  /**
   * The band paging listener interface
   */
  public interface PageNavigationListener {
    public void requestNavigation(PageNavigationEvent event);
  }

  /*
   * Event methods
   */
  private static final Method DATERANGE_CHANGED_METHOD;
  private static final Method EVENT_CLICK_METHOD;
  private static final Method PAGE_NAVIGATION_METHOD;
  private static final Method BAND_SELECTION_METHOD;

  static {
    try {
      DATERANGE_CHANGED_METHOD =
        DateRangeListener.class.getDeclaredMethod("dateRangeChanged",
            new Class[] { DateRangeChangedEvent.class });

      EVENT_CLICK_METHOD =
        EventClickListener.class.getDeclaredMethod("eventClick",
            new Class[] { EventButtonClickEvent.class });

      PAGE_NAVIGATION_METHOD =
        PageNavigationListener.class.getDeclaredMethod("requestNavigation",
            new Class[] { PageNavigationEvent.class });

      BAND_SELECTION_METHOD =
        BandSelectionListener.class.getDeclaredMethod("bandSelected",
            new Class[] { BandSelectionEvent.class });

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
    if (variables.containsKey(VEventTimelineWidget.ATTR_EVENTS)) {
      Object[] indexes = (Object[]) variables.get(VEventTimelineWidget.ATTR_EVENTS);
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

      eventsToSend.putAll(getAllEventsMap(eventsStartTime, eventsEndTime));
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

    // The user requests the page of the band area
    if (variables.containsKey("bandPage")) {
      int page = (Integer) variables.get("bandPage");
      fireBandPagingEvent(page);
    }

    // The user selected a band
    if (variables.containsKey("bandSel")) {
      int bandId = (Integer) variables.get("bandSel");
      fireBandSelectionEvent(bandId);
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
      target.startTag(VEventTimelineWidget.ATTR_EVENTS);
      target.addAttribute(VEventTimelineWidget.ATTR_START, eventsStartTime.getTime());
      target.addAttribute(VEventTimelineWidget.ATTR_END, eventsEndTime.getTime());

      for (Map.Entry<Integer, List<TimelineEvent>> entry : eventsToSend.entrySet()) {
        target.startTag(VEventTimelineWidget.ATTR_BAND);
        target.addAttribute(VEventTimelineWidget.ATTR_BANDID, entry.getKey());
        for (TimelineEvent event : entry.getValue()) {
          target.startTag(VEventTimelineWidget.ATTR_EVENT);
          paintEvent(event, target);
          target.endTag(VEventTimelineWidget.ATTR_EVENT);
        }
        target.endTag(VEventTimelineWidget.ATTR_BAND);
      }
      target.endTag(VEventTimelineWidget.ATTR_EVENTS);
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
      target.addAttribute("bandPagingVisible", pageNavigationVisible);
      target.addAttribute("bandSelectionEnabled", bandSelectionEnabled);
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
      target.addAttribute("bpgingCaption", pagingCaption.getCaption());
      target.addAttribute("bpgingCptPrevious", pagingCaption.getCaption_previous());
      target.addAttribute("bpgingCptNext", pagingCaption.getCaption_next());

      sendUICaptions = false;
    }

    // Send the number of event bands visible
    if (sendEventBandPageSize) {
      target.addAttribute(VEventTimelineWidget.ATTR_BAND_PAGE_SIZE, eventBandPageSize);
      sendEventBandPageSize = false;
    }

    if (sendBandHeight) {
      target.addAttribute("bandheight", bandHeight);
      sendBandHeight = false;
    }
    
    if (sendBands) {
      if (bandsToBeRemoved.size() > 0 || bandsToBeAdded.size() > 0) {
        BandsPainter.start(target);
        for (BandInfo band : bandsToBeRemoved) {
          BandsPainter.paintRemove(target, band.getBandId());
        }
        bandsToBeRemoved.clear();
        for (BandInfo band : bandsToBeAdded) {
          BandsPainter.paintAdd(target, band.getBandId(), band.getCaption());
        }
        bandsToBeAdded.clear();
        BandsPainter.end(target);
      }
      sendBands = false;
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
   * @return the created band
   */
  public BandInfo addEventBand(final String caption, final TimelineEventProvider provider) {
    lastBandId++;

    provider.addListener(this);
    BandInfo result = new BandInfo(lastBandId, provider, caption);
    bandInfos.add(result);
    bandsToBeAdded.add(result);

    if (initDone) {
      sendBands = true;
      List<TimelineEvent> events = provider.getEvents(minDate, maxDate);
      eventsToSend.put(result.getBandId(), events);
      eventsStartTime = minDate;
      eventsEndTime = maxDate;
      requestRepaint();
    }

    return result;
  }

  /**
   * Removes the event band.
   * 
   * @param provider
   *          The provider for events displayed in this band
   */
  public void removeEventBand(final TimelineEventProvider provider) {
    provider.removeListener(this);

    BandInfo result = null;
    for (BandInfo info : bandInfos) {
      if (info.getProvider() == provider) {
        result = info;
        break;
      }
    }

    if (result != null) {
      result.getProvider().removeListener(this);
      bandInfos.remove(result);
      bandsToBeRemoved.add(result);
    }

    if (initDone) {
      sendBands = true;
      requestRepaint();
    }
  }

  /**
   * Removes all event bands from the timeline.
   */
  public void removeAllEventBands() {
    for (BandInfo info : getBandInfos()) {
      removeEventBand(info.getProvider());
    }
  }

  @Override
  public void eventSetChange(EventSetChange changeEvent) {
    TimelineEventProvider provider = changeEvent.getProvider();
    List<TimelineEvent> events = provider.getEvents(minDate, maxDate);

    int bandId = -1;
    for (BandInfo info : bandInfos) {
      if (info.getProvider().equals(provider)) {
        bandId = info.getBandId();
        break;
      }
    }

    if (bandId >= 0) {
      eventsToSend.put(bandId, events);
      eventsStartTime = minDate;
      eventsEndTime = maxDate;
    }
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

    // Do consistency check
    if (start.equals(end) || end.after(start)) {
      // new time limits
      minDate = start;
      maxDate = end;
      sendTimeLimits = true;

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
   * Add a band paging listener
   * 
   * @param listener
   *          The listener to be added
   */
  public void addListener(PageNavigationListener listener) {
    addListener(PageNavigationEvent.class, listener, PAGE_NAVIGATION_METHOD);
  }

  /**
   * Add a band selection listener
   * 
   * @param listener
   *          The listener to be added
   */
  public void addListener(BandSelectionListener listener) {
    addListener(BandSelectionEvent.class, listener, BAND_SELECTION_METHOD);
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
   * Remove a page navigation listener
   * 
   * @param listener
   *          The listener to be removed
   */
  public void removeListener(PageNavigationListener listener) {
    removeListener(PageNavigationEvent.class, listener);
  }

  /**
   * Remove a band selection listener
   * 
   * @param listener
   *          The listener to be removed
   */
  public void removeListener(BandSelectionListener listener) {
    removeListener(BandSelectionEvent.class, listener);
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
   * Fires a band paging event if the user clicks on next / previous band.
   * 
   * @param page
   *          The visible page of the band area.
   */
  protected void fireBandPagingEvent(int page) {
    fireEvent(new EventTimeline.PageNavigationEvent(this, page));
  }

  /**
   * Fires a band selection event if the user selected a band.
   * 
   * @param page
   *          The id of the selected band.
   */
  protected void fireBandSelectionEvent(int bandId) {
    fireEvent(new EventTimeline.BandSelectionEvent(this, bandId));
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
   * Sets the band height. A value of -1 indicates that the bands shall use
   * automatic resizing to available space.
   * 
   * @param height the band heigth to be used
   * 
   */
  public void setBandHeight(int height) {
    this.bandHeight = height;
    sendBandHeight = true;
    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Get the configured band height
   * 
   * @return the band height
   */
  public int getBandHeight() {
    return this.bandHeight;
  }

  /**
   * Returns the captions of the band paging element.
   * 
   * @return the pagingCaption
   */
  public PageNavigationCaptions getPageNavigationCaptions() {
    return pagingCaption;
  }

  /**
   * Sets the pagingCaptions of the band paging element.
   * 
   * @param pagingCaption
   *          the pagingCaption to set
   */
  public void setPageNavigationCaptions(PageNavigationCaptions pagingCaption) {
    if (pagingCaption == null) {
      pagingCaption = new PageNavigationCaptions("", "", "");
    }

    this.pagingCaption = pagingCaption;
    sendUICaptions = true;
    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Returns a collection with informations about each added band.
   * 
   * @return the bandInfos
   */
  public List<BandInfo> getBandInfos() {
    return Collections.unmodifiableList(new ArrayList<BandInfo>(bandInfos));
  }

  /**
   * Returns the band info for the given bandId or <code>null</code> if no band could be found.
   * 
   * @param bandId
   * @return
   */
  public BandInfo getBand(int bandId) {
    for (BandInfo info : bandInfos) {
      if (info.getBandId() == bandId) {
        return info;
      }
    }
    return null;
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
   * Shows the page navigation.<br/>
   * Using the page navigation allows to observe the requested band page.
   * 
   * @param visible
   */
  public void setPageNavigationVisible(boolean visible) {
    pageNavigationVisible = visible;
    sendComponentVisibilities = true;
    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Returns true, if the page navigation is visible.
   * 
   * @return the pageNavigationVisible
   */
  public boolean isPageNavigationVisible() {
    return pageNavigationVisible;
  }

  /**
   * Returns true, if the band selection is enabled. See {@link #setBandSelectionEnabled(boolean)}
   * for more details.
   * 
   * @return
   */
  public boolean isBandSelectionEnabled() {
    return bandSelectionEnabled;
  }

  /**
   * If the band selection is enabled the selected band will be marked at the UI. Additionally band
   * selection events are sent, if the selected band changes.
   * 
   * @param bandSelectionEnabled
   */
  public void setBandSelectionEnabled(boolean bandSelectionEnabled) {
    this.bandSelectionEnabled = bandSelectionEnabled;
    sendComponentVisibilities = true;
    if (initDone) {
      requestRepaint();
    }
  }

  /**
   * Returns the numbers of event bands shown in the event bands area. A value lower equal 0 means
   * that an unlimited number of event bands can be shown simultaneously at the band area.
   * 
   * @return the eventBandPageSize
   */
  public int getEventBandPageSize() {
    return eventBandPageSize;
  }

  /**
   * Specifies the numbers of event bands shown in the event bands area.<br/>
   * Setting a value lower equal 0 means that an unlimited number of event bands can be shown
   * simultaneously at the band area.
   * 
   * @param eventBandPageSize
   *          the eventBandPageSize to set
   */
  public void setEventBandPageSize(int eventBandPageSize) {
    this.eventBandPageSize = eventBandPageSize;

    sendEventBandPageSize = true;

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

  /**
   * Returns a map with all events grouped by the bandId.
   * 
   * @param start
   * @param end
   * @return
   */
  private Map<Integer, List<TimelineEvent>> getAllEventsMap(Date start, Date end) {
    Map<Integer, List<TimelineEvent>> events = new HashMap<Integer, List<TimelineEvent>>();

    for (BandInfo info : bandInfos) {
      TimelineEventProvider provider = info.getProvider();
      events.put(info.getBandId(), provider.getEvents(start, end));
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
    sendUICaptions = true;
    sendBands = true;
    sendBandHeight = true;
    sendEventBandPageSize = true;
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

  /**
   * A helper class to paint band commands.
   */
  private static class BandsPainter {

    /**
     * Start tag {@link VEventTimelineWidget#ATTR_BANDS}
     * 
     * @param target
     * @throws PaintException
     */
    public static void start(PaintTarget target) throws PaintException {
      target.startTag(VEventTimelineWidget.ATTR_BANDS);
    }

    /**
     * End tag {@link VEventTimelineWidget#ATTR_BANDS}
     * 
     * @param target
     * @throws PaintException
     */
    public static void end(PaintTarget target) throws PaintException {
      target.endTag(VEventTimelineWidget.ATTR_BANDS);
    }

    /**
     * Paints the add band command.
     * 
     * @param target
     * @param bandId
     * @param caption
     * @throws PaintException
     */
    public static void paintAdd(PaintTarget target, int bandId, String caption)
        throws PaintException {
      target.startTag(VEventTimelineWidget.ATTR_BAND);
      target.addAttribute(VEventTimelineWidget.ATTR_BANDID, bandId);
      target.addAttribute(VEventTimelineWidget.ATTR_OPERATION, VEventTimelineWidget.OPERATION_ADD);
      target.addAttribute(VEventTimelineWidget.ATTR_BAND_CAPTION, caption);
      target.endTag(VEventTimelineWidget.ATTR_BAND);
    }

    /**
     * Paints the remove band command.
     * 
     * @param target
     * @param bandId
     * @throws PaintException
     */
    public static void paintRemove(PaintTarget target, int bandId) throws PaintException {
      target.startTag(VEventTimelineWidget.ATTR_BAND);
      target.addAttribute(VEventTimelineWidget.ATTR_BANDID, bandId);
      target.addAttribute(VEventTimelineWidget.ATTR_OPERATION,
          VEventTimelineWidget.OPERATION_REMOVE);
      target.endTag(VEventTimelineWidget.ATTR_BAND);
    }

  }

  /**
   * Can be used to specify the captions of the band paging navigation element.
   */
  public static class PageNavigationCaptions implements Serializable {

    private static final long serialVersionUID = 7731112570587996160L;
    
    private final String caption;
    private final String caption_next;
    private final String caption_previous;

    /**
     * 
     * @param caption
     *          main caption
     * @param caption_next
     *          caption of the next button
     * @param caption_previous
     *          caption of the previous button
     */
    public PageNavigationCaptions(String caption, String caption_next, String caption_previous) {
      super();
      this.caption = caption;
      this.caption_next = caption_next;
      this.caption_previous = caption_previous;
    }

    /**
     * Main caption.
     * 
     * @return the caption
     */
    public String getCaption() {
      return caption;
    }

    /**
     * Caption of the next button.
     * 
     * @return the caption_next
     */
    public String getCaption_next() {
      return caption_next;
    }

    /**
     * Caption of the previous button.
     * 
     * @return the caption_previous
     */
    public String getCaption_previous() {
      return caption_previous;
    }

  }

  /**
   * Class describing the
   */
  public static class BandInfo implements Serializable {
    
    private static final long serialVersionUID = 6388755571992132307L;
    
    private final int bandId;
    private final TimelineEventProvider provider;
    private String caption;

    public BandInfo(int bandId, TimelineEventProvider provider, String caption) {
      super();
      this.bandId = bandId;
      this.provider = provider;
      this.caption = caption;
    }

    /**
     * @return the caption
     */
    public String getCaption() {
      return caption;
    }

    /**
     * @param caption
     *          the caption to set
     */
    protected void setCaption(String caption) {
      this.caption = caption;
    }

    /**
     * @return the bandId
     */
    public int getBandId() {
      return bandId;
    }

    /**
     * @return the provider
     */
    public TimelineEventProvider getProvider() {
      return provider;
    }

  }
}
