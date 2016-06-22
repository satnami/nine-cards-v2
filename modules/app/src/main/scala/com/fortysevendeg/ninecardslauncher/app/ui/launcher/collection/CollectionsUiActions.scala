package com.fortysevendeg.ninecardslauncher.app.ui.launcher.collection

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import com.fortysevendeg.macroid.extras.DeviceVersion.KitKat
import com.fortysevendeg.macroid.extras.DrawerLayoutTweaks._
import com.fortysevendeg.macroid.extras.FragmentExtras._
import com.fortysevendeg.macroid.extras.ImageViewTweaks._
import com.fortysevendeg.macroid.extras.NavigationViewTweaks._
import com.fortysevendeg.macroid.extras.ResourcesExtras._
import com.fortysevendeg.macroid.extras.TextTweaks._
import com.fortysevendeg.macroid.extras.ViewGroupTweaks._
import com.fortysevendeg.macroid.extras.ViewTweaks._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.AppUtils._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.AsyncImageTweaks._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.ColorOps._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.CommonsTweak._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.ExtraTweaks._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.PositionsUtils._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.SafeUi._
import com.fortysevendeg.ninecardslauncher.app.ui.commons._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.actions.{ActionsBehaviours, BaseActionFragment}
import com.fortysevendeg.ninecardslauncher.app.ui.components.drawables.{CharDrawable, EdgeWorkspaceDrawable}
import com.fortysevendeg.ninecardslauncher.app.ui.components.layouts.tweaks.AnimatedWorkSpacesTweaks._
import com.fortysevendeg.ninecardslauncher.app.ui.components.layouts.tweaks.DockAppsPanelLayoutTweaks._
import com.fortysevendeg.ninecardslauncher.app.ui.components.layouts.tweaks.LauncherWorkSpacesTweaks._
import com.fortysevendeg.ninecardslauncher.app.ui.components.layouts.{LauncherWorkSpacesListener, WorkspaceItemMenu}
import com.fortysevendeg.ninecardslauncher.app.ui.components.models.LauncherData
import com.fortysevendeg.ninecardslauncher.app.ui.components.widgets.TintableImageView
import com.fortysevendeg.ninecardslauncher.app.ui.components.widgets.tweaks.TintableImageViewTweaks._
import com.fortysevendeg.ninecardslauncher.app.ui.launcher.LauncherUiActionsImpl
import com.fortysevendeg.ninecardslauncher.app.ui.launcher.actions.newcollection.NewCollectionFragment
import com.fortysevendeg.ninecardslauncher.app.ui.launcher.actions.privatecollections.PrivateCollectionsFragment
import com.fortysevendeg.ninecardslauncher.app.ui.launcher.actions.publicollections.PublicCollectionsFragment
import com.fortysevendeg.ninecardslauncher.app.ui.launcher.snails.LauncherSnails._
import com.fortysevendeg.ninecardslauncher.app.ui.preferences.NineCardsPreferencesActivity
import com.fortysevendeg.ninecardslauncher.app.ui.profile.ProfileActivity
import com.fortysevendeg.ninecardslauncher.process.commons.models.Collection
import com.fortysevendeg.ninecardslauncher.process.device.models.DockApp
import com.fortysevendeg.ninecardslauncher2.{R, TR, TypedFindView}
import macroid.FullDsl._
import macroid._

trait CollectionsUiActions
  extends Styles
  with ActionsBehaviours {

  self: TypedFindView with SystemBarsTint with Contexts[AppCompatActivity] with LauncherUiActionsImpl =>

  // TODO We select the page in ViewPager with collections. In the future this will be a user preference
  val selectedPageDefault = 1

  val maxBackgroundPercent: Float = 0.7f

  val pageMoments = 0

  val pageCollections = 1

  lazy val drawerLayout = Option(findView(TR.launcher_drawer_layout))

  lazy val navigationView = Option(findView(TR.launcher_navigation_view))

  lazy val menuName = Option(findView(TR.menu_name))

  lazy val menuEmail = Option(findView(TR.menu_email))

  lazy val menuAvatar = Option(findView(TR.menu_avatar))

  lazy val menuCover = Option(findView(TR.menu_cover))

  lazy val loading = Option(findView(TR.launcher_loading))

  lazy val root = Option(findView(TR.launcher_root))

  lazy val dockAppsPanel = Option(findView(TR.launcher_dock_apps_panel))

  lazy val content = Option(findView(TR.launcher_content))

  lazy val workspaces = Option(findView(TR.launcher_work_spaces))

  lazy val workspacesEdgeLeft = Option(findView(TR.launcher_work_spaces_edge_left))

  lazy val workspacesEdgeRight = Option(findView(TR.launcher_work_spaces_edge_right))

  lazy val paginationPanel = Option(findView(TR.launcher_pagination_panel))

  lazy val searchPanel = Option(findView(TR.launcher_search_panel))

  lazy val collectionActionsPanel = Option(findView(TR.launcher_collections_actions_panel))

  lazy val burgerIcon = Option(findView(TR.launcher_burger_icon))

  lazy val googleIcon = Option(findView(TR.launcher_google_icon))

  lazy val micIcon = Option(findView(TR.launcher_mic_icon))

  lazy val actionFragmentContent = Option(findView(TR.action_fragment_content))

  lazy val menuCollectionRoot = Option(findView(TR.menu_collection_root))

  lazy val menuWorkspaceContent = Option(findView(TR.menu_workspace_content))

  lazy val menuLauncherContent = Option(findView(TR.menu_launcher_content))

  lazy val menuLauncherWallpaper = Option(findView(TR.menu_launcher_wallpaper))

  lazy val menuLauncherWidgets = Option(findView(TR.menu_launcher_widgets))

  lazy val menuLauncherSettings = Option(findView(TR.menu_launcher_settings))

  def initCollectionsUi: Ui[_] =
    (drawerLayout <~ dlStatusBarBackground(android.R.color.transparent)) ~
      (navigationView <~ nvNavigationItemSelectedListener(itemId => {
        (goToMenuOption(itemId) ~ closeMenu()).run
        true
      })) ~
      (workspacesEdgeLeft <~ vBackground(new EdgeWorkspaceDrawable(left = true))) ~
      (workspacesEdgeRight <~ vBackground(new EdgeWorkspaceDrawable(left = false))) ~
      (menuCollectionRoot <~ vGone) ~
      (workspaces <~
        lwsInitialize(presenter, theme) <~
        lwsListener(
          LauncherWorkSpacesListener(
            onStartOpenMenu = startOpenCollectionMenu,
            onUpdateOpenMenu = updateOpenCollectionMenu,
            onEndOpenMenu = closeCollectionMenu
          )
        )) ~
      (searchPanel <~ searchContentStyle) ~
      (menuWorkspaceContent <~ vgAddViews(getItemsForFabMenu)) ~
      (menuLauncherWallpaper <~ launcherButtonWallpaperStyle <~ On.click {
        closeCollectionMenu() ~ uiStartIntent(new Intent(Intent.ACTION_SET_WALLPAPER))
      }) ~
      (menuLauncherWidgets <~ launcherButtonWidgetsStyle <~ On.click {
        closeCollectionMenu() ~ Ui(presenter.goToWidgets())
      }) ~
      (menuLauncherSettings <~ launcherButtonSettingsStyle <~ On.click {
        closeCollectionMenu() ~ uiStartIntent(new Intent(activityContextWrapper.getOriginal, classOf[NineCardsPreferencesActivity]))
      }) ~
      (burgerIcon <~ burgerButtonStyle <~ On.click(
        drawerLayout <~ dlOpenDrawer
      )) ~
      (googleIcon <~ googleButtonStyle <~ On.click(Ui(presenter.launchSearch))) ~
      (micIcon <~ micButtonStyle <~ On.click(Ui(presenter.launchVoiceSearch)))

  def showMessage(message: Int, args: Seq[String] = Seq.empty): Ui[_] =
    workspaces <~ Tweak[View] { view =>
      val snackbar = Snackbar.make(view, activityContextWrapper.application.getString(message, args:_*), Snackbar.LENGTH_SHORT)
      snackbar.getView.getLayoutParams match {
        case params : FrameLayout.LayoutParams =>
          val bottom = KitKat.ifSupportedThen (getNavigationBarHeight) getOrElse 0
          params.setMargins(0, 0, 0, bottom)
          snackbar.getView.setLayoutParams(params)
        case _ =>
      }
      snackbar.show()
    }

  def showCollectionsLoading: Ui[_] = loading <~ vVisible

  def showLauncherInfo(
    data: Seq[LauncherData],
    apps: Seq[DockApp]): Ui[_] = {
    (loading <~ vGone) ~
      (dockAppsPanel <~ daplInit(apps)) ~
      (workspaces <~
        vGlobalLayoutListener(_ =>
          (workspaces <~
            lwsData(data, selectedPageDefault) <~
            awsAddPageChangedObserver(currentPage => {
              (paginationPanel <~ reloadPager(currentPage)).run
            })) ~
            createPager(selectedPageDefault)
        ))
  }

  def userProfileMenu(
    maybeEmail: Option[String],
    maybeName: Option[String],
    maybeAvatarUrl: Option[String],
    maybeCoverUrl: Option[String]): Ui[_] =
    (menuName <~ tvText(maybeName.getOrElse(""))) ~
      (menuEmail <~ tvText(maybeEmail.getOrElse(""))) ~
      (menuAvatar <~
        ((maybeAvatarUrl, maybeName) match {
          case (Some(url), _) => ivUri(url)
          case (_, Some(name)) => ivSrc(new CharDrawable(name.substring(0, 1).toUpperCase))
          case _ => ivBlank
        }) <~
        menuAvatarStyle) ~
      (menuCover <~
        (maybeCoverUrl match {
          case Some(url) => ivUri(url)
          case None => ivBlank
        }))

  def closeMenu(): Ui[_] = drawerLayout <~ dlCloseDrawer

  def closeCollectionMenu(): Ui[_] = workspaces <~ lwsCloseMenu

  def cleanWorkspaces(): Ui[_] = workspaces <~ lwsClean

  def isMenuVisible: Boolean = drawerLayout exists (_.isDrawerOpen(GravityCompat.START))

  def isCollectionMenuVisible: Boolean = workspaces exists (_.workSpacesStatuses.openedMenu)

  def goToWorkspace(page: Int): Ui[_] = (workspaces <~ lwsSelect(page)) ~ (paginationPanel <~ reloadPager(page))

  def goToNextWorkspace(): Ui[_] =
    (workspaces ~> lwsNextScreen()).get.flatten map { next =>
      goToWorkspace(next)
    } getOrElse Ui.nop

  def goToPreviousWorkspace(): Ui[_] =
    (workspaces ~> lwsPreviousScreen()).get.flatten map { previous =>
      goToWorkspace(previous)
    } getOrElse Ui.nop

  protected def goToMenuOption(itemId: Int): Ui[_] = {
    (itemId, activityContextWrapper.original.get) match {
      case (R.id.menu_collections, _) => goToWorkspace(pageCollections)
      case (R.id.menu_moments, _) => goToWorkspace(pageMoments)
      case (R.id.menu_profile, Some(activity)) => uiStartIntentForResult(new Intent(activity, classOf[ProfileActivity]), RequestCodes.goToProfile)
      case (R.id.menu_send_feedback, _) => showNoImplementedYetMessage()
      case (R.id.menu_help, _) => showNoImplementedYetMessage()
      case (R.id.menu_change_moment, _) => Ui(presenter.goToChangeMoment())
      case _ => Ui.nop
    }
  }

  def getCollections: Seq[Collection] = (workspaces ~> lwsGetCollections()).get getOrElse Seq.empty

  def getCountCollections: Int = (workspaces ~> lwsCountCollections).get getOrElse 0

  protected def isEmptyCollections: Boolean = (workspaces ~> lwsEmptyCollections).get getOrElse false

  protected def getItemsForFabMenu = Seq(
    (w[WorkspaceItemMenu] <~ workspaceButtonCreateCollectionStyle <~ FuncOn.click { view: View =>
      showAction(f[NewCollectionFragment], view, resGetColor(R.color.collection_fab_button_item_create_new_collection))
    }).get,
    (w[WorkspaceItemMenu] <~ workspaceButtonMyCollectionsStyle <~ FuncOn.click { view: View =>
      showAction(f[PrivateCollectionsFragment], view, resGetColor(R.color.collection_fab_button_item_my_collections))
    }).get,
    (w[WorkspaceItemMenu] <~ workspaceButtonPublicCollectionStyle <~ FuncOn.click { view: View =>
      showAction(f[PublicCollectionsFragment], view, resGetColor(R.color.collection_fab_button_item_public_collection))
    }).get
  )

  private[this] def startOpenCollectionMenu(): Ui[_] = {
    val height = (menuLauncherContent map (_.getHeight) getOrElse 0) + getNavigationBarHeight
    (menuCollectionRoot <~ vVisible <~ vClearClick) ~
      (menuWorkspaceContent <~ vAlpha(0) <~ vTranslationY(height)) ~
      (menuLauncherContent <~ vTranslationY(height)) ~
      (dockAppsPanel <~ fade(out = true)) ~
      (paginationPanel <~ fade(out = true)) ~
      (searchPanel <~ fade(out = true))
  }

  private[this] def updateOpenCollectionMenu(percent: Float): Ui[_] = {
    val backgroundPercent = maxBackgroundPercent * percent
    val colorBackground = Color.BLACK.alpha(backgroundPercent)
    val height = (menuLauncherContent map (_.getHeight) getOrElse 0) + getNavigationBarHeight
    val translate = height - (height * percent)
    (menuCollectionRoot <~ vBackgroundColor(colorBackground)) ~
      (menuLauncherContent <~ vTranslationY(translate)) ~
      (menuWorkspaceContent <~ vAlpha(percent) <~ vTranslationY(translate))
  }

  private[this] def closeCollectionMenu(opened: Boolean): Ui[_] =
    if (opened) {
      menuCollectionRoot <~ On.click(closeCollectionMenu())
    } else {
      (dockAppsPanel <~ fade()) ~
        (paginationPanel <~ fade()) ~
        (searchPanel <~ fade()) ~
        (menuCollectionRoot <~ vGone)
    }

  private[this] def createPager(activePosition: Int): Ui[Any] =
    workspaces map { ws =>
      val maybeColorMoment = for {
        first <- getData.headOption
        moment <- first.moment
        collection <- moment.collection
      } yield resGetColor(getIndexColor(collection.themedColorIndex))
      val pagerViews = 0 until ws.getWorksSpacesCount map { position =>
        val view = pagination(position, if (position == 0) maybeColorMoment else None)
        view.setActivated(activePosition == position)
        view
      }
      paginationPanel <~ vgRemoveAllViews <~ vgAddViews(pagerViews)
    } getOrElse Ui.nop

  def reloadWorkspacePager: Ui[Any] = (workspaces ~> lwsCurrentPage()).get map createPager getOrElse Ui.nop

  def pagination(position: Int, maybeColor: Option[Int]) =
    (w[TintableImageView] <~ paginationItemStyle <~ vSetPosition(position) <~ (maybeColor map tivDefaultColor getOrElse Tweak.blank)).get

  private[this] def showAction[F <: BaseActionFragment]
  (fragmentBuilder: FragmentBuilder[F], view: View, color: Int, map: Map[String, String] = Map.empty): Ui[_] = {
    val sizeIconWorkSpaceMenuItem = resGetDimensionPixelSize(R.dimen.size_workspace_menu_item)
    val (startX: Int, startY: Int) = Option(view.findViewById(R.id.workspace_icon)) map calculateAnchorViewPosition getOrElse(0, 0)
    val x = startX + (sizeIconWorkSpaceMenuItem / 2)
    val y = startY + (sizeIconWorkSpaceMenuItem / 2)
    val args = new Bundle()
    args.putInt(BaseActionFragment.sizeIcon, sizeIconWorkSpaceMenuItem)
    args.putInt(BaseActionFragment.startRevealPosX, x)
    args.putInt(BaseActionFragment.startRevealPosY, y)
    args.putInt(BaseActionFragment.endRevealPosX, x)
    args.putInt(BaseActionFragment.endRevealPosY, y)
    map foreach {
      case (key, value) => args.putString(key, value)
    }
    args.putInt(BaseActionFragment.colorPrimary, color)
    (fragmentContent <~ vClickable(true)) ~
      addFragment(fragmentBuilder.pass(args), Option(R.id.action_fragment_content), Option(nameActionFragment))
  }

}
