package com.app.simostools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.lang.Exception

open class LoggingBaseFragment: Fragment() {
    open var TAG                            = "LoggingBaseFragment"
    open var mFragmentName                  = "All"
    open var mLastWarning                   = false
    open var mLayouts: Array<View?>?        = null
    open var mGauges: Array<SwitchGauge?>?  = null
    open var mTextViews: Array<TextView?>?  = null
    open var mPIDsPerLayout                 = 1
    open var mLayoutType                    = R.layout.pid_portrait
    open var mLayoutName: Int               = R.id.loggingLayoutScroll
    open var mPIDList                       = byteArrayOf()

    fun getName(): String {
        return mFragmentName
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //check orientation and type
        checkOrientation()
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(GUIMessage.READ_LOG.toString())
        onSetFilter(filter)
        this.activity?.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        this.activity?.unregisterReceiver(mBroadcastReceiver)
    }

    override fun onStart() {
        super.onStart()

        //Build our layout
        buildLayout()

        //update PID text
        updatePIDText()

        //Do we keep the screen on?
        view?.keepScreenOn = ConfigSettings.KEEP_SCREEN_ON.toBoolean()

        //Set background color
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)
    }

    open fun checkOrientation() {
        //check orientation and type
        var currentOrientation = resources.configuration.orientation

        if (ConfigSettings.ALWAYS_PORTRAIT.toBoolean())
            currentOrientation = Configuration.ORIENTATION_PORTRAIT

        when(currentOrientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                mLayoutType = R.layout.pid_land
                mPIDsPerLayout = 3
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                mLayoutType = R.layout.pid_portrait
                mPIDsPerLayout = 2
            }
        }
    }

    open fun buildLayout() {
        view?.let { currentview ->
            try {
                //Clear current layout
                var lLayout = currentview.findViewById<LinearLayout>(mLayoutName)
                lLayout.removeAllViews()
                mGauges = null
                mLayouts = null
                mTextViews = null

                //Build PID List
                buildPIDList()

                //Build layout
                PIDs.getList()?.let { list ->
                    var layoutCount = mPIDList.count() / mPIDsPerLayout
                    if (mPIDList.count() % mPIDsPerLayout != 0)
                        layoutCount++

                    mLayouts = arrayOfNulls(layoutCount)
                    mGauges = arrayOfNulls(mPIDList.count())
                    mTextViews = arrayOfNulls(mPIDList.count())
                    for (i in 0 until mPIDList.count()) {
                        //build child layout
                        var progID = 0
                        var txtID = 0
                        when (i % mPIDsPerLayout) {
                            0 -> {
                                val pidLayout = layoutInflater.inflate(mLayoutType, null)
                                lLayout = currentview.findViewById(mLayoutName)
                                lLayout.addView(pidLayout)
                                mLayouts!![i / mPIDsPerLayout] = pidLayout
                                progID = R.id.pid_gauge
                                txtID = R.id.pid_text
                            }
                            1 -> {
                                progID = R.id.pid_gauge1
                                txtID = R.id.pid_text1
                            }
                            2 -> {
                                progID = R.id.pid_gauge2
                                txtID = R.id.pid_text2
                            }
                        }

                        //Store progress and text views
                        mGauges!![i] = mLayouts!![i / mPIDsPerLayout]?.findViewById(progID)
                        mTextViews!![i] = mLayouts!![i / mPIDsPerLayout]?.findViewById(txtID)

                        //make visible
                        mGauges!![i]?.isVisible = true
                        mTextViews!![i]?.isVisible = true

                        //get current pid and data
                        val data = PIDs.getData()!![mPIDList[i].toInt()]!!
                        val pid = list[mPIDList[i].toInt()]!!

                        //find text view and set text
                        val textView = mTextViews!![i]!!
                        textView.text = getString(
                            R.string.textPID,
                            pid.name,
                            pid.format.format(pid.value),
                            pid.unit,
                            pid.format.format(data.min),
                            pid.format.format(data.max)
                        )
                        textView.setTextColor(ColorList.TEXT.value)

                        //Setup the progress bar
                        val gauge = mGauges!![i]!!
                        gauge.setProgressColor(ColorList.GAUGE_NORMAL.value, false)
                        gauge.setMinMaxColor(ColorList.GAUGE_WARN.value, false)
                        gauge.setMinMax(ConfigSettings.DRAW_MIN_MAX.toBoolean(), false)
                        val prog = when (data.inverted) {
                            true -> (0 - (pid.value - pid.progMin)) * data.multiplier
                            false -> (pid.value - pid.progMin) * data.multiplier
                        }
                        val progMin = when (data.inverted) {
                            true -> (0 - (data.min - pid.progMin)) * data.multiplier
                            false -> (data.min - pid.progMin) * data.multiplier
                        }
                        val progMax = when (data.inverted) {
                            true -> (0 - (data.max - pid.progMin)) * data.multiplier
                            false -> (data.max - pid.progMin) * data.multiplier
                        }
                        gauge.setProgress(prog, progMin, progMax, false)
                        gauge.setProgressBackgroundColor(ColorList.GAUGE_BG.value, false)
                        gauge.setStyle(ConfigSettings.GAUGE_TYPE.toGaugeType(), false)
                        when (ConfigSettings.GAUGE_TYPE.toGaugeType()) {
                            GaugeType.BAR -> gauge.setProgressWidth(250f, false)
                            GaugeType.ROUND -> gauge.setProgressWidth(50f, false)
                        }
                        if(kotlin.math.abs(pid.progMin) == kotlin.math.abs(pid.progMax))
                            gauge.setCentered(true, false)
                        gauge.setGraduations(ConfigSettings.DRAW_GRADUATIONS.toBoolean(), false)
                        gauge.setIndex(i)
                        gauge.setOnLongClickListener {
                            onGaugeClick(it)
                        }
                        gauge.setEnable(pid.enabled)
                    }

                    DebugLog.d(TAG, "Built layout.")
                }
            } catch (e: Exception) {
                DebugLog.e(TAG, "Unable to build PID layout.", e)
            }
        }
    }

    open fun updatePIDText() {
        var lastI = -1
        //Update text
        try {
            for (i in 0 until mPIDList.count()) {
                val pid = PIDs.getList()!![mPIDList[i].toInt()]
                mTextViews?.let { textView ->
                    PIDs.getData()?.let { datalist ->
                        val data = datalist[mPIDList[i].toInt()]

                        textView[i]?.text = getString(
                            R.string.textPID,
                            pid!!.name,
                            pid.format.format(pid.value),
                            pid.unit,
                            pid.format.format(data?.min),
                            pid.format.format(data?.max)
                        )
                    }
                }
                lastI = i
            }
        } catch (e: Exception) {
            DebugLog.e(TAG, "Unable to update text [$lastI:${mPIDList.count()}]", e)
            buildLayout()
        }
    }

    open fun updateProgress() {
        //Set the UI values
        var warnAny = false
        var lastI = -1
        try {
            for (i in 0 until mPIDList.count()) {
                //get the current pid
                val pid = PIDs.getList()!![mPIDList[i].toInt()]!!
                val data = PIDs.getData()!![mPIDList[i].toInt()]!!
                val gauge = mGauges!![i]!!

                var prog = when (data.inverted) {
                    true -> (0 - (pid.value - pid.progMin)) * data.multiplier
                    false -> (pid.value - pid.progMin) * data.multiplier
                }
                val progMin = when (data.inverted) {
                    true -> (0 - (data.min - pid.progMin)) * data.multiplier
                    false -> (data.min - pid.progMin) * data.multiplier
                }
                val progMax = when (data.inverted) {
                    true -> (0 - (data.max - pid.progMin)) * data.multiplier
                    false -> (data.max - pid.progMin) * data.multiplier
                }
                gauge.setProgress(prog, progMin, progMax, false)

                //constrain value
                if(prog > 100f) prog = 100f
                else if(prog < 0f) prog = 0f

                //check if previous value is different
                if (prog != gauge.getProgress()) {
                    gauge.setProgress(prog, progMin, progMax,false)
                }

                //Check to see if we should be warning user
                if(!data.warn)  {
                    gauge.setProgressColor(ColorList.GAUGE_NORMAL.value, false)
                    gauge.setMinMaxColor(ColorList.GAUGE_WARN.value)
                } else {
                    gauge.setProgressColor(ColorList.GAUGE_WARN.value, false)
                    gauge.setMinMaxColor(ColorList.GAUGE_NORMAL.value)
                    warnAny = true
                }
                lastI = i
            }

            //If any visible PIDS are in warning state set background color to warn
            if(warnAny) {
                if(!mLastWarning) {
                    view?.setBackgroundColor(ColorList.BG_WARN.value)
                }
            } else {
                if(mLastWarning) {
                    view?.setBackgroundColor(ColorList.BG_NORMAL.value)
                }
            }
            mLastWarning = warnAny
        } catch (e: Exception) {
            DebugLog.e(TAG, "Unable to update display [$lastI:${mPIDList.count()}]", e)
            buildLayout()
        }
    }

    open fun doUpdate(readCount: Int, readTime: Long) {
    }

    open fun buildPIDList() {
    }

    open fun onGaugeClick(view: View?): Boolean {
        return true
    }

    open fun onNewMessage(intent: Intent) {
    }

    open fun onSetFilter(filter: IntentFilter) {
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                GUIMessage.READ_LOG.toString() -> {
                    val readCount = intent.getIntExtra("readCount", 0)
                    val readTime = intent.getLongExtra("readTime", 0)
                    val readResult = intent.getSerializableExtra("readResult") as UDSReturn

                    //Make sure we received an ok
                    if(readResult != UDSReturn.OK) {
                        return
                    }

                    //Update PID Text
                    updatePIDText()

                    //Update progress
                    updateProgress()

                    //Update callback
                    doUpdate(readCount, readTime)
                }
                else -> onNewMessage(intent)
            }
        }
    }
}