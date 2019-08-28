package com.blackbox.fingerprintscanner

import android.content.Context

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import android.widget.Toast

class FingerPrintHandler(private val context: Context): FingerprintManagerCompat.AuthenticationCallback() {

    private var cancellationSignal : CancellationSignal?=null

    fun authenticate(fingerprintManagerCompat: FingerprintManagerCompat, cryptoObject: FingerprintManagerCompat.CryptoObject){
        cancellationSignal = CancellationSignal()
        fingerprintManagerCompat.authenticate(cryptoObject,0,cancellationSignal,this,null)
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
        super.onAuthenticationError(errMsgId, errString)
        Toast.makeText(context,errString,Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
        super.onAuthenticationHelp(helpMsgId, helpString)
        Toast.makeText(context,helpString,Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)
        Toast.makeText(context,"Success",Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        Toast.makeText(context,"Authentication failed",Toast.LENGTH_LONG).show()
    }

    fun removeCancellationSignal(){
        cancellationSignal?.let {
            it.cancel()
        }
    }
}