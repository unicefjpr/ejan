package com.example.raihan.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import org.ei.opensrp.view.activity.NativeANCSmartRegisterActivity;
import org.ei.opensrp.view.activity.NativeChildSmartRegisterActivity;
import org.ei.opensrp.view.activity.NativePNCSmartRegisterActivity;
import org.ei.opensrp.view.activity.ReportsActivity;
import org.ei.opensrp.view.activity.VideosActivity;
import org.ei.opensrp.view.controller.ANMController;

import mcare.elco.ElcoSmartRegisterActivity;
import mcare.household.HouseHoldSmartRegisterActivity;
import mcare.household.tutorial.tutorialCircleViewFlow;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static org.ei.opensrp.view.controller.ProfileNavigationController.navigateToANCProfile;
import static org.ei.opensrp.view.controller.ProfileNavigationController.navigateToChildProfile;
import static org.ei.opensrp.view.controller.ProfileNavigationController.navigateToECProfile;
import static org.ei.opensrp.view.controller.ProfileNavigationController.navigateToPNCProfile;

public class McareNavigationController extends org.ei.opensrp.view.controller.NavigationController {
    private Activity activity;
    private ANMController anmController;

    public McareNavigationController(Activity activity, ANMController anmController) {
        super(activity,anmController);
        this.activity = activity;
        this.anmController = anmController;
    }
    @Override
    public void startECSmartRegistry() {

        activity.startActivity(new Intent(activity, HouseHoldSmartRegisterActivity.class));
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this.activity);

        if(sharedPreferences.getBoolean("firstlauch",true)) {
            sharedPreferences.edit().putBoolean("firstlauch",false).commit();
            activity.startActivity(new Intent(activity, tutorialCircleViewFlow.class));
        }

    }
    @Override
    public void startFPSmartRegistry() {
        activity.startActivity(new Intent(activity, ElcoSmartRegisterActivity.class));
    }


}
