package vn.com.vng.zalopay.app;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by huuhoa on 11/25/16.
 * Hold global application state
 */

@Singleton
public class ApplicationState {
    public enum State {
        LAUNCHING,
        LOGIN,
        MAIN_SCREEN_DESTROYED, MAIN_SCREEN_CREATED
    }

    private State mState;

    @Inject
    public ApplicationState() {
        Timber.d("Create new instance of ApplicationState");
        mState = State.LAUNCHING;
    }

    public State currentState() {
        return mState;
    }

    public void moveToState(State state) {
        mState = state;
    }


}
