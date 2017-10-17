package com.hansion.data;

/**
 * Description：
 * Author: Hansion
 * Time: 2017/10/17 14:15
 */
public class NewDataTrans {


    byte[] raw_data;

    public NewDataTrans() {
        raw_data = new byte[9];
        raw_data[0] = (byte) 0xAA;
        raw_data[1] = 0x01;
        raw_data[2] = 0x55;
        raw_data[8] = (byte) 0xBB;
    }

    public byte[] getFinalData(byte data) {
        raw_data[3] = data;
        int sumCheckInt = raw_data[1] + raw_data[2] + raw_data[3];
        byte[] sumCheck = DataTypeChangeUtils.intToByteArray(sumCheckInt);
        System.arraycopy(sumCheck, 0, raw_data, 4, sumCheck.length);
        return raw_data;
    }
}
