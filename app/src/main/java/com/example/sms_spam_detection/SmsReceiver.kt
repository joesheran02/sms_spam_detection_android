    package com.example.sms_spam_detection
    
    import android.app.Application
    import android.app.Notification
    import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.app.PendingIntent
    import android.content.BroadcastReceiver
    import android.content.Context
    import android.content.Intent
    import android.os.Build
    import android.provider.Telephony
    import android.util.Log
    import androidx.core.app.NotificationCompat
    import androidx.core.content.ContextCompat
    import androidx.lifecycle.ViewModelProvider
    import androidx.lifecycle.ViewModelStoreOwner
    import com.fasterxml.jackson.databind.ObjectMapper
    import com.fasterxml.jackson.core.type.TypeReference
    import org.tensorflow.lite.Interpreter
    import java.io.FileInputStream
    import java.nio.ByteBuffer
    import java.nio.ByteOrder
    import java.nio.channels.FileChannel
    import java.util.regex.Pattern
    import com.example.sms_spam_detection.MyNotification
    import com.example.sms_spam_detection.ui.notifications.NotificationsFragment
    import com.example.sms_spam_detection.ui.notifications.NotificationsViewModel
    import com.google.gson.Gson
    import com.google.gson.reflect.TypeToken
    
    class SmsReceiver : BroadcastReceiver() {
    
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
                // Process incoming SMS messages here
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (message in messages) {
                    // Process each message
                    val sender = message.originatingAddress
                    val body = message.messageBody
                    // Add your spam detection and reporting logic here
                    Log.d("SmsReceiver", "Received SMS from $sender: $body")
                    val additionalFeatures: List<Float> = createAdditionalFeatures(body)
                    val cleanedText = cleanText(body)
                    Log.d("Additional Features", additionalFeatures.toString())
                    Log.d("Cleaned text", cleanedText)
    
                    // Determine if the message is spam (replace this with your spam detection logic)
                    val spamProb = spamProbability(context, cleanedText, additionalFeatures)
                    val isSpam = spamProb > 0.5
                    Log.d("Spam prob", spamProb.toString())
    
                    // Show a notification based on whether the message is spam or not
                    showNotification(context, sender, body, spamProb, isSpam)
    
                    val currentTimeMillis = System.currentTimeMillis()

                    // Create a new Notification object
                    val newNotification = sender?.let { MyNotification(it, body, spamProb, isSpam, currentTimeMillis) }

                    val updateAction = "com.example.counter.UPDATE_NOTIFICATION"

                    // Broadcast to the widget
                    val updateWidgetsIntent = Intent(context, MyAppWidgetProvider::class.java).apply {
                        action = updateAction
                    }
                    context.sendBroadcast(updateWidgetsIntent)

                    // Get the NotificationsViewModel and update the list
//                    val viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
//                        .create(NotificationsViewModel::class.java)

                    val viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
                        .create(NotificationsViewModel::class.java)

    
                    // If the ViewModel's notifications are empty, load from SharedPreferences
                    if (viewModel.notifications.value.isNullOrEmpty()) {
                        val sharedPreferences = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
                        val notificationsJson = sharedPreferences.getString("notifications", null)
    
                        val notificationsList: List<MyNotification> = if (notificationsJson != null) {
                            Gson().fromJson(notificationsJson, object : TypeToken<List<MyNotification>>() {}.type)
                        } else {
                            emptyList()
                        }
                        // Update the ViewModel with the loaded notifications
                        viewModel.updateNotifications(notificationsList)
                    }
                    else {
                        Log.d("Not empty", "initially")
                    }
    
                    Log.d("Before line 61", viewModel.notifications.value.toString())
                    viewModel.updateNotifications(
                        (viewModel.notifications.value.orEmpty() + newNotification) as List<MyNotification>
                    )
                    Log.d("After line 63", viewModel.notifications.value.toString())
                }
            }
        }


        fun createAdditionalFeatures(message: String): List<Float> {
            fun countSymbols(message: String): Double {
                val symbols = """!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~""".toSet()
                val count = message.count { it in symbols }
                return count.toDouble() / message.length
            }
    
            fun countCapitals(message: String): Double {
                val count = message.count { it.isUpperCase() }
                return count.toDouble() / message.length
            }
    
            fun countNumbers(message: String): Double {
                val digitCount = message.count { it.isDigit() }
                return digitCount.toDouble() / message.length
            }
    
            val messageLength = message.length.toFloat()
            val tokenLength = message.split(" ").size.toFloat()
            val numSymbols =  countSymbols(message).toFloat()
            val numCapitals = countCapitals(message).toFloat()
            val numNumbers = countNumbers(message).toFloat()
    
            return listOf(messageLength, tokenLength, numSymbols, numCapitals, numNumbers)
        }
    
        fun cleanText(message: String): String {
    
            // Replace all URLs with <URL> tag
            fun replaceUrls(message: String): String {
                val urlPattern = Pattern.compile("https?://.*\\S+|www\\.\\S+")
                val replacedTokens = message.split(" ").map { token ->
                    if (urlPattern.matcher(token).matches()) "urltoken" else token
                }
                return replacedTokens.joinToString(" ")
            }
    
            // Punctuation removal
            fun processText(message: String): String {
                return message.filter { it !in """${Regex.escape("""!"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~""")}""" }.toLowerCase()
            }
    
            // Replace all leetspeak with <LEET> tag
            fun replaceLeet(message: String): String {
                return message.split(" ").joinToString(" ") { token ->
                    if (Regex("[^\\w\\s]|\\d").containsMatchIn(token) && !token.all { it.isDigit() }) "leettoken" else token
                }
            }
    
            // Replace all numbers with <NUMBER> tag
            fun replaceNumbers(message: String): String {
                return message.split(" ").joinToString(" ") { token ->
                    if (token.all { it.isDigit() }) "numbertoken" else token
                }
            }
    
            // Remove all non-letters
            fun replaceNonLetters(message: String): String {
                val lowercaseAlphabet = ('a'..'z').joinToString("") + " "
                var processedWords = message.filter { it in lowercaseAlphabet }
                processedWords = processedWords.split(" ").joinToString(" ") { token ->
                    when (token) {
                        "urltoken" -> "<URL>"
                        "numbertoken" -> "<NUMBER>"
                        "leettoken" -> "<LEET>"
                        else -> token
                    }
                }
                processedWords = processedWords.split(" ").filter { it.length > 1 }.joinToString(" ")
                return processedWords
            }
            return replaceNonLetters(replaceNumbers(replaceLeet(processText(replaceUrls(message)))))
        }
    
        fun loadWordIndex(context: Context): Map<String, Int> {
            val wordIndexStream = context.assets.open("word_index.json")
            val wordIndex  = ObjectMapper().readValue(
                wordIndexStream.bufferedReader(),
                object: TypeReference<Map<String, Int>>() {}
            )
            wordIndexStream.close()
            return wordIndex
        }
    
        class Tokenizer(private val wordIndex: Map<String, Int>) {
            fun textsToSequences(texts: List<String>): List<List<Int>> {
                return texts.map { text ->
                    text.split(" ").map { word -> wordIndex.getOrDefault(word, 1) }
                }
            }
        }
    
        private fun tokenizeAndPadSequence(
            message: String,
            wordIndex: Map<String, Int>,
            maxLen: Int
        ): List<Int> {
            val tokenizer = Tokenizer(wordIndex)
    
            Log.d("sequence", tokenizer.textsToSequences(listOf(message)).toString())
            // Tokenize the message
            val sequence = tokenizer.textsToSequences(listOf(message))[0]
    
            // Pad the sequence
            return padSequence(sequence, maxLen)
        }
    
        fun padSequence(sequence: List<Int>, maxLen: Int): List<Int> {
            val sequenceLength = minOf(sequence.size, maxLen)
            val paddedSequence = MutableList(0) { 0 }
            val padding = MutableList(maxLen) { 0 }
            paddedSequence.addAll(padding.subList(0, maxLen - sequenceLength))
            paddedSequence.addAll(sequence.subList(0, sequenceLength))
    
            Log.d("Padded list", paddedSequence.toString())
            Log.d("Sequence", sequence.toString())
            return paddedSequence
        }
    
        private fun loadTFLiteModel(context: Context, modelName: String): Interpreter {
            // Load the TensorFlow Lite model
            val modelFileDescriptor = context.assets.openFd(modelName)
            val modelFileInputStream = FileInputStream(modelFileDescriptor.fileDescriptor)
            val modelByteBuffer = modelFileInputStream.channel.map(
                FileChannel.MapMode.READ_ONLY,
                modelFileDescriptor.startOffset,
                modelFileDescriptor.declaredLength
            )
            val options = Interpreter.Options()
            return Interpreter(modelByteBuffer, options)
        }
    
        private fun runInference(model: Interpreter, textInputSequence: List<Int>, additionalFeatures: List<Float>): Float {
            // Convert the input sequence and additional features to FloatArrays
            val textInputArray = textInputSequence.map { it.toFloat() }.toFloatArray()
            val additionalFeaturesArray = additionalFeatures.toFloatArray()
    
            // Run inference
            val outputs: MutableMap<Int, Any> = mutableMapOf()
            outputs[0] = Array(1) { FloatArray(1) }
    
            // Prepare the input tensors
            val inputTensors = model.inputTensorCount
            if (inputTensors != 2) {
                throw IllegalArgumentException("Expected 2 input tensors, but found $inputTensors")
            }
    
            val textInputBuffer = ByteBuffer.allocateDirect(textInputArray.size * 4)
                .order(ByteOrder.nativeOrder())
            val additionalFeaturesBuffer = ByteBuffer.allocateDirect(additionalFeaturesArray.size * 4)
                .order(ByteOrder.nativeOrder())
    
            textInputBuffer.asFloatBuffer().put(textInputArray)
            additionalFeaturesBuffer.asFloatBuffer().put(additionalFeaturesArray)
    
            // Run inference
            model.runForMultipleInputsOutputs(
                arrayOf(textInputBuffer, additionalFeaturesBuffer),
                outputs
            )
    
            // Extract the output
            val outputArray = (outputs[0] as Array<*>)[0] as FloatArray
            if (outputArray.size != 1) {
                throw IllegalArgumentException("Expected output array size of 1, but found ${outputArray.size}")
            }
            Log.d("WTF IS THIS", outputArray[0].toString())
            return outputArray[0]
        }
    
    
        fun spamProbability(
            context: Context,
            message: String,
            additionalFeatures: List<Float>
        ): Float {
            // Implement your spam detection logic here
            // This is a placeholder; replace it with your actual logic
            val wordIndex = loadWordIndex(context)
            val maxLen = 165
            val paddedSequence = tokenizeAndPadSequence(message, wordIndex, maxLen)
    
            // Load TensorFlow Lite model
            val model = loadTFLiteModel(context, "model.tflite")
    
            // Run inference
            // Decide based on the inference result (you might need to define a threshold)
            return runInference(model, paddedSequence, additionalFeatures)// > 0.5
        }
    
        private fun showNotification(context: Context, sender: String?, body: String?, spamProb: Float, isSpam: Boolean) {
            // Implement notification creation and display here
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationColor = ContextCompat.getColor(context, R.color.icon_yellow)
            val notificationId = System.currentTimeMillis().toInt()
    
            // Create a notification channel for Android Oreo and above
            createNotificationChannel(context)
    
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("destination", "notifications")
                putExtra("notification_id", notificationId)
                putExtra("group_summary_id", sender.hashCode())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    
            // Build the notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.chick)
                .setContentTitle("From: $sender")
                .setColor(notificationColor)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Body: $body\nSpam: ${if (isSpam) "Yes" else "No"}\nProbability of Spam: $spamProb")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(sender)  // Set the same group key for all notifications related to the same sender
                .setContentIntent(pendingIntent)
    
            // Show the notification with a unique ID
            notificationManager.notify(notificationId, builder.build())
    
            // Optionally, create a summary notification for the group
            createGroupSummaryNotification(context, sender)
        }
    
        private fun createGroupSummaryNotification(context: Context, sender: String?) {
            // Create a summary notification for the group
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationColor = ContextCompat.getColor(context, R.color.icon_yellow)
    
            // Build the summary notification
            val summaryBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.chick)
                .setContentTitle("SMS Detection")
                .setGroup(sender)
                .setColor(notificationColor)
                .setGroupSummary(true)  // Mark as a summary notification
    
            // Show the summary notification with a unique ID
            //val summaryNotificationId = "SMS_DETECTION_GROUP".hashCode()
            notificationManager.notify(sender.hashCode(), summaryBuilder.build())
        }
    
    
        private fun createNotificationChannel(context: Context) {
            // Create a notification channel for Android Oreo and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "SMS Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    
        companion object {
            private const val CHANNEL_ID = "sms_channel"
        }
    
    }