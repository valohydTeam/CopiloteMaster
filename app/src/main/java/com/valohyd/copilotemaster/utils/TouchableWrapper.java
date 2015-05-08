package com.valohyd.copilotemaster.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.valohyd.copilotemaster.MainActivity;

public  class TouchableWrapper extends FrameLayout {

public TouchableWrapper(Context context) {
    super(context);
    // TODO Auto-generated constructor stub
}

@Override
 public boolean dispatchTouchEvent(MotionEvent ev) {
  switch (ev.getAction()) {
    case MotionEvent.ACTION_DOWN:
        MainActivity.mMapIsTouched = true;
            break;
    case MotionEvent.ACTION_UP:
            MainActivity.mMapIsTouched = false;
            break;
    }

    return super.dispatchTouchEvent(ev);
  }
 }