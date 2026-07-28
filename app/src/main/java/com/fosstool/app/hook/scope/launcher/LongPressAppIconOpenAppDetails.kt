package com.fosstool.app.hook.scope.launcher

import android.view.View
import android.widget.TextView
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.openAppDetailIntent
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import java.lang.reflect.Method

object LongPressAppIconOpenAppDetails : YukiBaseHooker() {
    override fun onHook() {
        "com.android.quickstep.views.OplusTaskViewImpl".toClassOrNull(appClassLoader)?.let { clazz ->
            listOf(1, 2).forEach { count ->
                clazz.method { name = "setIcon"; paramCount = count }.ignored().hook {
                    after {
                        val headerView = clazz.findMethod("getHeaderView")?.invoke(instance)
                            ?: return@after
                        val iconView = runCatching {
                            headerView.javaClass.methods.firstOrNull { it.name == "getTaskIcon" && it.parameterCount == 0 }
                                ?.invoke(headerView) as? View
                        }.getOrNull() ?: return@after
                        val titleFieldName = if (SDK >= A13) "titleTv" else "mTitleView"
                        val titleView = runCatching {
                            var c: Class<*>? = headerView.javaClass
                            while (c != null) {
                                val f = runCatching { c!!.getDeclaredField(titleFieldName) }.getOrNull()
                                if (f != null) {
                                    f.isAccessible = true
                                    return@runCatching f.get(headerView) as? TextView
                                }
                                c = c.superclass
                            }
                            null
                        }.getOrNull() ?: return@after
                        val task = clazz.findMethod("getTask")?.invoke(instance)
                            ?: return@after
                        val key = runCatching {
                            task.javaClass.getDeclaredField("key").apply { isAccessible = true }.get(task)
                        }.getOrNull() ?: return@after
                        val packName = runCatching {
                            key.javaClass.methods.firstOrNull { it.name == "getPackageName" && it.parameterCount == 0 }
                                ?.invoke(key) as? String
                        }.getOrNull() ?: return@after
                        val userId = runCatching {
                            key.javaClass.getDeclaredField("userId").apply { isAccessible = true }.getInt(key)
                        }.getOrNull() ?: 0
                        iconView.setLongClick(packName, userId)
                        titleView.setLongClick(packName, userId)
                    }
                }
            }
        }

        "com.oplus.quickstep.dock.DockIconView".toClassOrNull(appClassLoader)?.let { clazz ->
            clazz.method { name = "setIcon"; paramCount = 1 }.ignored().hook {
                after {
                    val task = clazz.findMethod("getTask")?.invoke(instance) ?: return@after
                    val key = runCatching {
                        task.javaClass.getDeclaredField("key").apply { isAccessible = true }.get(task)
                    }.getOrNull() ?: return@after
                    val packName = runCatching {
                        key.javaClass.methods.firstOrNull { it.name == "getPackageName" && it.parameterCount == 0 }
                            ?.invoke(key) as? String
                    }.getOrNull() ?: return@after
                    val userId = runCatching {
                        key.javaClass.getDeclaredField("userId").apply { isAccessible = true }.getInt(key)
                    }.getOrNull() ?: 0
                    (instance as? View)?.setLongClick(packName, userId)
                }
            }
        }
    }

    private fun View.setLongClick(packName: String?, userId: Int? = 0) {
        setOnLongClickListener {
            packName?.let { its -> it.context.openAppDetailIntent(its, userId) }
            true
        }
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
