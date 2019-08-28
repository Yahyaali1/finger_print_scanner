package com.blackbox.fingerprintscanner

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.service.autofill.Validators.or
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.lang.Exception
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import android.security.keystore.KeyPermanentlyInvalidatedException
import java.security.*
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey


class MainActivity : AppCompatActivity() {

    private lateinit var cipher: Cipher
    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator
    /**
     * Deprecation is taken care in android P. However the support is not stable as yet.
     */
    private lateinit var cryptoObject: FingerprintManagerCompat.CryptoObject
    private lateinit var fingerprintManagerCompat: FingerprintManagerCompat
    private lateinit var keyguardManager: KeyguardManager
    private var systemVerified = false
    private var initCypherSucessfull = false
    private val fingerPrintHandler = FingerPrintHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        systemVerified = systemRequirementVerification()


        if (systemVerified) {

            try {
                generateKey()
                initCypherSucessfull = initCipher()
                if(initCypherSucessfull){
                    cryptoObject = FingerprintManagerCompat.CryptoObject(cipher)
                }

            } catch (e: Exception) {

            }
        }

        start.setOnClickListener {
            if(systemVerified && initCypherSucessfull)
            {
                fingerPrintHandler.authenticate(fingerprintManagerCompat,cryptoObject)
            }

        }
    }


    private fun init() {
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as (KeyguardManager)
        fingerprintManagerCompat = FingerprintManagerCompat.from(this)

    }

    private fun initCipher(): Boolean {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore.load(
                null
            )
            val key = keyStore.getKey(KEY_VALUE, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            //Return true if the cipher has been initialized successfully//
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {

            //Return false if cipher initialization failed//
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }

    }

    @Throws(Exception::class)
    private fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyGenerator.init(
                KeyGenParameterSpec.Builder(KEY_VALUE, KeyProperties.PURPOSE_ENCRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()
            )

            keyGenerator.generateKey()

        } catch (e: Exception) {
            throw Exception(e)
        }
    }

    private fun systemRequirementVerification(): Boolean {
        error_tv.text = "No Errors found"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!fingerprintManagerCompat.isHardwareDetected) {
                error_tv.text = " Your Device does not support fingerprint authentication"
                return false
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (checkSelfPermission(Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//
                error_tv.text = "Please enable the fingerprint permission";
                return false
            }

            //Check that the user has registered at least one fingerprint//
            if (!fingerprintManagerCompat.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message//
                error_tv.text =
                    "No fingerprint configured. Please register at least one fingerprint in your device's Settings";
                return false
            }

            //Check that the lockscreen is secured//
            if (!keyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
                error_tv.text = "Please enable lockscreen security in your device's Settings";
                return false

            }

            return true


        } else {
            error_tv.text = "Device does no support finger print api support"
            return false
        }
    }

    override fun onStop() {
        super.onStop()
       fingerPrintHandler.removeCancellationSignal()
    }
    companion object {
        private const val KEY_VALUE = "yourKey"
    }
}
