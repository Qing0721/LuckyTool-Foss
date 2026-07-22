package com.fosstool.app.ui.fragment

import android.content.pm.ResolveInfo
import com.fosstool.app.utils.AppIntentInfo

data class CandidateEntry(val info: AppIntentInfo, val resolveInfo: ResolveInfo)
