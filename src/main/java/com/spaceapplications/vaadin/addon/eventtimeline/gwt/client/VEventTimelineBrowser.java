/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
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
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.spaceapplications.vaadin.addon.eventtimeline.gwt.canvas.client.Canvas;
import com.vaadin.terminal.gwt.client.DateTimeService;

/**
 * VEventTimelineBrowser, based on original version from vaadin-timeline.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 * @author Peter Lehto / IT Mill Oy Ltd
 * @author John Ahlroos / IT Mill Oy Ltd
 */
public class VEventTimelineBrowser extends Widget implements VDataListener,
		MouseDownHandler, MouseMoveHandler, MouseUpHandler, MouseWheelHandler,
		DoubleClickHandler, NativePreviewHandler {

	private static final String CLASSNAME_CANVAS = VEventTimelineWidget.BROWSER_CLASSNAME	+ "-canvas";
	private static final String CLASSNAME_SCROLLBAR = VEventTimelineWidget.BROWSER_CLASSNAME + "-scrollbar";
	private static final String CLASSNAME_SCROLLBAR_LEFT = CLASSNAME_SCROLLBAR + "-left";
	private static final String CLASSNAME_SCROLLBAR_RIGHT = CLASSNAME_SCROLLBAR	+ "-right";
	private static final String CLASSNAME_FADE = CLASSNAME_SCROLLBAR + "-fade";
	private static final String CLASSNAME_SCALE = VEventTimelineWidget.BROWSER_CLASSNAME + "-scale";
	private static final String CLASSNAME_SCALELABEL = CLASSNAME_SCALE + "-label";
	private static final String CLASSNAME_CURTAIN = VEventTimelineWidget.BROWSER_CLASSNAME + "-curtain";

	private final Element browserRoot;

	private final Canvas canvas;

	private final Element scrollBar;

	private final Element scrollLeft;
	private final Element scrollRight;

	private boolean mouseDown;
	private boolean mouseIsActive = false;

	private final VEventTimelineWidget timelineWidget;

	private boolean sizeAdjustLeft;
	private boolean sizeAdjustRight;

	private int dragStartX;

	private final VEventTimelineBrowserScroller scroller = new VEventTimelineBrowserScroller();

	private final Map<Integer, Set<VEvent>> currentEvents = new TreeMap<Integer, Set<VEvent>>();

	private final Element leftFade;
	private final Element rightFade;

	private Date selectedStartDate = null;
	private Date selectedEndDate = null;

	private final AbsolutePanel horizontalScalePanel;
	private final List<Label> horizontalScaleComponents = new ArrayList<Label>();

	private final HTML disabledCurtain;

	private HandlerRegistration mouseMoveReg, mouseUpReg, mouseDownReg,
			mouseScrollReg, mouseClickReg, preview;

	private DateTimeFormat yearFormatShort = DateTimeFormat.getFormat("''yy");
	private DateTimeFormat yearFormatLong = DateTimeFormat.getFormat(PredefinedFormat.YEAR);

	private DateTimeFormat monthFormatShort = DateTimeFormat.getFormat(PredefinedFormat.YEAR_MONTH_ABBR);
	private DateTimeFormat monthFormatLong = DateTimeFormat.getFormat(PredefinedFormat.YEAR_MONTH);

	private DateTimeFormat dayFormatShort = DateTimeFormat.getFormat(PredefinedFormat.YEAR_MONTH_ABBR_DAY);
	private DateTimeFormat dayFormatLong = DateTimeFormat.getFormat(PredefinedFormat.YEAR_MONTH_DAY);

	private DateTimeFormat timeFormatShort = DateTimeFormat.getFormat(PredefinedFormat.HOUR24_MINUTE);
	private DateTimeFormat timeFormatLong = DateTimeFormat.getFormat(PredefinedFormat.HOUR24_MINUTE_SECOND);

	public VEventTimelineBrowser(VEventTimelineWidget tw) {
		timelineWidget = tw;

		browserRoot = DOM.createDiv();
		setElement(browserRoot);

		setHeight("64px");
		setStyleName(VEventTimelineWidget.BROWSER_CLASSNAME);

		canvas = new Canvas(300, 44);
		canvas.setStyleName(CLASSNAME_CANVAS);
		browserRoot.appendChild(canvas.getElement());

		// Add the horizontal scale
		horizontalScalePanel = new AbsolutePanel();
		horizontalScalePanel.setStyleName(CLASSNAME_SCALE);
		browserRoot.appendChild(horizontalScalePanel.getElement());

		// Add fade-outs
		leftFade = DOM.createDiv();
		leftFade.setClassName(CLASSNAME_FADE);
		browserRoot.appendChild(leftFade);

		rightFade = DOM.createDiv();
		rightFade.setClassName(CLASSNAME_FADE);
		browserRoot.appendChild(rightFade);

		// Add the scrollbar
		scrollBar = DOM.createDiv();
		scrollBar.setClassName(CLASSNAME_SCROLLBAR);

		scrollLeft = DOM.createDiv();
		scrollLeft.setClassName(CLASSNAME_SCROLLBAR_LEFT);

		scrollRight = DOM.createDiv();
		scrollRight.setClassName(CLASSNAME_SCROLLBAR_RIGHT);

		scrollBar.appendChild(scrollLeft);
		scrollBar.appendChild(scrollRight);

		browserRoot.appendChild(scrollBar);
		browserRoot.appendChild(scroller.getElement());

		// Create the disabled curtain
		disabledCurtain = new HTML("");
		disabledCurtain.setVisible(false);
		disabledCurtain.setStyleName(CLASSNAME_CURTAIN);
		disabledCurtain.setWidth("100%");
		disabledCurtain.setHeight("100%");
		disabledCurtain.getElement().getStyle().setZIndex(2);
		horizontalScalePanel.add(disabledCurtain);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		mouseDownReg = addDomHandler(this, MouseDownEvent.getType());
		mouseUpReg = addDomHandler(this, MouseUpEvent.getType());
		mouseMoveReg = addDomHandler(this, MouseMoveEvent.getType());
		mouseScrollReg = addDomHandler(this, MouseWheelEvent.getType());
		mouseClickReg = addDomHandler(this, DoubleClickEvent.getType());
		preview = Event.addNativePreviewHandler(this);

	}

	@Override
	public void setWidth(String width) {
		super.setWidth(width);
		int w = getOffsetWidth() - 28 - 2;
		canvas.setWidth(w + "px");
		browserRoot.setAttribute("width", w + "px");
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
		if (mouseClickReg != null) {
			mouseClickReg.removeHandler();
			mouseClickReg = null;
		}
		if (preview != null) {
			preview.removeHandler();
			preview = null;
		}
	}

	/**
	 * Initializes the canvas i.e. it will fetch the points for the whole
	 * timeline and render it to the canvas.
	 */
	public void refresh() {
		if (!isVisible() || timelineWidget.getStartDate() == null
				|| timelineWidget.getEndDate() == null) {
			return;
		}

		// Get the events
		timelineWidget.getEvents(this, timelineWidget.getStartDate(),
				timelineWidget.getEndDate(), true);
	}

	public void redraw() {
		// Plot the points
		plotData();

		// Set the scroller range
    // disable as this causes problems when an event is updated
		//setRange(selectedStartDate, selectedEndDate);
	}

	/*
	 * (non-Javadoc)
	 */
	public void dataReceived(Integer band, List<VEvent> events) {
		if (events != null) {
			Set<VEvent> bandEvents = currentEvents.get(band);
			if (bandEvents == null) {
				bandEvents = new TreeSet<VEvent>();
				currentEvents.put(band, bandEvents);
			}

			// TODO - Thomas -> what do you think about #clear()?
			// Should be OK since events always contains all events
			bandEvents.clear();
			bandEvents.addAll(events);
		}
	}

	/*
	 * (non-Javadoc)
	 */
	public void dataReceivedAll() {
		// Plot the received data
		plotData();

		/*
		 * If the selection is locked then we want to reset the range so it is
		 * pointing to the same selection in case a point has been added or
		 * removed from the graph. If the selection is not locked we need to
		 * recalculate the new selection.
		 */
		if (timelineWidget.isInitDone() && timelineWidget.isSelectionLocked()
				&& !mouseDown) {
		  // disable as this causes problems when an event is updated
			//setRange(selectedStartDate, selectedEndDate);
		} else {
			refreshSelection();
		}
	}

	@Override
	public void dataRemoved(Integer[] bands) {
		for (int i = 0; i < bands.length; i++) {
			Integer band = bands[i];
			currentEvents.remove(band);
		}
	}

	/**
	 * Plots the horizontal scale
	 */
	private void plotHorizontalScale(float xUnit, long startTime, long endTime) {
		long timeDiff = endTime - startTime;

		for (Label lbl : horizontalScaleComponents) {
			horizontalScalePanel.remove(lbl);
		}
		horizontalScaleComponents.clear();

		canvas.setGlobalCompositeOperation(Canvas.DESTINATION_OVER);

		// Selection is less than a minute
		if (timeDiff <= VEventTimelineDisplay.MINUTE) {
			plotHorizontalScale(startTime, endTime,
					10 * VEventTimelineDisplay.SECOND, xUnit, true);
		}

		// Selection is less than five minutes
		else if (timeDiff <= 5 * VEventTimelineDisplay.MINUTE) {
			plotHorizontalScale(startTime, endTime,
					VEventTimelineDisplay.MINUTE, xUnit, true);
		}

		// Selection is less than 30 minutes
		else if (timeDiff <= 30 * VEventTimelineDisplay.MINUTE) {
			plotHorizontalScale(startTime, endTime,
					5 * VEventTimelineDisplay.MINUTE, xUnit, true);
		}

		// Selection is less than 1 hour
		else if (timeDiff <= VEventTimelineDisplay.HOUR) {
			plotHorizontalScale(startTime, endTime,
					10 * VEventTimelineDisplay.MINUTE, xUnit, true);
		}

		// Selection is less then 6 hours
		else if (timeDiff <= 6 * VEventTimelineDisplay.HOUR) {
			plotHorizontalScale(startTime, endTime,
					30 * VEventTimelineDisplay.MINUTE, xUnit, true);
		}

		// Selection is less then a half day
		else if (timeDiff <= 12 * VEventTimelineDisplay.HOUR) {
			plotHorizontalScale(startTime, endTime, VEventTimelineDisplay.HOUR,
					xUnit, false);
		}

		// Selection is less than a day
		else if (timeDiff <= VEventTimelineDisplay.DAY) {
			plotHorizontalScale(startTime, endTime,
					2 * VEventTimelineDisplay.HOUR, xUnit, true);
		}

		// Selection is less than 3 days
		else if (timeDiff <= 3 * VEventTimelineDisplay.DAY) {
			plotHorizontalScale(startTime, endTime,
					6 * VEventTimelineDisplay.HOUR, xUnit, true);
		}

		// Selection is less than a week. Show dayly view
		else if (timeDiff <= VEventTimelineDisplay.WEEK) {
			plotHorizontalScale(startTime, endTime, VEventTimelineDisplay.DAY,
					xUnit, false);
		}

		// Selection is less than two weeks
		else if (timeDiff <= 2 * VEventTimelineDisplay.WEEK) {
			plotHorizontalScale(startTime, endTime,
					3 * VEventTimelineDisplay.DAY, xUnit, true);
		}

		// Selection is less than a month. Show weekly view
		else if (timeDiff <= 2 * VEventTimelineDisplay.MONTH) {
			plotHorizontalScale(startTime, endTime, VEventTimelineDisplay.WEEK,
					xUnit, true);
		}

		// Selection is less than a year
		else if (timeDiff <= 6 * VEventTimelineDisplay.MONTH) {
			plotHorizontalScale(startTime, endTime,
					VEventTimelineDisplay.MONTH, xUnit, false);
		}

		// Selection is less than a year
		else if (timeDiff <= VEventTimelineDisplay.YEAR) {
			plotHorizontalScale(startTime, endTime,
					VEventTimelineDisplay.MONTH, xUnit, false);
		}

		// Selection is more than two years
		else {
			plotHorizontalScale(startTime, endTime, VEventTimelineDisplay.YEAR,
					xUnit, false);
		}

		canvas.setGlobalCompositeOperation(Canvas.SOURCE_OVER);
	}

	@SuppressWarnings("deprecation")
	private void plotHorizontalScale(long startTime, long endTime,
			long unitTime, float xUnit, boolean leftAlign) {

		float width = unitTime * xUnit;
		boolean shortDateFormat = width < 100;
		int year = timelineWidget.getStartDate().getYear();
		long time = (new Date(year, 0, 1)).getTime();

		DateTimeFormat formatter;
		if (unitTime < VEventTimelineDisplay.DAY) {
			formatter = shortDateFormat ? timeFormatShort : timeFormatLong;
		} else if (unitTime < VEventTimelineDisplay.MONTH) {
			formatter = shortDateFormat ? dayFormatShort : dayFormatLong;
		} else if (unitTime < VEventTimelineDisplay.YEAR) {
			formatter = shortDateFormat ? monthFormatShort : monthFormatLong;
		} else {
			formatter = shortDateFormat ? yearFormatShort : yearFormatLong;
		}

		canvas.setStrokeStyle("rgb(200,200,200)");
		canvas.beginPath();

		long stepsUntilInRange = (startTime - time) / unitTime;
		time += stepsUntilInRange * unitTime;

		while (time <= endTime) {
			if (time >= startTime - unitTime && time <= endTime + unitTime) {
				Label lbl = new Label();
				lbl.setStyleName(CLASSNAME_SCALELABEL);
				lbl.setWidth(width + "px");
				Date date = new Date(time);
				lbl.setText(formatter.format(date));

				long timeFromStart = time - startTime;
				float x = timeFromStart * xUnit;

				canvas.moveTo(x, 0);
				canvas.lineTo(x, canvas.getHeight());

				horizontalScalePanel.add(
						lbl,
						(int) x + 14 + 1,
						horizontalScalePanel.getOffsetHeight()
								- scrollBar.getOffsetHeight() - 13);
				horizontalScaleComponents.add(lbl);
			}

			if (unitTime == VEventTimelineDisplay.MONTH) {
				/*
				 * Month resolution is not so easy since it changes depending on
				 * the month. We use the Date to resolve the new time
				 */
				time += DateTimeService.getNumberOfDaysInMonth(new Date(time))
						* VEventTimelineDisplay.DAY;
			} else if (unitTime == VEventTimelineDisplay.YEAR) {
				/*
				 * Take leap years into account
				 */
				if (DateTimeService.isLeapYear(new Date(time))) {
					time += unitTime + VEventTimelineDisplay.DAY;
				} else {
					time += unitTime;
				}

			} else {
				time += unitTime;
			}
		}

		canvas.closePath();
		canvas.stroke();
	}

	/**
	 * Plots the received data on to the canvas
	 */
	private void plotData() {
		if (!isVisible()) {
			return;
		}

		Float canvasWidth = (float) getCanvasWidth();
		Float canvasHeight = new Float(canvas.getHeight());

		// clear old drawings
		canvas.clear();

		Long timeDiff = timelineWidget.getEndDate().getTime()
				- timelineWidget.getStartDate().getTime();
		float xUnit = canvasWidth / timeDiff.floatValue();

		// draw current time if it is in the current display
		Date d = new Date();
		float timeX = (d.getTime() - timelineWidget.getStartDate().getTime())
				* xUnit;

		if (timeX > 0 && timeX < canvas.getWidth()) {
			canvas.beginPath();
			canvas.moveTo(timeX, canvas.getHeight());
			canvas.lineTo(timeX, 0);
			canvas.closePath();

			canvas.setStrokeStyle("#a00000");
			canvas.setLineWidth(1.0d);
			canvas.stroke();
		}

		// plot the events, the bands heights are evenly distributed
		int numBands = currentEvents.size();
		int bandHeight = canvasHeight.intValue() - 10;
		if (numBands > 0) {
			bandHeight = bandHeight / numBands - 2;
		}
		float y = 1;
		long startTime = timelineWidget.getStartDate().getTime();
		for (Set<VEvent> eventSet : currentEvents.values()) {
			float lastX = 0;
			for (VEvent event : eventSet) {
				Long eventStartTime = event.getStartTime();
				Long eventEndTime = event.getEndTime();

				Long timeFromStart = eventStartTime - startTime;
				float x = timeFromStart * xUnit;

				Long duration = eventEndTime - eventStartTime;
				float w = duration * xUnit;

				if (x < lastX) {
					w -= lastX - x;
					x = lastX;
				}

				plotEvent(event, x, y, w, bandHeight);
				lastX = x + w;
			}

			y += bandHeight + 2;
		}

		// plot the horizontal scale
		plotHorizontalScale(xUnit, timelineWidget.getStartDate().getTime(),
				timelineWidget.getEndDate().getTime());
	}

	/**
	 * Plots an event onto the display
	 * 
	 * @param event
	 *            The event
	 * @param x
	 *            The X-coordinate
	 * @param y
	 *            The Y-coordinate
	 * @param w
	 *            The width
	 * @param h
	 *            The height
	 */
	private void plotEvent(VEvent event, float x, float y, float w, float h) {
		canvas.save();
		canvas.beginPath();
		canvas.rect(x, y, w, h);
		canvas.closePath();

		canvas.setLineWidth(0.5);
		canvas.setStrokeStyle("#ccc");

		String bgColor = event.getBackgroundColor();
		String fillStyle = (bgColor == null) ? "#ccc" : CssColor.make(bgColor)
				.value();
		canvas.setFillStyle(fillStyle);

		canvas.stroke();
		canvas.fill();
		canvas.restore();
	}

	/**
	 * Set the selected range
	 * 
	 * @param startDate
	 *            The start date of the range
	 * @param endDate
	 *            The end date of the range
	 */
	public void setRange(Date startDate, Date endDate) {
		Date start = timelineWidget.getStartDate();
		Date end = timelineWidget.getEndDate();

		if (startDate == null || startDate.before(start)) {
			startDate = start;
		}

		if (endDate == null || endDate.after(end)) {
			endDate = end;
		}

		selectedStartDate = startDate;
		selectedEndDate = endDate;

		Long timeDiff = end.getTime() - start.getTime();
		Float timeUnit = (float) canvas.getOffsetWidth() / (float) timeDiff;

		Long startTime = startDate.getTime() - start.getTime();
		int startPixel = (int) (startTime * timeUnit) + 14;

		Long endTime = endDate.getTime() - start.getTime();
		int endPixel = (int) (endTime * timeUnit) - 3;

		if (startPixel < 14) {
			startPixel = 14;
		}

		if (endPixel > getOffsetWidth() - 16) {
			endPixel = getOffsetWidth() - 16;
		}

		scroller.setLeftPosition(startPixel);
		scroller.setRightPosition(endPixel);

		DOM.setStyleAttribute(leftFade, "width", startPixel + "px");
		DOM.setStyleAttribute(rightFade, "left", (endPixel + 14 + 2) + "px");
		DOM.setStyleAttribute(rightFade, "width",
				(getOffsetWidth() - endPixel - 28) + "px");
	}

	/**
	 * Converts the selectors start pixel position to a date
	 * 
	 * @return The calculated date
	 */
	private Date calculateStartPoint() {
		Date start = timelineWidget.getStartDate();
		Date end = timelineWidget.getEndDate();
		Float canvasWidth = (float) getCanvasWidth();
		Long timeDiff = end.getTime() - start.getTime();
		Float timeUnit = timeDiff.floatValue() / canvasWidth;

		int selectorLeftPixel = scroller.getLeftPosition();
		Float time = selectorLeftPixel * timeUnit;

		Date date = new Date(start.getTime() + time.longValue());

		if (date.before(start)) {
			date = start;
		}

		if (date.after(end)) {
			date = end;
		}

		return date;
	}

	/**
	 * Converts the selectors end pixel position to a date
	 * 
	 * @return The calculated date
	 */
	private Date calculateEndPoint() {
		Date start = timelineWidget.getStartDate();
		Date end = timelineWidget.getEndDate();
		Float canvasWidth = (float) getCanvasWidth();
		Long timeDiff = end.getTime() - start.getTime();
		Float timeUnit = timeDiff.floatValue() / canvasWidth;

		int selectorRightPixel = scroller.getRightPosition();
		Float time = selectorRightPixel * timeUnit;

		Date date = new Date(start.getTime() + time.longValue());

		if (date.before(start)) {
			date = start;
		}

		if (date.after(end)) {
			date = end;
		}

		return date;
	}

	/**
	 * Alias for calculateStartPoint
	 * 
	 * @return The calculated date
	 */
	public Date getSelectedStartDate() {
		return selectedStartDate;
	}

	/**
	 * Alias for calculateEndPoint
	 * 
	 * @return The calculated date
	 */
	public Date getSelectedEndDate() {
		return selectedEndDate;
	}

	/**
	 * Get the canvas width
	 * 
	 * @return The width in pixels
	 */
	public int getCanvasWidth() {
		if (isVisible()) {
			return canvas.getOffsetWidth();
		} else {
			return timelineWidget.getWidgetWidth() - 28 - 2;
		}
	}

	/**
	 * Checks if element exists in the browser
	 * 
	 * @param elem
	 *            The element
	 * @return True if the element exists, else false
	 */
	public boolean hasElement(com.google.gwt.dom.client.Element elem) {
		if (elem == getElement() || elem == browserRoot || elem == leftFade
				|| elem == rightFade || elem == scrollBar || elem == scrollLeft
				|| elem == scrollRight || elem == canvas.getElement()
				|| elem == horizontalScalePanel.getElement()
				|| scroller.hasElement(elem)
				|| elem == disabledCurtain.getElement()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Enables the browser
	 * 
	 * @param enabled
	 *            Is the browser usable by the user
	 */
	public void setEnabled(boolean enabled) {
		disabledCurtain.setVisible(!enabled);
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

		DOM.releaseCapture(horizontalScalePanel.getElement());

		if (sizeAdjustLeft || sizeAdjustRight) {
			scroller.lockSize();
			selectedStartDate = calculateStartPoint();
			selectedEndDate = calculateEndPoint();
			setDateRangeTimer.cancel();
			setDateRangeTimer.schedule(100);

			DOM.setStyleAttribute(leftFade, "width",
					(scroller.getLeftPosition() + 14) + "px");
			DOM.setStyleAttribute(rightFade, "left",
					(scroller.getRightPosition() + 14 + 2) + "px");
			DOM.setStyleAttribute(rightFade, "width", (getOffsetWidth()
					- scroller.getRightPosition() - 28)
					+ "px");
			timelineWidget.fireDateRangeChangedEvent();

		} else {
			selectedStartDate = calculateStartPoint();
			selectedEndDate = calculateEndPoint();
			setDateRangeTimer.cancel();
			setDateRangeTimer.schedule(100);

			DOM.setStyleAttribute(leftFade, "width",
					(scroller.getLeftPosition() + 14) + "px");
			DOM.setStyleAttribute(rightFade, "left",
					(scroller.getRightPosition() + 14 + 2) + "px");
			DOM.setStyleAttribute(rightFade, "width", (getOffsetWidth()
					- scroller.getRightPosition() - 28)
					+ "px");

			timelineWidget.fireDateRangeChangedEvent();
		}

		sizeAdjustLeft = false;
		sizeAdjustRight = false;
	}

	private Timer setDateRangeTimer = new Timer() {
		@Override
		public void run() {
			timelineWidget.setDisplayRange(selectedStartDate, selectedEndDate);
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
		if (mouseDown) {
			int adjustment = mouseEvent.getClientX() - dragStartX;

			// The area size is being adjusted
			if (sizeAdjustLeft && scroller.getLeftPosition() + adjustment > 0) {
				if (scroller.getWidth() > adjustment) {
					scroller.adjustLeftSideSize(adjustment);
				}
			} else if (sizeAdjustLeft) {
				scroller.adjustLeftSideSize(-scroller.getLeftPosition());
			} else if (sizeAdjustRight
					&& scroller.getRightPosition() + adjustment < scrollBar
							.getOffsetWidth() - 2 * 15) {
				scroller.adjustRightSideSize(adjustment);
			} else if (sizeAdjustRight) {
				int diff = (scrollBar.getOffsetWidth() - 2 * 15)
						- scroller.getRightPosition();
				scroller.adjustRightSideSize(diff - 1);
			} else {

				int scrollAreaX = mouseEvent.getClientX() - dragStartX;

				/*
				 * Calculate the right edge. Remember to add the left and right
				 * spacing 2*14px and the borders 2*1px to the calculation.
				 */
				int scrollAreaRight = scrollAreaX + scroller.getAreaWidth()
						+ 14 + 2;

				if (scrollAreaRight <= scrollBar.getOffsetWidth()) {
					int offset = scroller.getMouseOffset((Event) mouseEvent);
					int leftPosition = mouseEvent.getClientX() - dragStartX;

					if (offset != dragStartX) {
						scroller.setLeftPosition(leftPosition);

						if (leftPosition > 14) {
							dragStartX = scroller
									.getMouseOffset((Event) mouseEvent);
						}

						selectedStartDate = calculateStartPoint();
						selectedEndDate = calculateEndPoint();

						setDateRangeTimer.cancel();
						setDateRangeTimer.schedule(100);
					}

					/*
					 * Else if the cursor is over the right edge make sure the
					 * browser is in its rightmost position.
					 */
				} else {

					int width = scroller.getAreaWidth();
					int leftPosition = scrollBar.getOffsetWidth() - width - 14;

					scroller.setLeftPosition(leftPosition);

					selectedStartDate = calculateStartPoint();
					selectedEndDate = calculateEndPoint();

					setDateRangeTimer.cancel();
					setDateRangeTimer.schedule(100);
				}
			}

			DOM.setStyleAttribute(leftFade, "width",
					(scroller.getLeftPosition() + 14) + "px");
			DOM.setStyleAttribute(rightFade, "left",
					(scroller.getRightPosition() + 14 + 2) + "px");
			DOM.setStyleAttribute(rightFade, "width", (getOffsetWidth()
					- scroller.getRightPosition() - 28)
					+ "px");
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
		Element mouseOver = (Element) Element.as(mouseEvent.getEventTarget());

		mouseIsActive = true;

		event.preventDefault();
		event.stopPropagation();

		DOM.setCapture(horizontalScalePanel.getElement());

		if (mouseOver == scrollLeft) {
			sizeAdjustLeft = false;
			sizeAdjustRight = false;

			if (scroller.getLeftPosition() > 0) {
				scroller.move(-1);
				timelineWidget.fireDateRangeChangedEvent();
			}

		} else if (mouseOver == scrollRight) {
			sizeAdjustLeft = false;
			sizeAdjustRight = false;

			if (scroller.getRightPosition() + 5 < getCanvasWidth()) {
				scroller.move(1);
				timelineWidget.fireDateRangeChangedEvent();
			}

		} else if (scroller.isMouseOverScrollElement((Event) mouseEvent)
				|| scroller.isMouseOverScrollArea((Event) mouseEvent)) {
			sizeAdjustLeft = false;
			sizeAdjustRight = false;
			dragStartX = scroller.getMouseOffset((Event) mouseEvent);
		} else if (scroller.isMouseOverLeftSideSizeAdjuster((Event) mouseEvent)) {
			sizeAdjustRight = false;
			sizeAdjustLeft = true;
			dragStartX = mouseEvent.getClientX();
		} else if (scroller
				.isMouseOverRightSideSizeAdjuster((Event) mouseEvent)) {
			sizeAdjustLeft = false;
			sizeAdjustRight = true;
			dragStartX = mouseEvent.getClientX();
		} else if (mouseOver == horizontalScalePanel.getElement()
				|| mouseOver == leftFade || mouseOver == rightFade) {
			sizeAdjustLeft = false;
			sizeAdjustRight = false;

			// Calculate the centering point
			int center = mouseEvent.getClientX() - getAbsoluteLeft();
			int area = scroller.getAreaWidth();

			if (center - area / 2 < 14) {
				center = area / 2;
			}

			if (center + area / 2 > getOffsetWidth() - 28) {
				center = getOffsetWidth() - 28 - area / 2;
			}

			// Center scoller and set fades
			scroller.center(center);
			DOM.setStyleAttribute(leftFade, "width",
					(scroller.getLeftPosition() + 14) + "px");
			DOM.setStyleAttribute(rightFade, "left",
					(scroller.getRightPosition() + 14 + 2) + "px");
			DOM.setStyleAttribute(rightFade, "width", (getOffsetWidth()
					- scroller.getRightPosition() - 28)
					+ "px");

			dragStartX = scroller.getMouseOffset((Event) mouseEvent);

			setDateRangeTimer.cancel();
			setDateRangeTimer.schedule(100);
		}
	}

	private int scrollAdjustLeft = 0;
	private int scrollAdjustRight = 0;
	private Timer scrollTimer = new Timer() {
		@Override
		public void run() {

			// Check that we are not scrolling over the left side
			if (scroller.getLeftPosition() + scrollAdjustLeft >= 0) {
				scroller.adjustLeftSideSize(scrollAdjustLeft);
			} else {
				scroller.setLeftPosition(0);
			}

			// Check that we are not scrolling over the right side

			if (scroller.getRightPosition() + scrollAdjustRight <= getCanvasWidth() + 2) {
				scroller.adjustRightSideSize(scrollAdjustRight);
			}

			// Update
			refreshSelection();
			scrollAdjustLeft = 0;
			scrollAdjustRight = 0;
		}
	};

	public void onMouseWheel(MouseWheelEvent event) {
		NativeEvent mouseEvent = event.getNativeEvent();
		event.preventDefault();

		boolean up = mouseEvent.getMouseWheelVelocityY() > 0;

		// Only apply event to scroller
		if (!scroller.isMouseOverScrollArea((Event) mouseEvent)) {
			return;
		}

		sizeAdjustLeft = true;
		sizeAdjustRight = true;

		if (up) {
			scrollAdjustLeft += -5;
			scrollAdjustRight += 10;
		} else {
			scrollAdjustLeft += 5;
			scrollAdjustRight += -10;
		}

		scrollTimer.cancel();
		scrollTimer.schedule(50);
	}

	/**
	 * Refreshes selection, by recalculating position
	 */
	public void refreshSelection() {
		if (isVisible()) {
			selectedStartDate = calculateStartPoint();
			selectedEndDate = calculateEndPoint();
		} else {
			selectedStartDate = timelineWidget.getStartDate();
			selectedEndDate = timelineWidget.getEndDate();
		}

		setDateRangeTimer.cancel();
		setDateRangeTimer.schedule(100);

		if (!mouseDown) {
			scroller.lockSize();

			DOM.setStyleAttribute(leftFade, "width",
					(scroller.getLeftPosition() + 14) + "px");
			DOM.setStyleAttribute(rightFade, "left",
					(scroller.getRightPosition() + 14 + 2) + "px");
			DOM.setStyleAttribute(rightFade, "width", (getOffsetWidth()
					- scroller.getRightPosition() - 28)
					+ "px");

			sizeAdjustLeft = false;
			sizeAdjustRight = false;

			timelineWidget.fireDateRangeChangedEvent();
		}
	}

	public void onPreviewNativeEvent(NativePreviewEvent event) {
		// Monitor mouse button state
		if (event.getTypeInt() == Event.ONMOUSEUP
				&& event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
			mouseDown = false;
			if (mouseIsActive) {
				onMouseUp(null);
			}
		} else if (event.getTypeInt() == Event.ONMOUSEDOWN
				&& event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
			mouseDown = true;
		}
	}

	public void onDoubleClick(DoubleClickEvent event) {
		NativeEvent mouseEvent = event.getNativeEvent();
		if (scroller.isMouseOverScrollElement((Event) mouseEvent)
				|| scroller.isMouseOverScrollArea((Event) mouseEvent)) {
			if (mouseEvent.getButton() == NativeEvent.BUTTON_LEFT) {
				scroller.setLeftPosition(0);
				scroller.setRightPosition(getOffsetWidth() - 28 - 2);
				refreshSelection();
			}
		}
	}
}
