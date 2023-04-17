package com.translate.translator.voice.translation.dictionary.all.language.services

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.constraintlayout.widget.Group
import androidx.core.util.Pair
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.translate.translator.voice.translation.dictionary.all.language.TranslatorDrawOverActivity
import com.translate.translator.voice.translation.dictionary.all.language.util.NotificationUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import com.translate.translator.voice.translation.dictionary.all.language.util.callback.CloseEvent
import android.view.MotionEvent

import androidx.core.view.isVisible
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.util.LanguageManager


class ScreenCaptureService : Service(), CloseEvent{
    private var mMediaProjection: MediaProjection? = null
    private var mStoreDir: String? = null
    private var mImageReader: ImageReader? = null
    private var mHandler: Handler? = null
    private var mDisplay: Display? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mDensity = 0
    private var mWidth = 0
    private var mHeight = 0
    private var mRotation = 0
    private var mOrientationChangeCallback: OrientationChangeCallback? = null




    private var mWindowManager: WindowManager? = null
    private var view: View? = null



    private lateinit var mCamFab: FloatingActionButton
    private lateinit var mAllFab: FloatingActionButton
    private lateinit var mOneFab: FloatingActionButton


    private var toggle: Boolean = false
    private var clickType: Boolean = false

    private var resultCode: Int = 0
    private var data: Intent? = null

    private var oneClick = false


    private inner class ImageAvailableListener : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader) {
            var fos: FileOutputStream? = null
            var bitmap: Bitmap? = null
            try {
                mImageReader!!.acquireLatestImage().use { image ->
                    if (image != null) {
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * mWidth

                        // create bitmap
                        bitmap = Bitmap.createBitmap(
                            mWidth + rowPadding / pixelStride,
                            mHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap?.copyPixelsFromBuffer(buffer)



                        // write bitmap to a file
                        if(IMAGES_PRODUCED == 0){
                            fos =
                                FileOutputStream("$mStoreDir/myscreen.png")
                            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                            IMAGES_PRODUCED++
                            Log.e(
                                TAG,
                                "captured image: " + IMAGES_PRODUCED
                            )
                            stopProjection()
                            startIntent(clickType)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (fos != null) {
                    try {
                        fos!!.close()
                    } catch (ioe: IOException) {
                        ioe.printStackTrace()
                    }
                }
                if (bitmap != null) {
                    bitmap!!.recycle()
                }
            }
        }
    }

    private inner class OrientationChangeCallback internal constructor(context: Context?) :
        OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            val rotation = mDisplay!!.rotation
            if (rotation != mRotation) {
                mRotation = rotation
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay!!.release()
                    if (mImageReader != null) mImageReader!!.setOnImageAvailableListener(null, null)

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inner class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            Log.e(TAG, "stopping projection.")
            mHandler!!.post {
                if (mVirtualDisplay != null) mVirtualDisplay!!.release()
                if (mImageReader != null) mImageReader!!.setOnImageAvailableListener(null, null)
                if (mOrientationChangeCallback != null) mOrientationChangeCallback!!.disable()
                mMediaProjection!!.unregisterCallback(this@MediaProjectionStopCallback)
                mMediaProjection = null
                IMAGES_PRODUCED = 0
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }




    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()


        //Inflate the floating view layout we created
        setTheme(R.style.Theme_ELITE_TRANSLATOR)
        view = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)

        val layoutType: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        //Add the view to the window.
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        params.gravity =
            Gravity.TOP or Gravity.LEFT //Initially view will be added to top-left corner
        params.x = 0
        params.y = 100

        //Add the view to the window
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(view, params)

        TranslatorDrawOverActivity.reset = this


        mCamFab = view!!.findViewById(R.id.floating_action_button)
        mAllFab = view!!.findViewById(R.id.all_mini_fab)
       mOneFab = view!!.findViewById(R.id.one_mini_fab)
        val group = view!!.findViewById<Group>(R.id.group)

       
/*        val longPressDetector = GestureDetector(this,
            object : SimpleOnGestureListener() {

                override fun onShowPress(e: MotionEvent?) {

                    Log.i(TAG, "onShowPress: " + e)


                super.onShowPress(e)

                }

                override fun onLongPress(e: MotionEvent) {
                    Log.i(TAG, "onLongPress: ")
                    
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    Log.i(TAG, "onSingleTapUp: ")
                    return super.onSingleTapUp(e)
                   
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    Log.i(TAG, "onDoubleTap: ")
                    return super.onDoubleTap(e)
                }
            })


        mCamFab.setOnTouchListener { _, event -> longPressDetector.onTouchEvent(event)
        }*/





        //Drag and move floating view using user's touch action.
        //view!!.findViewById<View>(R.id.root_container_id)

            mCamFab.setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            Log.i(TAG, "onTouch: Down")

                            toggle = true

                            //remember the initial position.
                            initialX = params.x
                            initialY = params.y

                            //get the touch location
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {

                            Log.i(TAG, "onTouch: move")
                            if(!oneClick){
                                //Calculate the X and Y coordinates of the view.
                                Log.i(TAG, "onTouch: move")
                                params.x = initialX + (event.rawX - initialTouchX).toInt()
                                params.y = initialY + (event.rawY - initialTouchY).toInt()

                                Log.d(TAG, "onTouch: x "+ params.x+ " onTouch: y "+ params.y)


                                //Update the layout with new X & Y coordinate
                                mWindowManager!!.updateViewLayout(view, params)
                                return true
                            }else{
                                return false
                            }

                        }

                        MotionEvent.ACTION_UP -> {
                            Log.i(TAG, "onTouch: Up")

                            val Xdiff = (event.rawX - initialTouchX).toInt()
                            val Ydiff = (event.rawY - initialTouchY).toInt()

                            Log.i(TAG, "onTouch: "+ Xdiff)
                            Log.i(TAG, "onTouch: "+Ydiff)

                            if ((Xdiff in -10..10 || Ydiff in -10..10) && !group.isVisible){
                                if(oneClick){
                                    mCamFab.setImageResource(R.drawable.ic_tab_screen_icon)
                                    // mLanFab.visibility = View.GONE
                                    group.visibility = View.GONE
                                    toggle = false
                                    //   mSurfaceView.visibility = View.GONE
                                    Log.i(TAG, "onCreate: close")
                                    if(ScreenCaptureService.event != null){
                                        ScreenCaptureService.event!!.close()
                                    }
                                    oneClick = false

                                }else{
                                    mCamFab.setImageResource(R.drawable.ic_close_black_icon)
                                    group.visibility = View.VISIBLE

                                }

                            }else{
                                if(!oneClick){
                                    if(Xdiff in -10..10 && Ydiff in -10..10){
                                        mCamFab.setImageResource(R.drawable.ic_tab_screen_icon)
                                        // mLanFab.visibility = View.GONE
                                        group.visibility = View.GONE
                                        toggle = false
                                        //   mSurfaceView.visibility = View.GONE
                                        Log.i(TAG, "onCreate: without close")
                                        if(ScreenCaptureService.event != null){
                                            ScreenCaptureService.event!!.close()
                                        }
                                    }

                                }

                            }

                            return true
                        }

                    }
                    return false
                }
            })






/*        mCamFab.setOnClickListener {

            if(!toggle) {
                mCamFab.setImageResource(R.drawable.ic_close_black_icon)
                group.visibility = View.VISIBLE
                toggle = true

            } else{
                mCamFab.setImageResource(R.drawable.ic_tab_screen_icon)
               // mLanFab.visibility = View.GONE
                group.visibility = View.GONE
                toggle = false
             //   mSurfaceView.visibility = View.GONE
                Log.i(TAG, "onCreate: close")
                if(event != null){
                    event!!.close()
                }

            }

        }*/

        mOneFab.setOnClickListener {
            Log.i(TAG, "onCreate: start Projection One" )

            if(TranslatorDrawOverActivity.reset == null){
                TranslatorDrawOverActivity.reset = this
            }
            group.visibility = View.GONE
            clickType = true

            oneClick = true
          //  getStartIntent(this, resultCode, data)
            //IMAGES_PRODUCED = 0
            // delay needed because getMediaProjection() throws an error if it's called too soon
            Handler().postDelayed({
                startProjection(resultCode, data)
                //mediaProjection = mediaProjectionManager.getMediaProjection(activityResultCode, activityData)
               // startStreaming()
               // serviceBound = true
            }, 1000)

        }

        mAllFab.setOnClickListener {
            Log.i(TAG, "onCreate: start Projection All" )
            if(TranslatorDrawOverActivity.reset == null){
                TranslatorDrawOverActivity.reset = this
            }
           group.visibility = View.GONE
            clickType = false
            mCamFab.setImageResource(R.drawable.ic_tab_screen_icon)
            toggle = false
           // IMAGES_PRODUCED = 0
           // val notification: Pair<Int, Notification> = NotificationUtils.getNotification(this)
           // startForeground(notification.first, notification.second)
            startProjection(resultCode, data)
           // getStartIntent(this, resultCode, data)

        }



        // create store dir
        val externalFilesDir = getExternalFilesDir(null)
        if (externalFilesDir != null) {
            mStoreDir = externalFilesDir.absolutePath + "/screenshots/"
            val storeDirectory = File(mStoreDir)
            if (!storeDirectory.exists()) {
                val success = storeDirectory.mkdirs()
                if (!success) {
                    Log.e(TAG, "failed to create file storage directory.")
                    stopSelf()
                }
            }
        } else {
            Log.e(TAG, "failed to create file storage directory, getExternalFilesDir is null.")
            stopSelf()
        }

        // start capture handling thread
        object : Thread() {
            override fun run() {
                Looper.prepare()
                mHandler = Handler()
                Looper.loop()
            }
        }.start()

    }
    override fun attachBaseContext(base: Context) {
        Log.d(TAG, "attachBaseContext: called" )
        super.attachBaseContext(LanguageManager.setLocale(base))
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            Log.d(TAG, "onConfigurationChanged: "+ newConfig.locales[0].language)
            LanguageManager.setNewLocale(this, newConfig.locales[0].language)
            stopForeground(false)
            stopSelf()
            startService(getStartIntent(this, -1, oldIntent))
        }else{
            newConfig.locale
            Log.d(TAG, "onConfigurationChanged: "+ newConfig.locale)
            LanguageManager.setNewLocale(this, newConfig.locale.language)
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (isStartCommand(intent)) {
            // create notification
           val notification: Pair<Int, Notification> = NotificationUtils.getNotification(this)
            startForeground(notification.first, notification.second)
            // start projection
                resultCode = intent.getIntExtra(RESULT_CODE, Activity.RESULT_CANCELED)
            data = intent.getParcelableExtra<Intent>(DATA)
           // startProjection(resultCode, data)
            Log.e(TAG, "onStartCommand: code "+ resultCode)
            Log.e(TAG, "onStartCommand: data " + data)

        } else if (isStopCommand(intent)) {
            Log.e(TAG, "onStartCommand: is stop")
            stopProjection()
            stopForeground(false)
            stopSelf()
        } else {
            Log.e(TAG, "onStartCommand: is stop case")
            stopProjection()
            stopForeground(false)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: " )
        if(event != null){
            event!!.close()
        }
        TranslatorDrawOverActivity.reset = null
        mWindowManager?.removeView(view)
    }

    private fun startProjection(resultCode: Int, data: Intent?) {
        val mpManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (mMediaProjection == null) {
            mMediaProjection = mpManager.getMediaProjection(resultCode, data!!)
            if (mMediaProjection != null) {
                // display metrics
                mDensity = Resources.getSystem().displayMetrics.densityDpi
                val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                mDisplay = windowManager.defaultDisplay

                // create virtual display depending on device width / height
                createVirtualDisplay()

                // register orientation change callback
                mOrientationChangeCallback = OrientationChangeCallback(this)
                if (mOrientationChangeCallback!!.canDetectOrientation()) {
                    mOrientationChangeCallback!!.enable()
                }

                // register media projection stop callback
                mMediaProjection!!.registerCallback(MediaProjectionStopCallback(), mHandler)
            }
        }
    }

    private fun stopProjection() {
        if (mHandler != null) {
            mHandler!!.post {
                if (mMediaProjection != null) {
                    mMediaProjection!!.stop()
                }

            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun createVirtualDisplay() {
        // get width and height
        mWidth = Resources.getSystem().displayMetrics.widthPixels
        mHeight = Resources.getSystem().displayMetrics.heightPixels

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2)
        mVirtualDisplay = mMediaProjection!!.createVirtualDisplay(
            SCREENCAP_NAME, mWidth, mHeight,
            mDensity, virtualDisplayFlags, mImageReader!!.surface, null, mHandler
        )
        mImageReader!!.setOnImageAvailableListener(ImageAvailableListener(), mHandler)
    }

    fun startIntent(type: Boolean){
        val activityIntent = Intent(this, TranslatorDrawOverActivity::class.java)
        if(type){
            activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        }else{
            activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        activityIntent.putExtra("path", "$mStoreDir/myscreen.png")
        activityIntent.putExtra("type", type)
        startActivity(activityIntent)
    }




    companion object {
        private const val TAG = "ScreenCaptureService"
        private const val RESULT_CODE = "RESULT_CODE"
        private const val DATA = "DATA"
        private const val ACTION = "ACTION"
        private const val START = "START"
        private const val STOP = "STOP"
        private const val SCREENCAP_NAME = "screencap"
        private var IMAGES_PRODUCED = 0
        private var oldIntent: Intent? = null


        var  event: CloseEvent? = null

        fun getStartIntent(context: Context?, resultCode: Int, data: Intent?): Intent {
            oldIntent = data
            val intent = Intent(context, ScreenCaptureService::class.java)
            intent.putExtra(ACTION, START)
            intent.putExtra(RESULT_CODE, resultCode)
            intent.putExtra(DATA, data)
            return intent
        }

        fun getStopIntent(context: Context?): Intent {
            val intent = Intent(context, ScreenCaptureService::class.java)
            intent.putExtra(ACTION, STOP)
            return intent
        }

        private fun isStartCommand(intent: Intent): Boolean {
            return (intent.hasExtra(RESULT_CODE) && intent.hasExtra(DATA)
                    && intent.hasExtra(ACTION) && intent.getStringExtra(ACTION) == START)
        }

        private fun isStopCommand(intent: Intent): Boolean {
            return intent.hasExtra(ACTION) && intent.getStringExtra(ACTION) == STOP
        }

        private val virtualDisplayFlags: Int
            private get() = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC

    }

    override fun close() {

    }

    override fun reset(boolean: Boolean) {
        if(boolean){
            mCamFab.setImageResource(R.drawable.ic_tab_screen_icon)
            toggle = false
            oneClick = false
        }
    }
}