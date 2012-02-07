/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

/**
 * VEventChartBrowserScroller is the scroller widget in timeline browser.
 * 
 * @author Peter Lehto / IT Mill Oy Ltd
 * @author John Ahlroos / IT Mill Oy Ltd
 */
public class VEventTimelineBrowserScroller extends Widget {
  
    private static final String CLASSNAME_SCROLLAREA_CONTAINER = VEventTimelineWidget.BROWSER_CLASSNAME + "-scrollarea";
    private static final String CLASSNAME_SCROLLAREA_CONTENT = VEventTimelineWidget.BROWSER_CLASSNAME + "-scrollarea-content";
    private static final String CLASSNAME_SCROLLAREA_BAR = CLASSNAME_SCROLLAREA_CONTAINER + "-bar";
    private static final String CLASSNAME_SCROLLAREA_BAR_GRIP = CLASSNAME_SCROLLAREA_BAR + "-grip";
    private static final String CLASSNAME_SCROLLAREA_BAR_LEFT = CLASSNAME_SCROLLAREA_BAR + "-left";
    private static final String CLASSNAME_SCROLLAREA_BAR_RIGHT = CLASSNAME_SCROLLAREA_BAR + "-right";
    private static final String CLASSNAME_SIZE_ADJUSTER = "v-size-adjuster";
    private static final String CLASSNAME_SIZE_ADJUSTER_LEFT = CLASSNAME_SIZE_ADJUSTER + "-left";
    private static final String CLASSNAME_SIZE_ADJUSTER_RIGHT = CLASSNAME_SIZE_ADJUSTER + "-right";

    private final Element scrollAreaContainer;
    private final Element scrollAreaContent;

    private final Element scrollAreaBar;
    private final Element scrollAreaBarGrip;
    private final Element scrollAreaBarLeft;
    private final Element scrollAreaBarRight;

    private final Element scrollAreaLeftAdjuster;
    private final Element scrollAreaRightAdjuster;

    public VEventTimelineBrowserScroller() {
        scrollAreaContainer = DOM.createDiv();
        scrollAreaContainer.setClassName(CLASSNAME_SCROLLAREA_CONTAINER);

        scrollAreaContent = DOM.createDiv();
        scrollAreaContent.setClassName(CLASSNAME_SCROLLAREA_CONTENT);

        scrollAreaBar = DOM.createDiv();
        scrollAreaBar.setClassName(CLASSNAME_SCROLLAREA_BAR);

        scrollAreaBarLeft = DOM.createDiv();
        scrollAreaBarLeft.setClassName(CLASSNAME_SCROLLAREA_BAR_LEFT);
        scrollAreaBar.appendChild(scrollAreaBarLeft);

        scrollAreaBarRight = DOM.createDiv();
        scrollAreaBarRight.setClassName(CLASSNAME_SCROLLAREA_BAR_RIGHT);
        scrollAreaBar.appendChild(scrollAreaBarRight);

        scrollAreaBarGrip = DOM.createDiv();
        scrollAreaBarGrip.setClassName(CLASSNAME_SCROLLAREA_BAR_GRIP);
        scrollAreaBar.appendChild(scrollAreaBarGrip);

        scrollAreaContainer.appendChild(scrollAreaContent);
        scrollAreaContainer.appendChild(scrollAreaBar);

        scrollAreaLeftAdjuster = DOM.createDiv();
        scrollAreaLeftAdjuster.setClassName(CLASSNAME_SIZE_ADJUSTER + " " + CLASSNAME_SIZE_ADJUSTER_LEFT);

        scrollAreaRightAdjuster = DOM.createDiv();
        scrollAreaRightAdjuster.setClassName(CLASSNAME_SIZE_ADJUSTER + " " + CLASSNAME_SIZE_ADJUSTER_RIGHT);

        scrollAreaContent.appendChild(scrollAreaLeftAdjuster);
        scrollAreaContent.appendChild(scrollAreaRightAdjuster);

        setElement(scrollAreaContainer);
    }

    public int getLeftPosition() {
        return getElement().getOffsetLeft() - 15;
    }

    public int getRightPosition() {
        return getLeftPosition() + getWidth() - 1;
    }

    public void setLeftPosition(float pos) {

        if (pos < 15) {
            pos = 15;
        }

        DOM.setStyleAttribute(scrollAreaContainer, "left", pos + "px");
    }

    public void setRightPosition(float pos) {

        float leftPos = getLeftPosition();

        if (pos <= leftPos) {
            float tmp = leftPos;
            leftPos = pos;
            pos = tmp;
        }

        int xDiff = Math.round(pos - leftPos);
        setWidth(xDiff);
    }

    public void center(float pos) {
        int width = getAreaWidth();

        setLeftPosition(pos - (int) Math.floor(width / 2.0));
        setWidth(width);
    }

    public int getWidth() {
        return getOffsetWidth();
    }

    public int getAreaWidth() {
        return Integer.parseInt(DOM.getStyleAttribute(scrollAreaContainer, "width").replace("px", ""));
    }

    /**
     * Moves scroller given amount of pixels. If amount is positive, scroller is
     * moved to right, if negative scroller is moved to left.
     * 
     * @param amount
     */

    public void move(int amount) {
        int scrollerPos = scrollAreaContainer.getOffsetLeft();
        scrollerPos += amount;
        setLeftPosition(scrollerPos);
    }

    public void adjustLeftSideSize(int amount) {
        if (amount < 0) {
            DOM.setStyleAttribute(scrollAreaContent, "width",
                    (getWidth() + Math.abs(amount)) + "px");
        } else {
            DOM.setStyleAttribute(scrollAreaContent, "width",
                    (getWidth() - amount) + "px");
        }

        // Move scroll area content div to left to compensate the change in size
        // to right. (add two additional pixels for borders).
        DOM.setStyleAttribute(scrollAreaContent, "left", (amount - 1) + "px");
    }

    public void adjustRightSideSize(int amount) {
        if (amount != 0) {
            DOM.setStyleAttribute(scrollAreaContent, "width",
                    (getWidth() + amount) + "px");
        }
    }

    public int getMouseOffset(Event event) {
        return event.getClientX() - getElement().getOffsetLeft();
    }

    public boolean isMouseOverScrollElement(Event mouseEvent) {
        Element mouseOver = (Element) Element.as(mouseEvent.getEventTarget());
        return mouseOver == scrollAreaBar || mouseOver == scrollAreaBarGrip
                || mouseOver == scrollAreaBarLeft
                || mouseOver == scrollAreaBarRight;
    }

    public boolean isMouseOverScrollArea(Event mouseEvent) {
        Element mouseOver = (Element) Element.as(mouseEvent.getEventTarget());
        return mouseOver == scrollAreaContainer
                || mouseOver == scrollAreaContent;

    }

    public boolean isMouseOverLeftSideSizeAdjuster(Event mouseEvent) {
        Element mouseOver = (Element) Element.as(mouseEvent.getEventTarget());
        return mouseOver == scrollAreaLeftAdjuster;
    }

    public boolean isMouseOverRightSideSizeAdjuster(Event mouseEvent) {
        Element mouseOver = (Element) Element.as(mouseEvent.getEventTarget());
        return mouseOver == scrollAreaRightAdjuster;
    }

    public void setWidth(int width) {
        DOM.setStyleAttribute(scrollAreaContainer, "width", (width) + "px");
        DOM.setStyleAttribute(scrollAreaContent, "width", (width - 1) + "px");
    }

    public void lockSize() {
        setWidth(scrollAreaContent.getOffsetWidth() - 1);
        setLeftPosition(scrollAreaContainer.getOffsetLeft()
                + scrollAreaContent.getOffsetLeft());
        DOM.setStyleAttribute(scrollAreaContent, "left", "0px");
    }

    public boolean hasElement(com.google.gwt.dom.client.Element elem) {
        if (elem == getElement() || elem == scrollAreaBar
                || elem == scrollAreaBarGrip || elem == scrollAreaContainer
                || elem == scrollAreaContent || elem == scrollAreaLeftAdjuster
                || elem == scrollAreaRightAdjuster || elem == scrollAreaBarLeft
                || elem == scrollAreaBarRight) {
            return true;
        } else {
            return false;
        }
    }
}
