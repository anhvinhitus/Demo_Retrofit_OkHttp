package vn.com.vng.zalopay.data.ws.parser;

import com.google.protobuf.GeneratedMessage;

import timber.log.Timber;
import vn.com.vng.zalopay.data.ws.message.MessageType;
import vn.com.vng.zalopay.data.ws.protobuf.ZPMsgProtos;


/**
 * Created by AnhHieu on 6/14/16.
 */
public class MessageParser implements Parser {

    @Override
    public GeneratedMessage parserMessage(byte[] msg) {

        GeneratedMessage ret = null;

        if (msg.length != 0) {
            try {
                ret = processMessage(msg);
            } catch (Exception ex) {
                Timber.e(ex, " parserMessage Exception");
            }
        }

        return ret;

    }

    private GeneratedMessage processMessage(byte[] msg) throws Exception {
        ZPMsgProtos.DataResponseUser respMsg = ZPMsgProtos.DataResponseUser.parseFrom(msg);
        switch (respMsg.getMsgtype()) {
            case MessageType.Response.KICK_OUT:
                return processAuthenticationLoginSuccess(respMsg.getData().toByteArray());
            case MessageType.Response.PUSH_NOTIFICATION:
                return processPushMessage(respMsg.getData().toByteArray());
            case MessageType.Response.AUTHEN_LOGIN_RESULT:
                return processKickoutUser(respMsg.getData().toByteArray());
            default:
        }

        return null;
    }


    public GeneratedMessage processAuthenticationLoginSuccess(byte[] data) {
        try {

            ZPMsgProtos.ResultAuth res = ZPMsgProtos.ResultAuth.parseFrom(data);
            System.out.print("Result" + res.getResult() + " code " + res.getCode());
            //   Log.e(TAG, "Resule:"+res.getResult()+"---"+res.getCode());

            return res;
        } catch (Exception ex) {
            //    Log.e(TAG, "Resule error");
        }
        return null;
    }

    public GeneratedMessage processKickoutUser(byte[] data) {
        System.out.print("You kickedout");

        return null;
    }

    public GeneratedMessage processPushMessage(byte[] data) {
        System.out.println("You receive data fom TPE: " + data.toString());
        String str = new String(data);
        //  Log.e(TAG, "Resule push: "+str.toString());

        return null;
    }
}
