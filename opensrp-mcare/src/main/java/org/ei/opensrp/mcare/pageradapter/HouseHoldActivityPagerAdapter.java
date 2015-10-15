package org.ei.opensrp.mcare.pageradapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.ei.opensrp.mcare.fragment.HouseHoldSmartRegisterFragment;
import org.ei.opensrp.view.fragment.DisplayFormFragment;

/**
 * Created by koros on 10/12/15.
 */
public class HouseHoldActivityPagerAdapter extends FragmentPagerAdapter{
    public static final String ARG_PAGE = "page";

    public HouseHoldActivityPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new HouseHoldSmartRegisterFragment();
                break;
            case 1:
                DisplayFormFragment f = new DisplayFormFragment();
                f.setFormName("new_household_registration");
                fragment = f;
                break;
            default:
                break;
        }

        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
