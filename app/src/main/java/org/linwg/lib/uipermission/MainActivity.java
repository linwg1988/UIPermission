package org.linwg.lib.uipermission;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.linwg.lib.PerRelation;
import org.linwg.lib.annotation.LUIPermission;
import org.linwg.lib.api.UIPermissions;

import java.util.List;

public class MainActivity extends BaseActivity {
    @LUIPermission(per = {"A", "C"}, relation = PerRelation.OR, actingOnClick = true, toastHint = "fasfasfa")
    FloatingActionButton fab;
    @LUIPermission(per = {"A", "B"}, actingOnClick = true)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("onClick","onCreate="+System.currentTimeMillis());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final EditText etPer = findViewById(R.id.etPer);
        textView = findViewById(R.id.tvText);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> permissionList = UIPermissions.getPermissionList();
                String s = etPer.getText().toString();
                if (!permissionList.contains(s)) {
                    permissionList.add(s);
                }
                UIPermissions.setPermissionList(permissionList);
            }
        });
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> permissionList = UIPermissions.getPermissionList();
                String s = etPer.getText().toString();
                permissionList.remove(s);
                UIPermissions.setPermissionList(permissionList);
            }
        });
        View obj = findViewById(R.id.button3);

        obj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("onClick","startActivity="+System.currentTimeMillis());
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                Log.e("onClick","startActivityOver="+System.currentTimeMillis());
//                ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
//                decorView.setDrawingCacheEnabled(true);
//                decorView.buildDrawingCache();
//                Bitmap bitmap = decorView.getDrawingCache();
//                ImageView view = decorView.findViewById(R.id.ivMirror);
//                if(view == null){
//                    view = new ImageView(mContext);
//                    view.setId(R.id.ivMirror);
//                    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
//                    decorView.addView(view);
//                }
//                ActivityStack.get().setBitmap(bitmap);
//                view.setImageBitmap(ActivityStack.get().getBitmap());
//                view.setVisibility(View.VISIBLE);
//                decorView.setDrawingCacheEnabled(false);
                ValueAnimator valueAnimator = ValueAnimator.ofInt(250);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    long l = System.currentTimeMillis();
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Log.e("onAnimationUpdate", "value=" + animation.getAnimatedValue());
                        Log.e("onAnimationUpdate", "time=" + (System.currentTimeMillis() - l));
                        l = System.currentTimeMillis();
                    }
                });
                valueAnimator.setInterpolator(new DecelerateInterpolator());
                valueAnimator.setDuration(250);
                valueAnimator.start();
                Log.e("onClick","getBitmapOver="+System.currentTimeMillis());
            }
        });


        UIPermissions.subscribe(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.e("MainActivity", "onPostCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UIPermissions.unsubscribe(this);
    }

    public static class ProxyClickListener implements View.OnClickListener {

        public ProxyClickListener(View.OnClickListener listener) {
            this.listener = listener;
        }

        View.OnClickListener listener;

        @Override
        public void onClick(View v) {
            Log.e("ProxyClickListener", "onClick");
            if (listener != null) {
                listener.onClick(v);
            }
        }
    }
}
