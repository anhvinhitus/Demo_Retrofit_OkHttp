package vn.com.vng.zalopay.internal.di.modules.user;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

@Module
public class UserModule {

    private final User user;

    public UserModule(User user) {
        this.user = user;
    }

    @Provides
    @UserScope
    User provideUser() {
        return user;
    }

  /*  @Singleton
    @Provides
    VideoRepository provideVideoRepository(VideoRepositoryImpl videoRepository) {
        return videoRepository;
    }*/

  /*  @Provides
    @UserScope
    RepositoriesManager provideRepositoriesManager(User user, GithubApiService githubApiService) {
        return new RepositoriesManager(user, githubApiService);
    }*/


    /*@Provides
    @PerActivity
    @Named("userList")
    UseCase provideGetUserListUseCase(
            GetUserList getUserList) {
        return getUserList;
    }

    @Provides
    @PerActivity
    @Named("userDetails")
    UseCase provideGetUserDetailsUseCase(
            UserRepository userRepository, ThreadExecutor threadExecutor,
            PostExecutionThread postExecutionThread) {
        return new GetUserDetails(userId, userRepository, threadExecutor, postExecutionThread);
    }*/

   /* @Provides   @UserScope @Named("userList")
    UseCase provideGetUserListUseCase(
            GetUserList getUserList) {
        return getUserList;
    }

    @Provides @PerActivity @Named("userDetails") UseCase provideGetUserDetailsUseCase(
            UserRepository userRepository, ThreadExecutor threadExecutor,
            PostExecutionThread postExecutionThread) {
        return new GetUserDetails(userId, userRepository, threadExecutor, postExecutionThread);
    }
*/


}