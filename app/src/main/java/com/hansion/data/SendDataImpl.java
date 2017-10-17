package com.hansion.data;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/10/17 11:51
 */
public class SendDataImpl implements SendDataInterface {

    private NewDataTrans dataTrans;

    public SendDataImpl() {
        dataTrans = new NewDataTrans();
    }

    @Override
    public void systemOpen() {
        DataTools.getInstance().sendData( dataTrans.getFinalData((byte) 0x01));
    }

    @Override
    public void systemClose() {
        DataTools.getInstance().sendData( dataTrans.getFinalData((byte) 0x02));
    }

    @Override
    public void musicOpen() {
        DataTools.getInstance().sendData( dataTrans.getFinalData((byte) 0x03));
    }

    @Override
    public void musicClose() {
        DataTools.getInstance().sendData( dataTrans.getFinalData((byte) 0x04));
    }

    @Override
    public void lightOpen() {
        DataTools.getInstance().sendData( dataTrans.getFinalData((byte) 0x05));
    }

    @Override
    public void lightClose() {
        DataTools.getInstance().sendData( dataTrans.getFinalData((byte) 0x06));
    }
}
