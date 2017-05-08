package vn.com.vng.zalopay.data.filelog;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by hieuvm on 4/21/17.
 * *
 */

public interface FileLogStore {

    interface RequestService {
        @Multipart
        @API_NAME(ZPEvents.API_V001_ZP_UPLOAD_CLIENTLOGS)
        @POST(Constants.UMUPLOAD_API.FILE_LOG)
        Observable<BaseResponse> uploadFileLog(@Part("userid") RequestBody userid, @Part MultipartBody.Part file);
    }

    interface Repository {
        Observable<String> uploadFileLog(String zipFile);
    }
}
