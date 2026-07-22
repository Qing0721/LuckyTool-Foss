package com.fosstool.app.hook.scope.CorePatch;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;

public class ReturnConstant extends XC_MethodHook {
    private final XSharedPreferences prefs;
    private final String prefsKey;
    private final Object value;
    private final boolean defaultValue;

    public ReturnConstant(XSharedPreferences prefs, String prefsKey, Object value) {
        this(prefs, prefsKey, value, true);
    }

    public ReturnConstant(XSharedPreferences prefs, String prefsKey, Object value, boolean defaultValue) {
        this.prefs = prefs;
        this.prefsKey = prefsKey;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        prefs.reload();
        if (prefs.getBoolean(prefsKey, defaultValue)) {
            param.setResult(value);
        }
    }
}
