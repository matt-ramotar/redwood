/*
 * Copyright (C) 2022 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.treehouse

import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.cValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UITraitCollection
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIView
import platform.UIKit.removeFromSuperview
import platform.UIKit.setFrame
import platform.UIKit.subviews
import platform.UIKit.superview

@ObjCName("TreehouseUIKitView", exact = true)
public class TreehouseUIKitView(
  override val widgetSystem: WidgetSystem,
) : TreehouseView {
  public val view: UIView = RootUiView(this)

  override var readyForContentChangeListener: ReadyForContentChangeListener? = null
    set(value) {
      check(value == null || field == null) { "View already bound to a listener" }
      field = value
    }

  override val readyForContent: Boolean
    get() = view.superview != null

  private val _children = UIViewChildren(view)
  override val children: Widget.Children<UIView> get() = _children

  private val mutableHostConfiguration =
    MutableStateFlow(computeHostConfiguration(view.traitCollection))

  override val hostConfiguration: StateFlow<HostConfiguration>
    get() = mutableHostConfiguration

  override fun reset() {
    _children.remove(0, _children.widgets.size)

    // Ensure any out-of-band views are also removed.
    @Suppress("UNCHECKED_CAST") // Correct generic lost by cinterop.
    (view.subviews as List<UIView>).forEach(UIView::removeFromSuperview)
  }

  internal fun superviewChanged() {
    readyForContentChangeListener?.onReadyForContentChanged(this)
  }

  internal fun updateHostConfiguration() {
    mutableHostConfiguration.value = computeHostConfiguration(view.traitCollection)
  }
}

private fun computeHostConfiguration(
  traitCollection: UITraitCollection,
): HostConfiguration {
  return HostConfiguration(
    darkMode = traitCollection.userInterfaceStyle == UIUserInterfaceStyleDark,
  )
}

@Suppress("unused") // cinterop erroneously exposes these as extension functions.
private class RootUiView(
  private val treehouseView: TreehouseUIKitView,
) : UIView(cValue { CGRectZero }) {
  @ObjCAction fun layoutSubviews() {
    subviews.forEach {
      (it as UIView).setFrame(bounds)
    }
  }

  @ObjCAction fun didMoveToSuperview() {
    treehouseView.superviewChanged()
  }

  override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
    super.traitCollectionDidChange(previousTraitCollection)
    treehouseView.updateHostConfiguration()
  }
}
