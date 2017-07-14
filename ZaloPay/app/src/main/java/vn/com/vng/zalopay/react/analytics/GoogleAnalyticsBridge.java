package vn.com.vng.zalopay.react.analytics;

import android.text.TextUtils;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;
import java.util.Map;

import vn.com.vng.zalopay.tracker.GoogleReporter;

class GoogleAnalyticsBridge extends ReactContextBaseJavaModule {

    GoogleAnalyticsBridge(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "GoogleAnalyticsBridge";
    }

    private HashMap<String, GoogleReporter> mTrackers = new HashMap<>();

    private synchronized GoogleReporter getTracker(String trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleReporter reporter = new GoogleReporter(getReactApplicationContext(), trackerId);
            mTrackers.put(trackerId, reporter);
        }
        return mTrackers.get(trackerId);
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("nativeTrackerId", "");
        return constants;
    }

    @ReactMethod
    public void trackScreenView(String trackerId, String screenName) {
        GoogleReporter tracker = getTracker(trackerId);

        if (tracker != null) {
            tracker.trackScreen(screenName);
        }
    }

    @ReactMethod
    public void trackEvent(String trackerId, String category, String action, ReadableMap optionalValues) {
        GoogleReporter tracker = getTracker(trackerId);

        if (tracker != null) {

            String label = null;
            String value = null;

            if (optionalValues.hasKey("label")) {
                label = (optionalValues.getString("label"));
            }

            if (optionalValues.hasKey("value")) {
                value = String.valueOf(optionalValues.getInt("value"));
            }

            tracker.trackEvent(category, action, label, value);
        }
    }

    @ReactMethod
    public void trackTiming(String trackerId, String category, Double value, ReadableMap optionalValues) {
        GoogleReporter tracker = getTracker(trackerId);

        if (tracker != null) {

            String variable = null;
            String label = null;

            if (optionalValues.hasKey("name")) {
                variable = (optionalValues.getString("name"));
            }

            if (TextUtils.isEmpty(variable)) { //Required
                return;
            }

            if (optionalValues.hasKey("label")) {
                label = (optionalValues.getString("label"));
            }

            tracker.trackTiming(category, value, variable, label);
        }
    }

    @ReactMethod
    public void trackPurchaseEvent(String trackerId, ReadableMap product, ReadableMap transaction, String eventCategory, String eventAction) {
       /* Tracker tracker = getTracker(trackerId);

        if (tracker != null) {
            Product ecommerceProduct = new Product()
                    .setId(product.getString("id"))
                    .setName(product.getString("name"))
                    .setCategory(product.getString("category"))
                    .setBrand(product.getString("brand"))
                    .setVariant(product.getString("variant"))
                    .setPrice(product.getDouble("price"))
                    .setCouponCode(product.getString("couponCode"))
                    .setQuantity(product.getInt("quantity"));

            ProductAction productAction = new ProductAction(ProductAction.ACTION_PURCHASE)
                    .setTransactionId(transaction.getString("id"))
                    .setTransactionAffiliation(transaction.getString("affiliation"))
                    .setTransactionRevenue(transaction.getDouble("revenue"))
                    .setTransactionTax(transaction.getDouble("tax"))
                    .setTransactionShipping(transaction.getDouble("shipping"))
                    .setTransactionCouponCode(transaction.getString("couponCode"));

            HitBuilders.EventBuilder hit = new HitBuilders.EventBuilder()
                    .addProduct(ecommerceProduct)
                    .setProductAction(productAction)
                    .setCategory(eventCategory)
                    .setAction(eventAction);

            tracker.send(hit.build());
        }*/
    }

    @ReactMethod
    public void trackException(String trackerId, String error, Boolean fatal) {
        GoogleReporter tracker = getTracker(trackerId);
        if (tracker != null) {
            tracker.trackException(error, fatal);
        }
    }

    @ReactMethod
    public void setUser(String trackerId, String userId) {
        GoogleReporter tracker = getTracker(trackerId);

        if (tracker != null) {
            tracker.setUserId(userId);
        }
    }

    @ReactMethod
    public void allowIDFA(String trackerId, Boolean enabled) {
      /*  Tracker tracker = getTracker(trackerId);

        if (tracker != null) {
            tracker.enableAdvertisingIdCollection(enabled);
        }*/
    }

    @ReactMethod
    public void trackSocialInteraction(String trackerId, String network, String action, String targetUrl) {
        GoogleReporter tracker = getTracker(trackerId);
        if (tracker != null) {
            tracker.trackSocialInteractions(network, action, targetUrl);
        }
    }

    @ReactMethod
    public void trackScreenViewWithCustomDimensionValues(String trackerId, String screenName, ReadableMap dimensionIndexValues) {
       /* Tracker tracker = getTracker(trackerId);

        if (tracker != null) {
            tracker.setScreenName(screenName);
            HitBuilders.ScreenViewBuilder screenBuilder = new HitBuilders.ScreenViewBuilder();
            ReadableMapKeySetIterator iterator = dimensionIndexValues.keySetIterator();
            while (iterator.hasNextKey()) {
                String dimensionIndex = iterator.nextKey();
                String dimensionValue = dimensionIndexValues.getString(dimensionIndex);
                screenBuilder.setCustomDimension(Integer.parseInt(dimensionIndex), dimensionValue);
            }
            tracker.send(screenBuilder.build());
        }*/
    }

    @ReactMethod
    public void trackEventWithCustomDimensionValues(String trackerId, String category, String action, ReadableMap optionalValues, ReadableMap dimensionIndexValues) {
       /* Tracker tracker = getTracker(trackerId);

        if (tracker != null) {
            HitBuilders.EventBuilder hit = new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action);

            if (optionalValues.hasKey("label")) {
                hit.setLabel(optionalValues.getString("label"));
            }
            if (optionalValues.hasKey("value")) {
                hit.setValue(optionalValues.getInt("value"));
            }

            ReadableMapKeySetIterator iterator = dimensionIndexValues.keySetIterator();
            while (iterator.hasNextKey()) {
                String dimensionIndex = iterator.nextKey();
                String dimensionValue = dimensionIndexValues.getString(dimensionIndex);
                hit.setCustomDimension(Integer.parseInt(dimensionIndex), dimensionValue);
            }

            tracker.send(hit.build());
        }*/
    }

    @ReactMethod
    public void setDryRun(Boolean enabled) {
       /* GoogleAnalytics analytics = getAnalyticsInstance();

        if (analytics != null) {
            analytics.setDryRun(enabled);
        }*/
    }

    @ReactMethod
    public void setDispatchInterval(Integer intervalInSeconds) {
       /* GoogleAnalytics analytics = getAnalyticsInstance();

        if (analytics != null) {
            analytics.setLocalDispatchPeriod(intervalInSeconds);
        }*/
    }

    @ReactMethod
    public void setTrackUncaughtExceptions(String trackerId, Boolean enabled) {
       /* Tracker tracker = getTracker(trackerId);

        if (tracker != null) {
            tracker.enableExceptionReporting(enabled);
        }*/
    }


    @ReactMethod
    public void setAnonymizeIp(String trackerId, Boolean enabled) {
     /*   Tracker tracker = getTracker(trackerId);

        if (tracker != null) {
            tracker.setAnonymizeIp(enabled);
        }*/
    }

    @ReactMethod
    public void setOptOut(Boolean enabled) {
      /*  GoogleAnalytics analytics = getAnalyticsInstance();

        if (analytics != null) {
            analytics.setAppOptOut(enabled);
        }*/
    }

    @ReactMethod
    public void setAppName(String trackerId, String appName) {
        GoogleReporter tracker = getTracker(trackerId);
        if (tracker != null) {
            tracker.setAppName(appName);
        }
    }

    @ReactMethod
    public void setAppVersion(String trackerId, String appVersion) {
        GoogleReporter tracker = getTracker(trackerId);
        if (tracker != null) {
            tracker.setAppVersion(appVersion);
        }
    }
}