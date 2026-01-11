package com.kredily.tracking.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kredily.tracking.util.LogTags
import com.kredily.tracking.util.SyncScheduler

/**
 * Created by Ritik on: 10/01/26
 */

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            /**--------- Restart periodic sync -------------*/
            SyncScheduler.scheduleLocationSync(context)

            /**--------- Trigger immediate sync ----------*/
            SyncScheduler.startOneTimeSync(context)
        }
    }
}