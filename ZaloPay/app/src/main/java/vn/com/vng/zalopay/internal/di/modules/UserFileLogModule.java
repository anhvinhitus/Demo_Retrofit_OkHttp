package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.filelog.FileLogRepository;
import vn.com.vng.zalopay.data.filelog.FileLogStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by hieuvm on 4/21/17.
 * *
 */

@Module
public class UserFileLogModule {

    @UserScope
    @Provides
    FileLogStore.Repository providesFileLogRepository(User user, @Named("retrofitClientLog") Retrofit retrofit) {
        return new FileLogRepository(user, retrofit.create(FileLogStore.RequestService.class));
    }

}
