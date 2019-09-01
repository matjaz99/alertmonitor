package si.matjazcerkvenik.alertmonitor.webhook;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

@FacesComponent(value="MultiTag")
public class MultiTagComponent extends UIComponentBase {

	@Override
	public String getFamily() {
		return "alertmonitor.multiTag";
	}

}