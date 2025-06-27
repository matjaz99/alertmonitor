package si.matjazcerkvenik.alertmonitor.web.uibeans;

import java.io.IOException;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 * This beas serves for redirection from index.xhtml to first page (ie. alerts)
 */
@Named("uiRedirectBean")
@RequestScoped
public class UiRedirectBean {
    public void redirect() {
        try {
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .redirect("/alertmonitor/alerts");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
