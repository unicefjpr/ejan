package org.ei.drishti.dhakasprintregistry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;

import org.ei.drishti.R;
import org.ei.drishti.commonregistry.CommonPersonObjectClient;
import org.ei.drishti.commonregistry.CommonPersonObjectController;
import org.ei.drishti.provider.SmartRegisterClientsProvider;
import org.ei.drishti.view.contract.SmartRegisterClient;
import org.ei.drishti.view.contract.SmartRegisterClients;
import org.ei.drishti.view.dialog.FilterOption;
import org.ei.drishti.view.dialog.ServiceModeOption;
import org.ei.drishti.view.dialog.SortOption;
import org.ei.drishti.view.viewHolder.OnClickFormLauncher;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by user on 2/12/15.
 */
public class DhakaClientsProvider implements SmartRegisterClientsProvider {

    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;

    private final int txtColorBlack;
    private final AbsListView.LayoutParams clientViewLayoutParams;

    protected CommonPersonObjectController controller;

    public DhakaClientsProvider(Context context,
                                View.OnClickListener onClickListener,
                                CommonPersonObjectController controller) {
        this.onClickListener = onClickListener;
        this.controller = controller;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT,
                (int) context.getResources().getDimension(R.dimen.list_item_height));
        txtColorBlack = context.getResources().getColor(R.color.text_black);
    }

    @Override
    public View getView(SmartRegisterClient smartRegisterClient, View convertView, ViewGroup viewGroup) {
        ViewGroup itemView;

        itemView = (ViewGroup) inflater().inflate(R.layout.dhakasprinthhregister, null);
        TextView id = (TextView)itemView.findViewById(R.id.hhid);
        TextView name = (TextView)itemView.findViewById(R.id.woman_name);
        TextView husbandname = (TextView)itemView.findViewById(R.id.husbandname);
        TextView hhname = (TextView)itemView.findViewById(R.id.hhname);

        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;

        id.setText(pc.getDetails().get("EC_HH_ID")!=null?pc.getDetails().get("EC_HH_ID"):"");
        name.setText(pc.getColumnmaps().get("woman_name")!=null?pc.getColumnmaps().get("woman_name"):"");
        husbandname.setText(pc.getDetails().get("husband_name")!=null?pc.getDetails().get("husband_name"):"");
        hhname.setText(pc.getDetails().get("EC_HH_Name")!=null?pc.getDetails().get("EC_HH_Name"):"");

        itemView.setLayoutParams(clientViewLayoutParams);
        return itemView;
    }

    @Override
    public SmartRegisterClients getClients() {
        return controller.getClients();
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption,
                                              FilterOption searchFilter, SortOption sortOption) {
        return getClients().applyFilter(villageFilter, serviceModeOption, searchFilter, sortOption);
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {
        // do nothing.
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    public LayoutInflater inflater() {
        return inflater;
    }
}
