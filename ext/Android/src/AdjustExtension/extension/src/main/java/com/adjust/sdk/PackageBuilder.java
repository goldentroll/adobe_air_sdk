//
//  PackageBuilder.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.content.ContentResolver;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.adjust.sdk.Constants.CALLBACK_PARAMETERS;
import static com.adjust.sdk.Constants.PARTNER_PARAMETERS;

class PackageBuilder {
    private AdjustConfig adjustConfig;
    private DeviceInfo deviceInfo;
    private ActivityStateCopy activityStateCopy;
    private SessionParameters sessionParameters;
    private long createdAt;

    // reattributions
    Map<String, String> extraParameters;
    AdjustAttribution attribution;
    String reftag;
    String referrer;
    String rawReferrer;
    String deeplink;
    long clickTimeInMilliseconds = -1;

    long clicktTimeInSeconds = -1;
    long installBeginTimeInSeconds = -1;

    private static ILogger logger = AdjustFactory.getLogger();

    private class ActivityStateCopy {
        long lastInterval = -1;
        int eventCount = -1;
        String uuid = null;
        int sessionCount = -1;
        int subsessionCount = -1;
        long sessionLength = -1;
        long timeSpent = -1;
        String pushToken = null;

        ActivityStateCopy(ActivityState activityState) {
            if (activityState == null) {
                return;
            }
            this.lastInterval = activityState.lastInterval;
            this.eventCount = activityState.eventCount;
            this.uuid = activityState.uuid;
            this.sessionCount = activityState.sessionCount;
            this.subsessionCount = activityState.subsessionCount;
            this.sessionLength = activityState.sessionLength;
            this.timeSpent = activityState.timeSpent;
            this.pushToken = activityState.pushToken;
        }
    }

    public PackageBuilder(AdjustConfig adjustConfig,
                          DeviceInfo deviceInfo,
                          ActivityState activityState,
                          SessionParameters sessionParameters,
                          long createdAt) {
        this.adjustConfig = adjustConfig;
        this.deviceInfo = deviceInfo;
        this.activityStateCopy = new ActivityStateCopy(activityState);
        this.sessionParameters = sessionParameters;
        this.createdAt = createdAt;
    }

    public ActivityPackage buildSessionPackage(boolean isInDelay) {
        Map<String, String> parameters;
        parameters = getAttributableParameters(isInDelay);

        ActivityPackage sessionPackage = getDefaultActivityPackage(ActivityKind.SESSION);
        sessionPackage.setPath("/session");
        sessionPackage.setSuffix("");
        sessionPackage.setParameters(parameters);

        return sessionPackage;
    }

    public ActivityPackage buildEventPackage(AdjustEvent event,
                                             boolean isInDelay) {
        Map<String, String> parameters = getDefaultParameters();
        PackageBuilder.addLong(parameters, "event_count", activityStateCopy.eventCount);
        PackageBuilder.addString(parameters, "event_token", event.eventToken);
        PackageBuilder.addDouble(parameters, "revenue", event.revenue);
        PackageBuilder.addString(parameters, "currency", event.currency);

        if (!isInDelay) {
            PackageBuilder.addMapJson(parameters, CALLBACK_PARAMETERS,
                    Util.mergeParameters(this.sessionParameters.callbackParameters, event.callbackParameters, "Callback"));
            PackageBuilder.addMapJson(parameters, PARTNER_PARAMETERS,
                    Util.mergeParameters(this.sessionParameters.partnerParameters, event.partnerParameters, "Partner"));
        }
        ActivityPackage eventPackage = getDefaultActivityPackage(ActivityKind.EVENT);
        eventPackage.setPath("/event");
        eventPackage.setSuffix(getEventSuffix(event));
        eventPackage.setParameters(parameters);

        if (isInDelay) {
            eventPackage.setCallbackParameters(event.callbackParameters);
            eventPackage.setPartnerParameters(event.partnerParameters);
        }

        return eventPackage;
    }

    public ActivityPackage buildClickPackage(String source) {
        Map<String, String> parameters = getAttributableParameters(false);

        PackageBuilder.addString(parameters, "source", source);
        PackageBuilder.addDateInMilliseconds(parameters, "click_time", clickTimeInMilliseconds);
        PackageBuilder.addString(parameters, "reftag", reftag);
        PackageBuilder.addMapJson(parameters, "params", extraParameters);
        PackageBuilder.addString(parameters, "referrer", referrer);
        PackageBuilder.addString(parameters, "raw_referrer", rawReferrer);
        PackageBuilder.addString(parameters, "deeplink", deeplink);
        PackageBuilder.addDateInSeconds(parameters, "click_time", clicktTimeInSeconds);
        PackageBuilder.addDateInSeconds(parameters, "install_begin_time", installBeginTimeInSeconds);
        injectAttribution(parameters);

        ActivityPackage clickPackage = getDefaultActivityPackage(ActivityKind.CLICK);
        clickPackage.setPath("/sdk_click");
        clickPackage.setSuffix("");
        clickPackage.setClickTimeInMilliseconds(clickTimeInMilliseconds);
        clickPackage.setClickTimeInSeconds(clicktTimeInSeconds);
        clickPackage.setInstallBeginTimeInSeconds(installBeginTimeInSeconds);
        clickPackage.setParameters(parameters);

        return clickPackage;
    }

    public ActivityPackage buildInfoPackage(String source) {
        Map<String, String> parameters = getIdsParameters();

        PackageBuilder.addString(parameters, "source", source);

        ActivityPackage clickPackage = getDefaultActivityPackage(ActivityKind.INFO);
        clickPackage.setPath("/sdk_info");
        clickPackage.setSuffix("");
        clickPackage.setParameters(parameters);

        return clickPackage;
    }

    public ActivityPackage buildAttributionPackage() {
        Map<String, String> parameters = getIdsParameters();

        ActivityPackage attributionPackage = getDefaultActivityPackage(ActivityKind.ATTRIBUTION);
        attributionPackage.setPath("attribution"); // does not contain '/' because of Uri.Builder.appendPath
        attributionPackage.setSuffix("");
        attributionPackage.setParameters(parameters);

        return attributionPackage;
    }

    public ActivityPackage buildGdprPackage() {
        Map<String, String> parameters = getIdsParameters();

        ActivityPackage gdprPackage = getDefaultActivityPackage(ActivityKind.GDPR);
        gdprPackage.setPath("/gdpr_forget_device");
        gdprPackage.setSuffix("");
        gdprPackage.setParameters(parameters);

        return gdprPackage;
    }

    private ActivityPackage getDefaultActivityPackage(ActivityKind activityKind) {
        ActivityPackage activityPackage = new ActivityPackage(activityKind);
        activityPackage.setClientSdk(deviceInfo.clientSdk);
        return activityPackage;
    }

    private Map<String, String> getAttributableParameters(boolean isInDelay) {
        Map<String, String> parameters = getDefaultParameters();
        PackageBuilder.addDuration(parameters, "last_interval", activityStateCopy.lastInterval);
        PackageBuilder.addString(parameters, "default_tracker", adjustConfig.defaultTracker);
        PackageBuilder.addString(parameters, "installed_at", deviceInfo.appInstallTime);
        PackageBuilder.addString(parameters, "updated_at", deviceInfo.appUpdateTime);

        if (!isInDelay) {
            PackageBuilder.addMapJson(parameters, CALLBACK_PARAMETERS, this.sessionParameters.callbackParameters);
            PackageBuilder.addMapJson(parameters, PARTNER_PARAMETERS, this.sessionParameters.partnerParameters);
        }

        return parameters;
    }

    private Map<String, String> getDefaultParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        injectDeviceInfo(parameters);
        injectConfig(parameters);
        injectActivityState(parameters);
        injectCommonParameters(parameters);

        // general
        checkDeviceIds(parameters);

        return parameters;
    }

    private Map<String, String> getIdsParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        injectDeviceInfoIds(parameters);
        injectConfig(parameters);
        injectCommonParameters(parameters);

        checkDeviceIds(parameters);

        return parameters;
    }

    private void injectDeviceInfo(Map<String, String> parameters) {
        injectDeviceInfoIds(parameters);
        PackageBuilder.addString(parameters, "fb_id", deviceInfo.fbAttributionId);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_manufacturer", deviceInfo.deviceManufacturer);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "language", deviceInfo.language);
        PackageBuilder.addString(parameters, "country", deviceInfo.country);
        PackageBuilder.addString(parameters, "screen_size", deviceInfo.screenSize);
        PackageBuilder.addString(parameters, "screen_format", deviceInfo.screenFormat);
        PackageBuilder.addString(parameters, "screen_density", deviceInfo.screenDensity);
        PackageBuilder.addString(parameters, "display_width", deviceInfo.displayWidth);
        PackageBuilder.addString(parameters, "display_height", deviceInfo.displayHeight);
        PackageBuilder.addString(parameters, "hardware_name", deviceInfo.hardwareName);
        PackageBuilder.addString(parameters, "cpu_type", deviceInfo.abi);
        PackageBuilder.addString(parameters, "os_build", deviceInfo.buildName);
        PackageBuilder.addString(parameters, "vm_isa", deviceInfo.vmInstructionSet);
        PackageBuilder.addString(parameters, "mcc", Util.getMcc(adjustConfig.context));
        PackageBuilder.addString(parameters, "mnc", Util.getMnc(adjustConfig.context));
        PackageBuilder.addLong(parameters, "connectivity_type", Util.getConnectivityType(adjustConfig.context));
        PackageBuilder.addLong(parameters, "network_type", Util.getNetworkType(adjustConfig.context));
        fillPluginKeys(parameters);
    }

    private void injectDeviceInfoIds(Map<String, String> parameters) {
        deviceInfo.reloadDeviceIds(adjustConfig.context);

        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);

        if (deviceInfo.playAdId == null) {
            PackageBuilder.addString(parameters, "mac_sha1", deviceInfo.macSha1);
            PackageBuilder.addString(parameters, "mac_md5", deviceInfo.macShortMd5);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }
    }

    private void injectConfig(Map<String, String> parameters) {
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addBoolean(parameters, "device_known", adjustConfig.deviceKnown);

        PackageBuilder.addBoolean(parameters, "event_buffering_enabled", adjustConfig.eventBufferingEnabled);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);
        ContentResolver contentResolver = adjustConfig.context.getContentResolver();
        String fireAdId = Util.getFireAdvertisingId(contentResolver);
        PackageBuilder.addString(parameters, "fire_adid", fireAdId);
        Boolean fireTrackingEnabled = Util.getFireTrackingEnabled(contentResolver);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", fireTrackingEnabled);

        PackageBuilder.addString(parameters, "secret_id", adjustConfig.secretId);
        PackageBuilder.addString(parameters, "app_secret", adjustConfig.appSecret);
        if (adjustConfig.readMobileEquipmentIdentity) {
            TelephonyManager telephonyManager = (TelephonyManager)adjustConfig.context.getSystemService(Context.TELEPHONY_SERVICE);

            PackageBuilder.addString(parameters, "device_ids", Util.getTelephonyIds(telephonyManager));
            PackageBuilder.addString(parameters, "imeis", Util.getIMEIs(telephonyManager));
            PackageBuilder.addString(parameters, "meids", Util.getMEIDs(telephonyManager));
        }
    }

    private void injectActivityState(Map<String, String> parameters) {
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addLong(parameters, "session_count", activityStateCopy.sessionCount);
        PackageBuilder.addLong(parameters, "subsession_count", activityStateCopy.subsessionCount);
        PackageBuilder.addDuration(parameters, "session_length", activityStateCopy.sessionLength);
        PackageBuilder.addDuration(parameters, "time_spent", activityStateCopy.timeSpent);
    }

    private void injectCommonParameters(Map<String, String> parameters) {
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
    }

    private void injectAttribution(Map<String, String> parameters) {
        if (attribution == null) {
            return;
        }
        PackageBuilder.addString(parameters, "tracker", attribution.trackerName);
        PackageBuilder.addString(parameters, "campaign", attribution.campaign);
        PackageBuilder.addString(parameters, "adgroup", attribution.adgroup);
        PackageBuilder.addString(parameters, "creative", attribution.creative);
    }

    private void checkDeviceIds(Map<String, String> parameters) {
        if (!parameters.containsKey("mac_sha1")
                && !parameters.containsKey("mac_md5")
                && !parameters.containsKey("android_id")
                && !parameters.containsKey("gps_adid")) {
            logger.error("Missing device id's. Please check if Proguard is correctly set with Adjust SDK");
        }
    }

    private void fillPluginKeys(Map<String, String> parameters) {
        if (deviceInfo.pluginKeys == null) {
            return;
        }

        for (Map.Entry<String, String> entry : deviceInfo.pluginKeys.entrySet()) {
            PackageBuilder.addString(parameters, entry.getKey(), entry.getValue());
        }
    }

    private String getEventSuffix(AdjustEvent event) {
        if (event.revenue == null) {
            return Util.formatString("'%s'", event.eventToken);
        } else {
            return Util.formatString("(%.5f %s, '%s')", event.revenue, event.currency, event.eventToken);
        }
    }

    public static void addString(Map<String, String> parameters, String key, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }

        parameters.put(key, value);
    }

    public static void addLong(Map<String, String> parameters, String key, long value) {
        if (value < 0) {
            return;
        }

        String valueString = Long.toString(value);
        PackageBuilder.addString(parameters, key, valueString);
    }

    public static void addDateInMilliseconds(Map<String, String> parameters, String key, long value) {
        if (value <= 0) {
            return;
        }

        Date date = new Date(value);
        PackageBuilder.addDate(parameters, key, date);
    }

    public static void addDateInSeconds(Map<String, String> parameters, String key, long value) {
        if (value <= 0) {
            return;
        }

        Date date = new Date(value * 1000);
        PackageBuilder.addDate(parameters, key, date);
    }

    public static void addDate(Map<String, String> parameters, String key, Date value) {
        if (value == null) {
            return;
        }

        String dateString = Util.dateFormatter.format(value);
        PackageBuilder.addString(parameters, key, dateString);
    }

    public static void addDuration(Map<String, String> parameters, String key, long durationInMilliSeconds) {
        if (durationInMilliSeconds < 0) {
            return;
        }

        long durationInSeconds = (durationInMilliSeconds + 500) / 1000;
        PackageBuilder.addLong(parameters, key, durationInSeconds);
    }

    public static void addMapJson(Map<String, String> parameters, String key, Map<String, String> map) {
        if (map == null) {
            return;
        }

        if (map.size() == 0) {
            return;
        }

        JSONObject jsonObject = new JSONObject(map);
        String jsonString = jsonObject.toString();

        PackageBuilder.addString(parameters, key, jsonString);
    }

    public static void addBoolean(Map<String, String> parameters, String key, Boolean value) {
        if (value == null) {
            return;
        }

        int intValue = value ? 1 : 0;

        PackageBuilder.addLong(parameters, key, intValue);
    }

    public static void addDouble(Map<String, String> parameters, String key, Double value) {
        if (value == null) return;

        String doubleString = Util.formatString("%.5f", value);

        PackageBuilder.addString(parameters, key, doubleString);
    }
}
