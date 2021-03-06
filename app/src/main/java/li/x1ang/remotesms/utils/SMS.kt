package li.x1ang.remotesms.utils


import java.text.SimpleDateFormat
import java.util.*

import kotlin.concurrent.thread

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SmsMessage
import android.telephony.TelephonyManager

import cz.msebera.android.httpclient.HttpStatus
import cz.msebera.android.httpclient.client.methods.HttpPost
import cz.msebera.android.httpclient.entity.StringEntity
import cz.msebera.android.httpclient.impl.client.HttpClients
import cz.msebera.android.httpclient.util.EntityUtils


import li.x1ang.remotesms.App
import li.x1ang.remotesms.DINGTALK_ENDPOINT
import li.x1ang.remotesms.PhoneMessage

/**
 * 短信工具类
 */


/**
 * 通过webhook上传短信
 */



class SmsHelper(context: Context?) {
    private val DATE_FORMAT = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA)

    var context = context
    var receiver_id = "123*3570"

    init {
    //获取本机手机号码,由于系统限制，并非所有手机都能获取到
        receiver_id = getLocalPhoneNumber()
    }

    fun sendMsg(msg: String) {
        thread {
            val httpclient = HttpClients.createDefault()
            val httpPost = HttpPost(DINGTALK_ENDPOINT)
            httpPost.addHeader("Content-Type", "application/json; charset=utf-8")

            val se = StringEntity(msg, "utf-8")
            httpPost.entity = se

            val response = httpclient.execute(httpPost)
            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                val result = EntityUtils.toString(response.entity, "utf-8")
                log("sendMsg = $result")
                App.msgSend++
            }

            notify(context!!)
        }
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    fun getLocalPhoneNumber(): String {
        val tm = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.line1Number
    }

    fun genMsgMarkdown(phoneMessage: PhoneMessage): String {
        var toReturn = ""
        with(phoneMessage) {
            toReturn = """{
                "msgtype" : "markdown",
                "markdown" :
                    {
                        "title" : "转发自: $receiver_id",
                        "text"  : "#### 时间: ${DATE_FORMAT.format(timestamp)}\n
                                   #### 来自: $sender_id\n
                                   #### 正文: \n
                                   > $content "
                    }
            }""".trimIndent()
        }
        return toReturn
    }

    fun getMsgBody(messages: List<SmsMessage>): String {
        val messageBody = StringBuilder()

        messages.map {
            messageBody.append(it.messageBody)
        }
        return messageBody.toString()
    }


}


/*
val nameCache = HashMap<String, String>()

fun getContactName(contentResolver: ContentResolver?, number: String,
                   cache: MutableMap<String, String>? = nameCache): String? {
    if (cache?.containsKey(number) == true) {
        return cache[number]
    }

    val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
    contentResolver?.query(uri, arrayOf(PhoneLookup._ID, PhoneLookup.DISPLAY_NAME), null, null, null).use { cursor ->
        if (cursor != null && cursor.count > 0) {
            cursor.moveToNext()
            val name = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME))
            cursor.close()
            cache?.set(number, name)
            return name
        }
    }

    return null
}
*/