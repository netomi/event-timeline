/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import java.util.List;

/**
 * Interface for components that receive data updates from the server.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 */
public interface VDataListener {
	public void dataReceived(Integer band, List<VEvent> events);

	public void dataReceivedAll();

	public boolean isVisible();

	/**
	 * Is called after a band was removed.
	 * 
	 * @param bands
	 */
	public void dataRemoved(Integer[] bands);
}
