package org.ei.opensrp.service;

import android.content.res.AssetManager;
import android.os.Environment;

import org.apache.commons.io.IOUtils;
import org.ei.opensrp.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Dimas Ciputra on 3/22/15.
 */
public class FormPathService {

    public static String sdcardPath = Environment.getExternalStorageDirectory().getPath() +"/Download/OpenSRP/form/";
    public static String sdcardPathDownload = Environment.getExternalStorageDirectory().getPath() + "/Download/OpenSRP/zip/";
    public static String appPath = "www/form/";
    private AssetManager assetManager;

    public FormPathService(Context context) {
        this.assetManager = context.applicationContext().getAssets();
    }

    public FormPathService(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public String getForms(String file, String encoding) throws IOException {
        File formFile = new File(sdcardPath + file);

        if(formFile.exists()) {
            return IOUtils.toString(new FileInputStream(formFile), encoding);
        }

        return IOUtils.toString(this.assetManager.open(appPath + file), encoding);
    }

}