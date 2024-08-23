/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.modules.dialog

import android.content.DialogInterface
import android.os.Looper.getMainLooper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.facebook.react.R
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.JavaOnlyMap
import com.facebook.react.bridge.ReactApplicationContext
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class DialogModuleTest {

  private lateinit var activityController: ActivityController<FragmentActivity>
  private lateinit var activity: FragmentActivity
  private lateinit var dialogModule: DialogModule

  class SimpleCallback : Callback {
    var args: Array<out Any?>? = null
      private set

    var calls: Int = 0
      private set

    override fun invoke(vararg args: Any?) {
      calls++
      this.args = args
    }
  }

  @Before
  fun setUp() {
    setupActivity()
  }

  @After
  fun tearDown() {
    activityController.pause().stop().destroy()
  }

  @Test
  fun testAllOptions() {
    val options =
        JavaOnlyMap().apply {
          putString("title", "Title")
          putString("message", "Message")
          putString("buttonPositive", "OK")
          putString("buttonNegative", "Cancel")
          putString("buttonNeutral", "Later")
          putBoolean("cancelable", false)
        }

    dialogModule.showAlert(options, null, null)
    shadowOf(getMainLooper()).idle()

    val fragment = getFragment()

    assertFalse(fragment.isCancelable)

    val dialog = fragment.dialog as AlertDialog
    assertEquals("OK", dialog.getButton(DialogInterface.BUTTON_POSITIVE).text.toString())
    assertEquals("Cancel", dialog.getButton(DialogInterface.BUTTON_NEGATIVE).text.toString())
    assertEquals("Later", dialog.getButton(DialogInterface.BUTTON_NEUTRAL).text.toString())
  }

  @Test
  fun testCallbackPositive() {
    val options = JavaOnlyMap().apply { putString("buttonPositive", "OK") }

    val actionCallback = SimpleCallback()
    dialogModule.showAlert(options, null, actionCallback)
    shadowOf(getMainLooper()).idle()

    val dialog = getFragment().dialog as AlertDialog
    dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
    shadowOf(getMainLooper()).idle()

    assertEquals(1, actionCallback.calls)
    assertEquals(DialogModule.ACTION_BUTTON_CLICKED, actionCallback.args?.get(0))
    assertEquals(DialogInterface.BUTTON_POSITIVE, actionCallback.args?.get(1))
  }

  @Test
  fun testCallbackNegative() {
    val options = JavaOnlyMap().apply { putString("buttonNegative", "Cancel") }

    val actionCallback = SimpleCallback()
    dialogModule.showAlert(options, null, actionCallback)
    shadowOf(getMainLooper()).idle()

    val dialog = getFragment().dialog as AlertDialog
    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()
    shadowOf(getMainLooper()).idle()

    assertEquals(1, actionCallback.calls)
    assertEquals(DialogModule.ACTION_BUTTON_CLICKED, actionCallback.args?.get(0))
    assertEquals(DialogInterface.BUTTON_NEGATIVE, actionCallback.args?.get(1))
  }

  @Test
  fun testCallbackNeutral() {
    val options = JavaOnlyMap().apply { putString("buttonNeutral", "Later") }

    val actionCallback = SimpleCallback()
    dialogModule.showAlert(options, null, actionCallback)
    shadowOf(getMainLooper()).idle()

    val dialog = getFragment().dialog as AlertDialog
    dialog.getButton(DialogInterface.BUTTON_NEUTRAL).performClick()
    shadowOf(getMainLooper()).idle()

    assertEquals(1, actionCallback.calls)
    assertEquals(DialogModule.ACTION_BUTTON_CLICKED, actionCallback.args?.get(0))
    assertEquals(DialogInterface.BUTTON_NEUTRAL, actionCallback.args?.get(1))
  }

  @Test
  fun testCallbackDismiss() {
    val options = JavaOnlyMap()

    val actionCallback = SimpleCallback()
    dialogModule.showAlert(options, null, actionCallback)
    shadowOf(getMainLooper()).idle()

    getFragment().dialog?.dismiss()
    shadowOf(getMainLooper()).idle()

    assertEquals(1, actionCallback.calls)
    assertEquals(DialogModule.ACTION_DISMISSED, actionCallback.args?.get(0))
  }

  @Test
  fun testNonAppCompatActivityTheme() {
    setupActivity(NON_APP_COMPAT_THEME)

    val options = JavaOnlyMap()

    val actionCallback = SimpleCallback()
    dialogModule.showAlert(options, null, actionCallback)
    shadowOf(getMainLooper()).idle()

    getFragment().dialog?.dismiss()
    shadowOf(getMainLooper()).idle()

    assertEquals(1, actionCallback.calls)
    assertEquals(DialogModule.ACTION_DISMISSED, actionCallback.args?.get(0))
  }

  private fun setupActivity(theme: Int = APP_COMPAT_THEME) {
    activityController = Robolectric.buildActivity(FragmentActivity::class.java)
    activity = activityController.create().start().resume().get()

    // We must set the theme to a descendant of AppCompat for the AlertDialog to show without
    // raising an exception
    activity.setTheme(theme)

    val context: ReactApplicationContext = mock(ReactApplicationContext::class.java)
    whenever(context.hasActiveReactInstance()).thenReturn(true)
    whenever(context.currentActivity).thenReturn(activity)

    dialogModule = DialogModule(context)
    dialogModule.onHostResume()
  }

  private fun getFragment(): AlertFragment {
    val maybeFragment = activity.supportFragmentManager.findFragmentByTag(DialogModule.FRAGMENT_TAG)
    if (maybeFragment == null || !(maybeFragment is AlertFragment)) {
      error("Fragment was not displayed")
    }
    return maybeFragment
  }

  companion object {
    private val APP_COMPAT_THEME: Int = R.style.Theme_ReactNative_AppCompat_Light
    private val NON_APP_COMPAT_THEME: Int = android.R.style.Theme_DeviceDefault_Light
  }
}
