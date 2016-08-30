package vn.com.zalopay.game.businnesslogic.entity.base;

import vn.com.zalopay.game.businnesslogic.enums.EAppGameError;

public class AppGameError
{
    public EAppGameError payError;
    public String messError;

    public AppGameError(EAppGameError pErrorCode, String pErrorMessage)
    {
        this.payError = pErrorCode;
        this.messError = pErrorMessage;
    }
}
