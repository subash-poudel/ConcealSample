package com.poudel.subash.concealsample;

import android.content.Context;
import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.keychain.KeyChain;
import com.facebook.soloader.SoLoader;

public final class CryptoUtil {

  private static CryptoUtil cryptoUtil;

  private Crypto crypto;

  private CryptoUtil() {

  }

  public static CryptoUtil get() {
    if (cryptoUtil == null) {
      cryptoUtil = new CryptoUtil();
    }
    return cryptoUtil;
  }

  public void init(Context context) {
    SoLoader.init(context, false);
    // Creates a new Crypto object with default implementations of a key chain
    KeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
    crypto = AndroidConceal.get().createDefaultCrypto(keyChain);

    // Check for whether the crypto functionality is available
    // This might fail if Android does not load libaries correctly.
    if (!crypto.isAvailable()) {
      return;
    }
  }

  public Crypto getCrypto() {
    return crypto;
  }
}
