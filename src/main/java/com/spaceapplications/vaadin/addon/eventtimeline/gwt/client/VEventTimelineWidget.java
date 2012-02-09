/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.DateTimeService;
import com.vaadin.terminal.gwt.client.LocaleNotLoadedException;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * VEventTimelineWidget, based on original version from vaadin-timeline
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 * @author Florian Pirchner (add / remove bands)
 * @author Peter Lehto / IT Mill Oy Ltd
 * @author John Ahlroos / IT Mill Oy Ltd
 */
public class VEventTimelineWidget extends Composite implements Paintable {

	// Style names
	public static final String TAGNAME = "eventtimeline-widget";
	public static final String CLASSNAME = "v-" + TAGNAME;

	public static final String DISPLAY_CLASSNAME = CLASSNAME + "-display";
	public static final String BROWSER_CLASSNAME = CLASSNAME + "-browser";
	public static final String CAPTION_CLASSNAME = CLASSNAME + "-caption";

	private static final String CLASSNAME_TOPBAR = CLASSNAME + "-topbar";
	private static final String CLASSNAME_ZOOMBAR = CLASSNAME + "-zoombar";
	private static final String CLASSNAME_ZOOMBARLABEL = CLASSNAME + "-label";
	private static final String CLASSNAME_DATEFIELD = CLASSNAME + "-datefield";
	private static final String CLASSNAME_DATEFIELDEDIT = CLASSNAME_DATEFIELD	+ "-edit";
	private static final String CLASSNAME_DATERANGE = CLASSNAME + "-daterange";
	private static final String CLASSNAME_LEGEND = CLASSNAME + "-legend";
	private static final String CLASSNAME_MODELEGEND_ROW = CLASSNAME + "-modelegend";
	private static final String CLASSNAME_BANDPAGE = CLASSNAME + "-pagenavigation";
	private static final String CLASSNAME_BANDPAGE_LABEL = CLASSNAME_BANDPAGE + "-label";
	private static final String CLASSNAME_BANDPAGE_NEXT = CLASSNAME_BANDPAGE + "-next";
	private static final String CLASSNAME_BANDPAGE_PREVIOUS = CLASSNAME_BANDPAGE + "-previous";
	private static final String CLASSNAME_BANDPAGE_PAGENUMBER = CLASSNAME_BANDPAGE + "-pagenumber";

	public static final String ATTR_DATE = "date";
	public static final String ATTR_STYLE = "css";
	public static final String ATTR_DESCRIPTION = "desc";
	public static final String ATTR_TIMETO = "tto";
	public static final String ATTR_TIMEFROM = "tfrom";
	public static final String ATTR_DATETO = "dto";
	public static final String ATTR_DATEFROM = "dfrom";
	public static final String ATTR_CAPTION = "caption";
	public static final String ATTR_ID = "id";

	public static final String ATTR_EVENT = "event";
	public static final String ATTR_BANDID = "bandid";
	public static final String ATTR_BAND = "band";
	public static final String ATTR_BAND_PAGE_SIZE = "bandPageSize";
	public static final String ATTR_END = "end";
	public static final String ATTR_START = "start";
	public static final String ATTR_EVENTS = "events";
	public static final String ATTR_BAND_CAPTION = "bcaption";
	public static final String ATTR_OPERATION = "operation";
	public static final String ATTR_BANDS = "bands";
	public static final String OPERATION_REMOVE = "remove";
	public static final String OPERATION_ADD = "add";

	private final ClickHandler zoomClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			zoomLevelClicked(event);
		}
	};

	private final ClickHandler pageNavigationClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			pageNavigationClicked(event);
		}
	};

	// Panels
	private final HorizontalPanel topBar;
	private final HorizontalPanel zoomBar;
	private final HorizontalPanel dateRangeBar;
	private final HorizontalPanel modeLegendBar;

	// Legend
	private final HorizontalPanel legend;
	private final List<Label> legendValues = new ArrayList<Label>();

	// Bands
	private final VEventTimelineBandArea bandArea;

	// Date selection
	private final TextBox dateFrom;
	private final TextBox dateTo;
	private Date intervalStartDate, intervalEndDate;

	// if true, then the current date ranges has to be set to the display and browser
	private boolean uiRequiresRangeRefresh;

	// Default UIDL stuff
	private ApplicationConnection client;
	private String uidlId;

	// Initialization
	private boolean initStage1Done = false;
	private boolean initDone = false;
	private boolean noDataAvailable = false;
	private Date selectedStartDate, selectedEndDate;

	// Components
	private VEventTimelineDisplay display;
	private VEventTimelineBrowser browser;

	// Base
	private final VerticalPanel root = new VerticalPanel();
	private final Label caption;
	private boolean isIdle = true;
	private String initGridColor = "rgb(200,200,200)";
	private boolean selectionLock = true;
	private String currentLocale;
	private DateTimeService dts;

	// Event specific properties
	private Date startDate = null;
	private Date endDate = null;

	// Band height property
	private int bandHeight = -1;
	
	// Zoom levels
	private final Map<Anchor, Long> zoomLevels = new HashMap<Anchor, Long>();

	// Component visibilities
	private boolean browserIsVisible = true;
	private boolean zoomIsVisible = true;
	private boolean dateSelectVisible = true;
	private boolean dateSelectEnabled = true;
	private boolean legendVisible = false;
	private boolean bandPagingVisible = false;

	// Date formats
	private DateTimeFormat displayFormat = DateTimeFormat.getFormat("MMM d, y");
	private DateTimeFormat editFormat = DateTimeFormat.getFormat("dd-MM-yyyy");

	private final DateTimeFormat dateformat_date = DateTimeFormat.getFormat("yyyy-MM-dd");

	// Data Cache
	private VClientCache cache = new VClientCache(this);
	private boolean runningDataRequest = false;

	// Band Navigation
	private HorizontalPanel pageNavigationBar;
	private Anchor previousPage;
	private Label pageNumberText;
	private Anchor nextPage;

	public VEventTimelineWidget() {

		dts = new DateTimeService();

		root.setStyleName(CLASSNAME);
		initWidget(root);

		caption = new Label("");
		caption.setStyleName(CAPTION_CLASSNAME);
		caption.setVisible(false);
		root.add(caption);

		endDate = new Date();
		startDate = new Date(endDate.getTime() - VEventTimelineDisplay.MONTH);

		topBar = new HorizontalPanel();
		topBar.setStyleName(CLASSNAME_TOPBAR);
		topBar.setVisible(zoomIsVisible || dateSelectVisible
				|| bandPagingVisible);
		root.add(topBar);

		zoomBar = new HorizontalPanel();
		zoomBar.setStyleName(CLASSNAME_ZOOMBAR);
		zoomBar.setVisible(zoomIsVisible);

		Label zoomLbl = new Label("Zoom:");
		zoomLbl.addStyleName(CLASSNAME_ZOOMBARLABEL);
		zoomBar.add(zoomLbl);
		topBar.add(zoomBar);

		dateRangeBar = new HorizontalPanel();
		dateRangeBar.setStyleName(CLASSNAME_DATERANGE);
		dateRangeBar.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		dateRangeBar.setVisible(dateSelectVisible);

		dateFrom = new TextBox();
		dateFrom.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				try {
					Date newDate = dts.parseDate(event.getValue(),
							editFormat.getPattern(), true);
					if ((newDate.equals(startDate) || newDate.after(startDate))
							&& (newDate.equals(endDate) || newDate
									.before(endDate))) {
						intervalStartDate = newDate;
						setBrowserRange(intervalStartDate, intervalEndDate);
						setDisplayRange(intervalStartDate, intervalEndDate);
						dateFrom.setFocus(false);
					} else {
						dateFrom.setText(dts.formatDate(intervalStartDate,
								editFormat.getPattern()));
					}
				} catch (IllegalArgumentException iae) {
					dateFrom.setText(dts.formatDate(intervalStartDate,
							editFormat.getPattern()));
				}
			}
		});
		dateFrom.addFocusHandler(new FocusHandler() {
			public void onFocus(FocusEvent event) {
				dateFrom.setStyleName(CLASSNAME_DATEFIELDEDIT);
				dateFrom.setText(dts.formatDate(intervalStartDate,
						editFormat.getPattern()));
				dateFrom.selectAll();
			}
		});
		dateFrom.addBlurHandler(new BlurHandler() {
			public void onBlur(BlurEvent event) {
				dateFrom.setStyleName(CLASSNAME_DATEFIELD);
				dateFrom.setText(dts.formatDate(intervalStartDate,
						displayFormat.getPattern()));
			}
		});
		dateFrom.setReadOnly(!dateSelectEnabled);
		dateFrom.setStyleName(CLASSNAME_DATEFIELD);
		dateRangeBar.add(dateFrom);

		Label dash = new Label();
		dash.setText("-");
		dateRangeBar.add(dash);

		dateTo = new TextBox();
		dateTo.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				try {
					Date newDate = dts.parseDate(event.getValue(),
							editFormat.getPattern(), true);
					if ((newDate.equals(startDate) || newDate.after(startDate))
							&& (newDate.equals(endDate) || newDate
									.before(endDate))) {
						intervalEndDate = newDate;
						setBrowserRange(intervalStartDate, intervalEndDate);
						setDisplayRange(intervalStartDate, intervalEndDate);
						dateTo.setFocus(false);
					} else {
						dateTo.setText(dts.formatDate(intervalEndDate,
								editFormat.getPattern()));
					}
				} catch (IllegalArgumentException iae) {
					dateTo.setText(dts.formatDate(intervalEndDate,
							editFormat.getPattern()));
				}
			}
		});
		dateTo.addFocusHandler(new FocusHandler() {
			public void onFocus(FocusEvent event) {
				dateTo.setStyleName(CLASSNAME_DATEFIELDEDIT);
				dateTo.setText(dts.formatDate(intervalEndDate,
						editFormat.getPattern()));
				dateTo.selectAll();
			}
		});
		dateTo.addBlurHandler(new BlurHandler() {
			public void onBlur(BlurEvent event) {
				dateTo.setStyleName(CLASSNAME_DATEFIELD);
				dateTo.setText(dts.formatDate(intervalEndDate,
						displayFormat.getPattern()));
			}
		});
		dateTo.setReadOnly(!dateSelectEnabled);
		dateTo.setStyleName(CLASSNAME_DATEFIELD);
		dateRangeBar.add(dateTo);

		topBar.add(dateRangeBar);
		topBar.setCellHorizontalAlignment(dateRangeBar,
				HorizontalPanel.ALIGN_RIGHT);

		//
		// band navigation area
		//
		pageNavigationBar = new HorizontalPanel();
		pageNavigationBar.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		pageNavigationBar.setStyleName(CLASSNAME_BANDPAGE);
		pageNavigationBar.setVisible(bandPagingVisible);
		pageNavigationBar.setHeight("31px");
		topBar.add(pageNavigationBar);
		topBar.setCellHorizontalAlignment(pageNavigationBar,
				HorizontalPanel.ALIGN_RIGHT);

		Label navigationLbl = new Label("Navigation:");
		navigationLbl.addStyleName(CLASSNAME_BANDPAGE_LABEL);
		pageNavigationBar.add(navigationLbl);

		previousPage = new Anchor("Previous page");
		pageNavigationBar.add(previousPage);
		previousPage.addStyleName(CLASSNAME_BANDPAGE_PREVIOUS);
		previousPage.addClickHandler(pageNavigationClickHandler);

		pageNumberText = new Label();
		pageNavigationBar.add(pageNumberText);
		pageNumberText.addStyleName(CLASSNAME_BANDPAGE_PAGENUMBER);
		pageNumberText.setText("1");

		nextPage = new Anchor("Next page");
		pageNavigationBar.add(nextPage);
		nextPage.addStyleName(CLASSNAME_BANDPAGE_NEXT);
		nextPage.addClickHandler(pageNavigationClickHandler);

		legend = new HorizontalPanel();
		legend.setVisible(legendVisible);
		legend.setHeight("30px");
		legend.setStyleName(CLASSNAME_LEGEND);

		modeLegendBar = new HorizontalPanel();
		modeLegendBar.setVisible(legendVisible);
		modeLegendBar.setWidth("100%");
		modeLegendBar.setHeight("31px");
		modeLegendBar.setStyleName(CLASSNAME_MODELEGEND_ROW);
		modeLegendBar.add(legend);
		modeLegendBar.setCellHorizontalAlignment(legend, HorizontalPanel.ALIGN_RIGHT);

		root.add(modeLegendBar);

		//
		// the band area
		//

		HorizontalPanel layout = new HorizontalPanel();
		bandArea = new VEventTimelineBandArea(this);
		bandArea.setWidth("100px");
		layout.add(bandArea);

		VerticalPanel main = new VerticalPanel();

		// Add the display
		display = new VEventTimelineDisplay(this);
		main.add(display);

		// Add the browser
		browser = new VEventTimelineBrowser(this);
		browser.setVisible(browserIsVisible);
		main.add(browser);

		layout.add(main);
		root.add(layout);
	}

	public DateTimeService getDateTimeService() {
		return dts;
	}

	private String currentHeight = null;

	@Override
	public void setHeight(String height) {
		super.setHeight(height);

		if (!height.equals(currentHeight)) {
			// Reduce the amount of redraws caused by resizing
			currentHeight = height;

			recalculateHeights();

			if (initDone) {
				display.redraw();
				browser.redraw();
			}
		}
	}

	private void recalculateHeights() {
		int captionHeight = getCaptionHeight();
		int topBarHeight = topBar.isVisible() ? 28 : 0;
		int modeLegendBarHeight = modeLegendBar.isVisible() ? 31 : 0;
		int browserHeight = browser.isVisible() ? 64 : 0;
		int displayHeight = getWidgetHeight() - captionHeight - topBarHeight
				- modeLegendBarHeight - browserHeight;
		display.setHeight(displayHeight + "px");
	}

	private String currentWidth = null;

	@Override
	public void setWidth(String width) {
		super.setWidth(width);

		if (!width.equals(currentWidth) && width.contains("px")) {
			currentWidth = width;

			recalculateWidths();
			if (initDone) {
				display.redraw();
				browser.redraw();
			}
		}
	}

	private void recalculateWidths() {
		int width = getWidgetWidth();
		topBar.setWidth(width + "px");
		caption.setWidth((width - 5) + "px");
		modeLegendBar.setWidth(width + "px");
		display.setWidth(width - bandArea.getOffsetWidth() + "px");
		browser.setWidth(width - bandArea.getOffsetWidth() + "px");
	}

	/**
	 * Adds a zoom level to the zoom bar
	 * 
	 * @param caption
	 *            The caption of the zoom level
	 * @param time
	 *            The time in milliseconds of the zoom level
	 */
	private void addZoomLevel(String caption, Long time) {
		Anchor level = new Anchor(caption);
		level.addClickHandler(zoomClickHandler);
		zoomLevels.put(level, time);
		zoomBar.add(level);
	}

	/**
	 * A page navigation button was clicked
	 * 
	 * @param evt
	 *            The click event
	 */
	private void pageNavigationClicked(ClickEvent evt) {

		boolean redraw = false;
		int bandPage = bandArea.getVisiblePage();
		if (evt.getSource() == nextPage) {
			if (bandPage < bandArea.getPageCount() - 1) {
				bandPage++;
				bandArea.setVisiblePage(bandPage);
				redraw = true;
			}
		} else {
			if (bandPage > 0) {
				bandPage--;
				bandArea.setVisiblePage(bandPage);
				redraw = true;
			}
		}

		if (redraw) {
			refreshPageNumber();
			display.redraw();
			browser.redraw();
			fireBandNavigationClickEvent();
		}
	}

	/**
	 * Initializes the widget
	 */
	private boolean init1(UIDL uidl) {
		// Set initial data
		setStartDate(uidl);
		setEndDate(uidl);
		setCaption(uidl);
		setZoomCaption(uidl);
		setPageNavigationCaptions(uidl);
		setBandSelectionEnabled(uidl);
		setSelectionLock(uidl);
		setDateFormatInfo(uidl);
		setLocale(uidl);
		setBandHeight(uidl);

		return true;
	}

	private void setStartDate(UIDL uidl) {
		if (uidl.hasAttribute("startDate")) {
			startDate = new Date(uidl.getLongAttribute("startDate"));
			uiRequiresRangeRefresh = true;
		}
	}

	private void setEndDate(UIDL uidl) {
		if (uidl.hasAttribute("endDate")) {
			endDate = new Date(uidl.getLongAttribute("endDate"));
			uiRequiresRangeRefresh = true;
		}
	}

	private void setCaption(UIDL uidl) {
		if (uidl.hasAttribute("caption")) {
			String captionText = uidl.getStringAttribute("caption");
			if (!captionText.equals(caption.getText())) {
				boolean captionWasVisible = caption.isVisible();
				if (captionText.equals("")) {
					caption.setText("");
					caption.setVisible(false);
				} else {
					caption.setText(captionText);
					caption.setVisible(true);
				}
				if (captionWasVisible != caption.isVisible()) {
					recalculateHeights();
					if (initDone) {
						display.redraw();
						browser.redraw();
					}
				}
			}
		}
	}

	private void setSelectionLock(UIDL uidl) {
		if (uidl.hasAttribute("lock")) {
			selectionLock = uidl.getBooleanAttribute("lock");
		} else {
			selectionLock = false;
		}
	}

	private void setZoomLevels(UIDL uidl) {
		if (uidl.hasAttribute("zoomLevels")) {
			String[] levels = uidl.getStringArrayAttribute("zoomLevels");

			if (zoomBar != null) {
				for (Anchor lvl : zoomLevels.keySet()) {
					zoomBar.remove(lvl);
				}
			}
			zoomLevels.clear();

			for (String level : levels) {
				String[] levelArray = level.split(",");
				if (levelArray.length == 2) {
					String caption = levelArray[0];
					Long time = Long.parseLong(levelArray[1]);
					addZoomLevel(caption, time);
				}
			}
		}
	}

	private void setZoomVisibility(UIDL uidl) {
		if (uidl.hasAttribute("zoomVisibility")) {
			zoomIsVisible = uidl.getBooleanAttribute("zoomVisibility");

			if (zoomBar != null) {
				zoomBar.setVisible(zoomIsVisible);
				setTopBarVisibility(zoomIsVisible || dateSelectVisible
						|| bandPagingVisible);
			}
		}
	}

	private void setZoomCaption(UIDL uidl) {
		if (uidl.hasAttribute("zlvlcaption")) {
			String caption = uidl.getStringAttribute("zlvlcaption");
			Label lbl = (Label) zoomBar.getWidget(0);
			lbl.setText(caption);
		}
	}

	private void setBandHeight(UIDL uidl) {
	  if (uidl.hasAttribute("bandheight")) {
	    bandHeight = uidl.getIntAttribute("bandheight");
	  }
	}

	protected int getBandHeight() {
	  return bandHeight;
	}
	
	private void setPageNavigationVisibility(UIDL uidl) {
		if (uidl.hasAttribute("bandPagingVisible")) {
			bandPagingVisible = uidl.getBooleanAttribute("bandPagingVisible");

			if (pageNavigationBar != null) {
				pageNavigationBar.setVisible(bandPagingVisible);
				setTopBarVisibility(zoomIsVisible || dateSelectVisible
						|| bandPagingVisible);
			}
		}
	}

	private void setPageNavigationCaptions(UIDL uidl) {
		if (uidl.hasAttribute("bpgingCaption")) {
			String caption = uidl.getStringAttribute("bpgingCaption");
			Label lbl = (Label) pageNavigationBar.getWidget(0);
			lbl.setText(caption);
		}

		if (uidl.hasAttribute("bpgingCptPrevious")) {
			String caption = uidl.getStringAttribute("bpgingCptPrevious");
			previousPage.setText(caption);
		}

		if (uidl.hasAttribute("bpgingCptNext")) {
			String caption = uidl.getStringAttribute("bpgingCptNext");
			nextPage.setText(caption);
		}
	}

	private void setBandSelectionEnabled(UIDL uidl) {
		if (uidl.hasAttribute("bandSelectionEnabled")) {
			boolean bandSelectionEnabled = uidl
					.getBooleanAttribute("bandSelectionEnabled");
			boolean oldValue = bandArea.isBandSelectionEnabled();
			if (oldValue != bandSelectionEnabled) {
				bandArea.setBandSelectionEnabled(bandSelectionEnabled);
			}
		}
	}

	private void setModeLegendBarVisibility(boolean visibility) {
		boolean isVisible = modeLegendBar.isVisible();
		if (isVisible != visibility) {
			modeLegendBar.setVisible(visibility);
			recalculateHeights();
			if (initDone) {
				display.redraw();
				browser.redraw();
			}
		}
	}

	private void setTopBarVisibility(boolean visibility) {
		boolean isVisible = topBar.isVisible();
		if (isVisible != visibility) {
			topBar.setVisible(visibility);
			recalculateHeights();
			if (initDone) {
				display.redraw();
				browser.redraw();
			}
		}
	}

	private Timer refreshTimer = new Timer() {
		@Override
		public void run() {
			// Render the browser canvas (refreshes the selection after loading the data)
			browser.refresh();

			if (selectionLock) {
				/*
				 * Update the selection manually. If the selection lock is not
				 * on the browser will update the display once it has finished
				 * loading and recalculated the new selection
				 */
				display.setRange(browser.getSelectedStartDate(), browser.getSelectedEndDate());
			}
		}
	};

	public boolean isSelectionLocked() {
		return selectionLock;
	}

	private void setDirty(UIDL uidl) {
		if (uidl.getBooleanAttribute("dirty") && initDone) {
			refreshTimer.cancel();
			refreshTimer.schedule(500);
		}
	}

	private void setSelectionRange(UIDL uidl) {
		if (uidl.hasVariable("selectStart") && uidl.hasVariable("selectEnd")) {
			selectedStartDate = new Date(uidl.getLongVariable("selectStart"));
			selectedEndDate = new Date(uidl.getLongVariable("selectEnd"));
			uiRequiresRangeRefresh = true;
		}
	}

	private void setGridColor(UIDL uidl) {
		if (uidl.hasAttribute("gridColor")) {
			String color = uidl.getStringAttribute("gridColor");
			if (color == null || color.equals("")) {
				initGridColor = null;
			} else {
				initGridColor = color;
			}

			if (initStage1Done) {
				display.setGridColor(initGridColor);
			}
		}
	}

	private void setDateSelectEnabled(UIDL uidl) {
		if (uidl.hasAttribute("dateSelectEnabled")) {
			dateSelectEnabled = uidl.getBooleanAttribute("dateSelectEnabled");
			dateFrom.setReadOnly(!dateSelectEnabled);
			dateTo.setReadOnly(!dateSelectEnabled);
		}
	}

	private void setDateSelectVisibility(UIDL uidl) {
		if (uidl.hasAttribute("dateSelectVisibility")) {
			dateSelectVisible = uidl
					.getBooleanAttribute("dateSelectVisibility");

			if (dateRangeBar != null) {
				dateRangeBar.setVisible(dateSelectVisible);
				setTopBarVisibility(zoomIsVisible || dateSelectVisible
						|| bandPagingVisible);
			}
		}
	}

	private void setLegendVisibility(UIDL uidl) {
		if (uidl.hasAttribute("legendVisibility")) {
			legendVisible = uidl.getBooleanAttribute("legendVisibility");
			if (legend != null) {
				legend.setVisible(legendVisible);
				setModeLegendBarVisibility(legendVisible);
			}
		}
	}

	private void setBrowserVisibility(UIDL uidl) {
		if (uidl.hasAttribute("browserVisibility")) {
			boolean browserWasVisible = browserIsVisible;
			browserIsVisible = uidl.getBooleanAttribute("browserVisibility");
			if (browser != null && browserWasVisible != browserIsVisible) {
				browser.setVisible(browserIsVisible);
				recalculateHeights();
				if (initDone) {
					display.redraw();
					browser.redraw();
				}
			}
		}
	}

	private void setLocale(UIDL uidl) {
		if (uidl.hasAttribute("locale")) {
			final String locale = uidl.getStringAttribute("locale");
			try {
				dts.setLocale(locale);
				currentLocale = locale;
			} catch (final LocaleNotLoadedException e) {
				currentLocale = dts.getLocale();
				VConsole.error("Tried to use an unloaded locale \"" + locale
						+ "\". Using default locale (" + currentLocale + ").");
				VConsole.error(e);
			}
		}
	}

	/**
	 * Sets the bands that should be shown. The uidl contains information
	 * whether bands are added or removed.
	 * 
	 * @param uidl
	 */
	private void setBands(UIDL uidl) {

		if (uidl.hasAttribute(ATTR_BAND_PAGE_SIZE)) {
			int pageSize = uidl.getIntAttribute(ATTR_BAND_PAGE_SIZE);
			bandArea.setPageSize(pageSize);
		}

		boolean hasContent = bandArea.getBandCount() != 0;
		List<Integer> removedBands = new ArrayList<Integer>();
		UIDL bands = uidl.getChildByTagName(ATTR_BANDS);
		if (bands != null) {
			Iterator<Object> it = bands.getChildIterator();
			while (it.hasNext()) {
				UIDL child = (UIDL) it.next();
				if (child != null && ATTR_BAND.equals(child.getTag())) {
					Integer id = child.getIntAttribute(ATTR_BANDID);
					String operation = child.getStringAttribute("operation");
					if (operation.equals(OPERATION_ADD)) {
						String caption = child
								.getStringAttribute(ATTR_BAND_CAPTION);
						bandArea.addBand(id, caption);
						if (hasContent) {
							bandArea.navigateToBand(id);
						}
					} else if (operation.equals(OPERATION_REMOVE)) {
						bandArea.removeBand(id);
						removedBands.add(id);
					}
				}
			}
		}

		if (removedBands.size() > 0) {
			Integer[] bandIds = removedBands.toArray(new Integer[removedBands.size()]);
			display.dataRemoved(bandIds);
			browser.dataRemoved(bandIds);
		}

		if (!hasContent) {
			bandArea.navigateToPage(0);
		}

		refreshPageNumber();

		if (initDone) {
			display.redraw();
			browser.redraw();
		}
	}

	/**
	 * Refreshes the page number.
	 */
	private void refreshPageNumber() {
		pageNumberText.setText(Integer.toString(bandArea.getVisiblePage() + 1));
	}

	protected int getBandHeight(int band) {
		return bandArea.getBandHeight(band);
	}

	private void handleOnePointGraph() {
		if (startDate.equals(endDate)) {
			long halfDay = VEventTimelineDisplay.DAY / 2L;
			startDate = new Date(startDate.getTime() - halfDay);
			endDate = new Date(endDate.getTime() + halfDay);
			selectedStartDate = startDate;
			selectedEndDate = endDate;
			browser.setRange(selectedStartDate, selectedEndDate);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.terminal.gwt.client.Paintable#updateFromUIDL(com.vaadin.terminal
	 * .gwt.client.UIDL, com.vaadin.terminal.gwt.client.ApplicationConnection)
	 */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		// Check parent components
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		// Store reference variables for later usage
		this.client = client;
		uidlId = uidl.getId();

		// Initialization stage 1
		if (!initStage1Done) {
			initStage1Done = init1(uidl);

			// Send init flag back to server along with the canvas width
			client.updateVariable(uidlId, "init", true, false);
			client.updateVariable(uidlId, "canvasWidth", getOffsetWidth(), true);
			return;

			// Initialization done
		}

    setBandHeight(uidl);
		setCaption(uidl);
		setBands(uidl);
		setLocale(uidl);
		setNoData(uidl);
		setSelectionLock(uidl);

		setStartDate(uidl);
		setEndDate(uidl);
		handleOnePointGraph();

		setSelectionRange(uidl);
		setBrowserVisibility(uidl);
		setZoomVisibility(uidl);
		setDateSelectVisibility(uidl);
		setDateSelectEnabled(uidl);
		setLegendVisibility(uidl);
    setPageNavigationVisibility(uidl);
		
		setZoomLevels(uidl);
		setGridColor(uidl);
		setZoomCaption(uidl);
		
		setPageNavigationCaptions(uidl);
		setBandSelectionEnabled(uidl);
		setDateFormatInfo(uidl);

		// Data received
		List<VEvent> events = null;
		UIDL bands = uidl.getChildByTagName("events");
		if (bands != null) {
			Long startTime = bands.getLongAttribute("start");
			Long endTime = bands.getLongAttribute("end");

			Iterator<Object> it = bands.getChildIterator();
			while (it.hasNext()) {
				UIDL child = (UIDL) it.next();
				if (child != null && "band".equals(child.getTag())) {
					Integer id = child.getIntAttribute("bandid");

					// TODO Thomas
					cache.removeFromCache(id);
					
					events = getEvents(child);
					cache.addToCache(id, new Date(startTime), new Date(endTime), events);

					display.dataReceived(id, events);
					browser.dataReceived(id, events);
				}
			}

			runningDataRequest = false;
			display.redraw();
		}

		if (!initDone) {
	      initDone = true;
		}
		
    if (isInitDone() && uiRequiresRangeRefresh) {
      uiRequiresRangeRefresh = false;
      browser.setRange(selectedStartDate, selectedEndDate);
      display.setRange(selectedStartDate, selectedEndDate);
    }
    
    if (isInitDone()) {
      if (browserIsVisible) {
        browser.refresh();
      }
    }
    
    setDirty(uidl);
	}

	private void setNoData(UIDL uidl) {
		// Disable component since no data source has been defined on the server
		if (uidl.hasAttribute("nodata")) {
			setWidgetNoData(uidl.getBooleanAttribute("nodata"));
		} else {
			setWidgetNoData(false);
		}
	}

	/** Transforms uidl to list of TimelineEvents */
	protected List<VEvent> getEvents(UIDL childUIDL) {
		int eventCount = childUIDL.getChildCount();
		List<VEvent> events = new ArrayList<VEvent>();
		for (int i = 0; i < eventCount; i++) {
			UIDL eventUIDL = childUIDL.getChildUIDL(i);

			String id = eventUIDL.getStringAttribute(ATTR_ID);
			String caption = eventUIDL.getStringAttribute(ATTR_CAPTION);
			String datefrom = eventUIDL.getStringAttribute(ATTR_DATEFROM);
			String dateto = eventUIDL.getStringAttribute(ATTR_DATETO);
			Long timefrom = eventUIDL.getLongAttribute(ATTR_TIMEFROM);
			Long timeto = eventUIDL.getLongAttribute(ATTR_TIMETO);
			String desc = eventUIDL.getStringAttribute(ATTR_DESCRIPTION);
			String style = eventUIDL.getStringAttribute(ATTR_STYLE);

			VEvent e = new VEvent();

			e.setID(id);
			e.setCaption(caption);
			e.setDescription(desc);
			e.setEnd(dateformat_date.parse(dateto));
			e.setStart(dateformat_date.parse(datefrom));
			e.setStartTime(timefrom);
			e.setEndTime(timeto);
			e.setStyleName(style);

			events.add(e);
		}
		return events;
	}

	protected List<VEvent> getEventsFromStringArray(final String[] array) {
		List<VEvent> events = new ArrayList<VEvent>();
		for (String str : array) {
			String[] vals = str.split(";");

			VEvent e = new VEvent();

			e.setID(vals[0]);
			e.setCaption(vals[1]);
			e.setStart(dateformat_date.parse(vals[2]));
			e.setEnd(dateformat_date.parse(vals[3]));
			e.setStartTime(Long.valueOf(vals[4]));
			e.setEndTime(Long.valueOf(vals[5]));
			e.setDescription(vals[6]);
			e.setStyleName(vals[7]);

			events.add(e);
		}
		return events;
	}

	/**
	 * Set the not data state
	 * 
	 * @param enabled
	 */
	private void setWidgetNoData(boolean enabled) {
		noDataAvailable = enabled;

		display.displayNoDataMessage(enabled);

		display.setEnabled(!enabled);
		browser.setEnabled(!enabled);

		dateFrom.setEnabled(!enabled);
		dateTo.setEnabled(!enabled);

		for (Anchor lvl : zoomLevels.keySet()) {
			lvl.setEnabled(!enabled);
		}
	}

	/**
	 * Get data from server.
	 * 
	 * @param component
	 *            The component which need the events
	 * @param startDate
	 *            The start date of the events
	 * @param endDate
	 *            The end date of the events
	 * @return Was the data fetched from the cache
	 */
	public boolean getEvents(VDataListener component, Date startDate,
	                         Date endDate, boolean useCache) {

		if (!component.isVisible()) {
			return true;
		}

		isIdle = false;

		GWT.log("trying to retrieve events: start=" + startDate.toString() + ", end=" + endDate.toString());

		if (useCache) {
			boolean gotFromCache = true;
			for (VEventTimelineBand band : bandArea.getAllBands()) {
				List<VEvent> events = cache.getFromCache(band.getId(), startDate, endDate);
				if (events == null) {
					gotFromCache = false;
					break;
				} else {
					component.dataReceived(band.getId(), events);
				}
			}

			component.dataReceivedAll();

			if (gotFromCache) {
				return true;
			}
		}

		if (!runningDataRequest) {
		  GWT.log("retrieve from server");

		  // if the data could not be found in the cache, get it from the server
		  client.updateVariable(uidlId, "events",
				                    new Object[] { Long.valueOf(startDate.getTime()),
		                                       Long.valueOf(endDate.getTime()) },
		                        false);

		  getFromServer();
		}

		return false;
	}

	/**
	 * Set the displayed date range
	 * 
	 * @param start
	 *            The start date
	 * @param end
	 *            The end date
	 */
	public void setDisplayRange(Date start, Date end) {
		display.setRange(start, end);
	}

	/**
	 * Set the selected date range
	 * 
	 * @param start
	 *            The start date
	 * @param end
	 *            The end date
	 */
	public void setBrowserRange(Date start, Date end) {
		browser.setRange(start, end);
	}

	/**
	 * Calculates the maximum value of a list of values
	 * 
	 * @param values
	 *            The list of values
	 * @return The maximum value
	 */
	public static float getMaxValue(List<Float> values) {
		if (values == null || values.size() == 0) {
			return 0;
		}
		float max = values.get(0);
		for (Float value : values) {
			max = max < value.floatValue() ? value.floatValue() : max;
		}
		return max;
	}

	/**
	 * Returns the start date
	 * 
	 * @return The start date
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * Returns the end date
	 * 
	 * @return The end date
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Issues a get request to fetch the requested data points
	 */
	public void getFromServer() {
	  runningDataRequest = true;
		client.updateVariable(uidlId, "send", true, true);
	}

	/**
	 * Set the starting date text field
	 * 
	 * @param date
	 *            The date
	 */
	public void setFromDateTextField(Date date) {
		intervalStartDate = date;
		if (date != null) {
			dateFrom.setText(dts.formatDate(date, displayFormat.getPattern()));
		} else {
			dateFrom.setText("");
		}
	}

	/**
	 * Set the ending date text field
	 * 
	 * @param date
	 *            The date
	 */
	public void setToDateTextField(Date date) {
		intervalEndDate = date;
		if (date != null) {
			dateTo.setText(dts.formatDate(date, displayFormat.getPattern()));
		} else {
			dateTo.setText("");
		}
	}

	/**
	 * The top bar height
	 * 
	 * @return Height in pixels
	 */
	public int getTopBarHeight() {
		if (topBar.isVisible()) {
			return topBar.getOffsetHeight();
		} else {
			return 0;
		}
	}

	/**
	 * The caption height
	 * 
	 * @return Height in pixels
	 */
	public int getCaptionHeight() {
		if (caption.isVisible()) {
			return caption.getOffsetHeight();
		} else {
			return 0;
		}
	}

	/**
	 * The legend height
	 * 
	 * @return height in pixels
	 */
	public int getLegendHeight() {
		if (modeLegendBar.isVisible()) {
			return modeLegendBar.getOffsetHeight();
		} else {
			return 0;
		}
	}

	/**
	 * A zoom level was clicked
	 * 
	 * @param evt
	 *            The click event
	 */
	private void zoomLevelClicked(ClickEvent evt) {
		evt.preventDefault();

		// If we have no data do nothing
		if (noDataAvailable) {
			return;
		}

		// Was a zoom level clicked
		Long time = zoomLevels.get(evt.getSource());
		Long totalTime = getEndDate().getTime() - getStartDate().getTime();

		if (totalTime >= time) {

			// Calculate the center
			Date center;
			if (browserIsVisible) {
				Long selectedTime = browser.getSelectedEndDate().getTime()
						- browser.getSelectedStartDate().getTime();
				center = new Date(browser.getSelectedStartDate().getTime()
						+ selectedTime / 2L);
			} else {
				center = new Date(display.getSelectionStartDate().getTime()
						+ time / 2L);
			}

			// Calculate start date
			Date start = new Date(center.getTime() - time / 2L);
			if (start.before(getStartDate())) {
				start = getStartDate();
			}

			// Calculate end date
			Date end = new Date(start.getTime() + time);
			if (end.after(getEndDate())) {
				end = getEndDate();
			}

			// Set the browser
			if (browserIsVisible) {
				setBrowserRange(start, end);
			}

			// Set the display
			setDisplayRange(start, end);

		} else {
			if (browserIsVisible) {
				setBrowserRange(getStartDate(), getEndDate());
			}

			setDisplayRange(getStartDate(), getEndDate());
		}
	}

	/**
	 * Fire a date range change event
	 */
	public void fireDateRangeChangedEvent() {
		if (display.getSelectionStartDate() != null
				&& display.getSelectionEndDate() != null) {
			Object[] values = new Object[] {
					display.getSelectionStartDate().getTime(),
					display.getSelectionEndDate().getTime(), };

			client.updateVariable(uidlId, "drce", values, true);
		}
	}

	/**
	 * Fire a event button click event
	 * 
	 * @param indexes
	 */
	public void fireEventButtonClickEvent(String id) {
		client.updateVariable(uidlId, "ebce", id, true);
	}

	/**
	 * Fires a event band navigation click event
	 */
	public void fireBandNavigationClickEvent() {
		client.updateVariable(uidlId, "bandPage", bandArea.getVisiblePage(), true);
	}

	/**
	 * Fires a band selected event.
	 * 
	 * @param bandId
	 */
	public void fireBandSelected(int bandId) {
		client.updateVariable(uidlId, "bandSel", bandId, true);
	}

	/**
	 * Returns the height of the widget in pixels
	 * 
	 * @return A pixel height
	 */
	@SuppressWarnings("deprecation")
	public int getWidgetHeight() {
		try {
			int height = Integer.parseInt(DOM.getAttribute(root.getElement(),
					"height").replaceAll("px", ""));
			return height;
		} catch (Exception e) {
			try {
				int height = Integer.parseInt(DOM.getStyleAttribute(
						root.getElement(), "height").replaceAll("px", ""));
				return height;
			} catch (Exception f) {
				return root.getOffsetHeight();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public int getWidgetWidth() {
		try {
			int width = Integer.parseInt(DOM.getAttribute(root.getElement(),
					"width").replaceAll("px", ""));
			return width;
		} catch (Exception e) {
			try {
				int width = Integer.parseInt(DOM.getStyleAttribute(
						root.getElement(), "width").replaceAll("px", ""));
				return width;
			} catch (Exception f) {
				return root.getOffsetWidth();
			}
		}
	}

	/**
	 * Get the browser height
	 * 
	 * @return The height in pixels
	 */
	public int getBrowserHeight() {
		if (browserIsVisible) {
			return browser.getOffsetHeight();
		} else {
			return 0;
		}
	}

	/**
	 * The idle state of the widget
	 * 
	 * @return If the widget is idle true is returned else false
	 */
	public boolean isIdle() {
		return isIdle;
	}

	/**
	 * Sets the legend value
	 * 
	 * @param graph
	 *            The graph the value is from
	 * @param value
	 *            The value
	 */
	public void setLegendValue(int graph, String value) {
		if (legendValues.size() > graph) {
			Label lbl = legendValues.get(graph);
			if (lbl != null) {
				lbl.setText(value);
			}
		}
	}

	/**
	 * Has the widget been properly initialized
	 * 
	 * @return
	 */
	public boolean isInitDone() {
		return initDone;
	}

	public Date getSelectedStartDate() {
		return selectedStartDate;
	}

	public Date getSelectedEndDate() {
		return selectedEndDate;
	}

	private void setDateFormatInfo(UIDL uidl) {
		if (uidl.hasAttribute("dateformats")) {
			String[] formats = uidl.getStringAttribute("dateformats").split(
					"\\|");
			DateTimeFormat dateSelectDisplaySimpleDateFormat = DateTimeFormat
					.getFormat(formats[0]);
			setSelectDisplayFormat(dateSelectDisplaySimpleDateFormat);

			DateTimeFormat dateSelectEditSimpleDateFormat = DateTimeFormat
					.getFormat(formats[1]);
			setSelectEditFormat(dateSelectEditSimpleDateFormat);

			DateTimeFormat shortYearFormat = DateTimeFormat
					.getFormat(formats[2]);
			display.setYearFormatShort(shortYearFormat);

			DateTimeFormat longYearFormat = DateTimeFormat
					.getFormat(formats[3]);
			display.setYearFormatLong(longYearFormat);

			DateTimeFormat shortMonthFormat = DateTimeFormat
					.getFormat(formats[4]);
			display.setMonthFormatShort(shortMonthFormat);

			DateTimeFormat longMonthFormat = DateTimeFormat
					.getFormat(formats[5]);
			display.setMonthFormatLong(longMonthFormat);

			DateTimeFormat shortDayFormat = DateTimeFormat
					.getFormat(formats[6]);
			display.setDayFormatShort(shortDayFormat);

			DateTimeFormat longDayFormat = DateTimeFormat.getFormat(formats[7]);
			display.setDayFormatLong(longDayFormat);

			DateTimeFormat shortTimeFormat = DateTimeFormat
					.getFormat(formats[8]);
			display.setTimeFormatShort(shortTimeFormat);

			DateTimeFormat longTimeFormat = DateTimeFormat
					.getFormat(formats[9]);
			display.setTimeFormatLong(longTimeFormat);

			// Update scales by redrawing
			if (initDone) {
				display.redraw();
			}
		}
	}

	private void setSelectDisplayFormat(DateTimeFormat format) {
		displayFormat = format;
		setToDateTextField(intervalEndDate);
		setFromDateTextField(intervalStartDate);
	}

	private void setSelectEditFormat(DateTimeFormat format) {
		editFormat = format;
	}

	public void redrawDisplay() {
		display.redraw();
	}
}
