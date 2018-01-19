package com.weidi.customadapter;

import com.weidi.customadapter.interfaces.IMultiItemViewType;

/**
 * Convenient class for RecyclerView.Adapter.
 * <p>
 * Created by Cheney on 16/4/5.
 */
public abstract class SimpleMulItemViewType<T> implements IMultiItemViewType<T> {

    @Override
    public int getViewTypeCount() {
        return 1;
    }

}
