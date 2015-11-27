/**
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.EditText;

import org.thoughtcrime.securesms.BuildConfig;
import org.thoughtcrime.securesms.mms.OutgoingLegacyMmsConnection;
import org.whispersystems.textsecure.api.util.InvalidNumberException;
import org.whispersystems.textsecure.api.util.PhoneNumberFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ws.com.google.android.mms.pdu.CharacterSets;
import ws.com.google.android.mms.pdu.EncodedStringValue;

public class Util {

  private static final String TAG = Util.class.getSimpleName();

  public static Handler handler = new Handler(Looper.getMainLooper());

  public static String join(String[] list, String delimiter) {
    Log.d(TAG, "join(): delimter:" + delimiter + "list: " +list.toString());
    return join(Arrays.asList(list), delimiter);
  }

  public static String join(Collection<String> list, String delimiter) {
    Log.d(TAG, "join(): delimter:" + delimiter + "list: " +list.toString());

    StringBuilder result = new StringBuilder();
    int i = 0;

    for (String item : list) {
      result.append(item);

      if (++i < list.size())
        result.append(delimiter);
    }

    return result.toString();
  }

  public static String join(long[] list, String delimiter) {
    Log.d(TAG, "join(): delimter:" + delimiter + "list: " +list.toString());

    StringBuilder sb = new StringBuilder();

    for (int j=0;j<list.length;j++) {
      if (j != 0) sb.append(delimiter);
      sb.append(list[j]);
    }

    return sb.toString();
  }

  public static ExecutorService newSingleThreadedLifoExecutor() {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingLifoQueue<Runnable>());

    executor.execute(new Runnable() {
      @Override
      public void run() {
//        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
      }
    });

    return executor;
  }

  public static boolean isEmpty(EncodedStringValue[] value) {
    Log.d(TAG, "isEmpty(): value:" + value);
    return value == null || value.length == 0;
  }

  public static boolean isEmpty(EditText value) {
    Log.d(TAG, "isEmpty(): value:" + value);
    return value == null || value.getText() == null || TextUtils.isEmpty(value.getText().toString());
  }

  public static CharSequence getBoldedString(String value) {
    Log.d(TAG, "getBoldedString(): value:" + value);

    SpannableString spanned = new SpannableString(value);
    spanned.setSpan(new StyleSpan(Typeface.BOLD), 0,
                    spanned.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    return spanned;
  }

  public static @NonNull String toIsoString(byte[] bytes) {
    Log.d(TAG, "toIsoString(): bytes:" + bytes);

    try {
      return new String(bytes, CharacterSets.MIMENAME_ISO_8859_1);
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("ISO_8859_1 must be supported!");
    }
  }

  public static byte[] toIsoBytes(String isoString) {
    Log.d(TAG, "toIsoBytes(): isoString:" + isoString);

    try {
      return isoString.getBytes(CharacterSets.MIMENAME_ISO_8859_1);
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("ISO_8859_1 must be supported!");
    }
  }

  public static byte[] toUtf8Bytes(String utf8String) {
    Log.d(TAG, "toUtf8Bytes(): utf8String:" + utf8String);

    try {
      return utf8String.getBytes(CharacterSets.MIMENAME_UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("UTF_8 must be supported!");
    }
  }

  public static void wait(Object lock, long timeout) {
    Log.d(TAG, "wait(): timeout:" + timeout);

    try {
      lock.wait(timeout);
    } catch (InterruptedException ie) {
      throw new AssertionError(ie);
    }
  }

  public static String canonicalizeNumber(Context context, String number)
      throws InvalidNumberException
  {
    Log.d(TAG, "canonicalizeNumber(): number:" + number);

    String localNumber = TextSecurePreferences.getLocalNumber(context);
    return PhoneNumberFormatter.formatNumber(number, localNumber);
  }

  public static String canonicalizeNumberOrGroup(@NonNull Context context, @NonNull String number)
      throws InvalidNumberException
  {
    Log.d(TAG, "canonicalizeNumberOrGroup(): number:" + number);

    if (GroupUtil.isEncodedGroup(number)) return number;
    else                                  return canonicalizeNumber(context, number);
  }

  public static byte[] readFully(InputStream in) throws IOException {
    Log.d(TAG, "readFully()");

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    byte[] buffer              = new byte[4096];
    int read;

    while ((read = in.read(buffer)) != -1) {
      bout.write(buffer, 0, read);
    }

    in.close();

    return bout.toByteArray();
  }

  public static String readFullyAsString(InputStream in) throws IOException {
    Log.d(TAG, "readFullyAsString()");
    return new String(readFully(in));
  }

  public static long copy(InputStream in, OutputStream out) throws IOException {
    Log.d(TAG, "copy()");
    byte[] buffer = new byte[4096];
    int read;
    long total = 0;

    while ((read = in.read(buffer)) != -1) {
      out.write(buffer, 0, read);
      total += read;
    }

    in.close();
    out.close();

    return total;
  }

  public static String getDeviceE164Number(Context context) {
    Log.d(TAG, "getDeviceE164Number()");
    String localNumber = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
        .getLine1Number();

    if (!TextUtils.isEmpty(localNumber) && !localNumber.startsWith("+"))
    {
      if (localNumber.length() == 10) localNumber = "+1" + localNumber;
      else                            localNumber = "+"  + localNumber;

      return localNumber;
    }

    return null;
  }

  public static <T> List<List<T>> partition(List<T> list, int partitionSize) {
    List<List<T>> results = new LinkedList<>();

    for (int index=0;index<list.size();index+=partitionSize) {
      int subListSize = Math.min(partitionSize, list.size() - index);

      results.add(list.subList(index, index + subListSize));
    }

    return results;
  }

  public static List<String> split(String source, String delimiter) {
    Log.d(TAG, "getDeviceE164Number()");
    List<String> results = new LinkedList<>();

    if (TextUtils.isEmpty(source)) {
      return results;
    }

    String[] elements = source.split(delimiter);
    Collections.addAll(results, elements);

    return results;
  }

  public static byte[][] split(byte[] input, int firstLength, int secondLength) {
    Log.d(TAG, "split(): input:" + input.toString() + "firstLength: " + firstLength + "secondLength: " + secondLength);
    byte[][] parts = new byte[2][];

    parts[0] = new byte[firstLength];
    System.arraycopy(input, 0, parts[0], 0, firstLength);

    parts[1] = new byte[secondLength];
    System.arraycopy(input, firstLength, parts[1], 0, secondLength);

    return parts;
  }

  public static byte[] combine(byte[]... elements) {
    Log.d(TAG, "combine(): byte:" + elements.toString());

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      for (byte[] element : elements) {
        baos.write(element);
      }

      return baos.toByteArray();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  public static byte[] trim(byte[] input, int length) {
    Log.d(TAG, "trim(): byte:" + input.toString() + "length: " + length);

    byte[] result = new byte[length];
    System.arraycopy(input, 0, result, 0, result.length);

    return result;
  }

  @SuppressLint("NewApi")
  public static boolean isDefaultSmsProvider(Context context){
    Log.d(TAG, "isDefaultSmsProvider()");

    return (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) ||
      (context.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(context)));
  }

  public static int getCurrentApkReleaseVersion(Context context) {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      throw new AssertionError(e);
    }
  }

  public static String getSecret(int size) {
    Log.d(TAG, "getSecret(): size:" + size);

    byte[] secret = getSecretBytes(size);
    return Base64.encodeBytes(secret);
  }

  public static byte[] getSecretBytes(int size) {
    Log.d(TAG, "getSecretBytes(): size:" + size);

    byte[] secret = new byte[size];
    getSecureRandom().nextBytes(secret);
    return secret;
  }

  public static SecureRandom getSecureRandom() {
    Log.d(TAG, "getSecureRandom()");

    try {
      return SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

  public static boolean isBuildFresh() {
    Log.d(TAG, "isBuildFresh()");

    return BuildConfig.BUILD_TIMESTAMP + TimeUnit.DAYS.toMillis(90) > System.currentTimeMillis();
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  public static boolean isMmsCapable(Context context) {
    Log.d(TAG, "isMmsCapable()");

    return (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) || OutgoingLegacyMmsConnection.isConnectionPossible(context);
  }

  public static boolean isMainThread() {
    boolean isMainThread =  Looper.myLooper() == Looper.getMainLooper();
    Log.d(TAG, "isMainThread(): " + isMainThread);
    return isMainThread;
  }

  public static void assertMainThread() {
    Log.d(TAG, "assertMainThread()");

    if (!isMainThread()) {
      throw new AssertionError("Main-thread assertion failed.");
    }
  }

  public static void runOnMain(final @NonNull Runnable runnable) {
    Log.d(TAG, "runOnMain()");

    if (isMainThread()) runnable.run();
    else                handler.post(runnable);
  }

  public static void runOnMainSync(final @NonNull Runnable runnable) {
    Log.d(TAG, "runOnMainSync()");

    if (isMainThread()) {
      runnable.run();
    } else {
      final CountDownLatch sync = new CountDownLatch(1);
      runOnMain(new Runnable() {
        @Override public void run() {
          try {
            runnable.run();
          } finally {
            sync.countDown();
          }
        }
      });
      try {
        sync.await();
      } catch (InterruptedException ie) {
        throw new AssertionError(ie);
      }
    }
  }

  public static boolean equals(@Nullable Object a, @Nullable Object b) {
    return a == b || (a != null && a.equals(b));
  }

  public static int hashCode(@Nullable Object... objects) {
    return Arrays.hashCode(objects);
  }

  @TargetApi(VERSION_CODES.KITKAT)
  public static boolean isLowMemory(Context context) {
    Log.d(TAG, "isLowMemory()");

    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

    return (VERSION.SDK_INT >= VERSION_CODES.KITKAT && activityManager.isLowRamDevice()) ||
           activityManager.getMemoryClass() <= 64;
  }

  public static int clamp(int value, int min, int max) {
    Log.d(TAG, "clamp(): value: " + value + "min: " + min + "");
    return Math.min(Math.max(value, min), max);
  }

  public static float clamp(float value, float min, float max) {
    Log.d(TAG, "clamp(): value: " + value + "min: " + min + "");
    return Math.min(Math.max(value, min), max);
  }
}
