/*
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ceco.gm2.gravitybox;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import com.ceco.gm2.gravitybox.Utils.MethodState;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ModLowBatteryWarning {
    private static final String TAG = "GB:ModLowBatteryWarning";
    public static final String PACKAGE_NAME = "com.android.systemui";
    private static final String CLASS_POWER_UI = "com.android.systemui.power.PowerUI";
    private static final String CLASS_LIGHT_SERVICE_LIGHT = "com.android.server.LightsService$Light";
    private static final String CLASS_BATTERY_SERVICE_LED = "com.android.server.BatteryService$Led";
    public static final boolean DEBUG = false;

    private static ThreadLocal<MethodState> mUpdateLightsMethodState;

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
    }

    public static void initZygote(final XSharedPreferences prefs) {
        if (DEBUG) log("initZygote");
        try {
            final Class<?> lightServiceClass = XposedHelpers.findClass(CLASS_LIGHT_SERVICE_LIGHT, null);
            final Class<?> batteryServiceClass = XposedHelpers.findClass(CLASS_BATTERY_SERVICE_LED, null);
            mUpdateLightsMethodState = new ThreadLocal<MethodState>();
            mUpdateLightsMethodState.set(MethodState.UNKNOWN);

            XposedHelpers.findAndHookMethod(batteryServiceClass, "updateLightsLocked", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    mUpdateLightsMethodState.set(MethodState.METHOD_ENTERED);
                    if (DEBUG) {
                        log("BatteryService LED: updateLightsLocked ENTERED");
                        // for debugging purposes - simulate low battery
                        Object o = XposedHelpers.getSurroundingThis(param.thisObject);
                        XposedHelpers.setIntField(o, "mBatteryLevel", 10);
                    }
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mUpdateLightsMethodState.set(MethodState.METHOD_EXITED);
                    if (DEBUG) {
                        log("BatteryService LED: updateLightsLocked EXITED");
                    }
                }
            });

            XposedHelpers.findAndHookMethod(lightServiceClass, "setFlashing", 
                    int.class, int.class, int.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (mUpdateLightsMethodState.get() != null &&
                            mUpdateLightsMethodState.get().equals(MethodState.METHOD_ENTERED)) {
                        prefs.reload();
                        if (prefs.getBoolean(GravityBoxSettings.PREF_KEY_FLASHING_LED_DISABLE, false)) {
                            if (DEBUG) {
                                log("LightService: setFlashing called from BatteryService - ignoring");
                            }
                            XposedHelpers.callMethod(param.thisObject, "turnOff");
                            param.setResult(null);
                        }
                    }
                }
            });

            XposedHelpers.findAndHookMethod(lightServiceClass, "setColor", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (mUpdateLightsMethodState.get() != null &&
                            mUpdateLightsMethodState.get().equals(MethodState.METHOD_ENTERED)) {
                        prefs.reload();
                        if (prefs.getBoolean(GravityBoxSettings.PREF_KEY_CHARGING_LED_DISABLE, false)) {
                            if (DEBUG) {
                                log("LightService: setColor called from BatteryService - ignoring");
                            }
                            XposedHelpers.callMethod(param.thisObject, "turnOff");
                            param.setResult(null);
                        }
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    static void init(final XSharedPreferences prefs, ClassLoader classLoader) {
        try {
            if (DEBUG) log("init");

            Class<?> classPowerUI = findClass(CLASS_POWER_UI, classLoader);

            // for debugging purposes - simulate low battery even if it's not
            if (DEBUG) {
                findAndHookMethod(classPowerUI, "findBatteryLevelBucket", int.class, new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(-1);
                    }

                });
            }

            findAndHookMethod(classPowerUI, "playLowBatterySound", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    prefs.reload();
                    final int batteryWarningPolicy = Integer.valueOf(
                            prefs.getString(GravityBoxSettings.PREF_KEY_LOW_BATTERY_WARNING_POLICY, "3"));
                    final boolean playSound = ((batteryWarningPolicy & GravityBoxSettings.BATTERY_WARNING_SOUND) != 0);

                    if (DEBUG) log("playLowBatterySound called; playSound = " + playSound);
                    
                    if (!playSound)
                        param.setResult(null);
                }

            });

            findAndHookMethod(classPowerUI, "showLowBatteryWarning", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    prefs.reload();
                    final int batteryWarningPolicy = Integer.valueOf(
                            prefs.getString(GravityBoxSettings.PREF_KEY_LOW_BATTERY_WARNING_POLICY, "3"));
                    final boolean showPopup = ((batteryWarningPolicy & GravityBoxSettings.BATTERY_WARNING_POPUP) != 0);
                    
                    if (DEBUG) log("showLowBatteryWarning called; showPopup = " + showPopup);

                    if (!showPopup)
                        param.setResult(null);
                }

            });

        } catch (Throwable t) { XposedBridge.log(t); }
    }
}