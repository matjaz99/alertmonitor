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
package si.matjazcerkvenik.alertmonitor.webhook;

import si.matjazcerkvenik.alertmonitor.data.DAO;
import si.matjazcerkvenik.alertmonitor.model.DTag;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import java.io.IOException;
import java.util.List;

@FacesRenderer(componentFamily = "alertmonitor.multiTag", rendererType = "alertmonitor.MultiTagRenderer")
public class MultiTagRenderer extends Renderer {
	
	@Override
	public void encodeBegin(FacesContext context, UIComponent component)
			throws IOException {
		
		super.encodeBegin(context, component);
		
		ResponseWriter rw = context.getResponseWriter();
		
		//Tags tags = (Tags) component.getAttributes().get("value");

        List<DTag> tags = DAO.getInstance().getTags();
				
		rw.startElement("div", component);
		rw.writeAttribute("style", "float: left;", null);
		
		for (int i = 0; i < tags.size(); i++) {
			
			DTag t = tags.get(i);
			
			// default values (if tag definition does not exist)
			String backgroundColor = t.getColor();
			String textColor = "black";
			
			rw.startElement("div", component);
			rw.writeAttribute("class", "tagBorder", null);
			rw.writeAttribute("style", "background-color: " + backgroundColor + "; 	color: " + textColor + "; float: left;", null);
			rw.write(t.getName());
			rw.endElement("div");
		}
		
		rw.endElement("div");
		
		rw.startElement("div", component);
		rw.writeAttribute("style", "clear: both;", null);
		rw.endElement("div");
		
	}
	
	
}