package com.spaceapplications.vaadin.addon.eventtimeline.demo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.spaceapplications.vaadin.addon.eventtimeline.EventTimeline;
import com.spaceapplications.vaadin.addon.eventtimeline.EventTimeline.BandInfo;
import com.spaceapplications.vaadin.addon.eventtimeline.event.BasicEvent;
import com.spaceapplications.vaadin.addon.eventtimeline.event.BasicEventProvider;
import com.spaceapplications.vaadin.addon.eventtimeline.event.TimelineEventProvider;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

public class SimpleEventTimelineDemo extends Application {
	@SuppressWarnings("serial")
	@Override
	public void init() {
		Window mainWindow = new Window("Simple EventTimeline Demo");

		setMainWindow(mainWindow);

		Button addBandButton = new Button("Add band");
		mainWindow.addComponent(addBandButton);

		Button removeLastBand = new Button("Remove last band");
		mainWindow.addComponent(removeLastBand);

		// create the timeline
		final EventTimeline timeline = new EventTimeline("Our event timeline");
		timeline.setHeight("500px");
		timeline.setWidth("100%");

		// set the visible time range
		Calendar cal = Calendar.getInstance();
		Date start = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, 4);
		final Date end = cal.getTime();
		timeline.setVisibleDateRange(start, end);

		// add our data sources
		timeline.addEventBand("Band", createEventProvider(end));
		timeline.addEventBand("Band2", createEventProvider2(end));

		// Add some zoom levels
		timeline.addZoomLevel("Hour", 60 * 60 * 1000L);
		timeline.addZoomLevel("Day", 86400000L);

		mainWindow.addComponent(timeline);

		addBandButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				timeline.addEventBand(String.format("Band %d", timeline
						.getBandInfos().size() + 1),
						createEventProviderAdditional(end));
			}
		});

		removeLastBand.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				List<BandInfo> infos = timeline.getBandInfos();
				if (infos.size() > 0) {
					timeline.removeEventBand(infos.get(infos.size() - 1)
							.getProvider());
				}
			}
		});
	}

	public TimelineEventProvider createEventProvider(final Date end) {
		BasicEventProvider provider = new BasicEventProvider();

		// get events for a whole day
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 1);
		int idx = 0;
		while (cal.getTime().before(end)) {
			// create a simple event
			BasicEvent event = new BasicEvent();
			event.setEventId(String.valueOf(idx));

			// set the timestamp property
			event.setStart(cal.getTime());
			cal.add(Calendar.MINUTE, 10);
			event.setEnd(cal.getTime());
			// set the caption
			event.setCaption("Event");
			// style the event
			event.setStyleName("color1");

			provider.addEvent(event);
			cal.add(Calendar.MINUTE, 20);
		}

		return provider;
	}

	public TimelineEventProvider createEventProvider2(final Date end) {
		BasicEventProvider provider = new BasicEventProvider();

		// get events for a whole day
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 1);
		cal.add(Calendar.MINUTE, 17);
		int idx = 0;
		while (cal.getTime().before(end)) {
			// create a simple event
			BasicEvent event = new BasicEvent();
			event.setEventId(String.valueOf(idx));

			// set the timestamp property
			event.setStart(cal.getTime());
			cal.add(Calendar.MINUTE, 10);
			event.setEnd(cal.getTime());
			// set the caption
			event.setCaption("Event");
			// style the event
			event.setStyleName("color2");

			provider.addEvent(event);
			cal.add(Calendar.MINUTE, 20);
		}

		return provider;
	}

	public TimelineEventProvider createEventProviderAdditional(final Date end) {
		BasicEventProvider provider = new BasicEventProvider();

		// get events for a whole day
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 1);
		cal.add(Calendar.MINUTE, 11);
		int idx = 0;
		while (cal.getTime().before(end)) {
			// create a simple event
			BasicEvent event = new BasicEvent();
			event.setEventId(String.valueOf(idx));

			// set the timestamp property
			event.setStart(cal.getTime());
			cal.add(Calendar.MINUTE, 10);
			event.setEnd(cal.getTime());
			// set the caption
			event.setCaption("Event");
			// style the event
			event.setStyleName("color3");

			provider.addEvent(event);
			cal.add(Calendar.MINUTE, 20);
		}

		return provider;
	}
}