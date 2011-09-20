/*
 * Copyright 2010 ArkaSoft LLC.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.spaceapplications.vaadin.addon.eventtimeline.gwt.style.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.spaceapplications.vaadin.addon.eventtimeline.gwt.style.client.ComputedStyleImpl;

public class ComputedStyle {

	private static final ComputedStyleImpl impl = GWT
			.create(ComputedStyleImpl.class);

	private static final String STYLE_FONT_SIZE = "fontSize";
	private static final String STYLE_COLOR = "color";
	private static final String STYLE_BACKGROUND_COLOR = "backgroundColor";

	private static final String STYLE_MARGIN_TOP = "marginTop";
	private static final String STYLE_MARGIN_RIGHT = "marginRight";
	private static final String STYLE_MARGIN_LEFT = "marginLeft";
	private static final String STYLE_MARGIN_BOTTOM = "marginBottom";

	private static final String STYLE_BORDER_TOP_WIDTH = "borderTopWidth";
	private static final String STYLE_BORDER_RIGHT_WIDTH = "borderRightWidth";
	private static final String STYLE_BORDER_LEFT_WIDTH = "borderBottomWidth";
	private static final String STYLE_BORDER_BOTTOM_WIDTH = "borderLeftWidth";

	private static final String STYLE_PADDING_TOP = "paddingTop";
	private static final String STYLE_PADDING_RIGHT = "paddingRight";
	private static final String STYLE_PADDING_LEFT = "paddingLeft";
	private static final String STYLE_PADDING_BOTTOM = "paddingBottom";

	private static final String STYLE_WIDTH = "width";
	private static final String STYLE_HEIGHT = "height";

	private static final String STYLE_LINE_HEIGHT = "lineHeight";

	/**
	 * Returns the final computed value of a CSS property for the given element.
	 * 
	 * @param elem
	 *            the element
	 * @param attr
	 *            the name of the style property to be retrieved
	 * @return the computed property value
	 */
	public static String getProperty(Element elem, String property) {
		return impl.getProperty(elem, property);
	}

	public static int getFontSize(Element elem) {
		return parseInt(getProperty(elem, STYLE_FONT_SIZE), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static String getColor(Element elem) {
		return getProperty(elem, STYLE_COLOR);
	}

	public static String getBackgroundColor(Element elem) {
		return getProperty(elem, STYLE_BACKGROUND_COLOR);
	}

	public static int getMarginTop(Element elem) {
		return parseInt(getProperty(elem, STYLE_MARGIN_TOP), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getMarginLeft(Element elem) {
		return parseInt(getProperty(elem, STYLE_MARGIN_LEFT), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getMarginRight(Element elem) {
		return parseInt(getProperty(elem, STYLE_MARGIN_RIGHT), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getMarginBottom(Element elem) {
		return parseInt(getProperty(elem, STYLE_MARGIN_BOTTOM), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getBorderTopWidth(Element elem) {
		return parseInt(getProperty(elem, STYLE_BORDER_TOP_WIDTH), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getBorderLeftWidth(Element elem) {
		return parseInt(getProperty(elem, STYLE_BORDER_LEFT_WIDTH), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getBorderRightWidth(Element elem) {
		return parseInt(getProperty(elem, STYLE_BORDER_RIGHT_WIDTH), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getBorderBottomWidth(Element elem) {
		return parseInt(getProperty(elem, STYLE_BORDER_BOTTOM_WIDTH), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getPaddingTop(Element elem) {
		return parseInt(getProperty(elem, STYLE_PADDING_TOP), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getPaddingLeft(Element elem) {
		return parseInt(getProperty(elem, STYLE_PADDING_LEFT), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getPaddingRight(Element elem) {
		return parseInt(getProperty(elem, STYLE_PADDING_RIGHT), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getPaddingBottom(Element elem) {
		return parseInt(getProperty(elem, STYLE_PADDING_BOTTOM), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getWidth(Element elem) {
		return parseInt(getProperty(elem, STYLE_WIDTH), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getHeight(Element elem) {
		return parseInt(getProperty(elem, STYLE_HEIGHT), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	public static int getLineHeight(Element elem) {
		return parseInt(getProperty(elem, STYLE_LINE_HEIGHT), 10, 0);
		// IE will return NaN if the property value is set to 'auto', well set
		// it to 0.
	}

	/**
	 * Parses a string and returns an integer.
	 * <p>
	 * NOTE: Only the first number in the string is returned!
	 * <p>
	 * NOTE: Leading and trailing spaces are allowed.
	 * <p>
	 * NOTE: If the first character cannot be converted to a number,
	 * <code>parseInt()</code> returns <code>null</code>.
	 * 
	 * @param str
	 *            the string to be parsed
	 * @return the parsed value
	 */
	protected static Integer parseInt(String str) {
		return parseInt(str, 10);
	}

	/**
	 * Parses a string and returns an integer.
	 * <p>
	 * NOTE: Only the first number in the string is returned!
	 * <p>
	 * NOTE: Leading and trailing spaces are allowed.
	 * <p>
	 * NOTE: If the first character cannot be converted to a number,
	 * <code>parseInt()</code> returns <code>defaultValue</code>.
	 * 
	 * @param str
	 *            the string to be parsed
	 * @param radix
	 *            a number (from 2 to 36) that represents the numeric system to
	 *            be used
	 * @param defaultValue
	 *            the value to return if the parsed value was <code>null</code>
	 * @return the parsed value
	 */
	public static int parseInt(String str, int radix, int defaultValue) {
		final Integer result = parseInt(str, radix);
		return result == null ? defaultValue : result;
	}

	/**
	 * Parses a string and returns an integer.
	 * <p>
	 * NOTE: Only the first number in the string is returned!
	 * <p>
	 * NOTE: Leading and trailing spaces are allowed.
	 * <p>
	 * NOTE: If the first character cannot be converted to a number,
	 * {@code parseInt()} returns {@code null}.
	 * 
	 * @param str
	 *            the string to be parsed
	 * @param radix
	 *            a number (from 2 to 36) that represents the numeric system to
	 *            be used
	 * @return the parsed value
	 */
	public native static Integer parseInt(String str, int radix)
	/*-{
		var number = parseInt(str, radix);
		if (isNaN(number))
			return null;
		else
			return @java.lang.Integer::valueOf(I)(number);
	}-*/;

}