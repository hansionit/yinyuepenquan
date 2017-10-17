package com.hansion.data;


import com.hansion.utils.LogUtil;

import java.util.Random;

/**
 * Created by Hansion on 2016/6/22.
 */
public class DataTrans {


    /**
     * -------------------------------------------------------------------
     * ----------------------------编码部分--------------------------------
     * -------------------------------------------------------------------
     */

    /**
     * 最终结果数组
     *
     * @param orderInt 命令
     * @param data     数据
     * @return
     */
    public  byte[] finalByte(int orderInt, byte[] data, int sumCheckInt) {
        return escape(last(all(orderInt, data, sumCheckInt)));
    }

    /**
     * 获取长度
     * 把int转成byte数组要用长度4的byte[]
     *
     * @param length 数据的数量
     */
    public  byte[] length(int length) {
        byte[] length1 = new byte[4];
        try {
            length1[0] = (byte) ((length & 0xFF000000) >> 24);
            length1[1] = (byte) ((length & 0x00FF0000) >> 16);
            length1[2] = (byte) ((length & 0x0000FF00) >> 8);
            length1[3] = (byte) (length & 0x000000FF);
            return length1;
        } finally {
            length1 = null;
        }
    }

    public  byte[] transInt(int deep) {
        byte[] deep1 = new byte[2];
        try {
            deep1[0] = (byte) ((deep & 0x0000FF00) >> 8);
            deep1[1] = (byte) (deep & 0x000000FF);
            return deep1;
        } finally {
            deep1 = null;
        }
    }

    /**
     * 根据数量生成随机数组
     */
    public  byte[] random(int num) {
        byte[] randomByte = new byte[num];
        try {
            Random random = new Random();
            for (int i = 0; i < randomByte.length; i++) {
                randomByte[i] = (byte) random.nextInt(100);
                if(randomByte[i] == 0xAA || randomByte[i] == 0xBB || randomByte[i] == 0x7D || randomByte[i] == 0x3D) {
                    randomByte[i] = 0x05;
                }
            }
            return randomByte;
        } finally {
            randomByte = null;
        }
    }

    /**
     * 生成未加帧头、帧尾、未异或的数组
     *
     * @return 未加帧头、帧尾的数组
     */
    public  byte[] all(int orderInt, byte[] data, int sumCheckInt) {
        byte[] order = new byte[1];
        order[0] = (byte) orderInt;
        byte[] length = length(data.length);
        byte[] random4 = random(4);
        byte[] random5 = random(5);
        byte[] sumCheck = DataTypeChangeUtils.intToByteArray(sumCheckInt);

        byte[] last = new byte[length.length + random4.length + order.length + random5.length + data.length + sumCheck.length];
        System.arraycopy(length, 0, last, 0, length.length);
        System.arraycopy(random4, 0, last, length.length, random4.length);
        System.arraycopy(order, 0, last, length.length + random4.length, order.length);
        System.arraycopy(random5, 0, last, length.length + random4.length + order.length, random5.length);
        System.arraycopy(data, 0, last, length.length + random4.length + order.length + random5.length, data.length);
        System.arraycopy(sumCheck, 0, last, length.length + random4.length + order.length + random5.length + data.length, sumCheck.length);
        return last;
    }

    /**
     * 与0x40异或
     *
     * @param data
     * @return
     */
    public  byte[] last(byte[] data) {
        byte[] rec = new byte[data.length];
        try {
            for (int i = 0; i < data.length; i++) {
                rec[i] = (byte) (data[i] ^ 0x40);
            }
            return rec;
        } finally {
            rec = null;
        }
    }

    /**
     * 遇到0xAA和0xBB，以及转义符0x7D时，首先插入一个转义符0x7D，然后将数据和0x40进行异或操作,添加帧头帧尾
     *
     * @param buf 未添加帧头、帧尾的数组
     * @return 最终数组
     */
    public  byte[] escape(byte[] buf) {
        byte[] rec = new byte[1024];
        byte[] rec2;
        byte[] rec3;
        try {
            int i, j = 0;

            for (i = 0; i < buf.length; i++) {
                if ((buf[i] == (byte) 0xAA) ||
                        (buf[i] == (byte) 0xBB) || (buf[i] == 0x7D) && (i != 0)
                        && (i != buf.length - 1)) {
                    rec[j++] = 0x7D;
                    rec[j++] = (byte) (buf[i] ^ 0x40);
                } else {
                    rec[j++] = buf[i];
                }
            }

            rec2 =  new byte[j];
            for (i = 0; i < j; i++)
                rec2[i] = rec[i];

            rec3 = new byte[rec2.length + 2];
            rec3[0] = (byte) 0xAA;
            System.arraycopy(rec2, 0, rec3, 1, rec2.length);
            rec3[rec2.length + 1] = (byte) 0xBB;
            return rec3;
        } finally {
            rec = null;
            rec2 = null;
            rec3 = null;
        }
    }


    /**
     * -------------------------------------------------------------------
     * ----------------------------解码部分--------------------------------
     * -------------------------------------------------------------------
     */

    public static int transByte(byte[] buf) {
        LogUtil.e("收到："+buf[0]+","+buf[1]);
        int byte1 = 0;
        byte1 = byte1 + (buf[buf.length - 2] << 8);     //00 0A
        byte1 = byte1 + (buf[buf.length - 1] & 0xFF) ;
        return byte1;
    }
}
