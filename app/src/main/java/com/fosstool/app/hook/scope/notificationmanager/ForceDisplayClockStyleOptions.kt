package com.fosstool.app.hook.scope.notificationmanager

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.HashMap

object ForceDisplayClockStyleOptions : YukiBaseHooker() {
    override fun onHook() {
        val provider = "com.oplus.keyguard.keyguardsettings.KeyguardLauncherPageProvider"
            .toClassOrNull(appClassLoader) ?: return
        provider.method { name = "initKeyguardLandClockPf" }.ignored().hook {
            before {
                if (!isFlavorTwoDevice()) return@before
                val list = args.getOrNull(0) as? ArrayList<Any?> ?: return@before
                val host = instance ?: return@before
                val context = runCatching {
                    XposedHelpers.callMethod(host, "getContext") as? Context
                }.getOrNull()
                val title = context?.let { ctx ->
                    val id = ctx.resources.getIdentifier(
                        "oplus_keyguard_land_clock_type_title",
                        "string",
                        packageName,
                    )
                    if (id != 0) runCatching { ctx.getString(id) }.getOrNull() else null
                }
                val bean = runCatching {
                    XposedHelpers.callMethod(
                        host,
                        "createPerfrenceBean",
                        "TYPE_PREFRENCE_JUMP",
                        "key_keyguard_land_clock_screen",
                        70,
                        title,
                        "key_keyguard_category",
                    )
                }.getOrNull() ?: return@before
                runCatching {
                    XposedHelpers.callMethod(bean, "setIntentPackage", "com.oplus.notificationmanager")
                }
                runCatching {
                    XposedHelpers.callMethod(
                        bean,
                        "setIntentClass",
                        "com.oplus.keyguard.keyguardsettings.KeyguardLandClockActivity",
                    )
                }
                val map = runCatching {
                    XposedHelpers.getObjectField(host, "preferenceHashMap") as? HashMap<Any?, Any?>
                }.getOrNull()
                if (map != null) {
                    runCatching {
                        XposedHelpers.callMethod(
                            host,
                            "addPreferenceMap",
                            map,
                            "key_keyguard_land_clock_screen",
                            bean,
                        )
                    }
                }
                if (!list.contains(bean)) list.add(bean)

                resultNull()
            }
        }
    }

    private fun isFlavorTwoDevice(): Boolean {
        val client = "com.oplus.keyguard.common.KeyguardSettingProviderClient".toClassOrNull(appClassLoader) ?: return false
        client.findField("isFlavorTwoDevice")?.let { f ->
            runCatching {
                f.isAccessible = true
                val v = f.get(null)
                if (v is Boolean) return v
            }
        }
        client.findMethod("isFlavorTwoDevice")?.let { m ->
            runCatching {
                val v = m.invoke(null)
                if (v is Boolean) return v
            }
            runCatching {
                val inst = runCatching {
                    XposedHelpers.callStaticMethod(client, "getInstance")
                }.getOrNull()
                val v = m.invoke(inst)
                if (v is Boolean) return v
            }
        }
        return false
    }

    private fun Class<*>.findField(name: String): Field? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.firstOrNull { it.name == name }?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }

    private fun Class<*>.findMethod(name: String): Method? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredMethods.firstOrNull { it.name == name }?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }
}
