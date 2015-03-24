package org.ei.drishti.commonregistry;

import org.apache.commons.lang3.StringUtils;
import org.ei.drishti.Context;
import org.ei.drishti.R;
import org.ei.drishti.view.contract.SmartRegisterClient;
import org.ei.drishti.view.dialog.FilterOption;

import java.util.Iterator;
import java.util.Map;

public class CommonSearchOption implements FilterOption {
    private final String criteria;

    public CommonSearchOption(String criteria) {
        this.criteria = criteria;
    }

    @Override
    public String name() {
        return Context.getInstance().getStringResource(R.string.common_register_search_hint);
    }

    @Override
    public boolean filter(SmartRegisterClient client) {
        CommonPersonObjectClient cpoc = (CommonPersonObjectClient) client;
        boolean isthere = false;
        if (cpoc.getCaseId().contains(criteria)) {
            isthere = true;
        } else {
            for (int i = 0; i < cpoc.getColumnmaps().size(); i++) {
                Iterator it = cpoc.getColumnmaps().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (criteria.contains("" + pair.getValue())) {
                        isthere = true;
                        break;
                    }
                    it.remove(); // avoids a ConcurrentModificationException
                }

            }
            if (!isthere) {
                for (int i = 0; i < cpoc.getDetails().size(); i++) {
                    Iterator it = cpoc.getDetails().entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        if (criteria.contains("" + pair.getValue())) {
                            isthere = true;
                            break;
                        }
                        it.remove(); // avoids a ConcurrentModificationException
                    }
                }
            }

        }
        return isthere;
    }
}
