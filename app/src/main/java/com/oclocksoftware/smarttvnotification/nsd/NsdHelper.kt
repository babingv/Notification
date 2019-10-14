package com.oclocksoftware.smarttvnotification.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Module
class NsdHelper(val mNsdManager:NsdManager) {

    lateinit internal var mResolveListener: NsdManager.ResolveListener
    lateinit internal var mDiscoveryListener: NsdManager.DiscoveryListener
    lateinit internal var mRegistrationListener: NsdManager.RegistrationListener
    lateinit var mServiceName: String

    var chosenServiceInfo: NsdServiceInfo? = null
        internal set
/*
    init {
        mNsdManager = mContext.getSystemService(Context.NSD_SERVICE) as NsdManager
    }*/

    fun initializeNsd() {
        initializeResolveListener()
        //initializeDiscoveryListener()
        initializeRegistrationListener()

        //mNsdManager.init(mContext.getMainLooper(), this);

    }

    fun initializeDiscoveryListener(mDiscoveryListener: NsdManager.DiscoveryListener) {
        this.mDiscoveryListener=mDiscoveryListener;
        /*mDiscoveryListener = object : NsdManager.DiscoveryListener {

            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(TAG, "Service discovery success" + service)
                if (service.serviceType != SERVICE_TYPE) {
                    Log.d(TAG, "Unknown Service Type: " + service.serviceType)
                } else if (service.serviceName == mServiceName) {
                    Log.d(TAG, "Same machine: " + mServiceName)
                } else if (service.serviceName.contains(mServiceName)) {
                    mNsdManager.resolveService(service, mResolveListener)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.e(TAG, "service lost" + service)
                if (chosenServiceInfo == service) {
                    chosenServiceInfo = null
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: " + serviceType)
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode)
                mNsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode)
                mNsdManager.stopServiceDiscovery(this)
            }
        }*/
    }

    fun initializeResolveListener() {
        mResolveListener = object : NsdManager.ResolveListener {

            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Resolve failed" + errorCode)
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo)

                if (serviceInfo.serviceName == mServiceName) {
                    Log.d(TAG, "Same IP.")
                    return
                }
                chosenServiceInfo = serviceInfo
            }
        }
    }

    fun initializeRegistrationListener() {
        mRegistrationListener = object : NsdManager.RegistrationListener {

            override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                mServiceName = NsdServiceInfo.serviceName
            }

            override fun onRegistrationFailed(arg0: NsdServiceInfo, arg1: Int) {}

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {}

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}

        }
    }

    fun registerService(port: Int,appendedServiceName: String) {
        val serviceInfo = NsdServiceInfo()
        serviceInfo.port = port
        serviceInfo.serviceName = appendedServiceName
        serviceInfo.serviceType = SERVICE_TYPE

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener)

    }

    fun discoverServices() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)
    }

    fun stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener)
    }

    fun tearDown() {
        if(mRegistrationListener!=null)
        mNsdManager.unregisterService(mRegistrationListener)
    }

    companion object {

        val SERVICE_TYPE = "_http._tcp."

        val TAG = "NsdHelper"
    }
}
