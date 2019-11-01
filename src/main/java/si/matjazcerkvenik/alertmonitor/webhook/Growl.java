package si.matjazcerkvenik.alertmonitor.webhook;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class Growl {

    public static void showInfoGrowl(String txt, String addInfo) {
        showGrowl("growlInfo", txt, addInfo);
    }

    public static void showWarningGrowl(String txt, String addInfo) {
        showGrowl("growlWarn", txt, addInfo);
    }

    public static void showErrorGrowl(String txt, String addInfo) {
        showGrowl("growlError", txt, addInfo);
    }

    private static void showGrowl(String msgType, String txt, String addInfo) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (addInfo != null) {
            txt += ": " + addInfo;
        }
        context.getExternalContext().getFlash().setKeepMessages(true);
        FacesMessage facesMessage = null;
        if (msgType.equals("growlError")) {
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",  txt);
        } else if (msgType.equals("growlWarn")) {
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn",  txt);
        } else {
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",  txt);
        }
        context.addMessage(null, facesMessage);
    }

}
