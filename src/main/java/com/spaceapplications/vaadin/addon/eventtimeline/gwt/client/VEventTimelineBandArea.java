/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * VEventTimelineBandArea.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 */
public class VEventTimelineBandArea extends VerticalPanel {

  private final VEventTimelineWidget timelineWidget;

  // Band captions
  private final List<Integer> bandMinimumHeights = new ArrayList<Integer>();

  public VEventTimelineBandArea(VEventTimelineWidget tw) {
    timelineWidget = tw;    
  }
  
  public void setCaptions(final String[] captions) {
    int bandId = 0;
    for (String caption : captions) {
      // the minimum band height
      // TODO: make this configurable through the widget
      bandMinimumHeights.add(20);
      
      Widget band = new VEventTimelineBand(bandId++, caption, this);
      add(band);
      
      // TODO: currently the band heights are set to default 45 px -> make configurable
      band.setHeight(45 + "px");
      band.setWidth(100 + "px");
    }
  }
  
  public int getBandHeight(int band) {
    return getWidget(band).getOffsetHeight();
  }
  
  public boolean requestResize(int bandId, int newHeight) {
    int minimumHeight = bandMinimumHeights.get(bandId);
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

    int oldHeight = ((VEventTimelineBand) getWidget(bandId)).getHeight();

    if (totalHeight > maxHeight) {
      newHeight -= totalHeight - maxHeight; 
      if (newHeight < oldHeight) {
        return false;
      }
    }
    
    getWidget(bandId).setHeight(newHeight + "px");
    
    for (Widget w : getChildren()) {
      ((VEventTimelineBand) w).refreshAdjuster();
    }
    return true;
  }
  
  public void redraw() {
    timelineWidget.redrawDisplay();
  }
}
