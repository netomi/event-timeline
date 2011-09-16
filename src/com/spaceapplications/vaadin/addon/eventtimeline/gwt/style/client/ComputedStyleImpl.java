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

public class ComputedStyleImpl {

	public native String getProperty(Element elem, String property)
	/*-{
		if (document.defaultView && document.defaultView.getComputedStyle) { // W3C DOM method
			var value = null;
			if (property == 'float') { // fix reserved word
				property = 'cssFloat';
			}
			var computed = elem.ownerDocument.defaultView.getComputedStyle(
					elem, null);
			if (computed) { // test computed before touching for safari
				value = computed[property];
			}
			return (value || elem.style[property] || '');
		} else { // default to inline only
			return (elem.style[property] || '');
		}
	}-*/;
}