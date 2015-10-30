package org.ei.opensrp.mcare.household;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import org.ei.opensrp.domain.form.FormSubmission;
import org.ei.opensrp.mcare.R;

import org.ei.opensrp.Context;
import org.ei.opensrp.adapter.SmartRegisterPaginatedAdapter;
import org.ei.opensrp.commonregistry.CommonObjectFilterOption;
import org.ei.opensrp.commonregistry.CommonObjectSort;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.mcare.pageradapter.HouseHoldActivityPagerAdapter;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.service.ZiggyService;
import org.ei.opensrp.util.FormUtils;
import org.ei.opensrp.util.StringUtil;
import org.ei.opensrp.view.activity.FormActivity;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;
import org.ei.opensrp.view.contract.ECClient;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.controller.VillageController;
import org.ei.opensrp.view.dialog.AllClientsFilter;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.DialogOptionMapper;
import org.ei.opensrp.view.dialog.DialogOptionModel;
import org.ei.opensrp.view.dialog.EditOption;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.LocationSelectorDialogFragment;
import org.ei.opensrp.view.dialog.OpenFormOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.ei.opensrp.view.fragment.DisplayFormFragment;
import org.ei.opensrp.view.fragment.SecuredNativeSmartRegisterFragment;
import org.ei.opensrp.view.viewpager.SampleViewPager;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.EntityUtils;
import org.opensrp.api.util.LocationTree;
import org.opensrp.api.util.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import util.AsyncTask;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class HouseHoldSmartRegisterActivity extends SecuredNativeSmartRegisterActivity {

    @Bind(R.id.view_pager) SampleViewPager mPager;
    private FragmentPagerAdapter mPagerAdapter;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new HouseHoldActivityPagerAdapter(getSupportFragmentManager());
        mPager.setOffscreenPageLimit(3); // prevent the offscreen fragments from being destroyed
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                onPageChanged(position);
            }
        });
    }

    public void onPageChanged(int page){
        setRequestedOrientation(page == 0 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected DefaultOptionsProvider getDefaultOptionsProvider() {return null;}

    @Override
    protected void setupViews() {}

    @Override
    protected void onResumption(){}

    @Override
    protected NavBarOptionsProvider getNavBarOptionsProvider() {return null;}

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {return null;}

    @Override
    protected void onInitialization() {}

    public DialogOption[] getEditOptions() {
        HashMap <String,String> overridemap = new HashMap<String,String>();
        overridemap.put("existing_MWRA","MWRA");
        overridemap.put("existing_location","existing_location");
        return new DialogOption[]{

                new OpenFormOption("census enrollment form", "census_enrollment_form", formController,overridemap, OpenFormOption.ByColumnAndByDetails.byDetails)
        };
    }


    @Override
    public void startRegistration() {
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        if (entityId != null){
            String data = FormUtils.getInstance(getApplicationContext()).generateXMLInputForFormWithEntityId(entityId, formName, null);
            DisplayFormFragment displayFormFragment = getDisplayFormFragment();
            if (displayFormFragment != null) {
                displayFormFragment.setFormData(data);
                displayFormFragment.loadFormData();
                displayFormFragment.setRecordId(entityId);
            }
        }

        mPager.setCurrentItem(1, false); //Don't animate the view on orientation change the view disapears
    }

    @Override
    public void saveFormSubmission(String formSubmission, String id, String formName, Map<String, String> fieldOverrides){
        // save the form
        try{
            FormUtils formUtils = FormUtils.getInstance(getApplicationContext());
            FormSubmission submission = formUtils.generateFormSubmisionFromXMLString(id, formSubmission, formName, new HashMap<String, String>());

            org.ei.opensrp.Context context = org.ei.opensrp.Context.getInstance();
            ZiggyService ziggyService = context.ziggyService();
            ziggyService.saveForm(getParams(submission), submission.instance());

            //switch to forms list fragment
            switchToSelectFormFragment(formSubmission); // Unnecessary!! passing on data

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void switchToSelectFormFragment(final String data){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPager.setCurrentItem(0, false);
                SecuredNativeSmartRegisterFragment registerFragment = (SecuredNativeSmartRegisterFragment) findFragmentByPosition(0);
                if (registerFragment != null && data != null) {
                    registerFragment.refreshListView();
                }

                //hack reset the form
                DisplayFormFragment displayFormFragment = getDisplayFormFragment();
                if (displayFormFragment != null) {
                    displayFormFragment.setFormData(null);
                    displayFormFragment.loadFormData();
                }

                displayFormFragment.setRecordId(null);
            }
        });

    }

    public android.support.v4.app.Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = mPagerAdapter;
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + mPager.getId() + ":" + fragmentPagerAdapter.getItemId(position));
    }

    public DisplayFormFragment getDisplayFormFragment() {
        return  (DisplayFormFragment)findFragmentByPosition(1);
    }

    public void addChildToList(ArrayList<DialogOption> dialogOptionslist,Map<String,TreeNode<String, Location>> locationMap){
        for(Map.Entry<String, TreeNode<String, Location>> entry : locationMap.entrySet()) {

                    if(entry.getValue().getChildren() != null) {
                        addChildToList(dialogOptionslist,entry.getValue().getChildren());

                    }else{
                        StringUtil.humanize(entry.getValue().getLabel());
                        String name = StringUtil.humanize(entry.getValue().getLabel());
                        dialogOptionslist.add(new CommonObjectFilterOption(name.replace(" ","_"),"location_name", CommonObjectFilterOption.ByColumnAndByDetails.byDetails,name));

                    }
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPage != 0){
            switchToSelectFormFragment(null);
        }else if (currentPage == 0) {
            super.onBackPressed(); // allow back key only if we are
        }
    }
}
