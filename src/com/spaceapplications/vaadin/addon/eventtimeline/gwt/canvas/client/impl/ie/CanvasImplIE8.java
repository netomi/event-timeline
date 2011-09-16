/*
@VaadinAddonLicenseForJavaFiles@
 */

package com.spaceapplications.vaadin.addon.eventtimeline.gwt.canvas.client.impl.ie;

/**
 * Internet Explorer 8 fix for VML.
 * 
 * @author John Ahlroos / Vaadin Ltd
 * 
 */
public class CanvasImplIE8 extends CanvasImplIE {

    @Override
    protected native void init()
    /*-{
               if (!$doc.namespaces["v"]) {
                       $doc.namespaces.add('v', 'urn:schemas-microsoft-com:vml', "#default#VML");
                       $doc.createStyleSheet().cssText = "v\\:*{behavior:url(#default#VML);}";
                }
    }-*/;

    /*
     * (non-Javadoc)
     * 
     * @see com.spaceapplications.vaadin.addon.eventtimeline.gwt.canvas.client.impl.ie.CanvasImplIE#stroke()
     */
    @Override
    public void stroke() {
        if (pathStack.isEmpty()) {
            return;
        }

        scratchStack.clear();
        scratchStack
        .push("<v:shape style=\"position:absolute;width:10px;height:10px;\" coordsize=\"100,100\" filled=\"f\" strokecolor=\"");
        scratchStack.push(context.strokeStyle);
        scratchStack.push("\" strokeweight=\"" + context.lineWidth);
        scratchStack.push("px\" path=\"");
        scratchStack.push(pathStack.join());
        scratchStack
        .push(" e\"><v:stroke style=\"position:absolute;width:10px;height:10px;\" opacity=\""
                + context.globalAlpha
                * context.strokeAlpha);
        scratchStack.push("\" miterlimit=\"" + context.miterLimit);
        scratchStack.push("\" joinstyle=\"");
        scratchStack.push(context.lineJoin);
        scratchStack.push("\" endcap=\"");
        scratchStack.push(context.lineCap);
        scratchStack.push("\"></v:stroke></v:shape>");
        insert(context.globalCompositeOperation, scratchStack.join());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.spaceapplications.vaadin.addon.eventtimeline.gwt.canvas.client.impl.ie.CanvasImplIE#fill()
     */
    @Override
    public void fill() {
        if (pathStack.isEmpty()) {
            return;
        }
        scratchStack.clear();
        scratchStack
        .push("<v:shape style=\"position:absolute;width:10px;height:10px;\" coordsize=\"100,100\" fillcolor=\"");
        scratchStack.push(context.fillStyle);
        scratchStack.push("\" stroked=\"f\" path=\"");
        scratchStack.push(pathStack.join());
        scratchStack.push(" e\"><v:fill style=\"position:absolute;width:10px;height:10px;\" opacity=\""
                + context.globalAlpha
                * context.fillAlpha);
        // TODO add gradient code here
        scratchStack.push("\"></v:fill></v:shape>");
        insert(context.globalCompositeOperation, scratchStack.join());
    }
}
