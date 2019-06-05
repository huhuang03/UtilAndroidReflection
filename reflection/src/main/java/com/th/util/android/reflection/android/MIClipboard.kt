package com.th.util.android.reflection.android

import android.os.IBinder
import android.os.IInterface
import com.th.util.android.reflection.lib.util.callStaticMethod

class MIClipboard {
    companion object {
        val clazz = Class.forName("android.content.IClipboard")
    }

    class Stub {
        companion object {
            val clazz = Class.forName("android.content.IClipboard\$Stub")

            fun asInterface(binder: IBinder): IInterface {
                return callStaticMethod(clazz, "asInterface", binder) as IInterface
            }
        }
    }
}