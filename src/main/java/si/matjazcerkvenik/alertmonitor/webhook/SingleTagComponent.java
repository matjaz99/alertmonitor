package si.matjazcerkvenik.alertmonitor.webhook;

import si.matjazcerkvenik.alertmonitor.model.DTag;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

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