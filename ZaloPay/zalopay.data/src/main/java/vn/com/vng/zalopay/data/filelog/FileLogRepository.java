package vn.com.vng.zalopay.data.filelog;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by hieuvm on 4/21/17.
 * *
 */

public class FileLogRepository implements FileLogStore.Repository {

    private final FileLogStore.RequestService mRequestService;
    private final User mUser;
    private final String mClientLogUrl;

    public FileLogRepository(User user, String url, FileLogStore.RequestService requestService) {
        mRequestService = requestService;
        mUser = user;
        mClientLogUrl = url;
    }

    @Override
    public Observable<String> uploadFileLog(String zipFile) {
        return mRequestService.uploadFileLog(mClientLogUrl, requestBodyParam(mUser.zaloPayId), createMultipartBody(zipFile))
                .map(baseResponse -> zipFile)
                ;
    }

    private RequestBody requestBodyParam(String param) {
        return RequestBody.create(MediaType.parse("text/plain"), param);
    }

    private MultipartBody.Part createMultipartBody(String fileUpload) {
        File file = new File(fileUpload);
        return MultipartBody.Part.createFormData("fileUpload", file.getName(), RequestBody.create(MediaType.parse("application/zip"), file));
    }
}
