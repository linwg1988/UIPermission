package org.linwg.lib.uipermission;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.linwg.lib.PerRelation;
import org.linwg.lib.annotation.LUIPermission;
import org.linwg.lib.api.UIPermissions;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    @LUIPermission(value = {"A", "C"}, relation = PerRelation.OR, actingOnClick = true, toastHint = "This click event has been intercepted.")
    FloatingActionButton fab;
    @LUIPermission("A")
    TextView textView;
    @LUIPermission("B")
    TextView tvTextB;
    @LUIPermission("C")
    TextView tvTextC;
    @LUIPermission(value = {"A", "B"}, relation = PerRelation.OR)
    TextView tvTextAoB;
    @LUIPermission({"A", "B"})
    TextView tvTextAaB;
    @LUIPermission(grantStrategy = {SizeGrant.class})
    TextView tvTextPer4;
    @LUIPermission(grantStrategy = {SpecialGrant.class})
    TextView tvTextPerACnB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        final TextView tvPerList = findViewById(R.id.tvPerList);
        textView = findViewById(R.id.tvText);
        tvTextB = findViewById(R.id.tvTextB);
        tvTextC = findViewById(R.id.tvTextC);
        tvTextAoB = findViewById(R.id.tvTextAoB);
        tvTextAaB = findViewById(R.id.tvTextAaB);
        tvTextPer4 = findViewById(R.id.tvTextPer4);
        tvTextPerACnB = findViewById(R.id.tvTextPerACnB);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> permissionList = UIPermissions.getPermissionList();
                String s = etPer.getText().toString();
                if (!permissionList.contains(s)) {
                    permissionList.add(s);
                }
                tvPerList.setText("Current permission:" + listToString(permissionList));
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
                tvPerList.setText("Current permission:" + listToString(permissionList));
                UIPermissions.setPermissionList(permissionList);
            }
        });
        View obj = findViewById(R.id.button3);
        obj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("onClick", "startActivity=" + System.currentTimeMillis());
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                Log.e("onClick", "startActivityOver=" + System.currentTimeMillis());
            }
        });
        tvPerList.setText("Current permission:" + listToString(UIPermissions.getPermissionList()));
        UIPermissions.subscribe(this);
    }

    private String listToString(List<String> list) {
        String s = "";
        for (int i = 0; i < list.size(); i++) {
            s += list.get(i);
            if (i < list.size() - 1) {
                s += ",";
            }
        }
        return s;
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
}
