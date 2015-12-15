package com.fortysevendeg.ninecardslauncher.process.collection.impl

import com.fortysevendeg.ninecardslauncher.commons.NineCardExtensions.{CatchAll, _}
import com.fortysevendeg.ninecardslauncher.commons.contexts.ContextSupport
import com.fortysevendeg.ninecardslauncher.commons.services.Service
import com.fortysevendeg.ninecardslauncher.process.collection.models.Card
import com.fortysevendeg.ninecardslauncher.process.collection.{AddCardRequest, CardException}
import com.fortysevendeg.ninecardslauncher.process.types.{CardType, NoInstalledAppCardType}
import com.fortysevendeg.ninecardslauncher.services.persistence.{DeleteCardRequest => ServicesDeleteCardRequest, ImplicitsPersistenceServiceExceptions, PersistenceServiceException}
import rapture.core.Answer
import rapture.core.scalazInterop.ResultT

import scalaz.concurrent.Task

trait CardsProcessImpl {

  self: CollectionProcessDependencies
    with FormedCollectionConversions
    with ImplicitsPersistenceServiceExceptions =>

  def addCards(collectionId: Int, addCardListRequest: Seq[AddCardRequest]): ResultT[Task, Seq[Card], CardException] =
    (for {
      cardList <- persistenceServices.fetchCardsByCollection(toFetchCardsByCollectionRequest(collectionId))
      addedCardList <- addCardList(collectionId, addCardListRequest, cardList.size)
    } yield toCardSeq(addedCardList)).resolve[CardException]

  def deleteCard(collectionId: Int, cardId: Int) =
    (for {
      Some(card) <- persistenceServices.findCardById(toFindCardByIdRequest(cardId))
      cardList <- getCardsByCollectionId(collectionId)
      _ <- persistenceServices.deleteCard(ServicesDeleteCardRequest(card))
      _ <- updateCardList(moveCardList(cardList, card.position))
    } yield ()).resolve[CardException]

  def reorderCard(collectionId: Int, cardId: Int, newPosition: Int) =
    (for {
      Some(card) <- persistenceServices.findCardById(toFindCardByIdRequest(cardId))
      cardList <- getCardsByCollectionId(collectionId)
      _ <- updateCardList(reorderCardList(cardList, newPosition, card.position))
    } yield ()).resolve[CardException]

  def editCard(collectionId: Int, cardId: Int, name: String) =
    (for {
      Some(card) <- persistenceServices.findCardById(toFindCardByIdRequest(cardId))
      updatedCard = toUpdatedCard(toCard(card), name)
      _ <- updateCard(updatedCard)
    } yield updatedCard).resolve[CardException]

  def updateNoInstalledCardsInCollections(packageName: String)(implicit contextSupport: ContextSupport) =
    (for {
      app <- appsServices.getApplication(packageName)
      cardList <- persistenceServices.fetchCards
      cardsNoInstalled = cardList filter (card => CardType(card.cardType) == NoInstalledAppCardType && card.packageName.contains(packageName))
      card = toCardSeq(toInstalledApp(cardsNoInstalled, app))
      _ <- updateCardList(toCardSeq(toInstalledApp(cardsNoInstalled, app)))
    } yield ()).resolve[CardException]

  private[this] def addCardList(collectionId: Int, cardList: Seq[AddCardRequest], position: Int) = Service {
    val tasks = cardList.indices map (item => persistenceServices.addCard(toAddCardRequest(collectionId, cardList(item), position + item)).run)
    Task.gatherUnordered(tasks) map (c => CatchAll[PersistenceServiceException](c.collect { case Answer(r) => r}))
  }

  private[this] def moveCardList(cardList: Seq[Card], position: Int) =
    cardList map { card =>
      if (card.position > position) toNewPositionCard(card, position - 1) else card
    }

  private[this] def reorderCardList(cardList: Seq[Card], newPosition: Int, oldPosition: Int): Seq[Card] =
    cardList map { card =>
      val position = card.position
      (newPosition, oldPosition, position) match {
        case (n, o, p) if n < o && p > n && p < o => toNewPositionCard(card, p + 1)
        case (n, o, p) if n > o && p < n && p > o => toNewPositionCard(card, p - 1)
        case (n, o, _) if n < o || n > o => card
        case _ => toNewPositionCard(card, newPosition)
      }
    }

  private[this] def updateCard(card: Card) =
    (for {
      _ <- persistenceServices.updateCard(toServicesUpdateCardRequest(card))
    } yield ()).resolve[CardException]

  private[this] def updateCardList(cardList: Seq[Card]) = Service {
    val tasks = cardList map (card => updateCard(card).run)
    Task.gatherUnordered(tasks) map (c => CatchAll[CardException](c.collect { case Answer(r) => r}))
  }

  private[this] def getCardsByCollectionId(collectionId: Int) = (
    persistenceServices.fetchCardsByCollection(toFetchCardsByCollectionRequest(collectionId)) map toCardSeq).resolve[CardException]

}