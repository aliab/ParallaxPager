package com.prolificinteractive.parallaxpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Arrays;
import java.util.List;

public class ParallaxFactory {

    private final List<OnViewCreatedListener> otherListeners;

    public ParallaxFactory(OnViewCreatedListener... others) {
        otherListeners = Arrays.asList(others);
    }

    /**
     * Handle the created view
     *
     * @param view    nullable.
     * @param context shouldn't be null.
     * @param attrs   shouldn't be null.
     * @return null if null is passed in.
     */

    public View onViewCreated(View view, Context context, AttributeSet attrs) {
        if (view == null) {
            return null;
        }

        view = onViewCreatedInternal(view, context, attrs);
        for (OnViewCreatedListener listener : otherListeners) {
            if (listener != null) {
                view = listener.onViewCreated(view, context, attrs);
            }
        }
        return view;
    }

    private View onViewCreatedInternal(View view, final Context context, AttributeSet attrs) {

        int[] attrIds =
                {R.attr.a_in_left, R.attr.a_in_right, R.attr.a_out_left, R.attr.a_out_right,
                        R.attr.x_in_left, R.attr.x_in_right, R.attr.x_out_left, R.attr.x_out_right,
                        R.attr.y_in_left, R.attr.y_in_right, R.attr.y_out_left, R.attr.y_out_right,
                        R.attr.override_visibility,
                        R.attr.p_name};

        TypedArray a = context.obtainStyledAttributes(attrs, attrIds);

        if (a != null) {
            if (a.length() > 0) {
                ParallaxViewTag tag = new ParallaxViewTag();
                tag.alphaInLeft = a.getFloat(0, 0f);
                tag.alphaInRight = a.getFloat(1, 0f);
                tag.alphaOutLeft = a.getFloat(2, 0f);
                tag.alphaOutRight = a.getFloat(3, 0f);
                tag.xInLeft = a.getFloat(4, 0f);
                tag.xInRight = a.getFloat(5, 0f);
                tag.xOutLeft = a.getFloat(6, 0f);
                tag.xOutRight = a.getFloat(7, 0f);
                tag.yInLeft = a.getFloat(8, 0f);
                tag.yInRight = a.getFloat(9, 0f);
                tag.yOutLeft = a.getFloat(10, 0f);
                tag.yOutRight = a.getFloat(11, 0f);
                tag.overrideVisibility = a.getBoolean(12, false);
                float f = a.getFloat(13,0);
                String name = "";
                if (f != 1)
                    name = "empty";
                else
                    Log.e("view.Name",name);
                tag.name = name;
                view.setTag(R.id.parallax_view_tag, tag);
            }
            a.recycle();
        }

        return view;
    }
}
