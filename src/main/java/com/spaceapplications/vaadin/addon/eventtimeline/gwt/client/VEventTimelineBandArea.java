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
public class VEventTimelineBandArea extends VerticalPanel implements MouseOverHandler,
    MouseOutHandler {

  private final VEventTimelineWidget timelineWidget;

  private HandlerRegistration mouseOverReg, mouseOutReg;

  // Band captions
  private final List<Integer> bandMinimumHeights = new ArrayList<Integer>();

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

  public void setCaptions(final String[] captions) {
    int bandId = 0;
    int bandHeight = (getParent().getOffsetHeight() - timelineWidget.getBrowserHeight() - 16) / captions.length;
    for (String caption : captions) {
      // the minimum band height
      // TODO: make this configurable through the widget
      bandMinimumHeights.add(20);

      Widget band = new VEventTimelineBand(bandId++, caption, this);
      add(band);

      // TODO: currently the band heights are set to default 45 px -> make configurable
      band.setHeight(bandHeight + "px");
      band.setWidth(100 + "px");
    }
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
}
