package org.ei.opensrp.view.dialog;

import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.form.FieldOverrides;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.controller.FormController;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OpenFormOption implements EditOption {
    private final String name;
    private final String formName;
    private final FormController formController;
    FieldOverrides fieldOverrides = null;
    HashMap<String,String> overrideStringmap = null;
    ByColumnAndByDetails byColumnAndByDetails;

    public enum ByColumnAndByDetails{
        byColumn,byDetails,bydefault;
    }

    public OpenFormOption(String name, String formName, FormController formController,   HashMap<String,String> overrideStringmap,ByColumnAndByDetails byColumnAndByDetails) {
        this.name = name;
        this.formName = formName;
        this.formController = formController;
        this.overrideStringmap = overrideStringmap;
        this.byColumnAndByDetails = byColumnAndByDetails;
    }
    public OpenFormOption(String name, String formName, FormController formController) {
        this.name = name;
        this.formName = formName;
        this.formController = formController;
    }

    @Override
    public String name() {
        return name;
    }

    public String getFormName(){
        return formName;
    }

    @Override
    public void doEdit(SmartRegisterClient client) {

        if(overrideStringmap == null) {
            formController.startFormActivity(formName, client.entityId(), null);
        }else{
            JSONObject overridejsonobject = new JSONObject();
            try {
                for (Map.Entry<String, String> entry : overrideStringmap.entrySet()) {
                    switch (byColumnAndByDetails){
                        case byDetails:
                            overridejsonobject.put(entry.getKey() , ((CommonPersonObjectClient)client).getDetails().get(entry.getValue()));
                            break;
                        case byColumn:
                            overridejsonobject.put(entry.getKey() , ((CommonPersonObjectClient)client).getColumnmaps().get(entry.getValue()));
                            break;
                        case bydefault:
                            overridejsonobject.put(entry.getKey() ,entry.getValue());
                            break;
                    }
                }
//                overridejsonobject.put("existing_MWRA", );
            }catch (Exception e){

            }
            FieldOverrides fieldOverrides = new FieldOverrides(overridejsonobject.toString());
            formController.startFormActivity(formName, client.entityId(), fieldOverrides.getJSONString());
        }
    }
}
