package com.aurelian.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * 视频编辑状态草稿数据模型
 */
data class DraftData(
    val videoUri: String,
    val title: String,
    val bio: String,
    val selectedFilter: String,
    val selectedAudio: String,
    val sliderStart: Float,
    val sliderEnd: Float,
    val timestamp: Long
)

/**
 * 视频剪辑草稿箱管理类
 * 提供简单的基于 SharedPreferences 的草稿保存与恢复功能
 */
object DraftManager {
    private const val PREFS_NAME = "AurelianDraftPrefs"
    private const val KEY_DRAFT_DATA = "draft_data"

    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 将当前编辑状态保存为草稿
     */
    fun saveDraft(context: Context, draft: DraftData) {
        val json = gson.toJson(draft)
        getPrefs(context).edit().putString(KEY_DRAFT_DATA, json).apply()
    }

    /**
     * 获取草稿，如果没有则返回 null
     */
    fun getDraft(context: Context): DraftData? {
        val json = getPrefs(context).getString(KEY_DRAFT_DATA, null)
        return if (json != null) {
            try {
                gson.fromJson(json, DraftData::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * 检查是否存在草稿
     */
    fun hasDraft(context: Context): Boolean {
        return getPrefs(context).contains(KEY_DRAFT_DATA)
    }

    /**
     * 清除本地草稿
     */
    fun clearDraft(context: Context) {
        getPrefs(context).edit().remove(KEY_DRAFT_DATA).apply()
    }
}
