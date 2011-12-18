package com.spaceapplications.vaadin.addon.eventtimeline.demo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.spaceapplications.vaadin.addon.eventtimeline.EventTimeline;
import com.spaceapplications.vaadin.addon.eventtimeline.EventTimeline.BandInfo;
import com.spaceapplications.vaadin.addon.eventtimeline.event.BasicEvent;
import com.spaceapplications.vaadin.addon.eventtimeline.event.BasicEventProvider;
import com.spaceapplications.vaadin.addon.eventtimeline.event.TimelineEvent;
import com.spaceapplications.vaadin.addon.eventtimeline.event.TimelineEventProvider;
import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

public class SimpleEventTimelineDemo extends Application {
	@SuppressWarnings("serial")
	@Override
	public void init() {
		Window mainWindow = new Window("Simple EventTimeline Demo");

		setMainWindow(mainWindow);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		buttons.setMargin(true);
		Button addBandButton = new Button("Add band");
		buttons.addComponent(addBandButton);

		Button removeLastBand = new Button("Remove last band");
		buttons.addComponent(removeLastBand);

		final TextField pageSize = new TextField("Pagesize");
		pageSize.setImmediate(true);
		pageSize.setInvalidAllowed(false);
		pageSize.addValidator(new IntegerValidator("Only ints are allowed"));
		buttons.addComponent(pageSize);

		Button addEventToLastBand = new Button("Add new event...");
		addEventToLastBand.setDescription("...to last band");
		buttons.addComponent(addEventToLastBand);
		
		Button removeFirstEventFromLastBand = new Button(
				"Remove first event...");
		removeFirstEventFromLastBand.setDescription("...from last band");
		buttons.addComponent(removeFirstEventFromLastBand);

		mainWindow.addComponent(buttons);

		// create the timeline
		final EventTimeline timeline = new EventTimeline("Our event timeline");
		timeline.setHeight("500px");
		timeline.setWidth("100%");
		timeline.setPageNavigationVisible(true);

		// set the visible time range
		final Calendar cal = Calendar.getInstance();
		final Date start = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, 4);
		final Date end = cal.getTime();
		timeline.setVisibleDateRange(start, end);

		// add our data sources
		timeline.addEventBand("Band", createEventProvider(end));
		timeline.addEventBand("Band2", createEventProvider2(end));
		timeline.addEventBand("Band3", createEventProvider3(end));
		timeline.addEventBand("Band4", createEventProvider4(end));

		// Add some zoom levels
		timeline.addZoomLevel("Hour", 60 * 60 * 1000L);
		timeline.addZoomLevel("Day", 86400000L);

		mainWindow.addComponent(timeline);

		addBandButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				timeline.addEventBand(String.format("Band %d", timeline
						.getBandInfos().size() + 1),
						createEventProviderAdditional(end));
			}
		});

		removeLastBand.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				List<BandInfo> infos = timeline.getBandInfos();
				if (infos.size() > 0) {
					timeline.removeEventBand(infos.get(infos.size() - 1)
							.getProvider());
				}
			}
		});

		addEventToLastBand.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				List<BandInfo> infos = timeline.getBandInfos();
				if (infos.size() > 0) {
					BasicEventProvider provider = (BasicEventProvider) infos
							.get(infos.size() - 1).getProvider();
					List<TimelineEvent> events = provider.getEvents(start, end);
					// create a simple event
					BasicEvent result = new BasicEvent();
					result.setEventId(String.valueOf(events.size() + 1));

					// set the visible time range
					final Calendar cal = Calendar.getInstance();
					cal.add(Calendar.HOUR, 1);
					// set the timestamp property
					result.setStart(cal.getTime());
					cal.add(Calendar.MINUTE, 10);
					result.setEnd(cal.getTime());
					// set the caption
					result.setCaption("Event");
					// style the event
					result.setStyleName("color4");

					provider.addEvent(result);
					cal.add(Calendar.MINUTE, 4);
				}
			}
		});

		removeFirstEventFromLastBand.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				List<BandInfo> infos = timeline.getBandInfos();
				if (infos.size() > 0) {
					BasicEventProvider provider = (BasicEventProvider) infos
							.get(infos.size() - 1).getProvider();
					List<TimelineEvent> events = provider.getEvents(start, end);
					if (events.size() > 0) {
						BasicEvent result = (BasicEvent) events.get(0);
						provider.removeEvent(result);
					}
				}
			}
		});

		pageSize.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				timeline.setEventBandPageSize(Integer.valueOf((String) pageSize
						.getValue()));
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

	public TimelineEventProvider createEventProvider3(final Date end) {
		BasicEventProvider provider = new BasicEventProvider();

		// get events for a whole day
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 1);
		cal.add(Calendar.MINUTE, 25);
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

	public TimelineEventProvider createEventProvider4(final Date end) {
		BasicEventProvider provider = new BasicEventProvider();

		// get events for a whole day
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 1);
		cal.add(Calendar.MINUTE, 7);
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
			event.setStyleName("color4");

			provider.addEvent(event);
			cal.add(Calendar.MINUTE, 13);
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
			event.setStyleName("color2");

			provider.addEvent(event);
			cal.add(Calendar.MINUTE, 20);
		}

		return provider;
	}
}