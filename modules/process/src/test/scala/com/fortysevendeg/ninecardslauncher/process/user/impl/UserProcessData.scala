package com.fortysevendeg.ninecardslauncher.process.user.impl

import com.fortysevendeg.ninecardslauncher.process.user.models.Device
import com.fortysevendeg.ninecardslauncher.services.api.models.{AndroidDevice, GoogleDevice, Installation, User}
import com.fortysevendeg.ninecardslauncher.services.persistence.FindUserByIdRequest
import com.fortysevendeg.ninecardslauncher.services.persistence.models.{User => ServicesUser}
import org.specs2.mock.Mockito
import org.specs2.specification.Scope

trait UserProcessData
  extends Scope
  with Mockito {

  val statusCodeUser = 101

  val statusCodeOk = 200

  val userDBId = 1

  val userId = "fake-user-id"

  val userToken = "fake-user-token"

  val email = "example@47deg.com"

  val device = Device(
    name = "Nexus X",
    deviceId = "",
    secretToken = "",
    permissions = Seq.empty)

  val googleDevice = GoogleDevice(
    name = device.name,
    deviceId = device.deviceId,
    secretToken = device.secretToken,
    permissions = device.permissions)

  val user = User(
    id = Option(userId),
    sessionToken = Option(userToken),
    email = Option(email),
    devices = Seq(googleDevice))

  val persistenceUser = ServicesUser(
    id = userDBId,
    userId = None,
    email = None,
    sessionToken = None,
    installationId = None,
    deviceToken = None,
    androidToken = None
  )

  val installationStatusCode = 102

  val installationId = "fake-installation-id"
  val installationToken = "fake-user-token"
  val deviceType = Some(AndroidDevice)

  val initialInstallation = Installation(None, deviceType, None, None)

  val installation = Installation(
    id = Option(installationId),
    deviceType = deviceType,
    deviceToken = Option(installationToken),
    userId = Option(userId)
  )

  val fileFolder = "/file/example"

  val findUserByIdRequest = FindUserByIdRequest(userDBId)

}
