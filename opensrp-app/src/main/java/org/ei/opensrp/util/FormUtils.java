package org.ei.opensrp.util;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.codehaus.jackson.JsonNode;
import org.ei.drishti.dto.form.FormSubmissionDTO;
import org.ei.opensrp.domain.SyncStatus;
import org.ei.opensrp.domain.form.FormSubmission;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Created by koros on 9/28/15.
 */
public class FormUtils {

    static FormUtils instance;
    Context mContext;


    public FormUtils(Context context){
        mContext = context;
    }

    public static FormUtils getInstance(Context ctx){
        if (instance == null){
            instance = new FormUtils(ctx);
        }
        return instance;
    }

    public FormSubmission generateFormSubmisionFromXMLString(String formData, String formName, Map<String, String> overrides) throws Exception{
        JSONObject formSubmission = XML.toJSONObject(formData);
        System.out.println(formSubmission);

        // use the form_definition.json to iterate through fields
        String formDefinitionJson = readFileFromAssetsFolder("www/form/" + formName + "/form_definition.json");
        JSONObject formDefinition = new JSONObject(formDefinitionJson);

        //String bindPath = formDefinition.getJSONObject("form").getString("bind_type");
        JSONObject fieldsDefinition = formDefinition.getJSONObject("form");
        JSONArray populatedFieldsArray = getPopulatedFieldsForArray(fieldsDefinition, formSubmission, overrides);

        // replace all the fields in the form
        formDefinition.getJSONObject("form").put("fields", populatedFieldsArray);

        //get the subforms
        JSONObject subFormDefinition = formDefinition.getJSONObject("form").getJSONArray("sub_forms").getJSONObject(0);
        //get the bind path for the sub-form helps us to locate the node that holds the data in the corresponding data json
        String bindPath = subFormDefinition.getString("default_bind_path");

        //get the actual sub-form data
        JSONArray subForms = new JSONArray();
        Object subFormDataObject = getObjectAtPath(bindPath.split("/"), formSubmission);
        if(subFormDataObject instanceof JSONObject){
            JSONObject subFormData = (JSONObject)subFormDataObject;
            JSONArray subFormFields = getFieldsArrayForSubFormDefinition(subFormDefinition);
            JSONObject subFormInstance = getFieldValuesForSubFormDefinition(subFormDefinition, subFormData, overrides);
            JSONArray subFormInstances = new JSONArray();
            subFormInstances.put(0,subFormInstance);
            subFormDefinition.put("instances", subFormInstances);
            subFormDefinition.put("fields", subFormFields);
            subForms.put(0, subFormDefinition);
        }else if (subFormDataObject instanceof JSONArray){
            JSONArray subFormData = (JSONArray)subFormDataObject;
            JSONArray subFormFields = getFieldsArrayForSubFormDefinition(subFormDefinition);
            JSONArray subFormInstances = new JSONArray();

            for (int i = 0; i < subFormData.length(); i++){
                JSONObject subFormInstance = getFieldValuesForSubFormDefinition(subFormDefinition, subFormData.getJSONObject(i), overrides);
                subFormInstances.put(i,subFormInstance);
            }
            subFormDefinition.put("instances", subFormInstances);
            subFormDefinition.put("fields", subFormFields);
            subForms.put(0, subFormDefinition);
        }

        // replace the subforms field with real data
        formDefinition.getJSONObject("form").put("sub_forms", subForms);

        String instanceId = generateRandomUUIDString();
        String entityId = retrieveIdForSubmission(formDefinition);
        String formDefinitionVersionString = formDefinition.getString("form_data_definition_version");

        String clientVersion = String.valueOf(new Date().getTime());
        String instance = formDefinition.toString();
        FormSubmission fs = new FormSubmission(instanceId, entityId, formName, instance, clientVersion, SyncStatus.PENDING, formDefinitionVersionString);
        return fs;
    }

    private String generateRandomUUIDString(){
        return UUID.randomUUID().toString();
    }

    private String retrieveIdForSubmission(JSONObject jsonObject) throws Exception{
        JSONArray fields = jsonObject.getJSONObject("form").getJSONArray("fields");
        for (int i = 0; i < fields.length(); i++){
            JSONObject field = fields.getJSONObject(i);
            if (field.has("name") && field.getString("name").equalsIgnoreCase("id")){
                return field.getString("value");
            }
        }
        return null;
    }

    public Object getObjectAtPath(String[] path, JSONObject jsonObject) throws Exception{
        JSONObject object = jsonObject;
        int i = 0;
        while (i < path.length - 1) {
            if (object.has(path[i])) {
                Object o = object.get(path[i]);
                if (o instanceof JSONObject){
                    object = object.getJSONObject(path[i]);
                }
                else if (o instanceof JSONArray){
                    object = object.getJSONArray(path[i]).getJSONObject(0);
                }
            }
            i++;
        }
        return object.has(path[i]) ? object.get(path[i]) : null;
    }

    public JSONArray getPopulatedFieldsForArray(JSONObject fieldsDefinition, JSONObject jsonObject, Map<String, String> overrides) throws  Exception{
        JSONArray fieldsArray = fieldsDefinition.getJSONArray("fields");
        String bindPath = fieldsDefinition.getString("bind_type");
        for (int i = 0; i < fieldsArray.length(); i++){
            JSONObject item = fieldsArray.getJSONObject(i);
            if (!item.has("name"))
                continue; // skip elements without name
            if (item.has("bind")){
                String pathSting = item.getString("bind");
                pathSting = pathSting.startsWith("/") ? pathSting.substring(1) : pathSting;
                String[] path = pathSting.split("/");
                String value = getValueForPath(path, jsonObject);
                item.put("value", value);
                item.remove("bind");
            }

            item.put("source", bindPath + "." +  item.getString("name"));

            //FIXME: temp hack for existing location and id fields
            if (item.has("name") && item.getString("name").equalsIgnoreCase("existing_location")){
                if (overrides.containsKey("existing_location")){
                    item.put("value", overrides.get("existing_location"));
                }else
                    item.put("value", "4ccd5a33-c462-4b53-b8c1-a1ad1c3ba0cf");
            }

            if (item.has("name") && item.getString("name").equalsIgnoreCase("id")){
                item.put("value", generateRandomUUIDString());
            }
        }
        return fieldsArray;
    }

    public JSONArray getFieldsArrayForSubFormDefinition(JSONObject fieldsDefinition) throws  Exception{
        JSONArray fieldsArray = fieldsDefinition.getJSONArray("fields");
        String bindPath = fieldsDefinition.getString("bind_type");

        JSONArray subFormFieldsArray = new JSONArray();

        for (int i = 0; i < fieldsArray.length(); i++){
            JSONObject field = new JSONObject();
            JSONObject item = fieldsArray.getJSONObject(i);
            if (!item.has("name"))
                continue; // skip elements without name
            field.put("name", item.getString("name"));
            field.put("source", bindPath + "." +  item.getString("name"));
            subFormFieldsArray.put(i, field);
        }

        return subFormFieldsArray;
    }

    public JSONObject getFieldValuesForSubFormDefinition(JSONObject fieldsDefinition, JSONObject jsonObject, Map<String, String> overrides) throws  Exception{
        JSONArray fieldsArray = fieldsDefinition.getJSONArray("fields");

        JSONObject fieldsValues = new JSONObject();

        for (int i = 0; i < fieldsArray.length(); i++){
            JSONObject item = fieldsArray.getJSONObject(i);
            if (!item.has("name"))
                continue; // skip elements without name
            if (item.has("bind")){
                String pathSting = item.getString("bind");
                pathSting = pathSting.startsWith("/") ? pathSting.substring(1) : pathSting;
                String[] path = pathSting.split("/");

                //check if we need to override this val
                if (overrides.containsKey(item.getString("name"))){
                    fieldsValues.put(item.getString("name"), overrides.get(item.getString("name")));
                }
                else{
                    String value = getValueForPath(path, jsonObject);
                    fieldsValues.put(item.getString("name"), value);
                }
            }

            //TODO: generate the id for the record
            if (item.has("name") && item.getString("name").equalsIgnoreCase("id")){
                fieldsValues.put(item.getString("name"), generateRandomUUIDString());
            }
        }
        return fieldsValues;
    }


    public String getValueForPath(String[] path, JSONObject jsonObject) throws Exception{
        JSONObject object = jsonObject;
        String value = null;
        int i = 0;
        while (i < path.length - 1) {
            if (object.has(path[i])) {
                Object o = object.get(path[i]);
                if (o instanceof JSONObject){
                    object = object.getJSONObject(path[i]);
                }
                else if (o instanceof JSONArray){
                    object = object.getJSONArray(path[i]).getJSONObject(0);
                }
            }
            i++;
        }
        if(object.has(path[i]) && object.get(path[i]) instanceof JSONObject && ((JSONObject) object.get(path[i])).has("content")){
            value = ((JSONObject) object.get(path[i])).getString("content");
        }
        else if(object.has(path[i]) && !(object.get(path[i]) instanceof JSONObject)){
            value = object.has(path[i]) ? object.get(path[i]).toString() : null;
        }
        return value;
    }

    private String readFileFromAssetsFolder(String fileName){
        String fileContents = null;
        try {
            InputStream is = mContext.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            fileContents = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        //Log.d("File", fileContents);
        return fileContents;
    }


}
