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
 */
public class VEventTimelineBandArea extends VerticalPanel implements
		MouseOverHandler, MouseOutHandler {

	private final VEventTimelineWidget timelineWidget;

	private HandlerRegistration mouseOverReg, mouseOutReg;

	// Band captions
	private final List<Integer> bandMinimumHeights = new ArrayList<Integer>();

	private final List<VEventTimelineBand> bands = new ArrayList<VEventTimelineBand>();

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
		add(band);
		bands.add(band);

		// TODO: currently the band heights are set to default 45 px -> make
		// configurable
		bandMinimumHeights.add(20);
		int bandHeight = (getParent().getOffsetHeight()
				- timelineWidget.getBrowserHeight() - 16)
				/ bands.size();
		for (VEventTimelineBand existingBand : bands) {
			existingBand.setHeight(bandHeight + "px");
			existingBand.setWidth(100 + "px");
		}
	}

	/**
	 * Is called to remove the event band with the given id.
	 * 
	 * @param id
	 */
	public void removeBand(int id) {
		int index = -1;
		for (VEventTimelineBand band : bands) {
			if (band.getId() == id) {
				index = bands.indexOf(band);
				remove(band);
				bands.remove(band);
				break;
			}
		}

		if (index >= 0) {
			bandMinimumHeights.remove(index);
		}
		int bandHeight = (getParent().getOffsetHeight()
				- timelineWidget.getBrowserHeight() - 16)
				/ bands.size();
		for (VEventTimelineBand existingBand : bands) {
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
		return bands.size();
	}

	// public void setCaptions(final String[] captions) {
	// int bandId = 0;
	//
	// for (String caption : captions) {
	// // the minimum band height
	// // TODO: make this configurable through the widget
	//
	//
	// Widget band = new VEventTimelineBand(bandId++, caption, this);
	// add(band);
	//
	//
	// }
	// }

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
}
