package com.app.simostools

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import java.util.*

// Message types sent from the BluetoothChatService Handler
enum class GUIMessage {
    STATE_CONNECTION,
    STATE_TASK,
    TOAST,
    READ,
    READ_LOG,
    WRITE_LOG,
    FLASH_INFO,
    FLASH_PROGRESS,
    FLASH_PROGRESS_MAX,
    FLASH_PROGRESS_SHOW,
    PID_RELOAD
}

// Constants that indicate the current connection state
enum class BLEConnectionState {
    ERROR,
    NONE,
    CONNECTING,
    CONNECTED;

    var errorMessage: String = ""
    var deviceName: String = ""
}

//List of available tasks
enum class UDSTask {
    NONE,
    LOGGING,
    FLASHING,
    INFO,
    DTC
}

//BT functions
enum class BTServiceTask {
    STOP_SERVICE,
    START_SERVICE,
    REQ_STATUS,
    DO_CONNECT,
    DO_DISCONNECT,
    DO_START_LOG,
    DO_START_FLASH,
    DO_GET_INFO,
    DO_CLEAR_DTC,
    DO_STOP_TASK
}

//Intent constants
enum class RequiredPermissions(val permission: String, val version: Int, val required: Boolean, var result: Int) {
    BT(Manifest.permission.BLUETOOTH, 0, true, PackageManager.PERMISSION_DENIED),
    @RequiresApi(Build.VERSION_CODES.S) BT_ADVERTISE(Manifest.permission.BLUETOOTH_ADVERTISE, Build.VERSION_CODES.S, true, PackageManager.PERMISSION_DENIED),
    @RequiresApi(Build.VERSION_CODES.S) BT_SCAN(Manifest.permission.BLUETOOTH_SCAN, Build.VERSION_CODES.S, true, PackageManager.PERMISSION_DENIED),
    @RequiresApi(Build.VERSION_CODES.S) BT_CONNECT(Manifest.permission.BLUETOOTH_CONNECT, Build.VERSION_CODES.S, true, PackageManager.PERMISSION_DENIED),
    FINE_LOCATION(Manifest.permission.ACCESS_FINE_LOCATION, 0, true, PackageManager.PERMISSION_DENIED),
    COARSE_LOCATION(Manifest.permission.ACCESS_COARSE_LOCATION, 0, true, PackageManager.PERMISSION_DENIED),
    READ_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE, 0, false, PackageManager.PERMISSION_DENIED),
    WRITE_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE, 0, false, PackageManager.PERMISSION_DENIED),
}

//ISOTP bridge command flags
enum class BLECommandFlags(val value: Int) {
    PER_ENABLE(1),
    PER_CLEAR(2),
    PER_ADD(4),
    SPLIT_PK(8),
    SET_GET(64),
    SETTINGS(128)
}

//ISOTP bridge internal settings
enum class BLESettings(val value: Int) {
    ISOTP_STMIN(1),
    LED_COLOR(2),
    PERSIST_DELAY(3),
    PERSIST_Q_DELAY(4),
    BLE_SEND_DELAY(5),
    BLE_MULTI_DELAY(6),
    PASSWORD(7)
}

// UDS return codes
enum class UDSReturn {
    OK,
    ERROR_RESPONSE,
    ERROR_NULL,
    ERROR_HEADER,
    ERROR_CMDSIZE,
    ERROR_TIME_OUT,
    ERROR_UNKNOWN,
}

enum class ECUInfo(val str: String, val address: ByteArray) {
    VIN("VIN", byteArrayOf(0xf1.toByte(), 0x90.toByte())),
    ODX_IDENTIFIER("ASAM/ODX File Identifier", byteArrayOf(0xF1.toByte(), 0x9E.toByte())),
    ODX_VERSION("ASAM/ODX File Version", byteArrayOf(0xF1.toByte(), 0xA2.toByte())),
    VEHICLE_SPEED("Vehicle Speed", byteArrayOf(0xF4.toByte(), 0x0D.toByte())),
    CAL_NUMBER("Calibration Version Numbers", byteArrayOf(0xF8.toByte(), 0x06.toByte())),
    PART_NUMBER("VW Spare part Number", byteArrayOf(0xF1.toByte(), 0x87.toByte())),
    ASW_VERSION("VW ASW Version", byteArrayOf(0xF1.toByte(), 0x89.toByte())),
    HW_NUMBER("ECU Hardware Number", byteArrayOf(0xF1.toByte(), 0x91.toByte())),
    HW_VERSION("ECU Hardware Version Number", byteArrayOf(0xF1.toByte(), 0xA3.toByte())),
    ENGINE_CODE("Engine Code", byteArrayOf(0xF1.toByte(), 0xAD.toByte())),
    WORKSHOP_NAME("VW Workshop Name", byteArrayOf(0xF1.toByte(), 0xAA.toByte())),
    FLASH_STATE("State of Flash Mem", byteArrayOf(0x04.toByte(), 0x05.toByte())),
    CODE_VALUE("VW Coding Value", byteArrayOf(0x06.toByte(), 0x00.toByte()))
}

enum class FLASH_ECU_CAL_SUBTASK {
    NONE,
    GET_ECU_BOX_CODE,
    READ_FILE_FROM_STORAGE,
    CHECK_FILE_COMPAT,
    CHECKSUM_BIN,
    COMPRESS_BIN,
    ENCRYPT_BIN,
    CLEAR_DTC,
    OPEN_EXTENDED_DIAGNOSTIC,
    CHECK_PROGRAMMING_PRECONDITION, //routine 0x0203
    SA2SEEDKEY,
    WRITE_WORKSHOP_LOG,
    FLASH_BLOCK,
    CHECKSUM_BLOCK, //0x0202
    VERIFY_PROGRAMMING_DEPENDENCIES,
    RESET_ECU;

    fun next(): FLASH_ECU_CAL_SUBTASK {
        val vals = values()
        return vals[(this.ordinal+1) % vals.size]

    }
}

//Color List
enum class ColorList(var value: Int, val cfgName: String) {
    BG_NORMAL(Color.rgb(64, 64, 64), "BGNormal"),
    BG_WARN(Color.rgb(127, 127, 255),"BGWarn"),
    TEXT(Color.rgb(255,   255,   255), "Text"),
    GAUGE_NORMAL(Color.rgb(0,   255, 0), "GaugeNormal"),
    GAUGE_WARN(Color.rgb(255, 0,   0), "GaugeWarn"),
    GAUGE_BG(Color.rgb(0, 0,   0), "GaugeBG"),
    ST_ERROR(Color.rgb(255, 32,   0), "StateError"),
    ST_NONE(Color.rgb(255, 0,   0), "StateNone"),
    ST_CONNECTING(Color.rgb(255, 32, 32), "StateConnecting"),
    ST_CONNECTED(Color.rgb(0,   255,   0), "StateConnected"),
    ST_LOGGING(Color.rgb(32, 255, 0), "StateLogging"),
    ST_WRITING(Color.rgb(0,   0, 255), "StateWriting"),
    BT_RIM(Color.rgb(110, 140, 255), "ButtonRIm"),
    BT_TEXT(Color.rgb(255, 255, 255), "ButtonText"),
    BT_BG(Color.rgb(64,   64, 64), "ButtonBG");

    val key = "Color"
}

//Logging modes
enum class UDSLoggingMode(val cfgName: String, val addressMin: Long, val addressMax: Long) {
    MODE_22("22", 0x1000.toLong(), 0xFFFFL),
    MODE_3E("3E", 0x10000000.toLong(), 0xFFFFFFFF);

    val key = "UDSLoggingMode"
}

enum class GearRatios(val gear: String, var ratio: Float) {
    GEAR1("1", 2.92f),
    GEAR2("2",1.79f),
    GEAR3("3",1.14f),
    GEAR4("4",0.78f),
    GEAR5("5",0.58f),
    GEAR6("6",0.46f),
    GEAR7("7",0.0f),
    FINAL("Final",4.77f);

    val key = "GearRatio"
}

enum class DirectoryList(val cfgName: String, val location: String) {
    APP("App",""),
    DOWNLOADS("Downloads", Environment.DIRECTORY_DOWNLOADS),
    DOCUMENTS("Documents", Environment.DIRECTORY_DOCUMENTS);
}

enum class GaugeType(val cfgName: String) {
    BAR("Bar"),
    ROUND("Round")
}

enum class CSVItems(val csvName: String) {
    NAME("Name"),
    UNIT("Unit"),
    EQUATION("Equation"),
    FORMAT("Format"),
    ADDRESS("Address"),
    LENGTH("Length"),
    SIGNED("Signed"),
    PROG_MIN("ProgMin"),
    PROG_MAX("ProgMax"),
    WARN_MIN("WarnMin"),
    WARN_MAX("WarnMax"),
    SMOOTHING("Smoothing"),
    ENABLED("Enabled"),
    TABS("Tabs"),
    ASSIGN_TO("Assign To");

    fun getHeader(): String {
        var header = ""
        values().forEachIndexed {  i, item ->
            header += item.csvName
            if(i != values().count() - 1)
                header += ","
        }

        return header
    }
}

enum class ConfigSettings(val cfgName: String, var value: Any) {
    KEEP_SCREEN_ON("KeepScreenOn", true),
    INVERT_CRUISE("InvertCruise", false),
    UPDATE_RATE("UpdateRate", 4),
    PERSIST_DELAY("PersistDelay", 20),
    PERSIST_Q_DELAY("PersistQDelay", 10),
    CALCULATE_HP("CalculateHP", true),
    USE_MS2("UseMS2Torque", true),
    TIRE_DIAMETER("TireDiameter", 0.632f),
    CURB_WEIGHT("CurbWeight", 1500f),
    DRAG_COEFFICIENT("DragCoefficient", 1500f),
    ALWAYS_PORTRAIT("AlwaysPortrait", false),
    OUT_DIRECTORY("OutputDirectory", DirectoryList.APP),
    GAUGE_TYPE("GaugeType", GaugeType.ROUND),
    DRAW_MIN_MAX("DrawMinMax", true),
    DRAW_GRADUATIONS("DrawGraduations", true),
    DEBUG_LOG("DebugMode", DEBUG_LOG_INFO or DEBUG_LOG_WARNING or DEBUG_LOG_EXCEPTION),
    AUTO_LOG("AutoLog", false);

    fun set(newValue: String) {
        try {
            if (value is String)
                value = newValue

            if (value is Int)
                value = newValue.toInt()

            if (value is Boolean)
                value = newValue.toBoolean()

            if (value is Float)
                value = newValue.toFloat()

            if (value is Double)
                value = newValue.toDouble()

            if (value is GaugeType)
                value = GaugeType.values().find {it.cfgName == newValue} ?: value

            if (value is DirectoryList)
                value = DirectoryList.values().find {it.cfgName == newValue} ?: value

        } catch(e: Exception) {
            DebugLog.e("Settings", "Unable to set $name.", e)
        }
    }
    fun toInt(): Int {
        return if(value is Int) (value as Int) else 0
    }
    fun toFloat(): Float {
        return if(value is Float) (value as Float) else 0f
    }
    fun toDouble(): Double {
        return if(value is Double) (value as Double) else 0.0
    }
    fun toBoolean(): Boolean {
        return if(value is Boolean) (value as Boolean) else false
    }
    fun toGaugeType(): GaugeType {
        return if(value is GaugeType) (value as GaugeType) else GaugeType.ROUND
    }
    fun toDirectory(): DirectoryList {
        return if(value is DirectoryList) (value as DirectoryList) else DirectoryList.APP
    }
    override fun toString(): String {
        if(value is String)
            return value as String

        if(value is Int)
            return (value as Int).toString()

        if(value is Boolean)
            return (value as Boolean).toString()

        if(value is Float)
            return (value as Float).toString()

        if(value is Double)
            return (value as Double).toString()

        if(value is GaugeType)
            return (value as GaugeType).toString()

        if(value is DirectoryList)
            return (value as DirectoryList).toString()

        return name
    }
}

//Delays and timeouts
val TASK_BUMP_DELAY             = 250
val TASK_END_DELAY              = 500
val TASK_END_TIMEOUT            = 3000
val TIME_OUT_LOGGING            = 10
val TIME_OUT_DTC                = 10
val TIME_OUT_FLASH              = 10
val TIME_OUT_INFO               = 10

//Service info
val CHANNEL_ID                  = "BTService"
val CHANNEL_NAME                = "BTService"

//BLE settings
val BLE_DEVICE_NAME             = "BLE_TO_ISOTP"
val BLE_GATT_MTU_SIZE           = 512
val BLE_SCAN_PERIOD             = 5000L
val BLE_THREAD_PRIORITY         = 5 //Priority (max is 10)

//ISOTP bridge UUIDS
val BLE_CCCD_UUID               = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
val BLE_SERVICE_UUID            = UUID.fromString("0000abf0-0000-1000-8000-00805f9b34fb")
val BLE_DATA_TX_UUID            = UUID.fromString("0000abf1-0000-1000-8000-00805f9b34fb")
val BLE_DATA_RX_UUID            = UUID.fromString("0000abf2-0000-1000-8000-00805f9b34fb")
val BLE_CMD_TX_UUID             = UUID.fromString("0000abf3-0000-1000-8000-00805f9b34fb")
val BLE_CMD_RX_UUID             = UUID.fromString("0000abf4-0000-1000-8000-00805f9b34fb")

//ISOTP bridge BLE header defaults
val BLE_HEADER_ID               = 0xF1
val BLE_HEADER_PT               = 0xF2
val BLE_HEADER_RX               = 0x7E8
val BLE_HEADER_TX               = 0x7E0

//Log files
val DEBUG_LOG_NONE              = 0
val DEBUG_LOG_INFO              = 1
val DEBUG_LOG_WARNING           = 2
val DEBUG_LOG_DEBUG             = 4
val DEBUG_LOG_EXCEPTION         = 8
val DEBUG_LOG_COMMUNICATIONS    = 16

//TQ/HP Calculations
val KG_TO_N                     = 9.80665f
val TQ_CONSTANT                 = 16.3f

//Max allowed PID count
val MAX_PIDS                    = 100

//Additional properties
infix fun Byte.shl(that: Int): Int = this.toInt().shl(that)
infix fun Short.shl(that: Int): Int = this.toInt().shl(that)
infix fun Byte.shr(that: Int): Int = this.toInt().shr(that)
infix fun Short.shr(that: Int): Int = this.toInt().shr(that)
infix fun Byte.and(that: Int): Int = this.toInt().and(that)
infix fun Short.and(that: Int): Int = this.toInt().and(that)
fun Byte.toHex(): String = "%02x".format(this)
fun Byte.toHexS(): String = " %02x".format(this)
fun Short.toHex(): String = "%04x".format(this)
fun Int.toHex(): String = "%08x".format(this)
fun Int.toColorInverse(): Int = Color.WHITE xor this or 0xFF000000.toInt()
fun Int.toColorHex(): String = "%06x".format(this and 0xFFFFFF)
fun Int.toTwo(): String = "%02d".format(this)
fun Int.toArray2(): ByteArray = byteArrayOf((this and 0xFF00 shr 8).toByte(), (this and 0xFF).toByte())
fun Long.toColorInt(): Int = (this.toInt() and 0xFFFFFF) or 0xFF000000.toInt()
fun Long.toHex2(): String = "%04x".format(this)
fun Long.toHex4(): String = "%08x".format(this)
fun Long.toHex(): String = "%16x".format(this)
fun Long.toArray2(): ByteArray = byteArrayOf((this and 0xFF00 shr 8).toByte(), (this and 0xFF).toByte())
fun Long.toArray4(): ByteArray = byteArrayOf((this and 0xFF000000 shr 24).toByte(), (this and 0xFF0000 shr 16).toByte(), (this and 0xFF00 shr 8).toByte(), (this and 0xFF).toByte())
fun ByteArray.toHex(): String = joinToString(separator = " ") { eachByte -> "%02x".format(eachByte) }

