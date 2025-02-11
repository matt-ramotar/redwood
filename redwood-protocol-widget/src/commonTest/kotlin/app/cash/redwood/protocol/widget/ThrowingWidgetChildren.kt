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
package app.cash.redwood.protocol.widget

import app.cash.redwood.widget.Widget

class ThrowingWidgetChildren<W : Any> : Widget.Children<W> {
  override fun insert(index: Int, widget: Widget<W>) = throw AssertionError()
  override fun move(fromIndex: Int, toIndex: Int, count: Int) = throw AssertionError()
  override fun remove(index: Int, count: Int) = throw AssertionError()
  override fun onLayoutModifierUpdated(index: Int) = throw AssertionError()
}
