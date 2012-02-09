/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.spaceapplications.vaadin.addon.eventtimeline.gwt.canvas.client.Canvas;
import com.spaceapplications.vaadin.addon.eventtimeline.gwt.style.client.ComputedStyle;
import com.vaadin.terminal.gwt.client.DateTimeService;

/**
 * VEventTimelineDisplay, based on original version from vaadin-timeline.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 * @author John Ahlroos / IT Mill Oy Ltd
 */
public class VEventTimelineDisplay extends Widget implements VDataListener,
		MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseWheelHandler,
		NativePreviewHandler {

	private static final String CLASSNAME_BOTTOMBAR = VEventTimelineWidget.DISPLAY_CLASSNAME + "-bottombar";
	private static final String CLASSNAME_CANVAS = VEventTimelineWidget.DISPLAY_CLASSNAME	+ "-canvas";
	private static final String CLASSNAME_CANVASDRAG = CLASSNAME_CANVAS	+ "-drag";
	private static final String CLASSNAME_LOADINGCURTAIN = VEventTimelineWidget.DISPLAY_CLASSNAME	+ "-curtain";
	public static final String CLASSNAME_EVENT = VEventTimelineWidget.CLASSNAME	+ "-event";
	public static final String CLASSNAME_EVENT_SELECTED = CLASSNAME_EVENT	+ "-selected";
	public static final String CLASSNAME_EVENT_CAPTION = CLASSNAME_EVENT + "-caption";
	public static final String CLASSNAME_EVENT_CONTENT = CLASSNAME_EVENT + "-content";
	private static final String CLASSNAME_SCALEVALUE = VEventTimelineWidget.DISPLAY_CLASSNAME	+ "-vscale";
	private static final String CLASSNAME_SCALEVALUEDRAG = CLASSNAME_SCALEVALUE	+ "-drag";
	private static final String CLASSNAME_SCALEDATE = VEventTimelineWidget.DISPLAY_CLASSNAME + "-hscale";
	private static final String CLASSNAME_SCALEDATE_LEFT = CLASSNAME_SCALEDATE + "-left";

	public static final Long SECOND = 1000L;
	public static final Long MINUTE = 60000L;
	public static final Long HOUR = 3600000L;
	public static final Long DAY = 86400000L;
	public static final Long WEEK = 604800000L;
	public static final Long MONTH = 2629743830L;
	public static final Long YEAR = 31556926000L;

	private final VEventTimelineWidget widget;

	private final Element browserRoot;
	private final AbsolutePanel displayComponentPanel;

	private final Canvas canvas;

	private final HTML bottomBar;

	private final Map<Integer, List<VEvent>> currentEvents = new TreeMap<Integer, List<VEvent>>();
	private final List<VEventLabel> events = new ArrayList<VEventLabel>();

	private Map<Integer, Integer> maxSlots = new HashMap<Integer, Integer>();

	// The selected date range
	private Date currentStartDate = null;
	private Date currentEndDate = null;

	// Mouse actions
	private boolean mouseIsDown = false;
	private boolean mouseIsActive = false;
	private int mouseDownX = 0;
	private int lastMouseX = 0;

	// States
	private boolean enabled = true;
	private boolean forcePlot = false;

	// Dragging
	private Date currentStartDragDate = null;
	private Date currentEndDragDate = null;

	// Curtains
	private final HTML loadingCurtain;
	private final HTML disabledCurtain;

	// Scale components lists
	private final List<Label> horizontalScaleComponents = new ArrayList<Label>();
	private final List<Label> verticalScaleComponents = new ArrayList<Label>();

	// Error and loading messages
	private final Label noDataLabel = new Label("No data source.");

	// Graph formatting
	private String gridColor = "rgb(200,200,200)";

	private HandlerRegistration mouseMoveReg, mouseUpReg, mouseDownReg,
			mouseScrollReg, preview;

	private DateTimeFormat yearFormatShort = DateTimeFormat.getFormat("''yy");
	private DateTimeFormat yearFormatLong = DateTimeFormat.getFormat("yyyy");

	private DateTimeFormat monthFormatShort = DateTimeFormat.getFormat("MMM yyyy");
	private DateTimeFormat monthFormatLong = DateTimeFormat.getFormat("MMMM yyyy");

	private DateTimeFormat dayFormatShort = DateTimeFormat.getFormat("MMM d, yyyy");
	private DateTimeFormat dayFormatLong = DateTimeFormat.getFormat("MMMM d, yyyy");

	private DateTimeFormat timeFormatShort = DateTimeFormat.getFormat("HH:mm");
	private DateTimeFormat timeFormatLong = DateTimeFormat.getFormat("HH:mm:ss");

	public VEventTimelineDisplay(VEventTimelineWidget w) {
		widget = w;

		browserRoot = DOM.createDiv();
		setElement(browserRoot);
		setStyleName(VEventTimelineWidget.DISPLAY_CLASSNAME);

		// Add the components
		displayComponentPanel = new AbsolutePanel();
		browserRoot.appendChild(displayComponentPanel.getElement());
		DOM.setStyleAttribute(displayComponentPanel.getElement(), "position", "relative");

		// Add the canvas
		canvas = new Canvas(100, 100);

		canvas.setStyleName(CLASSNAME_CANVAS);
		displayComponentPanel.add(canvas, 0, 0);

		// Create the loading indicator
		loadingCurtain = new HTML("");
		loadingCurtain.setStyleName("v-app-loading");
		loadingCurtain.addStyleName(CLASSNAME_LOADINGCURTAIN);
		loadingCurtain.setWidth("100%");
		loadingCurtain.setHeight("100%");
		displayComponentPanel.add(loadingCurtain);

		// Create the bottom bar
		bottomBar = new HTML();
		bottomBar.setWidth("100%");
		bottomBar.setStyleName(CLASSNAME_BOTTOMBAR);
		displayComponentPanel.add(bottomBar);

		// Add no data source label
		noDataLabel.setVisible(false);
		displayComponentPanel.add(noDataLabel, 10, 10);

		// Create the disabled curtain
		disabledCurtain = new HTML("");
		disabledCurtain.setVisible(false);
		disabledCurtain.setStyleName(CLASSNAME_LOADINGCURTAIN);
		disabledCurtain.setWidth("100%");
		disabledCurtain.setHeight("100%");
		displayComponentPanel.add(disabledCurtain);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		mouseDownReg = addDomHandler(this, MouseDownEvent.getType());
		mouseUpReg = addDomHandler(this, MouseUpEvent.getType());
		mouseMoveReg = addDomHandler(this, MouseMoveEvent.getType());
		mouseScrollReg = addDomHandler(this, MouseWheelEvent.getType());
		preview = Event.addNativePreviewHandler(this);
	}

	@Override
	public void setHeight(String height) {
		super.setHeight(height);
		int canvasHeight = getOffsetHeight() - bottomBar.getOffsetHeight();
		canvas.setHeight(canvasHeight + "px");
		displayComponentPanel.setHeight(getOffsetHeight() + "px");
		displayComponentPanel.setWidgetPosition(bottomBar, 0, getOffsetHeight()
				- bottomBar.getOffsetHeight());
	}

	@Override
	public void setWidth(String width) {
		super.setWidth(width);
		canvas.setWidth(getOffsetWidth() + "px");
		displayComponentPanel.setWidth(getOffsetWidth() + "px");
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		if (mouseDownReg != null) {
			mouseDownReg.removeHandler();
			mouseDownReg = null;
		}
		if (mouseUpReg != null) {
			mouseUpReg.removeHandler();
			mouseUpReg = null;
		}
		if (mouseMoveReg != null) {
			mouseMoveReg.removeHandler();
			mouseMoveReg = null;
		}
		if (mouseScrollReg != null) {
			mouseScrollReg.removeHandler();
			mouseScrollReg = null;
		}
		if (preview != null) {
			preview.removeHandler();
			preview = null;
		}
	}

	@SuppressWarnings("deprecation")
	private void plotHorizontalScale(long startTime, long endTime,
			long unitTime, float xUnit, boolean leftAlign) {

		if (unitTime <= 0 || xUnit <= 0) {
			return;
		}

		float width = unitTime * xUnit;
		boolean shortDateFormat = width < 100;
		int year = widget.getStartDate().getYear();
		long time = (new Date(year, 0, 1)).getTime();

		DateTimeFormat formatter;
		if (unitTime < DAY) {
			formatter = shortDateFormat ? timeFormatShort : timeFormatLong;
		} else if (unitTime < MONTH) {
			formatter = shortDateFormat ? dayFormatShort : dayFormatLong;
		} else if (unitTime < YEAR) {
			formatter = shortDateFormat ? monthFormatShort : monthFormatLong;
		} else {
			formatter = shortDateFormat ? yearFormatShort : yearFormatLong;
		}

		if (gridColor != null) {
			canvas.setStrokeStyle(gridColor);
			canvas.setLineWidth(0.8);
			canvas.beginPath();
		}

		long stepsUntilInRange = (startTime - time) / unitTime;
		time += stepsUntilInRange * unitTime;

		while (time <= endTime) {
			if (time >= startTime - unitTime && time <= endTime + unitTime) {
				Label lbl = new Label();
				lbl.setStyleName(leftAlign ? CLASSNAME_SCALEDATE_LEFT : CLASSNAME_SCALEDATE);
				lbl.setWidth(width + "px");
				Date date = new Date(time);

				lbl.setText(widget.getDateTimeService().formatDate(date,
						formatter.getPattern()));

				long timeFromStart = time - startTime;
				float x = timeFromStart * xUnit;

				if (gridColor != null) {
					canvas.moveTo(x, 0);
					canvas.lineTo(x, canvas.getHeight());
				}

				displayComponentPanel.add(lbl, (int) x,
						displayComponentPanel.getOffsetHeight() - 15);
				horizontalScaleComponents.add(lbl);
			}

			if (unitTime == MONTH) {
				/*
				 * Month resolution is not so easy since it changes depending on
				 * the month. We use the Date to resolve the new time
				 */
				time += DateTimeService.getNumberOfDaysInMonth(new Date(time)) * DAY;
			} else if (unitTime == YEAR) {
				/*
				 * Take leap years into account
				 */
				if (DateTimeService.isLeapYear(new Date(time))) {
					time += unitTime + DAY;
				} else {
					time += unitTime;
				}

			} else {
				time += unitTime;
			}
		}

		if (gridColor != null) {
			canvas.closePath();
			canvas.stroke();
		}
	}

	/**
	 * Plots the horizontal scale
	 */
	private void plotHorizontalScale(float xUnit, long startTime, long endTime) {

		long timeDiff = endTime - startTime;

		for (Label lbl : horizontalScaleComponents) {
			displayComponentPanel.remove(lbl);
		}
		horizontalScaleComponents.clear();

		canvas.setGlobalCompositeOperation(Canvas.DESTINATION_OVER);

		// Selection is less than a minute
		if (timeDiff <= VEventTimelineDisplay.MINUTE) {
			plotHorizontalScale(startTime, endTime,
					10 * VEventTimelineDisplay.SECOND, xUnit, true);
		}

		// Selections is less the 5 minutes
		else if (timeDiff <= 5 * MINUTE) {
			plotHorizontalScale(startTime, endTime,
					VEventTimelineDisplay.MINUTE, xUnit, true);
		}

		// Selection is less than 30 minutes
		else if (timeDiff <= 30 * MINUTE) {
			plotHorizontalScale(startTime, endTime, 5 * MINUTE, xUnit, true);
		}

		// Selection is less than 1 hour
		else if (timeDiff <= HOUR) {
			plotHorizontalScale(startTime, endTime, 10 * MINUTE, xUnit, true);
		}

		// Selection is less then 6 hours
		else if (timeDiff <= 6 * HOUR) {
			plotHorizontalScale(startTime, endTime, 30 * MINUTE, xUnit, true);
		}

		// Selection is less then a half day
		else if (timeDiff <= 12 * HOUR) {
			plotHorizontalScale(startTime, endTime, HOUR, xUnit, false);
		}

		// Selection is less than a day
		else if (timeDiff <= DAY) {
			plotHorizontalScale(startTime, endTime, 2 * HOUR, xUnit, true);
		}

		// Selection is less than 3 days
		else if (timeDiff <= 3 * DAY) {
			plotHorizontalScale(startTime, endTime, 6 * HOUR, xUnit, true);
		}

		// Selection is less than a week. Show dayly view
		else if (timeDiff <= WEEK) {
			plotHorizontalScale(startTime, endTime, DAY, xUnit, false);
		}

		// Selection is less than two weeks
		else if (timeDiff <= 2 * WEEK) {
			plotHorizontalScale(startTime, endTime, 3 * DAY, xUnit, true);
		}

		// Selection is less than a month. Show weekly view
		else if (timeDiff <= 2 * MONTH) {
			plotHorizontalScale(startTime, endTime, WEEK, xUnit, true);
		}

		// Selection is less than two years
		else if (timeDiff <= 2 * YEAR) {
			plotHorizontalScale(startTime, endTime, MONTH, xUnit, false);
		}

		// Selection is more than two years
		else {
			plotHorizontalScale(startTime, endTime, YEAR, xUnit, false);
		}

		canvas.setGlobalCompositeOperation(Canvas.SOURCE_OVER);
	}

	public void redraw() {
		plotData(true);
	}

	private void plotData(boolean force) {
		// set the text fields with the correct date
		widget.setFromDateTextField(currentStartDate);
		widget.setToDateTextField(currentEndDate);

		// calculate some needed values
		Float canvasWidth = new Float(canvas.getWidth());
		// Float canvasHeight = new Float(canvas.getWidth());
		Long startTime = currentStartDate.getTime();
		Long endTime = currentEndDate.getTime();

		Long timeDiff = endTime - startTime;

		// ensure we have something to plot
		if (timeDiff <= 0) {
			return;
		}

		Float xUnit = canvasWidth / timeDiff.floatValue();

		// clear old drawings
		canvas.clear();

		// draw current time if it is in the current display
		Date d = new Date();
		float timeX = (d.getTime() - startTime) * xUnit;

		if (timeX > 0 && timeX < canvas.getWidth()) {
			canvas.beginPath();
			canvas.moveTo(timeX, canvas.getWidth());
			canvas.lineTo(timeX, 0);
			canvas.closePath();

			canvas.setStrokeStyle("#a00000");
			canvas.setLineWidth(1.0d);
			canvas.stroke();
		}

		if (force) {
			// remove old events
			for (VEventLabel w : events) {
				displayComponentPanel.remove(w);
			}

			events.clear();
		}

		// if there are no event widgets yet, force creation
		if (events.isEmpty()) {
			force = true;
		}

		float y = 5;

		if (!force) {
			// in case a redraw is not forced, just move the existing labels
			// this is an optimization in case the user is dragging the display
			for (VEventLabel w : events) {
				VEvent event = w.getEvent();
				Long eventStartTime = event.getStartTime();

				Long timeFromStart = eventStartTime - startTime;

				float x = timeFromStart * xUnit;

				DOM.setStyleAttribute(w.getElement(), "left", x + "px");
			}
		} else {

			// paint events
			for (Integer band : currentEvents.keySet()) {
				List<VEvent> eventList = currentEvents.get(band);

				int bandHeight = widget.getBandHeight(band);
				int slots = maxSlots.get(band);

				if (bandHeight > 0) {
					for (int idx = 0; idx < eventList.size(); idx++) {
						VEvent event = eventList.get(idx);
						Long eventStartTime = event.getStartTime();
						Long eventEndTime = event.getEndTime();

						Long timeFromStart = eventStartTime - startTime;

						float x = timeFromStart * xUnit;

						Long duration = eventEndTime - eventStartTime;
						float w = duration * xUnit;

						int cw = canvas.getWidth();
						if ((x >= 0 && x + w < cw)
								|| (x < 0 && x + w > 0 && x + w < cw)
								|| (x >= 0 && x < cw && x + w > cw)
								|| (x <= 0 && x + w > cw)) {
							if (force) {
								int height = Math.max(0, bandHeight - 6);
								plotEvent(event, x, y, w, slots, height);
							}
						}
					}
					y += bandHeight;
				}
			}
		}

		// paint horizontal lines to separate bands
		canvas.setStrokeStyle("#000");
		canvas.setLineWidth(0.5);
		canvas.beginPath();

		y = 1;
		for (Integer band : currentEvents.keySet()) {
			y += widget.getBandHeight(band);

			canvas.moveTo(0, y);
			canvas.lineTo(canvas.getWidth(), y);
		}

		canvas.closePath();
		canvas.stroke();

		// plot the horizontal scale
		plotHorizontalScale(xUnit, startTime, endTime);
	}

	private int checkOverlaps(List<VEvent> events, Long startTime, Long endTime) {
		ArrayList<VEvent> slots = new ArrayList<VEvent>();
		// first sweep get maximum slots
		for (VEvent event : events) {
			if (slots.isEmpty()) {
				slots.add(event);
				event.setSlotIndex(0);
				event.setHeight(1);
			} else {
				boolean foundSlot = false;
				for (int slotIdx = 0; slotIdx < slots.size(); slotIdx++) {
					VEvent slotEvent = slots.get(slotIdx);

					boolean overlaps = overlaps(slotEvent, event);

					if (foundSlot) {
						if (overlaps) {
							event.setHeight(slotIdx - event.getSlotIndex());
							break;
						}
					} else {
						if (!overlaps) {
							slots.set(slotIdx, event);
							event.setSlotIndex(slotIdx);
							event.setHeight(1);
							foundSlot = true;
						}
					}
				}
				if (!foundSlot) {
					slots.add(event);
					event.setSlotIndex(slots.size() - 1);
					event.setHeight(1);
				}
			}
		}

		return slots.size();
	}

	public boolean overlaps(VEvent evt1, VEvent evt2) {
		if ((evt1.getStartTime() >= evt2.getStartTime() && evt1.getStartTime() <= evt2
				.getEndTime())
				|| (evt2.getStartTime() >= evt1.getStartTime() && evt2
						.getStartTime() <= evt1.getEndTime())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Plots an event onto the display
	 * 
	 * @param caption
	 *            The caption that shows on the screen
	 * @param date
	 *            The date the event happened
	 * @param index
	 *            The server index of the event
	 * @param x
	 *            The X-coordinate
	 * @param y
	 *            The Y-coordinate
	 * @param w
	 *            The width
	 * @param maxHeight
	 *            The maximum height of the event
	 */
	private void plotEvent(VEvent event, float x, float y, float w, int slots,
			int maxHeight) {
		int space = 3;
		float slotHeight = (float) maxHeight / (float) slots;
		int slotPosition = (int) (slotHeight * (float) event.getSlotIndex());
		int elementHeight = (int) (slotHeight * (float) event.getHeight())
				- space;

		final VEventLabel eventElement = new VEventLabel(event, w, Math.max(0,
				elementHeight));
		displayComponentPanel
				.add(eventElement, (int) x, (int) y + slotPosition);

		String bg = ComputedStyle.getBackgroundColor(eventElement.getElement());
		event.setBackgroundColor(bg);

		events.add(eventElement);
	}

	public void dataReceived(Integer band, List<VEvent> events) {
		// Check if we have events
		if (events != null) {
			currentEvents.put(band, events);

			Long startTime = currentStartDate.getTime();
			Long endTime = currentEndDate.getTime();
			int slots = checkOverlaps(events, startTime, endTime);
			maxSlots.put(band, slots);
		}
	}

	public void dataReceivedAll() {
		// we have received data for all bands, redraw now the display

		plotData(forcePlot);

		// forced plotting is always turned off after a forced plot.
		// To force it again you have to set it on again be
		// before the next plot.
		forcePlot = false;

		// hide curtain
		setLoadingIndicatorVisible(false);
	}

	@Override
	public void dataRemoved(Integer[] bands) {
		for (int i = 0; i < bands.length; i++) {
			Integer band = bands[i];
			// Check if we have events
			if (events != null) {
				currentEvents.remove(band);
				maxSlots.remove(band.intValue());

				// // we have received data for all bands, redraw now the
				// display
				// plotData(forcePlot);
				//
				// // forced plotting is always turned off after a forced plot.
				// // To force it again you have to set it on again be
				// // before the next plot.
				// forcePlot = false;
				//
				// // hide curtain
				// setLoadingIndicatorVisible(false);
			}
		}

	}

	/**
	 * Should the spinning loading indicator be visible?
	 * 
	 * @param visible
	 *            Is it visible
	 */
	private void setLoadingIndicatorVisible(boolean visible) {
		loadingCurtain.setVisible(visible);
	}

	public boolean setRange(Date start, Date end) {
		if (mouseIsActive) {
			// if we are dragging then ignore any setRange requests
			return false;
		}

		return setRange(start, end, false, false, true);
	}

	/**
	 * Set the range to display
	 * 
	 * @param start
	 *            The start date
	 * @param end
	 *            The end date
	 * @return
	 */
	private boolean setRange(Date start, Date end, Boolean useCurtain,
			boolean forceServer, boolean forceRedraw) {
		if (!isVisible()) {
			return false;
		}

		if (start == null || end == null) {
			return false;
		}

		currentStartDate = start;
		currentEndDate = end;

		if (forceRedraw) {
			resetDisplayCache();

			// Get the events
			forcePlot = true;
			boolean cached = widget.getEvents(this, start, end, !forceServer);
			setLoadingIndicatorVisible(useCurtain && !cached);
		} else {
			plotData(false);
		}

		return true;
	}

	/**
	 * Resets the display cache
	 */
	public void resetDisplayCache() {
		currentEvents.clear();
	}

	/**
	 * Returns the canvas width
	 * 
	 * @return The width in pixels
	 */
	public int getCanvasWidth() {
		return canvas.getWidth();
	}

	/**
	 * Get the current starting date
	 * 
	 * @return The date
	 */
	public Date getSelectionStartDate() {
		return currentStartDate;
	}

	/**
	 * Get the current ending date
	 * 
	 * @return The date
	 */
	public Date getSelectionEndDate() {
		return currentEndDate;
	}

	/**
	 * Triggered when an VEvent widget is pressed
	 * 
	 * @param eventElement
	 */
	private void eventClick(VEventLabel eventElement) {
		VEvent event = eventElement.getEvent();
		widget.fireEventButtonClickEvent(event.getID());
	}

	/**
	 * Does the component have an specific element
	 * 
	 * @param elem
	 *            The element
	 * @return True if the component has the element
	 */
	public boolean hasElement(com.google.gwt.dom.client.Element elem) {
		for (Label lbl : horizontalScaleComponents) {
			if (lbl.getElement() == elem) {
				return true;
			}
		}

		for (Label lbl : verticalScaleComponents) {
			if (lbl.getElement() == elem) {
				return true;
			}
		}

		for (VEventLabel eventWidget : events) {
			if (eventWidget.getElement() == elem) {
				return true;
			}
		}

		if (elem == displayComponentPanel.getElement() || elem == getElement()
				|| elem == canvas.getElement()
				|| elem == loadingCurtain.getElement() || elem == browserRoot
				|| elem == bottomBar.getElement()
				|| elem == disabledCurtain.getElement()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Displays the no data found message
	 * 
	 * @param enabled
	 *            Is the message displayed
	 */
	public void displayNoDataMessage(boolean enabled) {
		noDataLabel.setVisible(enabled);
		setLoadingIndicatorVisible(false);
	}

	/**
	 * Is the display enabled
	 * 
	 * @param enabled
	 *            True if enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setLoadingIndicatorVisible(false);
	}

	/**
	 * Force plotting the the data even if it is the same time range. Forced
	 * plotting is always turned off after a forced plot so you will have to
	 * turn on forced plotting again before the next plot.
	 * 
	 * @param enabled
	 *            The plotting is forced
	 */
	public void setForcedPlotting(boolean enabled) {
		forcePlot = enabled;
	}

	/**
	 * Sets the scale grid color, set to null to not draw the grid
	 * 
	 * @param color
	 *            The color
	 */
	public void setGridColor(String color) {
		gridColor = color;
	}

	int dragX = 0;
	private Command drag = new Command() {
		public void execute() {
			Long timeDiff = currentEndDragDate.getTime()
					- currentStartDragDate.getTime();
			float xdiff = (dragX - mouseDownX) / 2f;
			float canvasWidth = canvas.getWidth();
			float widthUnit = xdiff / canvasWidth;

			Float time = timeDiff * widthUnit;

			Long start = currentStartDate.getTime() - time.longValue();
			Long end = currentEndDate.getTime() - time.longValue();

			Date startDate = new Date(start);
			Date endDate = new Date(end);

			setRange(startDate, endDate, false, false, false);
			widget.setBrowserRange(startDate, endDate);
			mouseDownX = dragX;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.event.dom.client.MouseMoveHandler#onMouseMove(com.google
	 * .gwt.event.dom.client.MouseMoveEvent)
	 */
	public void onMouseMove(MouseMoveEvent event) {
		NativeEvent mouseEvent = event.getNativeEvent();
		lastMouseX = event.getClientX();

		// Dragging action occurring..
		if (mouseIsDown && enabled && currentEndDragDate != null
				&& currentStartDragDate != null) {
			dragX = mouseEvent.getClientX();
			drag.execute();

			// Mouse is hovering over the display area
		} else if (enabled) {
			// int x = mouseEvent.getClientX() - getAbsoluteLeft();
			// int y = mouseEvent.getClientY() - getAbsoluteTop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.event.dom.client.MouseUpHandler#onMouseUp(com.google.gwt
	 * .event.dom.client.MouseUpEvent)
	 */
	public void onMouseUp(MouseUpEvent event) {
		mouseIsActive = false;

		DOM.releaseCapture(canvas.getElement());

		if (enabled && currentEndDragDate != null
				&& currentStartDragDate != null) {
			Long timeDiff = currentEndDragDate.getTime()
					- currentStartDragDate.getTime();
			float xdiff = (lastMouseX - mouseDownX) / 2f;
			float canvasWidth = canvas.getWidth();
			float widthUnit = xdiff / canvasWidth;

			Float time = timeDiff * widthUnit;

			Long start = currentStartDate.getTime() - time.longValue();
			Long end = currentEndDate.getTime() - time.longValue();

			Date startDate = new Date(start);
			Date endDate = new Date(end);

			if (startDate.compareTo(widget.getStartDate()) >= 0
					&& endDate.compareTo(widget.getEndDate()) <= 0) {

				// only trigger a redraw of the display, if the mouse has been
				// moved (dragged)
				boolean mouseMoved = startDate.compareTo(currentStartDragDate) != 0;
				if (mouseMoved
						&& setRange(startDate, endDate, true, false, true)) {
					// widget.setBrowserRange(startDate, endDate);
					mouseDownX = lastMouseX;
					widget.fireDateRangeChangedEvent();
				}
			}

			for (Label lbl : verticalScaleComponents) {
				lbl.setStyleName(CLASSNAME_SCALEVALUE);
			}
			canvas.setStyleName(CLASSNAME_CANVAS);

			currentStartDragDate = null;
			currentEndDragDate = null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google
	 * .gwt.event.dom.client.MouseDownEvent)
	 */
	public void onMouseDown(MouseDownEvent event) {
		NativeEvent mouseEvent = event.getNativeEvent();

		mouseIsActive = true;

		event.preventDefault();
		event.stopPropagation();

		if (enabled) {
			mouseDownX = mouseEvent.getClientX();
			canvas.setStyleName(CLASSNAME_CANVASDRAG);

			for (Label lbl : verticalScaleComponents) {
				lbl.setStyleName(CLASSNAME_SCALEVALUEDRAG);
			}

			DOM.setCapture(canvas.getElement());

			currentStartDragDate = new Date(currentStartDate.getTime());
			currentEndDragDate = new Date(currentEndDate.getTime());
		}

		// Remove the selected style from the widget again
		for (VEventLabel w : events) {
			w.removeStyleName(CLASSNAME_EVENT_SELECTED);
		}

		// Check if element is a event button
		for (VEventLabel w : events) {
			w.removeStyleName(CLASSNAME_EVENT_SELECTED);
			com.google.gwt.dom.client.Element mouseEventElement = Element
					.as(mouseEvent.getEventTarget());
			if (mouseEventElement == w.getElement()
					|| mouseEventElement.getParentElement() == w.getElement()) {
				w.addStyleName(CLASSNAME_EVENT_SELECTED);
				eventClick(w);
				break;
			}
		}
	}

	public void onMouseWheel(MouseWheelEvent event) {
		NativeEvent mouseEvent = event.getNativeEvent();
		if (hasElement(Element.as(mouseEvent.getEventTarget()))) {
			event.preventDefault();
			float x = mouseEvent.getClientX();
			boolean up = mouseEvent.getMouseWheelVelocityY() > 0;
			float canvasWidth = canvas.getWidth();
			float ratio = x / canvasWidth;

			// Calculate the minimum timeunit for the whole range
			long diff = widget.getEndDate().getTime()
					- widget.getStartDate().getTime();
			float minTimeSpan = (diff / canvasWidth) * 60f;
			float minAllowedTimeSpan = (diff / canvasWidth) * 20f;

			Float startRatio = ratio * minTimeSpan;
			Float endRatio = (1f - ratio) * minTimeSpan;

			if (up) {
				Date start = new Date(currentStartDate.getTime()
						- startRatio.longValue());

				if (start.before(widget.getStartDate())) {
					/*
					 * Ensure we are in bounds
					 */
					start = widget.getStartDate();
				}

				Date end = new Date(currentEndDate.getTime()
						+ endRatio.longValue());

				if (end.after(widget.getEndDate())) {
					/*
					 * Ensure we are in bounds
					 */
					end = widget.getEndDate();
				}

				if (end.getTime() - start.getTime() > minAllowedTimeSpan) {
					currentStartDate = start;
					currentEndDate = end;
					refresh();
					widget.setBrowserRange(currentStartDate, currentEndDate);
				}

			} else {
				Date start = new Date(currentStartDate.getTime()
						+ startRatio.longValue());

				if (start.before(widget.getStartDate())) {
					/*
					 * Ensure we are in bounds
					 */
					start = widget.getStartDate();
				}

				Date end = new Date(currentEndDate.getTime()
						- endRatio.longValue());

				if (end.after(widget.getEndDate())) {
					/*
					 * Ensure we are in bounds
					 */
					end = widget.getEndDate();
				}

				if (end.getTime() - start.getTime() > minAllowedTimeSpan) {
					currentStartDate = start;
					currentEndDate = end;
					refresh();
					widget.setBrowserRange(currentStartDate, currentEndDate);
				}
			}
		}
	}

	public void onPreviewNativeEvent(NativePreviewEvent event) {
		// Monitor mouse button state
		if (event.getTypeInt() == Event.ONMOUSEUP
				&& event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
			mouseIsDown = false;
			if (mouseIsActive) {
				onMouseUp(null);
			}
		} else if (event.getTypeInt() == Event.ONMOUSEDOWN
				&& event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
			mouseIsDown = true;
		}
	}

	/**
	 * Initializes the canvas i.e. it will fetch the points for the whole
	 * timeline and render it to the canvas.
	 */
	public void refresh() {
		if (!isVisible()) {
			return;
		}
		setRange(getSelectionStartDate(), getSelectionEndDate(), false, false,
				true);
	}

	public void setYearFormatShort(DateTimeFormat format) {
		this.yearFormatShort = format;
	}

	public void setYearFormatLong(DateTimeFormat format) {
		this.yearFormatLong = format;
	}

	public void setMonthFormatShort(DateTimeFormat format) {
		this.monthFormatShort = format;
	}

	public void setMonthFormatLong(DateTimeFormat format) {
		this.monthFormatLong = format;
	}

	public void setDayFormatShort(DateTimeFormat format) {
		this.dayFormatShort = format;
	}

	public void setDayFormatLong(DateTimeFormat format) {
		this.dayFormatLong = format;
	}

	public void setTimeFormatShort(DateTimeFormat format) {
		this.timeFormatShort = format;
	}

	public void setTimeFormatLong(DateTimeFormat format) {
		this.timeFormatLong = format;
	}

	public boolean isMouseActive() {
		return mouseIsActive;
	}
}
