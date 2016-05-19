package vn.com.vng.zalopay.config;

/**
 * Created by huuhoa on 5/18/16.
 * React support config
 */
public enum ReactSupport {
    /// App load internal JS bundle from npm and ignore external JS bundle
    DEV_INTERNAL,

    /// App load internal JS bundle from assets, load external JS bundle from npm
    DEV_EXTERNAL,

    /// App load internal, external JS from assets
    RELEASE
}
