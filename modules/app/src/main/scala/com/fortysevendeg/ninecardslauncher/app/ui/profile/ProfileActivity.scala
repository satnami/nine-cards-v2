package com.fortysevendeg.ninecardslauncher.app.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.view.{Menu, MenuItem}
import com.fortysevendeg.ninecardslauncher.app.commons.{BroadcastDispatcher, ContextSupportProvider}
import com.fortysevendeg.ninecardslauncher.app.di.Injector
import com.fortysevendeg.ninecardslauncher.app.services.SynchronizeDeviceService
import com.fortysevendeg.ninecardslauncher.app.ui.commons._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.action_filters._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.google_api.GoogleApiClientActivityProvider
import com.fortysevendeg.ninecardslauncher.process.theme.models.NineCardsTheme
import com.fortysevendeg.ninecardslauncher2.{R, TypedFindView}
import com.google.android.gms.common.api.GoogleApiClient
import macroid.{Contexts, Ui}
import rapture.core.Answer

import scala.util.Try
import scalaz.concurrent.Task

case class GoogleApiClientStatuses(
  apiClient: Option[GoogleApiClient] = None,
  username: Option[String] = None)

class ProfileActivity
  extends AppCompatActivity
  with Contexts[AppCompatActivity]
  with ContextSupportProvider
  with TypedFindView
  with SystemBarsTint
  with ProfileListener
  with ProfileComposer
  with ProfileTasks
  with GoogleApiClientActivityProvider
  with AppBarLayout.OnOffsetChangedListener
  with BroadcastDispatcher {

  self =>

  import AppUtils._
  import SyncDeviceState._
  import TasksOps._

  implicit lazy val di = new Injector

  implicit lazy val uiContext: UiContext[Activity] = ActivityUiContext(this)

  implicit lazy val theme: NineCardsTheme = di.themeProcess.getSelectedTheme.run.run match {
    case Answer(t) => t
    case _ => getDefaultTheme
  }

  var clientStatuses = GoogleApiClientStatuses()

  var userProfileStatuses = UserProfileStatuses()

  var syncEnabled: Boolean = false

  override val actionsFilters: Seq[String] = SyncActionFilter.cases map (_.action)

  override def manageCommand(action: String, data: Option[String]): Unit = (SyncActionFilter(action), data) match {
    case (SyncStateActionFilter, Some(`stateSuccess`)) =>
      // TODO - Reload adapter
      showMessage(R.string.accountSynced).run
      syncEnabled = false
    case (SyncStateActionFilter, Some(`stateFailure`)) =>
      showMessage(R.string.errorSyncing).run
      syncEnabled = true
    case (SyncAnswerActionFilter, Some(`stateSyncing`)) =>
      // Nothing now. We can show a loading here if it's necessary
      syncEnabled = false
    case _ =>
  }

  override def onCreate(bundle: Bundle) = {
    super.onCreate(bundle)
    loadUserProfile()
    setContentView(R.layout.profile_activity)
    initUi.run

    toolbar foreach setSupportActionBar
    Option(getSupportActionBar) foreach { actionBar =>
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(iconIndicatorDrawable)
    }

    barLayout foreach (_.addOnOffsetChangedListener(this))
  }

  override def onResume(): Unit = {
    super.onResume()
    registerDispatchers
    self ? SyncAskActionFilter.action
  }

  override def onPause(): Unit = {
    super.onPause()
    unregisterDispatcher
  }

  override def onStop(): Unit = {
    clientStatuses match {
      case GoogleApiClientStatuses(Some(client), _) => Try(client.disconnect())
      case _ =>
    }
    super.onStop()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.profile_menu, menu)
    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = item.getItemId match {
    case android.R.id.home =>
      finish()
      true
    case R.id.action_logout =>
      quit()
      true
    case _ =>
      super.onOptionsItemSelected(item)
  }

  override def onOffsetChanged(appBarLayout: AppBarLayout, offset: Int): Unit = {
    val maxScroll = appBarLayout.getTotalScrollRange.toFloat
    val percentage = Math.abs(offset) / maxScroll

    (handleToolbarVisibility(percentage) ~ handleProfileVisibility(percentage)).run
  }

  override def onRequestConnectionError(errorCode: Int): Unit =
    showError(R.string.errorConnectingGoogle, () => tryToConnect())

  override def onResolveConnectionError(): Unit =
    showError(R.string.errorConnectingGoogle, () => tryToConnect())

  override def tryToConnect(): Unit = clientStatuses.apiClient foreach (_.connect())

  override def onConnected(bundle: Bundle): Unit = {
    super.onConnected(bundle)
    clientStatuses match {
      case GoogleApiClientStatuses(Some(client), Some(email)) if client.isConnected =>
        loadUserAccounts(client, email)
      case _ => showError(R.string.errorConnectingGoogle, () => tryToConnect())
    }
  }

  def sampleItems(tab: String) = 1 to 20 map (i => s"$tab Item $i")

  override def onProfileTabSelected(profileTab: ProfileTab): Unit = profileTab match {
    case PublicationsTab =>
      // TODO - Load publications and set adapter
      setPublicationsAdapter(sampleItems("Publication")).run
    case SubscriptionsTab =>
      // TODO - Load subscriptions and set adapter
      setSubscriptionsAdapter(sampleItems("Subscription")).run
    case AccountsTab =>
      clientStatuses match {
        case GoogleApiClientStatuses(Some(client), Some(email)) if client.isConnected =>
          loadUserAccounts(client, email)
        case GoogleApiClientStatuses(Some(client), Some(email)) =>
          tryToConnect()
          showLoading.run
        case _ =>
          loadUserInfo()
      }
  }

  override def onSyncActionClicked(): Unit = if (syncEnabled) launchService()

  def onConnectedUserProfile(name: String, email: String, avatarUrl: Option[String]): Unit = userProfile(name, email, avatarUrl).run

  def onConnectedPlusProfile(coverPhotoUrl: String): Unit = {}

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit =
    userProfileStatuses.userProfile foreach (_.connectUserProfile(requestCode, resultCode, data))

  def launchService(): Unit = {
    syncEnabled = false
    startService(new Intent(this, classOf[SynchronizeDeviceService]))
  }

  private[this] def loadUserProfile(): Unit =
    Task.fork(loadUserEmail().run).resolveAsyncUi(
      onResult = email => Ui {
        val userProfile = email map { email =>
          new UserProfileProvider(
            account = email,
            onConnectedUserProfile = onConnectedUserProfile,
            onConnectedPlusProfile = onConnectedPlusProfile)
        }
        userProfileStatuses = userProfileStatuses.copy(userProfile = userProfile)
        userProfileStatuses.userProfile foreach (_.connect())
      })

  private[this] def loadUserInfo(): Unit =
    Task.fork(loadUserEmail().run).resolveAsyncUi(
      onResult = email => Ui {
        val client = email map createGoogleDriveClient
        clientStatuses = clientStatuses.copy(
          apiClient = client,
          username = email)
        client foreach (_.connect())
      },
      onException = (_) => showError(R.string.errorLoadingUser, loadUserInfo),
      onPreTask = () => showLoading
    )

  private[this] def loadUserAccounts(client: GoogleApiClient, username: String): Unit =
    Task.fork(loadAccounts(client, username).run).resolveAsyncUi(
      onResult = accountSyncs => {
        syncEnabled = true
        setAccountsAdapter(accountSyncs)
      },
      onException = (_) => showError(R.string.errorConnectingGoogle, () => loadUserAccounts(client, username)),
      onPreTask = () => showLoading
    )

  private[this] def quit(): Unit =
    Task.fork(logout().run).resolveAsyncUi(
      onResult = (_) => Ui {
        setResult(ResultCodes.logoutSuccessful)
        finish()
      },
      onException = (_) => showError(R.string.contactUsError, quit))

}