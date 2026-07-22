package com.fosstool.app.ui.service

import android.content.Intent
import android.os.Parcel
import android.os.RemoteException
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.current
import com.fosstool.app.IRefreshRateController
import com.fosstool.app.hook.utils.DisplayManagerUtils
import com.fosstool.app.hook.utils.ServiceManagerUtils
import com.fosstool.app.utils.DisplayMode
import com.fosstool.app.utils.LogUtils
import com.topjohnwu.superuser.ipc.RootService

class RefreshRateControllerService : RootService() {
    val tag = "RefreshRateControllerService"

    companion object {
        private const val serviceName = "SurfaceFlinger"
        private const val interfaceClazz = "android.ui.ISurfaceComposer"

        private val surfaceFlinger by lazy {
            ServiceManagerUtils(null).getService(serviceName)
        }
    }

    override fun onBind(intent: Intent) = object : IRefreshRateController.Stub() {
        override fun getRefreshRateDisplay(): Boolean {
            try {
                if (surfaceFlinger != null) {
                    val obtain = Parcel.obtain()
                    val obtain2 = Parcel.obtain()
                    obtain.writeInterfaceToken(interfaceClazz)
                    obtain.writeInt(2)
                    surfaceFlinger?.transact(1034, obtain, obtain2, 0)
                    val status = obtain2.readBoolean()
                    obtain2.recycle()
                    obtain.recycle()
                    return status
                }
                LogUtils.d(tag, "getRefreshRateDisplay", "surfaceFlinger is null")
                return false
            } catch (e: RemoteException) {
                LogUtils.d(tag, "getRefreshRateDisplay ", "$e")
                return false
            }
        }

        override fun setRefreshRateDisplay(status: Boolean) {
            try {
                if (surfaceFlinger != null) {
                    val obtain = Parcel.obtain()
                    obtain.writeInterfaceToken(interfaceClazz)
                    obtain.writeInt(if (status) 1 else 0)
                    surfaceFlinger?.transact(1034, obtain, null, 0)
                    obtain.recycle()
                    return
                }
                LogUtils.d(tag, "setRefreshRateDisplay", "surfaceFlinger is null")
            } catch (e: RemoteException) {
                LogUtils.d(tag, "setRefreshRateDisplay ", "$e")
            }
        }

        override fun getSupportModes(): ArrayList<DisplayMode> {
            val list = ArrayList<DisplayMode>()
            val context = this@RefreshRateControllerService
            return try {
                DisplayManagerUtils(null).apply {
                    val displayManager = getService(context)
                    LogUtils.d(tag, "getSupportModes", "${displayManager.javaClass}")
                    val display = displayManager.getDisplay(0)
                    LogUtils.d(tag, "getSupportModes", "${display.javaClass}")
                    val displayInfo = displayInfoClazz.buildOf { emptyParam() } ?: return list
                    LogUtils.d(tag, "getSupportModes", "${displayInfo.javaClass}")
                    if (display.getDisplayInfo(displayInfo) != true) return list
                    LogUtils.d(tag, "getSupportModes", "getDisplayInfo true")
                    val dynamicInfo = getDynamicDisplayInfo(displayInfo)
                    LogUtils.d(tag, "getSupportModes", "${dynamicInfo?.javaClass}")
                    val supportedDisplayModes = dynamicInfo?.current()?.field {
                        name = "supportedDisplayModes"
                    }?.array<Any>()
                    LogUtils.d(tag, "getSupportModes", "AllMode ${supportedDisplayModes?.toList()}")
                    supportedDisplayModes?.forEach {
                        LogUtils.d(tag, "getSupportModes", "Mode $it")
                        val id = it.current().field { name = "id" }.cast<Int>() ?: return@forEach
                        val width = it.current().field { name = "width" }.cast<Int>()
                        val height = it.current().field { name = "height" }.cast<Int>()
                        val xDpi = it.current().field { name = "xDpi" }.cast<Float>()
                        val yDpi = it.current().field { name = "yDpi" }.cast<Float>()
                        val refreshRate = it.current().field { name = "refreshRate" }.cast<Float>()
                            ?: readFloatFieldReflect(it, "refreshRate")
                        val appVsyncOffsetNanos = it.current().field {
                            name = "appVsyncOffsetNanos"
                        }.cast<Long>()
                        val presentationDeadlineNanos = it.current().field {
                            name = "presentationDeadlineNanos"
                        }.cast<Long>()
                        val group = it.current().field { name = "group" }.cast<Int>()
                        val mode = DisplayMode(
                            id, width, height, xDpi, yDpi,
                            refreshRate, appVsyncOffsetNanos, presentationDeadlineNanos, group
                        )
                        list.add(id, mode)
                        LogUtils.d(tag, "getSupportModes", "Mode is add")
                    }
                }
                LogUtils.d(tag, "getSupportModes", "Size ${list.size}")
                list
            } catch (e: Throwable) {
                LogUtils.d(tag, "getSupportModes", " $e")
                list
            }
        }

        override fun setRefreshRateMode(modeId: Int) {
            try {
                if (surfaceFlinger != null) {
                    val obtain = Parcel.obtain()
                    obtain.writeInterfaceToken(interfaceClazz)
                    obtain.writeInt(modeId)
                    surfaceFlinger?.transact(1035, obtain, null, 0)
                    obtain.recycle()
                    return
                }
                LogUtils.d(tag, "setRefreshRateMode", "surfaceFlinger is null")
            } catch (e: Throwable) {
                LogUtils.d(tag, "setRefreshRateMode", "$e")
            }
        }

        override fun resetRefreshRateMode() {
            try {
                setRefreshRateMode(-1)
            } catch (e: Throwable) {
                LogUtils.d(tag, "resetRefreshRateMode", "$e")
            }
        }
    }

    private fun readFloatFieldReflect(obj: Any, fieldName: String): Float? {
        var cls: Class<*>? = obj.javaClass
        while (cls != null) {
            try {
                val field = cls.getDeclaredField(fieldName)
                field.isAccessible = true
                val value = field.get(obj)
                return when (value) {
                    is Float -> value
                    is Number -> value.toFloat()
                    else -> null
                }
            } catch (_: NoSuchFieldException) {
                val matched = cls.declaredFields.firstOrNull {
                    it.name.equals(fieldName, ignoreCase = true) &&
                        (it.type == Float::class.javaPrimitiveType || it.type == Float::class.javaObjectType)
                }
                if (matched != null) {
                    try {
                        matched.isAccessible = true
                        val value = matched.get(obj)
                        return when (value) {
                            is Float -> value
                            is Number -> value.toFloat()
                            else -> null
                        }
                    } catch (_: Throwable) {
                    }
                }
            }
            cls = cls.superclass
        }
        return null
    }
}
