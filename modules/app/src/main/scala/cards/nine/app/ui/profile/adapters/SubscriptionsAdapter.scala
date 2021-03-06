/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cards.nine.app.ui.profile.adapters

import android.support.v7.widget.RecyclerView
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.TextView
import cards.nine.app.ui.commons.UiContext
import cards.nine.app.ui.commons.styles.CommonStyles
import cards.nine.app.ui.components.widgets.tweaks.CollectionCheckBoxTweaks._
import cards.nine.app.ui.commons.ops.SubscriptionOps._
import cards.nine.commons.ops.ColorOps._
import cards.nine.models.types.theme.DrawerTextColor
import cards.nine.models.{NineCardsTheme, Subscription}
import macroid.extras.ResourcesExtras._
import macroid.extras.TextViewTweaks._
import com.fortysevendeg.ninecardslauncher.{R, TR, TypedFindView}
import macroid.FullDsl._
import macroid._

case class SubscriptionsAdapter(
    subscriptions: Seq[Subscription],
    onSubscribe: (String, Boolean) => Unit)(
    implicit activityContext: ActivityContextWrapper,
    uiContext: UiContext[_],
    theme: NineCardsTheme)
    extends RecyclerView.Adapter[ViewHolderSubscriptionsAdapter] {

  override def getItemCount: Int = subscriptions.size

  override def onBindViewHolder(viewHolder: ViewHolderSubscriptionsAdapter, position: Int): Unit =
    viewHolder.bind(subscriptions(position), position).run

  override def onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolderSubscriptionsAdapter = {
    val view = LayoutInflater
      .from(parent.getContext)
      .inflate(R.layout.profile_subscription_item, parent, false)
    ViewHolderSubscriptionsAdapter(view, onSubscribe)
  }
}

case class ViewHolderSubscriptionsAdapter(content: View, onSubscribe: (String, Boolean) => Unit)(
    implicit context: ActivityContextWrapper,
    uiContext: UiContext[_],
    val theme: NineCardsTheme)
    extends RecyclerView.ViewHolder(content)
    with TypedFindView
    with CommonStyles {

  lazy val root = findView(TR.subscriptions_item_layout)

  lazy val iconContent = findView(TR.subscriptions_item_content)

  lazy val checkbox = findView(TR.collection_subscription_checkbox)

  lazy val name = findView(TR.subscriptions_item_name)

  lazy val status = findView(TR.subscriptions_item_status)

  def setNameStyle(subscribed: Boolean): Tweak[TextView] = {
    val deactivatedTitleAlpha = 0.37f
    val alpha                 = if (subscribed) titleAlpha else deactivatedTitleAlpha
    tvColor(theme.get(DrawerTextColor).alpha(alpha))
  }

  def setStatusStyle(subscribed: Boolean): Tweak[TextView] = {
    val deactivatedSubtitleAlpha = 0.24f
    val alpha                    = if (subscribed) subtitleAlpha else deactivatedSubtitleAlpha
    tvText(
      resGetString(if (subscribed) R.string.subscriptionActivated
      else R.string.subscriptionDeactivated)) +
      tvColor(theme.get(DrawerTextColor).alpha(alpha))
  }

  ((name <~ titleTextStyle) ~
    (status <~ subtitleTextStyle)).run

  def bind(subscription: Subscription, position: Int)(implicit uiContext: UiContext[_]): Ui[_] = {
    val subscriptionColor = theme.getIndexColor(subscription.themedColorIndex)
    val subscribed        = subscription.subscribed
    (checkbox <~ ccbInitialize(
      subscription.getIconSubscriptionDetail,
      subscriptionColor,
      theme,
      defaultCheck = subscribed)) ~
      (name <~ tvText(resGetString(subscription.name) getOrElse subscription.name) + setNameStyle(
        subscribed)) ~
      (status <~ setStatusStyle(subscribed)) ~
      (content <~ On.click(Ui(onSubscribe(subscription.sharedCollectionId, !subscribed))))
  }

  override def findViewById(id: Int): View = content.findViewById(id)

}
