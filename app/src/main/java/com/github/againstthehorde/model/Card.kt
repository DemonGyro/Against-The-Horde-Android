package com.github.againstthehorde.model

import android.graphics.drawable.Drawable
import java.util.*

//struct CardsToCast {
//    var cardsFromGraveyard: [Card]
//    var tokensFromLibrary: [Card]
//    var cardFromLibrary: Card
//}

enum class CardType {
    token,
    creature,
    enchantment,
    artifact,
    sorcery,
    instant
}

//struct DeckEditorCardList {
//    var deckList: MainDeckList
//    var tooStrongPermanentsList: [Card]
//    var availableTokensList: [Card]
//    var weakPermanentsList: [Card]
//    var powerfullPermanentsList: [Card]
//}
//
//struct MainDeckList {
//    var creatures: [Card]
//    var tokens: [Card]
//    var instantsAndSorceries: [Card]
//    var artifactsAndEnchantments: [Card]
//}

class Card: Cloneable {
    companion object {
        fun getUrlCardName(cardName: String): String {
            return cardName
                .replace(" ", "-")
                .replace("\"", "")
                .replace(",", "") // Maybe "-" instead of "" ?????
                .replace("'", "")
        }

        fun getScryfallImageUrlFromCardName(cardName: String, specificSet: String?): String? {
            // Examples
            // https://api.scryfall.com/cards/named?exact=Zombie-Giant&format=img&version=normal
            // https://api.scryfall.com/cards/named?exact=Amethyst-Dragon-//-Explosive-Crystal&format=img&version=normal

            val cardNameForUrl = getUrlCardName(cardName)
            val cardResolution = "normal"
            var url = "https://api.scryfall.com/cards/named?exact=$cardNameForUrl&format=img&version=$cardResolution"

            if (specificSet != null) {
                url += "&set=$specificSet"
            }

            print(url)
            return url
        }

        fun getScryfallImageUrl(cardId: String): String {
            val cardResolution = "normal"
            val url = "https://api.scryfall.com/cards/$cardId?format=img&version=$cardResolution"

            print(url)
            return url
        }

        fun emptyCard(): Card {
            return Card("Polyraptor", CardType.token)
        }
    }

    private val uuid: UUID = UUID.randomUUID()
    private var cardName: String
    private var cardType: CardType          // Can be changed in deckeditor
    private var cardImageUrl: String?
    private var cardUIImage: Drawable?
    private var hasFlashback: Boolean
    private var specificSet: String?
    private var cardOracleId: String?       // Unique id of a card but same for each reprints
    private var cardId: String?             // Unique id of card and unique between reprints
    private var cardCount: Int = 1

    constructor(cardName: String, cardType: CardType,
                cardImageUrl: String? = null,
                cardUIImage: Drawable? = null,
                hasFlashback: Boolean = false,
                specificSet: String? = null,
                cardOracleId: String? = null,
                cardId: String? = null) {
        this.cardType = cardType;
        this.cardUIImage = cardUIImage;
        this.hasFlashback = hasFlashback;
        this.specificSet = specificSet;
        this.cardOracleId = cardOracleId;
        this.cardId = cardId;

        // Remove after "//" in name, example : "Amethyst Dragon // Explosive Crystal" -> only keep Amethyst Dragon
        val indexOfSlashes = cardName.indexOf(" //")
        this.cardName = cardName.substring(if (indexOfSlashes >= 0) indexOfSlashes + 4 else 0).trim()

        if (cardImageUrl == null) {
            if (cardId != null) {
                this.cardImageUrl = getScryfallImageUrl(cardId)
            } else {
                this.cardImageUrl = getScryfallImageUrlFromCardName(cardName, specificSet)
            }
        } else {
            this.cardImageUrl = cardImageUrl;
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(uuid, this.cardId)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Card) {
            other.cardId == this.cardId
        } else {
            super.equals(other)
        }
    }

    fun recreateCard(): Card {
        val tmpCard = Card(this.cardName, this.cardType, this.cardImageUrl, this.cardUIImage, this.hasFlashback, this.specificSet, this.cardOracleId, this.cardId)
        tmpCard.cardCount = this.cardCount;
        return tmpCard
    }
}