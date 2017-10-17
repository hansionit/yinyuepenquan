package com.hansion.penquan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.hansion.data.DataTools;
import com.hansion.data.SendDataImpl;
import com.hansion.netty.INettyTcpClient;
import com.hansion.utils.MyToast;
import com.hansion.utils.NoFastClickUtils;
import com.hansion.utils.SharedPrefsUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.hansion.Constants.IP;
import static com.hansion.Constants.PORT;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.mConnectStatus)
    TextView mConnectStatus;
    private SendDataImpl sendData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //TODO
        IP = SharedPrefsUtil.getValue(this, "video", "ip", "");
        PORT = Integer.parseInt(SharedPrefsUtil.getValue(this, "video", "port", ""));


        DataTools.getInstance().connectServer(true);
        DataTools.getInstance().setOnConnectStatusListener(new INettyTcpClient.OnConnectStatusListener() {
            @Override
            public void onDisconnected(boolean isBreakByMyself) {
                mConnectStatus.setText("未连接");
            }

            @Override
            public void onTcpConnect() {
                mConnectStatus.setText("已连接");
            }
        });

        sendData = new SendDataImpl();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataTools.getInstance().disconnectTcp();
    }

    @OnClick({R.id.mSystemOpen, R.id.mSystemClose, R.id.mMusicOpen, R.id.mMusicClose, R.id.mLightOpen, R.id.mLightClose})
    public void onClick(View view) {
        if (DataTools.getInstance().isTcpDisconnected()) {
            MyToast.show("请先连接设备WIFI");
            return;
        }
        if(NoFastClickUtils.isFastDoubleClick_short()) {
            return;
        }
        switch (view.getId()) {
            case R.id.mSystemOpen:
                sendData.systemOpen();
                break;
            case R.id.mSystemClose:
                sendData.systemClose();
                break;
            case R.id.mMusicOpen:
                sendData.musicOpen();
                break;
            case R.id.mMusicClose:
                sendData.musicClose();
                break;
            case R.id.mLightOpen:
                sendData.lightOpen();
                break;
            case R.id.mLightClose:
                sendData.lightClose();
                break;
        }
    }
}
