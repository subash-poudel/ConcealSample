package com.poudel.subash.concealsample;

import android.app.Application;
import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.keychain.KeyChain;
import com.facebook.soloader.SoLoader;

public class MyApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    super.onCreate();
    CryptoUtil.get().init(this);
  }
}
