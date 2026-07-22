package com.fosstool.app.hook.scope.systemui

import android.view.MotionEvent
import android.view.ViewGroup
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.closeScreen
import kotlin.math.abs

object DoubleClickLockScreen : YukiBaseHooker() {
    override fun onHook() {
        var curTouchTime = 0L
        var curTouchX = 0F
        var curTouchY = 0F

        "com.android.systemui.statusbar.phone.PhoneStatusBarView".toClass().apply {
            method { name = "onFinishInflate" }.hook {
                before {
                    val statusbar = instance<ViewGroup>()
                    statusbar.setOnTouchListener { v, event ->
                        if (event.action != MotionEvent.ACTION_DOWN) return@setOnTouchListener false
                        val lastTouchTime = curTouchTime
                        val lastTouchX = curTouchX
                        val lastTouchY = curTouchY
                        curTouchTime = System.currentTimeMillis()
                        curTouchX = event.x
                        curTouchY = event.y
                        if (curTouchTime - lastTouchTime < 250L && abs(curTouchX - lastTouchX) < 100f && abs(
                                curTouchY - lastTouchY
                            ) < 100f
                        ) {
                            closeScreen(v.context)
                            curTouchTime = 0L
                            curTouchX = 0F
                            curTouchY = 0F
                        }
                        v.performClick()
                        false
                    }
                }
            }
        }
    }
}
