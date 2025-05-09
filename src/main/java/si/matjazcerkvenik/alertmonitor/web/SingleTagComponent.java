/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package si.matjazcerkvenik.alertmonitor.web;

import si.matjazcerkvenik.alertmonitor.model.DTag;

import java.io.IOException;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

@FacesComponent(value="SingleTag")
public class SingleTagComponent extends UIComponentBase {
	
	@Override
	public void encodeBegin(FacesContext ctx) throws IOException {
		super.encodeBegin(ctx);
				
		ResponseWriter rw = ctx.getResponseWriter();
		
		DTag t = (DTag) getAttributes().get("value");
		

		String backgroundColor = "white";
		if (t.isSelected()) {
			backgroundColor = t.getColor();
		}
		String textColor = "black";
		
		rw.startElement("div", this);
		rw.writeAttribute("class", "tagBorder", null);
		rw.writeAttribute("style", "background-color: " + backgroundColor + "; color: " + textColor + ";", null);
		//rw.write(t.getName());
		//rw.endElement("div");
		
	}

	@Override
	public void encodeEnd(FacesContext ctx) throws IOException {
		super.encodeEnd(ctx);

		ResponseWriter rw = ctx.getResponseWriter();

		rw.endElement("div");
	}

	@Override
	public String getFamily() {
		return "alertmonitor.singleTag";
	}

}