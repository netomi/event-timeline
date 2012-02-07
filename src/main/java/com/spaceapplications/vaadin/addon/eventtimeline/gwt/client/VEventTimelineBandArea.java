/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
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
 * @author Florian Pirchner (add / remove bands)
 */
public class VEventTimelineBandArea extends VerticalPanel implements
		MouseOverHandler, MouseOutHandler {

	private final VEventTimelineWidget timelineWidget;

	private HandlerRegistration mouseOverReg, mouseOutReg;

	// Band captions
	private final List<Integer> bandMinimumHeights = new ArrayList<Integer>();
	private final List<VEventTimelineBand> allBands = new ArrayList<VEventTimelineBand>();
	private final List<VEventTimelineBand> visibleBands = new ArrayList<VEventTimelineBand>();

	private int pageSize = -1;

	private int pageNumber;

	private boolean bandSelectionEnabled;

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
	 * Is called to add an event band. Do not miss to navigate to the requested
	 * page. For performance issues this is not done automatically.
	 * 
	 * @param id
	 * @param caption
	 */
	public void addBand(int id, String caption) {
		VEventTimelineBand band = new VEventTimelineBand(id, caption, this);
		allBands.add(band);
		bandMinimumHeights.add(20);
	}

	/**
	 * Returns the page number that contains the given band. If the band is not
	 * part of the available bands then -1 is returned.
	 * 
	 * @param band
	 * @return
	 */
	protected int getContainingPage(VEventTimelineBand band) {
		int indexOf = allBands.indexOf(band);
		if (indexOf < 0) {
			return -1;
		}

		if (this.pageSize <= 0) {
			return 0;
		}

		return indexOf / pageSize;
	}

	/**
	 * Requests the build of the visible page.
	 * 
	 * @param firstBandInPageIndex
	 *            The first band in the page. The value is the index of the band
	 *            in allBands
	 * @param pageSize
	 *            The number of event bands visible in the band area
	 */
	protected void requestBuildPage(int firstBandInPageIndex, int pageSize) {

		// remove all bands
		for (VEventTimelineBand band : visibleBands) {
			remove(band);
			band.setHeight("0px");
		}
		visibleBands.clear();

		if (pageSize > 0) {
			int height = calcHeight(pageSize);
			for (int i = 0; i < pageSize; i++) {
				int currentBandIndex = firstBandInPageIndex + i;
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
				band.updateBandAdjuster();
			}
		} else {
			int height = calcHeight(allBands.size());
			for (VEventTimelineBand band : allBands) {
				band.setHeight(height + "px");
				band.setWidth(100 + "px");

				// add the band
				add(band);
				visibleBands.add(band);
				band.updateBandAdjuster();
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
	  int bandHeight = timelineWidget.getBandHeight();
	  
	  // only adjust height if no specific height is configured
	  if (bandHeight == -1) {
	    int calcBase = pageSize <= 0 ? allBands.size() : pageSize;
	    if (calcBase > 0) {
	      bandHeight = (getParent().getOffsetHeight()
	          - timelineWidget.getBrowserHeight() - 16) / calcBase;
	    }
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
		VEventTimelineBand band = null;
		for (VEventTimelineBand temp : allBands) {
			if (temp.getId() == id) {
				index = allBands.indexOf(temp);
				band = temp;
				break;
			}
		}

		if (index >= 0) {
			bandMinimumHeights.remove(index);
		}

		// remove the band
		if (visibleBands.contains(band)) {
			int page = getContainingPage(band);
			allBands.remove(band);
			remove(band);
			visibleBands.remove(band);

			// test if current page would be empty since removed was first
			// element of last page
			if (page > 0) {
				int firstBandInPage = calcFirstBandInPage(page);
				if (firstBandInPage >= allBands.size()) {
					page--;
				}
			}

			// if the page is valid, navigate to it
			if (page >= 0) {
				setVisiblePage(page);
			}
		} else {
			allBands.remove(band);
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

	/**
	 * Returns all available bands.
	 * 
	 * @return the allBands
	 */
	public List<VEventTimelineBand> getAllBands() {
		return Collections.unmodifiableList(allBands);
	}

	/**
	 * Returns all visible bands.
	 * 
	 * @return the visibleBands
	 */
	public List<VEventTimelineBand> getVisibleBands() {
		return Collections.unmodifiableList(visibleBands);
	}

	/**
	 * Returns the height of the band.
	 * 
	 * @param bandId
	 * @return
	 */
	public int getBandHeight(int bandId) {
		VEventTimelineBand result = getBandById(bandId);
		return result != null ? result.getHeight() : 0;
	}

	/**
	 * Returns the band by its id or <code>null</code> if the band does not
	 * exist.
	 * 
	 * @param bandId
	 * @return
	 */
	protected VEventTimelineBand getBandById(int bandId) {
		VEventTimelineBand result = null;
		for (VEventTimelineBand band : allBands) {
			if (band.getId() == bandId) {
				result = band;
				break;
			}
		}
		return result;
	}

	public boolean requestResize(int bandId, int oldHeight, int adjustment) {
		int minimumHeight = bandMinimumHeights.get(bandId);
		int newHeight = oldHeight + adjustment;
		if (newHeight < minimumHeight) {
			return false;
		}

		// maximum height of the parent widget
		int maxHeight = getParent().getOffsetHeight() - timelineWidget.getBrowserHeight() - 16;

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

		setVisiblePage(0);
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

	/**
	 * Returns the number of event band pages available.
	 * 
	 * @return
	 */
	public int getPageCount() {
		if (allBands.size() == 0) {
			return 0;
		}

		int pageSize = this.pageSize;
		if (pageSize <= 0) {
			pageSize = allBands.size();
		}

		BigDecimal bd = new BigDecimal((double) allBands.size() / pageSize);
		return bd.setScale(0, RoundingMode.CEILING).intValue();
	}

	/**
	 * Returns the number of the current page. The first page is 0.
	 * 
	 * @return the pageNumber
	 */
	public int getVisiblePage() {
		return pageNumber;
	}

	/**
	 * Sets the number of the current visible page<br/>
	 * If the given number is &gt the maximum available page, the last page will
	 * be shown.<br/>
	 * The first page is 0.
	 */
	public void setVisiblePage(int pageNumber) {
		if (pageNumber < 0 || getPageCount() == 0) {
			this.pageNumber = 0;
		} else {
			if (pageNumber > getPageCount() - 1) {
				this.pageNumber = getPageCount() - 1;
			} else {
				this.pageNumber = pageNumber;
			}
		}

		navigateToPage(pageNumber);
	}

	/**
	 * Navigates to the page with the given number.
	 * 
	 * @param pageNumber
	 */
	public void navigateToPage(int pageNumber) {
		if (pageNumber < 0) {
			return;
		}

		int firstBandInPage = calcFirstBandInPage(pageNumber);
		requestBuildPage(firstBandInPage, pageSize);
	}

	/**
	 * Calculates the index of the first band in the page.
	 * 
	 * @param pageNumber
	 * @return
	 */
	protected int calcFirstBandInPage(int pageNumber) {
		int firstBandInPage = 0;
		if (pageSize > 0) {
			firstBandInPage = pageNumber * pageSize;
		}
		return firstBandInPage;
	}

	/**
	 * Navigates to the band with the given id.
	 * 
	 * @param bandId
	 */
	public void navigateToBand(int bandId) {
		VEventTimelineBand band = getBandById(bandId);
		if (band != null) {
			int containingPage = getContainingPage(band);
			if (containingPage >= 0) {
				setVisiblePage(containingPage);
			}
		}
	}

	/**
	 * Returns true, if the band is in the visible portion of the UI.
	 * 
	 * @param bandId
	 * @return true if the band is visible
	 */
	public boolean isBandVisible(int bandId) {
		VEventTimelineBand band = getBandById(bandId);
		if (band != null) {
			int containingPage = getContainingPage(band);
			return containingPage == getVisiblePage();
		}
		return false;
	}

	/**
	 * Is called if the band with the given id was selected.
	 * 
	 * @param bandId
	 */
	public void bandSelected(int bandId) {

		// band selection not enabled
		if (!bandSelectionEnabled) {
			return;
		}

		// nothing to do if the band is already selected
		if (isBandSelected(bandId)) {
			return;
		}

		if (!isBandVisible(bandId)) {
			navigateToBand(bandId);
		}

		for (VEventTimelineBand band : allBands) {
			if (band.getId() == bandId) {
				band.setSelected(true);
			} else {
				band.setSelected(false);
			}
		}

		timelineWidget.fireBandSelected(bandId);
	}

	/**
	 * Is called to deselect all bands.
	 */
	public void deselectBands() {
		for (VEventTimelineBand band : allBands) {
			if (band.isSelected()) {
				band.setSelected(false);
			}
		}
	}

	/**
	 * Returns true, if the band with the given id is selected.
	 * 
	 * @param bandId
	 * @return
	 */
	private boolean isBandSelected(int bandId) {
		VEventTimelineBand band = getBandById(bandId);
		return band != null && band.isSelected();
	}

	/**
	 * Sets whether the band selection is enabled or not.
	 * 
	 * @param bandSelectionEnabled
	 */
	public void setBandSelectionEnabled(boolean bandSelectionEnabled) {
		this.bandSelectionEnabled = bandSelectionEnabled;
		if (!bandSelectionEnabled) {
			// deselect all bands
			deselectBands();
		}
	}

	/**
	 * Returns true, if the band selection is enabled. False otherwise.
	 * 
	 * @return
	 */
	public boolean isBandSelectionEnabled() {
		return bandSelectionEnabled;
	}

}
