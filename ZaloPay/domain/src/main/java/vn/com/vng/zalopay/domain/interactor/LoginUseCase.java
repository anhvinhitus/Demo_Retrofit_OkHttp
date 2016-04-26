package vn.com.vng.zalopay.domain.interactor;

import javax.inject.Inject;

import rx.Observable;
import vn.com.vng.zalopay.domain.executor.PostExecutionThread;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.repository.PassportRepository;


public class LoginUseCase extends UseCase {

    private PassportRepository passportRepository;

    @Inject
    public LoginUseCase(ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread, PassportRepository passportRepository) {
        super(threadExecutor, postExecutionThread);
        this.passportRepository = passportRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return passportRepository.login();
    }
}
