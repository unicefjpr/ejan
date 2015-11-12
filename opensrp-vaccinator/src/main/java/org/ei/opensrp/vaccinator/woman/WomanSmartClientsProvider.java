package org.ei.opensrp.vaccinator.woman;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.service.AlertService;
import org.ei.opensrp.vaccinator.R;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.ei.opensrp.view.viewHolder.OnClickFormLauncher;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by Ahmed on 19-Oct-15.
 */
public class WomanSmartClientsProvider implements SmartRegisterClientsProvider {



    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    AlertService alertService;
    private final int txtColorBlack;
    private final AbsListView.LayoutParams clientViewLayoutParams;

    protected CommonPersonObjectController controller;

    public WomanSmartClientsProvider(Context context,
                                     View.OnClickListener onClickListener,
                                     CommonPersonObjectController controller, AlertService alertService) {
        this.onClickListener = onClickListener;
        this.controller = controller;
        this.context = context;
        this.alertService = alertService;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT,
                (int) context.getResources().getDimension(org.ei.opensrp.R.dimen.list_item_height));
        txtColorBlack = context.getResources().getColor(org.ei.opensrp.R.color.text_black);
    }
    @Override
    public View getView(SmartRegisterClient client, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = (ViewGroup) inflater().inflate(R.layout.smart_register_woman_client, null);
            viewHolder = new ViewHolder();
            viewHolder.profilelayout =  (LinearLayout)convertView.findViewById(R.id.woman_profile_info_layout);
            viewHolder.womanId=(TextView)convertView.findViewById(R.id.woman_id);
            viewHolder.womanName=(TextView)convertView.findViewById(R.id.woman_name);
            viewHolder.fatherName=(TextView)convertView.findViewById(R.id.woman_fh_name);
            viewHolder.womanDOB=(TextView)convertView.findViewById(R.id.woman_dob);
            viewHolder.profilepic=(ImageView)convertView.findViewById(R.id.woman_profilepic);
            viewHolder.last_visit_date=(TextView)convertView.findViewById(R.id.woman_last_visit_date);
            viewHolder.last_vaccine=(TextView)convertView.findViewById(R.id.woman_last_vaccine);
            viewHolder.next_visit_date=(TextView)convertView.findViewById(R.id.woman_next_visit);
            //viewHolder.profilepic.setImageResource();
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();

        }

        ViewGroup itemView = viewGroup;

        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;

        if(pc.getDetails().get("profilepic")!=null){
            if( pc.getDetails().get("gender").equalsIgnoreCase("female")) {
                viewHolder.profilepic.setImageResource(org.ei.opensrp.R.drawable.child_girl_infant);
                // HouseHoldDetailActivity.setImagetoHolder((Activity) context, pc.getDetails().get("profilepic"), profilepic, R.drawable.child_boy_infant);
            }else{
                viewHolder.profilepic.setImageResource(org.ei.opensrp.R.drawable.child_boy_infant);
                // HouseHoldDetailActivity.setImagetoHolder((Activity) context, pc.getDetails().get("profilepic"), profilepic, R.drawable.child_girl_infant);

            }
        }

        viewHolder.womanId.setText(pc.getDetails().get("program_client_id") != null ? pc.getDetails().get("program_client_id") : "");
        viewHolder.womanName.setText(pc.getDetails().get("first_name") != null ? pc.getDetails().get("first_name") : "");
        viewHolder.fatherName.setText(pc.getDetails().get("father_name") != null ? pc.getDetails().get("father_name") : "");
        viewHolder.womanDOB.setText(pc.getDetails().get("client_dob_confirm") != null ? pc.getDetails().get("chid_dob_confirm") : "");
        viewHolder.edd.setText(pc.getDetails().get("final_edd")!=null ?pc.getDetails().get("final_edd"):"N/A");
        viewHolder. profilepic.setOnClickListener(onClickListener);
        viewHolder. profilepic.setTag(client);

        ImageView lastVisit = (ImageView)itemView.findViewById(R.id.woman_last_visit_date);
        ImageView nextVisit = (ImageView)itemView.findViewById(R.id.woman_next_visit);

        // lastVisit.setOnClickListener(onClickListener);
        //   lastVisit.setTag(client);
        viewHolder.next_visit_date.setOnClickListener(onClickListener);
        viewHolder.next_visit_date.setTag(client);
        convertView.setLayoutParams(clientViewLayoutParams);
        return convertView;
    }

    @Override
    public SmartRegisterClients getClients() {
        return controller.getClients();
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        return getClients().applyFilter(villageFilter, serviceModeOption, searchFilter, sortOption);
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {

    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    public LayoutInflater inflater() {
        return inflater;
    }

    class ViewHolder {

        TextView womanId ;
        TextView womanName ;
        TextView fatherName;
        TextView edd;
        TextView womanDOB;
        TextView last_vaccine;
        TextView last_visit_date;
        TextView next_visit_date;
        ImageButton follow_up;
        LinearLayout profilelayout;
        ImageView profilepic;
        FrameLayout due_date_holder;
    }
}
