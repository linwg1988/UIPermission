package org.linwg.lib.api;

import android.view.View;

public abstract class InterceptOnClickListener implements View.OnClickListener {

    protected View.OnClickListener target;

    public InterceptOnClickListener(View.OnClickListener target) {
        this.target = target;
    }
}
