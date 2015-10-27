package org.ei.opensrp.indonesia.view.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

import org.ei.opensrp.adapter.SmartRegisterPaginatedAdapter;
import org.ei.opensrp.domain.form.FieldOverrides;
import org.ei.opensrp.indonesia.AllConstantsINA;
import org.ei.opensrp.indonesia.Context;
import org.ei.opensrp.indonesia.R;
import org.ei.opensrp.indonesia.lib.FlurryFacade;
import org.ei.opensrp.indonesia.provider.KIClientsProvider;
import org.ei.opensrp.indonesia.service.formSubmissionHandler.KIRegistrationHandler;
import org.ei.opensrp.indonesia.view.contract.KartuIbuClient;
import org.ei.opensrp.indonesia.view.controller.BidanVillageController;
import org.ei.opensrp.indonesia.view.controller.KartuIbuRegisterController;
import org.ei.opensrp.indonesia.view.dialog.AllHighRiskSort;
import org.ei.opensrp.indonesia.view.dialog.AllKartuIbuServiceMode;
import org.ei.opensrp.indonesia.view.dialog.EstimatedDateOfDeliverySortKI;
import org.ei.opensrp.indonesia.view.dialog.NoIbuSort;
import org.ei.opensrp.indonesia.view.dialog.ReverseNameSort;
import org.ei.opensrp.indonesia.view.pageradapter.KISmartRegisterActivityPagerAdapter;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.util.FormUtils;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.dialog.AllClientsFilter;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.DialogOptionMapper;
import org.ei.opensrp.view.dialog.DialogOptionModel;
import org.ei.opensrp.view.dialog.EditOption;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.NameSort;
import org.ei.opensrp.view.dialog.OpenFormOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.ei.opensrp.view.fragment.DisplayFormFragment;
import org.ei.opensrp.view.fragment.SecuredNativeSmartRegisterFragment;
import org.ei.opensrp.view.viewpager.SampleViewPager;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.toArray;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.ANAK_BAYI_REGISTRATION;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_ANC_REGISTRATION;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_CLOSE;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_EDIT;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_REGISTRATION;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KOHORT_KB_PELAYANAN;

/**
 * Created by Dimas Ciputra on 2/18/15.
 */
public class NativeKISmartRegisterActivity extends BidanSecuredNativeSmartRegisterActivity {

    private SmartRegisterClientsProvider clientProvider = null;
    private KartuIbuRegisterController controller;
    private DialogOptionMapper dialogOptionMapper;
    private BidanVillageController villageController;

    private final ClientActionHandler clientActionHandler = new ClientActionHandler();

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
        mPagerAdapter = new KISmartRegisterActivityPagerAdapter(getSupportFragmentManager(), getEditOptions());
        mPager.setOffscreenPageLimit(getEditOptions().length);
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
    protected SmartRegisterPaginatedAdapter adapter() {
        return new SmartRegisterPaginatedAdapter(clientsProvider());
    }

    private DialogOption[] getEditOptions() {
        return new DialogOption[]{
                new OpenFormOption(getString(R.string.str_register_kb_form), KOHORT_KB_PELAYANAN,
                        formController),
                new OpenFormOption(getString(R.string.str_register_anc_form), KARTU_IBU_ANC_REGISTRATION, formController),
                new OpenFormOption(getString(R.string.str_register_anak_form), ANAK_BAYI_REGISTRATION, formController),
                new OpenFormOption(getString(R.string.str_edit_ki_form), KARTU_IBU_EDIT, formController),
                new OpenFormOption(getString(R.string.str_close_ki_form),KARTU_IBU_CLOSE, formController),
        };
    }

    @Override
    protected DefaultOptionsProvider getDefaultOptionsProvider() {
        return new DefaultOptionsProvider() {

            @Override
            public ServiceModeOption serviceMode() {
                return new AllKartuIbuServiceMode(clientsProvider());
            }

            @Override
            public FilterOption villageFilter() {
                return new AllClientsFilter();
            }

            @Override
            public SortOption sortOption() {
                return new NameSort();
            }

            @Override
            public String nameInShortFormForTitle() {
                return getResources().getString(R.string.ki_register_title_in_short);
            }
        };
    }

    @Override
    public void setupViews() {
        super.setupViews();
    }

    @Override
    protected NavBarOptionsProvider getNavBarOptionsProvider() {

        return new NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
                Iterable<? extends DialogOption> villageFilterOptions =
                        dialogOptionMapper.mapToVillageFilterOptions(villageController.getVillagesIndonesia());
                return toArray(concat(DEFAULT_FILTER_OPTIONS, villageFilterOptions), DialogOption.class);
            }

            @Override
            public DialogOption[] serviceModeOptions() {
                return new DialogOption[]{};
            }

            @Override
            public DialogOption[] sortingOptions() {
                return new DialogOption[]{new NameSort(),
                        new ReverseNameSort(), new NoIbuSort(),
                        new EstimatedDateOfDeliverySortKI(), new AllHighRiskSort()};
            }

            @Override
            public String searchHint() {
                return getString(R.string.str_ki_search_hint);
            }
        };
    }

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        if (clientProvider == null) {
            clientProvider = new KIClientsProvider(
                    this, clientActionHandler, controller);
        }
        return clientProvider;
    }

    @Override
    protected void onInitialization() {
        controller = new KartuIbuRegisterController(((Context)context).allKartuIbus(),
                context.listCache(),((Context)context).kiClientsCache(),((Context)context).allKohort());
        villageController = new BidanVillageController(context.villagesCache(), ((Context)context).allKartuIbus());
        dialogOptionMapper = new DialogOptionMapper();
        context.formSubmissionRouter().getHandlerMap()
                .put(AllConstantsINA.FormNames.KARTU_IBU_REGISTRATION,
                        new KIRegistrationHandler(((Context)context).kartuIbuService()));
    }

    @Override
    protected void startRegistration() {
        String uniqueIdJson = ((Context)context).uniqueIdController().getUniqueIdJson();
        if(uniqueIdJson == null || uniqueIdJson.isEmpty()) {
            Toast.makeText(this, "No Unique Id", Toast.LENGTH_SHORT).show();
            return;
        }
        FieldOverrides fieldOverrides = new FieldOverrides(uniqueIdJson);
        startFormActivity(KARTU_IBU_REGISTRATION, null, fieldOverrides.getJSONString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryFacade.logEvent("kohort_ibu_dashboard");
    }

    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.profile_info_layout_ki:
                    showProfileView((KartuIbuClient) view.getTag());
                    break;
                case R.id.btn_edit:
                    showFragmentDialog(new EditDialogOptionModel(), view.getTag());
                    break;
            }
        }

        private void showProfileView(KartuIbuClient kartuIbuClient) {
            navigationControllerINA.startMotherDetail(kartuIbuClient.entityId());
        }
    }

    private class EditDialogOptionModel implements DialogOptionModel {

        @Override
        public DialogOption[] getDialogOptions() {
            return getEditOptions();
        }

        @Override
        public void onDialogOptionSelection(DialogOption option, Object tag) {
            SmartRegisterClient client = (SmartRegisterClient) tag;

            if(option.name().equalsIgnoreCase(getString(R.string.str_register_anc_form)) ) {
                if(controller.isMotherInANCorPNC(client.entityId())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.mother_already_registered), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            onShowDialogOptionSelection((EditOption) option, client, controller.getRandomNameChars(client));
        }
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        int formIndex = FormUtils.getIndexForFormName(formName, getEditOptions()) + 1; // add the offset
        if (entityId != null){
            String data = FormUtils.getInstance(getApplicationContext()).generateXMLInputForFormWithEntityId(entityId, formName, null);
            DisplayFormFragment displayFormFragment = getDisplayFormFragmentAtIndex(formIndex);
            if (displayFormFragment != null) {
                displayFormFragment.setFormData(data);
                displayFormFragment.loadFormData();
                displayFormFragment.setRecordId(entityId);
            }
        }

        mPager.setCurrentItem(formIndex, false); //Don't animate the view on orientation change the view disapears
    }

    @Override
    public void saveFormSubmission(String formSubmission, String id, String formName, Map<String, String> fieldOverrides){
        // save the form
        try{
//            FormUtils formUtils = FormUtils.getInstance(getApplicationContext());
//            FormSubmission submission = formUtils.generateFormSubmisionFromXMLString(id, formSubmission, formName, new HashMap<String, String>());
//
//            org.ei.opensrp.Context context = org.ei.opensrp.Context.getInstance();
//            ZiggyService ziggyService = context.ziggyService();
//            ziggyService.saveForm(getParams(submission), submission.instance());

            //switch to forms list fragment
            switchToBaseFragment(formSubmission); // Unnecessary!! passing on data

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void switchToBaseFragment(final String data){
        final int prevPageIndex = currentPage;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPager.setCurrentItem(0, false);
                SecuredNativeSmartRegisterFragment registerFragment = (SecuredNativeSmartRegisterFragment) findFragmentByPosition(0);
                if (registerFragment != null && data != null) {
                    registerFragment.refreshListView();
                }

                //hack reset the form
                DisplayFormFragment displayFormFragment = getDisplayFormFragmentAtIndex(prevPageIndex);
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

    public DisplayFormFragment getDisplayFormFragmentAtIndex(int index) {
        return  (DisplayFormFragment)findFragmentByPosition(index);
    }

    @Override
    public void onBackPressed() {
        if (currentPage != 0){
            switchToBaseFragment(null);
        }else if (currentPage == 0) {
            super.onBackPressed(); // allow back key only if we are
        }
    }
}
