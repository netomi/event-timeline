/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * VEventTimelineBandArea.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 *         <p/>
 *         Contributors:<br/>
 *         Florian Pirchner <florian.pirchner@gmail.com> Add / remove bands
 */
public class VEventTimelineBandArea extends VerticalPanel implements
		MouseOverHandler, MouseOutHandler {

	private final VEventTimelineWidget timelineWidget;

	private HandlerRegistration mouseOverReg, mouseOutReg;

	// Band captions
	private final List<Integer> bandMinimumHeights = new ArrayList<Integer>();
	private final List<VEventTimelineBand> allBands = new ArrayList<VEventTimelineBand>();
	private final List<VEventTimelineBand> visibleBands = new ArrayList<VEventTimelineBand>();

	// a pointer that indicates the first visible band in the page. Its value is
	// the index of allBands-list
	private short pagePointer;

	private int pageSize = -1;

	public VEventTimelineBandArea(VEventTimelineWidget tw) {
		timelineWidget = tw;
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		mouseOverReg = addDomHandler(this, MouseOverEvent.getType());
		mouseOutReg = addDomHandler(this, MouseOutEvent.getType());
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		if (mouseOverReg != null) {
			mouseOverReg.removeHandler();
			mouseOverReg = null;
		}
		if (mouseOutReg != null) {
			mouseOutReg.removeHandler();
			mouseOutReg = null;
		}
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		for (Widget w : getChildren()) {
			((VEventTimelineBand) w).disableAdjuster();
		}
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		for (Widget w : getChildren()) {
			((VEventTimelineBand) w).enableAdjuster();
		}
	}

	/**
	 * Is called to add an event band.
	 * 
	 * @param id
	 * @param caption
	 */
	public void addBand(int id, String caption) {
		VEventTimelineBand band = new VEventTimelineBand(id, caption, this);
		allBands.add(band);

		// TODO: currently the band heights are set to default 45 px -> make
		// configurable
		bandMinimumHeights.add(20);

		requestBandPage(pagePointer, pageSize);
	}

	/**
	 * Requests the build of the visible page.
	 * 
	 * @param pagePointer
	 *            The first band in the page. The value is the index of the band
	 *            in allBands
	 * @param pageSize
	 *            The number of event bands visible in the band area
	 */
	protected void requestBandPage(short pagePointer, int pageSize) {

		// remove all bands
		for (VEventTimelineBand band : visibleBands) {
			remove(band);
			band.setHeight("0px");
		}
		visibleBands.clear();

		if (pageSize > 0) {
			int height = calcHeight(pageSize);
			for (int i = 0; i < pageSize; i++) {
				int currentBandIndex = pagePointer + i;
				if (currentBandIndex > allBands.size() - 1) {
					// end of allBands reached -> leave
					break;
				}
				VEventTimelineBand band = allBands.get(currentBandIndex);
				band.setHeight(height + "px");
				band.setWidth(100 + "px");

				// add the band
				add(band);
				visibleBands.add(band);
			}
		} else {
			int height = calcHeight(allBands.size());
			for (VEventTimelineBand band : allBands) {
				band.setHeight(height + "px");
				band.setWidth(100 + "px");

				// add the band
				add(band);
				visibleBands.add(band);
			}
		}
	}

	/**
	 * Applies the bound to the event bands by calculating height and width.
	 * 
	 * @param pageSize
	 *            The number of visible event bands
	 */
	protected int calcHeight(int pageSize) {
		int bandHeight = 0;
		int calcBase = pageSize <= 0 ? allBands.size() : pageSize;
		if (calcBase > 0) {
			bandHeight = (getParent().getOffsetHeight()
					- timelineWidget.getBrowserHeight() - 16)
					/ calcBase;
		}

		return bandHeight;
	}

	/**
	 * Is called to remove the event band with the given id.
	 * 
	 * @param id
	 */
	public void removeBand(int id) {
		int index = -1;
		for (VEventTimelineBand band : allBands) {
			if (band.getId() == id) {
				index = allBands.indexOf(band);
				remove(band);
				allBands.remove(band);
				break;
			}
		}

		if (index >= 0) {
			bandMinimumHeights.remove(index);
		}
		int bandHeight = (getParent().getOffsetHeight()
				- timelineWidget.getBrowserHeight() - 16)
				/ allBands.size();
		for (VEventTimelineBand existingBand : allBands) {
			existingBand.setHeight(bandHeight + "px");
			existingBand.setWidth(100 + "px");
		}
	}

	/**
	 * Returns the number of bands.
	 * 
	 * @return
	 */
	public int getBandCount() {
		return allBands.size();
	}

	public int getBandHeight(int band) {
		return getWidget(band).getOffsetHeight();
	}

	public boolean requestResize(int bandId, int oldHeight, int adjustment) {
		int minimumHeight = bandMinimumHeights.get(bandId);
		int newHeight = oldHeight + adjustment;
		if (newHeight < minimumHeight) {
			return false;
		}

		// maximum height of the parent widget
		int maxHeight = getParent().getOffsetHeight()
				- timelineWidget.getBrowserHeight() - 16;

		int totalHeight = 0;
		for (int idx = 0; idx < getWidgetCount(); idx++) {
			VEventTimelineBand w = (VEventTimelineBand) getWidget(idx);
			if (idx != bandId) {
				totalHeight += w.getHeight();
			} else {
				totalHeight += newHeight;
			}
		}

		if (totalHeight > maxHeight) {
			newHeight -= totalHeight - maxHeight;
			if (newHeight < oldHeight) {
				return false;
			}
		}

		getWidget(bandId).setHeight(newHeight + "px");

		for (Widget w : getChildren()) {
			((VEventTimelineBand) w).updateBandAdjuster();
		}
		return true;
	}

	public void redraw() {
		timelineWidget.redrawDisplay();
	}

	/**
	 * The maximum number of event bands shown in the band area.<br/>
	 * If a value lower equal 0 is set, an unlimited number of bands can be
	 * added.
	 * 
	 * @param pageSize
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;

		requestBandPage(pagePointer, pageSize);
	}

	/**
	 * Returns the maximum number of event bands shown in the band area. If a
	 * value lower equal 0 is returned, an unlimited number of bands can be
	 * added.
	 * 
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

}
