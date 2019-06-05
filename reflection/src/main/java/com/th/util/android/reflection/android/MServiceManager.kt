package com.th.util.android.reflection.android

import android.os.IBinder
import com.th.util.android.reflection.lib.BaseMock
import com.th.util.android.reflection.lib.util.callStaticMethod
import com.th.util.android.reflection.lib.util.getStaticObjectField

class MServiceManager : BaseMock(null) {

    companion object {
        val clazz = Class.forName("android.os.ServiceManager")

        fun getCache(): HashMap<String, IBinder> {
            return getStaticObjectField(clazz, "sCache") as HashMap<String, IBinder>
        }

        fun getService(name: String): IBinder? {
            return callStaticMethod(clazz, "getService", name) as IBinder?
        }
    }

}