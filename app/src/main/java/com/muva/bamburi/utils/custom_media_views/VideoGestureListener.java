package com.muva.bamburi.utils.custom_media_views;


/**
 * Created by theophilus
 * On 2018/03/21
 * At 10:53
 */
public interface VideoGestureListener {
    /**
     * single tap controller view
     */
    void onSingleTap();

    /**
     * Horizontal scroll to control progress of video
     *
     * @param seekForward seek to forward or not
     */
    void onHorizontalScroll(boolean seekForward);

    /**
     * vertical scroll listen
     *
     * @param percent   swipe percent
     * @param direction left or right edge for control brightness or volume
     */
    void onVerticalScroll(float percent, int direction);
}
