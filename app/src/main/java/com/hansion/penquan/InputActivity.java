package com.hansion.penquan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hansion.utils.SharedPrefsUtil;
import com.hansion.utils.StringValidationUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.hansion.utils.StringValidationUtils.RegexIP;
import static com.hansion.utils.StringValidationUtils.RegexPort;

public class InputActivity extends AppCompatActivity {

    @BindView(R.id.mIp)
    EditText mIp;
    @BindView(R.id.mPort)
    EditText mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        ButterKnife.bind(this);

        String ip = SharedPrefsUtil.getValue(this, "video", "ip", "");
        String port = SharedPrefsUtil.getValue(this, "video", "port", "");
        if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port)) {
            mIp.setText(ip);
            mPort.setText(port);
        }
    }

    @OnClick(R.id.mGoA)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mGoA:
                goVideoActivity();
                break;
        }
    }

    public void goVideoActivity() {
        String ip = mIp.getText().toString().trim();
        String port = mPort.getText().toString().trim();
        if (!StringValidationUtils.validateRegex(ip, RegexIP) || !StringValidationUtils.validateRegex(port, RegexPort)) {
            Toast.makeText(this, "请输入正确的ip地址和端口号", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPrefsUtil.putValue(this, "video", "ip", ip);
        SharedPrefsUtil.putValue(this, "video", "port", port);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
