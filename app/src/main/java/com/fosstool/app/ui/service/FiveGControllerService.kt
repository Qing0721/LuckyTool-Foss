package com.fosstool.app.ui.service

import android.content.Intent
import android.os.RemoteException
import com.fosstool.app.IFiveGController
import com.fosstool.app.hook.utils.ITelephonyUtils
import com.fosstool.app.utils.A12
import com.fosstool.app.utils.SDK
import com.topjohnwu.superuser.ipc.RootService

class FiveGControllerService : RootService() {

    companion object {

        private val telephonyService by lazy {
            ITelephonyUtils(null).getService()
        }

        private val iTelephony by lazy {
            ITelephonyUtils(null).getInstance(telephonyService)
        }
    }

    override fun onBind(intent: Intent) = object : IFiveGController.Stub() {
        override fun checkCompatibility(subId: Int): Boolean {
            return try {
                if (SDK >= A12) {
                    ITelephonyUtils(null).let {
                        it.getAllowedNetworkTypesForReason(iTelephony, subId, it.reasonUser)
                            ?.let { it1 ->
                                it.setAllowedNetworkTypesForReason(
                                    iTelephony, subId,
                                    it.reasonUser, it1
                                )
                            }
                    } == true
                } else {
                    ITelephonyUtils(null).let {
                        it.getPreferredNetworkType(iTelephony, subId)?.let { it1 ->
                            it.setPreferredNetworkType(iTelephony, subId, it1)
                        }
                    } == true
                }
            } catch (_: Throwable) {
                false
            } catch (_: RemoteException) {
                false
            }
        }

        override fun getFiveGStatus(subId: Int): Boolean {
            return try {
                if (SDK >= A12) {
                    ITelephonyUtils(null).let {
                        it.getAllowedNetworkTypesForReason(
                            iTelephony, subId, it.reasonUser
                        )?.let { it1 ->
                            it1 and it.bitMaskNR != 0L
                        }
                    } == true
                } else {
                    ITelephonyUtils(null).let {
                        it.getPreferredNetworkType(iTelephony, subId)?.let { it1 ->
                            it1 == it.modeNR
                        }
                    } == true
                }
            } catch (_: Throwable) {
                false
            } catch (_: RemoteException) {
                false
            }
        }

        override fun setFiveGStatus(subId: Int, enabled: Boolean) {
            try {
                if (SDK >= A12) {
                    ITelephonyUtils(null).let {
                        var curTypes = it.getAllowedNetworkTypesForReason(
                            iTelephony, subId, it.reasonUser
                        )
                        curTypes = if (enabled) curTypes?.or(it.bitMaskNR)
                        else curTypes?.and(it.bitMaskNR.inv())
                        curTypes?.let { it1 ->
                            it.setAllowedNetworkTypesForReason(
                                iTelephony, subId, it.reasonUser, it1
                            )
                        }
                    }
                } else {
                    ITelephonyUtils(null).let {
                        it.setPreferredNetworkType(
                            iTelephony, subId, if (enabled) it.modeNR else it.modeLTE
                        )
                    }
                }
            } catch (_: Throwable) {

            } catch (_: RemoteException) {

            }
        }
    }
}
