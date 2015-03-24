package org.ei.drishti.commonregistry;

import org.apache.commons.lang3.StringUtils;
import org.ei.drishti.Context;
import org.ei.drishti.R;
import org.ei.drishti.view.contract.SmartRegisterClient;
import org.ei.drishti.view.dialog.FilterOption;

public class CommonSearchOption implements FilterOption {
    private final String criteria;

    public CommonSearchOption(String criteria) {
        this.criteria = criteria;
    }

    @Override
    public String name() {
        return Context.getInstance().getStringResource(R.string.str_ec_search_hint);
    }

    @Override
    public boolean filter(SmartRegisterClient client) {
        return StringUtils.isBlank(criteria) || client.satisfiesFilter(criteria);
    }
}
