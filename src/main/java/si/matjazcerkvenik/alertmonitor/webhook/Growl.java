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
