/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import java.util.List;

/**
 * Interface for components that received data updates from the server.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 */
public interface VDataListener {
  public void dataReceived(Integer band, List<VEvent> events);

  public void dataReceivedAll();

  public boolean isVisible();
}
