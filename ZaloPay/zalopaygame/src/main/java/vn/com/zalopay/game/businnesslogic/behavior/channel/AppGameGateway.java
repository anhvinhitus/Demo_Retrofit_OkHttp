package vn.com.zalopay.game.businnesslogic.behavior.channel;

import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.entity.base.AppGameError;
import vn.com.zalopay.game.businnesslogic.enums.EAppGameError;
import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameStartFlow;

/**
 * Created by admin on 8/27/16.
 */
public class AppGameGateway extends AppGameBaseChannel
{

    private static AppGameGateway _object;

    public synchronized static AppGameGateway getInstance(IAppGameStartFlow pStartFlow)
    {
        if(AppGameGateway._object == null)
            AppGameGateway._object = new AppGameGateway(pStartFlow);

        return AppGameGateway._object;
    }

    private IAppGameStartFlow mStartFlowInterFace;

    public AppGameGateway(IAppGameStartFlow pStartFlow)
    {
        this.mStartFlowInterFace = pStartFlow;
    }

    public void setStartFlowInterface(IAppGameStartFlow pStartFlow)
    {
        this.mStartFlowInterFace = pStartFlow;
    }

    @Override
    public void startFlow()
    {
        if(mStartFlowInterFace != null)
            mStartFlowInterFace.startFlow();

        else if(AppGameGlobal.getResultListener() != null)
            AppGameGlobal.getResultListener().onError(new AppGameError(EAppGameError.DATA_INVALID,
                    AppGameGlobal.getString(R.string.appgame_alert_error_start_channel)));

        else
            AppGameGlobal.getLog().e(getClass().getName(),"===can not start channel==");
    }
}
