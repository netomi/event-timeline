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


import com.google.gwt.dom.client.Element;
import com.spaceapplications.vaadin.addon.eventtimeline.gwt.style.client.ComputedStyleImpl;

public class ComputedStyleImplIE6 extends ComputedStyleImpl {

	@Override
	public native String getProperty(Element elem, String property)
	/*-{
		var value;
		if (document.documentElement.currentStyle) { // IE method
			switch (property) {
			case 'opacity': // IE opacity uses filter
				var val = 100;
				try { // will error if no DXImageTransform
					val = elem.filters['DXImageTransform.Microsoft.Alpha'].opacity;
				} catch (e) {
					try { // make sure its in the document
						val = elem.filters('alpha').opacity;
					} catch (e) {
						// ignore
					}
				}
				return val / 100;
			case 'float': // fix reserved word
				property = 'styleFloat'; // fall through
			default:
				// test currentStyle before touching
				value = elem.currentStyle ? elem.currentStyle[property] : null;
				value = (value || elem.style[property] || null);
			}
		} else { // default to inline only
			value = (elem.style[property] || null);
		}
		return (value == null) ? null : '' + value;
	}-*/;

}