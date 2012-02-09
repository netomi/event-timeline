/*
@VaadinAddonLicenseForJavaFiles@
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * Client-side cache for received events, based on original version from
 * vaadin-timeline.
 * 
 * @author Thomas Neidhart / Space Applications Services NV/SA
 */
public class VClientCache {
	/**
	 * A range of events sorted by time
	 */
	public static class DataRange implements Comparable<DataRange> {
		private final Long from;
		private final Long to;
		private final List<VEvent> events;

		/**
		 * Constructor
		 * 
		 * @param from
		 *            Date that range starts
		 * @param to
		 *            Date the range ends
		 * @param events
		 *            The events in the range
		 */
		public DataRange(Date from, Date to, List<VEvent> events) {
			this.from = from.getTime();
			this.to = to.getTime();

			// Add events
			this.events = events;
		}

		/**
		 * Checks if a date is in the range
		 * 
		 * @param date
		 *            The date to check
		 * @return Returns true if date is in range, else false
		 */
		public boolean inRange(Date date) {
			Long time = date.getTime();
			return time >= from && time <= to;
		}

		/**
		 * Gets the events in the range
		 * 
		 * @return Returns the events as a list of TimelineEvents
		 */
		public List<VEvent> getEvents() {
			return events;
		}

		/**
		 * Gets the starting time of the range
		 * 
		 * @return The starting time as Long
		 */
		public Long getStartTime() {
			return from;
		}

		/**
		 * Gets the ending time of the range
		 * 
		 * @return The ending time
		 */
		public Long getEndTime() {
			return to;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(DataRange o) {
			Date start = new Date(o.getStartTime());
			Date end = new Date(o.getEndTime());

			if (inRange(start) || inRange(end)) {
				return 0;
			}

			if (o.getStartTime() > to) {
				return -1;
			}

			return 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (o instanceof DataRange) {
				DataRange r = (DataRange) o;
				Date start = new Date(r.getStartTime());
				Date end = new Date(r.getEndTime());
				if (inRange(start) || inRange(end)) {
					return true;
				}
			}

			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return from.hashCode() + to.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			DateTimeFormat formatter = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
			Date f = new Date(from);
			Date t = new Date(to);
			return "Datarange: " + formatter.format(f) + " - "
					+ formatter.format(t) + ", events=" + events.size();
		}
	}

	// Band - Data range map
	private final Map<Integer, List<DataRange>> dataCache;

	// The main widget
	private final VEventTimelineWidget widget;

	public VClientCache(VEventTimelineWidget widget) {
		dataCache = new HashMap<Integer, List<DataRange>>();
		this.widget = widget;
	}

	/**
	 * Get a date range by start and end dates. Note: A range with different
	 * start and end dates might be returned!
	 * 
	 * @param band
	 *            The band of the range
	 * @param from
	 *            The start date of the range
	 * @param to
	 *            The end date of the range
	 * @return A date range
	 */
	private DataRange getRange(Integer band, Date from, Date to) {
		List<DataRange> ranges = dataCache.get(band);

		if (ranges == null) {
			return null;
		}

		for (DataRange r : ranges) {
			if (r.inRange(from) || r.inRange(to)) {
				return r;
			}
		}

		return null;
	}

	/**
	 * Merge two overlapping date ranges into one
	 * 
	 * @param band
	 *            The band of the ranges
	 * @param r1
	 *            The first date range
	 * @param r2
	 *            The second date range
	 */
	private void merge(Integer band, DataRange r1, DataRange r2) {
		List<DataRange> ranges = dataCache.get(band);
		if (ranges == null) {
			return;
		}

		// Remove both ranges from the cache
		if (ranges.contains(r1)) {
			ranges.remove(r1);
		}

		if (ranges.contains(r2)) {
			ranges.remove(r2);
		}

		// Calculate new from and to times
		Date from = r1.getStartTime() > r2.getStartTime() ? new Date(r2.getStartTime()) : new Date(r1.getStartTime());
		Date to = r1.getEndTime() > r2.getEndTime() ? new Date(r1.getEndTime())	: new Date(r2.getEndTime());

		// Add events
		Set<VEvent> events = new HashSet<VEvent>();
		if (r1.getEvents() != null) {
			events.addAll(r1.getEvents());
		}
		if (r2.getEvents() != null) {
			events.addAll(r2.getEvents());
		}

		// Create a new Data range
		List<VEvent> mergedEvents = new ArrayList<VEvent>(events);
		Collections.sort(mergedEvents);
		DataRange range = new DataRange(from, to, mergedEvents);
		ranges.add(range);
	}

	/**
	 * Add data points to cache
	 * 
	 * @param band
	 *            The band id
	 * @param from
	 *            THe from date
	 * @param to
	 *            The to date
	 * @param events
	 *            THe events
	 */
	public void addToCache(Integer band, Date from, Date to, List<VEvent> events) {

		List<DataRange> ranges = dataCache.get(band);
		if (ranges == null) {
			ranges = new LinkedList<DataRange>();
			dataCache.put(band, ranges);
		}

		// Check if we have an mergeable data range
		DataRange range = getRange(band, from, to);

		// No existing range was found, so creating a new one
		if (range == null) {
			range = new DataRange(from, to, events);
			ranges.add(range);
		}

		// Else merge the ranges
		else {
			merge(band, range, new DataRange(from, to, events));
		}
	}

	/**
	 * Removes all events for the given band id from the cache.
	 * 
	 * @param band
	 *            The band id
	 */
	public void removeFromCache(Integer band) {
		dataCache.remove(band);
	}

	/**
	 * Get events from cache
	 * 
	 * @param band
	 *            The band index number
	 * @param from
	 *            The start date
	 * @param to
	 *            The end date
	 * @return Returns a list of events. If the range was not found in cache
	 *         null is returned.
	 */
	public List<VEvent> getFromCache(Integer band, Date from, Date to) {
		if (band == null || from == null || to == null) {
			return null;
		}

		List<DataRange> ranges = dataCache.get(band);
		if (ranges == null) {
			return null;
		}

		for (DataRange dr : ranges) {
			if ((dr.inRange(from) && dr.inRange(to))
					|| (dr.inRange(from)
							&& to.getTime() > widget.getEndDate().getTime() && dr
							.getEndTime() == widget.getEndDate().getTime())
					|| (dr.inRange(to)
							&& from.getTime() < widget.getStartDate().getTime() && dr
							.getStartTime() == widget.getStartDate().getTime())) {
				List<VEvent> events = new ArrayList<VEvent>();

				// Get the events
				if (dr.getEvents() != null) {
					for (VEvent event : dr.getEvents()) {
						Long time = event.getStartTime();
						if (time >= dr.getStartTime()
								&& time <= dr.getEndTime()) {
							events.add(event);
						}
					}
				}

				return events;
			}
		}

		return null;
	}
}
