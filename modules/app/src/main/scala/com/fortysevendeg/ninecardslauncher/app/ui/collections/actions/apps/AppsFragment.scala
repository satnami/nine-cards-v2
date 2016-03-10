package com.fortysevendeg.ninecardslauncher.app.ui.collections.actions.apps

import android.os.Bundle
import android.view.View
import com.fortysevendeg.ninecardslauncher.app.commons.NineCardIntentConversions
import com.fortysevendeg.ninecardslauncher.app.ui.collections.CollectionsDetailsActivity
import com.fortysevendeg.ninecardslauncher.app.ui.commons.SafeUi._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.TasksOps._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.UiExtensions
import com.fortysevendeg.ninecardslauncher.app.ui.commons.actions.BaseActionFragment
import com.fortysevendeg.ninecardslauncher.process.collection.AddCardRequest
import com.fortysevendeg.ninecardslauncher.process.commons.types.{AllAppsCategory, AppCardType, NineCardCategory}
import com.fortysevendeg.ninecardslauncher.process.device.GetByName
import com.fortysevendeg.ninecardslauncher.process.device.models.{App, IterableApps}
import com.fortysevendeg.ninecardslauncher2.R

import scalaz.concurrent.Task

class AppsFragment
  extends BaseActionFragment
  with AppsComposer
  with UiExtensions
  with NineCardIntentConversions {

  val allApps = AllAppsCategory

  lazy val category = NineCardCategory(getString(Seq(getArguments), AppsFragment.categoryKey, AllAppsCategory.name))

  override def getLayoutId: Int = R.layout.list_action_with_scroller_fragment

  override def onViewCreated(view: View, savedInstanceState: Bundle): Unit = {
    super.onViewCreated(view, savedInstanceState)
    initUi(category == allApps, checked => loadApps(if (checked) {
      AppsByCategory
    } else {
      AllApps
    }, reload = true)).run

    loadApps(if (category == allApps) AllApps else AppsByCategory)
  }

  private[this] def loadApps(
    filter: AppsFilter,
    reload: Boolean = false): Unit = // TODO Use filter by category in ticket 9C-350
    Task.fork(di.deviceProcess.getIterableApps(GetByName).run).resolveAsyncUi(
      onPreTask = () => showLoading,
      onResult = (apps: IterableApps) => if (reload) {
        reloadAppsAdapter(apps, filter, category)
      } else {
        generateAppsAdapter(apps, filter, category, (app: App) => {
          val card = AddCardRequest(
            term = app.name,
            packageName = Option(app.packageName),
            cardType = AppCardType,
            intent = toNineCardIntent(app),
            imagePath = app.imagePath
          )
          activity[CollectionsDetailsActivity] foreach (_.addCards(Seq(card)))
          unreveal().run
        })
      },
      onException = (ex: Throwable) => showError(R.string.errorLoadingApps, loadApps(filter, reload))
    )

}

object AppsFragment {
  val categoryKey = "category"
}

sealed trait AppsFilter

case object AllApps extends AppsFilter

case object AppsByCategory extends AppsFilter
