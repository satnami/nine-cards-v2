package com.fortysevendeg.ninecardslauncher.process.sharedcollections.models

import com.fortysevendeg.ninecardslauncher.process.commons.types.NineCardCategory

sealed trait SubscriptionType

case object NotSubscribed extends SubscriptionType

case object Subscribed extends SubscriptionType

case object Owned extends SubscriptionType

case class SharedCollection(
  id: String,
  sharedCollectionId: String,
  publishedOn: Long,
  author: String,
  name: String,
  packages: Seq[String],
  resolvedPackages: Seq[SharedCollectionPackage],
  views: Int,
  subscriptions: Option[Int],
  category: NineCardCategory,
  icon: String,
  community: Boolean,
  subscriptionType: SubscriptionType)

case class CreateSharedCollection(
   author: String,
   name: String,
   packages: Seq[String],
   category: NineCardCategory,
   icon: String,
   community: Boolean)

case class UpdateSharedCollection(
   sharedCollectionId: String,
   name: String,
   packages: Seq[String])

case class SharedCollectionPackage(
  packageName: String,
  title: String,
  icon: String,
  stars: Double,
  downloads: String,
  free: Boolean)

case class CreatedCollection(
  name: String,
  author: String,
  packages: Seq[String],
  category: NineCardCategory,
  sharedCollectionId: String,
  icon: String,
  community: Boolean
)

case class Subscription(
  id: Int,
  sharedCollectionId: String,
  name: String,
  apps: Int,
  icon: String,
  themedColorIndex: Int,
  subscribed: Boolean)
