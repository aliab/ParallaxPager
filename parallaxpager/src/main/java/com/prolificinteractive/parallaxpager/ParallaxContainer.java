package com.prolificinteractive.parallaxpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

@SuppressWarnings("UnusedDeclaration")
public class ParallaxContainer extends FrameLayout implements ViewPager.OnPageChangeListener {

    private List<View> parallaxViews = new ArrayList<>();
    private ViewPager viewPager;
    private int pageCount = 0;
    private int containerWidth;
    private int containerHeight;
    private boolean isLooping = false;
    private final ParallaxPagerAdapter adapter;
    private ViewPager.OnPageChangeListener pageChangeListener;
    private CirclePageIndicator cpi;

    public int getContainerWidth() {
        return containerWidth;
    }

    public int getContainerHeight() {
        return containerHeight;
    }

    /*mhp*/
    private float mPreviousPositionOffset;
    private boolean mViewPagerScrollingLeft = true;
    private int mPreviousPosition;

    public ParallaxContainer(Context context) {
        this(context, null);
    }

    public ParallaxContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParallaxContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        adapter = new ParallaxPagerAdapter(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        containerWidth = getMeasuredWidth();
        containerHeight = getMeasuredHeight();
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
        updateAdapterCount();
    }

    private void updateAdapterCount() {
        adapter.setCount(isLooping ? Integer.MAX_VALUE : pageCount);
    }

    public void setupChildren(int... childIds) {
        setupChildren(LayoutInflater.from(getContext()), childIds);
    }

    public void setupChildren(LayoutInflater inflater, int... childIds) {
        if (getChildCount() > 0) {
            throw new RuntimeException(
                    "setupChildren should only be called once when ParallaxContainer is empty");
        }

        if (childIds.length == 1) {
            int id = childIds[0];
            childIds = new int[2];
            childIds[0] = id;
            childIds[1] = id;
        }

        for (int childId : childIds) {
            inflater.inflate(childId, this);
        }

        // hold pageCount because it will change after we add viewpager
        pageCount = getChildCount();
        for (int i = 0; i < pageCount; i++) {
            View view = getChildAt(i);
            addParallaxView(view, i);
        }

        updateAdapterCount();

        // make view pager with same attributes as container
        viewPager = new ViewPager(getContext());
        viewPager.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        viewPager.setPageMargin(pageMargin);
        viewPager.setId(R.id.parallax_pager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);

        cpi.setViewPager(viewPager);
        cpi.setOnPageChangeListener(this);
        //attachOnPageChangeListener(viewPager, this);

        addView(viewPager, 0);
    }

    /**
     * Sets the {@link ViewPager.OnPageChangeListener} to the embedded {@link ViewPager}
     * created by the container.
     * <p/>
     * This method can be overridden to add an page indicator to the parallax view. If
     * this method is overriden, make sure that the listener methods are called on this
     * class as well.
     */
    @Deprecated
    protected void attachOnPageChangeListener(ViewPager viewPager,
                                              ViewPager.OnPageChangeListener listener) {
        viewPager.setOnPageChangeListener(listener);
    }

    // attach attributes in tag
    private void addParallaxView(View view, int pageIndex) {
        if (view instanceof ViewGroup) {
            // recurse children
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0, childCount = viewGroup.getChildCount(); i < childCount; i++) {
                addParallaxView(viewGroup.getChildAt(i), pageIndex);
            }
        }

        ParallaxViewTag tag = (ParallaxViewTag) view.getTag(R.id.parallax_view_tag);
        if (tag != null) {
            // only track view if it has a parallax tag
            tag.index = pageIndex;
            parallaxViews.add(view);
        }
    }

    /**
     * <b>NOTE:</b> this is exposed for use with existing code which requires a {@linkplain ViewPager} instance.
     * Please make sure that if you call methods like {@linkplain ViewPager#setAdapter(android.support.v4.view.PagerAdapter) setAdapter()}
     * or {@linkplain ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener) setOnPageChangeListener()}
     * on the instance returned, that you do so with forethought and good reason.
     *
     * @return the internal ViewPager, null before {@linkplain #setupChildren(int...) setupChildren()} is called
     */
    public ViewPager getViewPager() {
        return viewPager;
    }

    /**
     * Set a listener to recieve page change events
     *
     * @param pageChangeListener the listener, or null to clear
     * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }

    @Override
    public void onPageScrolled(int pageIndex, float offset, int offsetPixels) {
        /*mhp*/

        //Log.e("containerWidth",containerWidth+"");
        Log.e("offset", offsetPixels + "");
        if ((offset > mPreviousPositionOffset && pageIndex == mPreviousPosition) ||
                (offset < mPreviousPositionOffset && pageIndex > mPreviousPosition)) {
            mViewPagerScrollingLeft = true;
        } else if (offset < mPreviousPositionOffset) {
            mViewPagerScrollingLeft = false;
        }
        mPreviousPositionOffset = offset;
        mPreviousPosition = pageIndex;
        /*mhp-end*/

        Log.e("isLeft", mViewPagerScrollingLeft + "");
        if (pageCount > 0) {
            pageIndex = pageIndex % pageCount;
        }
        Log.e("page", pageIndex + "");

        ParallaxViewTag tag;
        for (View view : parallaxViews) {
            setupViewAnimationsSwip(pageIndex, offset, offsetPixels, view, mViewPagerScrollingLeft);
        }

        if (pageChangeListener != null) {
            pageChangeListener.onPageScrolled(pageIndex, offset, offsetPixels);
        }
    }

    private void setupViewAnimationsSwip(int pageIndex, float offset, int offsetPixels, View view, boolean mScrollingLeft) {
        ParallaxViewTag tag;
        tag = (ParallaxViewTag) view.getTag(R.id.parallax_view_tag);
        if (tag == null) {
            return;
        }

        if (tag.name.equalsIgnoreCase("p_3")) {
            Log.e("view.Name", tag.name);
        }

        if ((pageIndex == tag.index - 1
                || (isLooping && (pageIndex == tag.index - 1 + pageCount)))
                && containerWidth != 0) {


            //Log.e("tag.index",tag.index+"");
            //Log.e("tag.view.width",view.getWidth()+"");

            if (!tag.overrideVisibility) {
                // make visible
                view.setVisibility(VISIBLE);
            }

            // slide in from right
            //Log.e("TX",(containerWidth - offsetPixels) * (mScrollingLeft?tag.xInLeft:tag.xInRight)+"");
            float tx = (containerWidth - offsetPixels) * (mScrollingLeft ? tag.xInLeft : tag.xInRight);
            if (tx < 0)
                tx = 0;
            view.setTranslationX(tx);

            // slide in from top
            //view.setTranslationY(0 - (containerWidth - offsetPixels) * (mScrollingLeft ? tag.yInLeft : tag.yInRight));
            view.setTranslationY(0 - (containerHeight * offset / 7) * (mScrollingLeft ? tag.yInLeft : tag.yInRight));

            // fade in
            float alpha = (mScrollingLeft ? tag.alphaInLeft : tag.alphaInRight);
            if (alpha == -1000)
                view.setAlpha(0);
            else
                view.setAlpha(1.0f - (containerWidth - offsetPixels) * (mScrollingLeft ? tag.alphaInLeft : tag.alphaInRight) / containerWidth);
        } else if (pageIndex == tag.index) {
            if (!tag.overrideVisibility) {
                // make visible
                view.setVisibility(VISIBLE);
            }
            //Log.e("tag.index",tag.index+"");

            // slide out to left
            //Log.e("TX_2",0 - offsetPixels * (mScrollingLeft?tag.xOutLeft:tag.xOutRight)+"");
            view.setTranslationX(0 - offsetPixels * (mScrollingLeft ? tag.xOutLeft : tag.xOutRight));

            // slide out to top
            //view.setTranslationY(0 - offsetPixels * (mScrollingLeft ? tag.yOutLeft : tag.yOutRight));
            if(pageIndex == 3 && offset == 0){
                view.setTranslationY(0 - (containerHeight * 1 / 7) * (mScrollingLeft ? tag.yOutLeft : tag.yOutRight));
            }else {
                view.setTranslationY(0 - (containerHeight * offset / 7) * (mScrollingLeft ? tag.yOutLeft : tag.yOutRight));
            }

            // fade out
            float alpha = (mScrollingLeft ? tag.alphaInLeft : tag.alphaInRight);
            if (alpha == -1000)
                view.setAlpha(1.0f - (containerWidth - offsetPixels) * (mScrollingLeft ? tag.alphaOutLeft : tag.alphaOutLeft) / containerWidth);
            else
                view.setAlpha(1.0f - offsetPixels * (mScrollingLeft ? tag.alphaOutLeft : tag.alphaOutRight) / containerWidth);
        } else {
            //Log.e("tag.index",tag.index+"");
            if (!tag.overrideVisibility) {
                view.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (pageChangeListener != null) {
            pageChangeListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
        if (pageChangeListener != null) {
            pageChangeListener.onPageScrollStateChanged(i);
        }
    }

    public void setCpi(CirclePageIndicator cpi) {
        this.cpi = cpi;

    }
}
