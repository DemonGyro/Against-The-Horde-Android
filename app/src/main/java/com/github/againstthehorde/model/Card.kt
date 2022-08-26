package com.github.againstthehorde.model

import android.graphics.drawable.Drawable
import java.util.*

enum class CardType {
    Token,
    Creature,
    Enchantment,
    Artifact,
    Sorcery,
    Instant
}

data class CardsToCast(
    val cardsFromGraveyard: List<Card>,
    val tokensFromLibrary: List<Card>,
    val cardFromLibrary: Card
)

data class MainDeckList(
    val creatures: List<Card>,
    val tokens: List<Card>,
    val instantsAndSorceries: List<Card>,
    val artifactsAndEnchantments: List<Card>
)

data class DeckEditorCardList(
    val deckList: MainDeckList,
    val tooStrongPermanentsList: List<Card>,
    val availableTokensList: List<Card>,
    val weakPermanentsList: List<Card>,
    val powerfulPermanentsList: List<Card>
)

open class Card
    (
    cardName: String,// Can be changed in deck editor
    private var cardType: CardType,
    cardImageUrl: String? = null,
    private var cardUIImage: Drawable? = null,
    private var hasFlashback: Boolean = false,
    private var specificSet: String? = null,// Unique id of a card but same for each reprints
    private var cardOracleId: String? = null,// Unique id of card and unique between reprints
    private var cardId: String? = null
) {
    // static functions and variables
    companion object {

        private fun getScryfallImageUrl(cardId: String): String {
            val cardResolution = "normal"
            val url = "https://api.scryfall.com/cards/$cardId?format=img&version=$cardResolution"

            print(url)
            return url
        }

        private fun getScryfallImageUrl(cardName: String, specificSet: String?): String {
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

        private fun getUrlCardName(cardName: String): String {
            return cardName
                .replace(" ", "-")
                .replace("\"", "")
                .replace(",", "") // Maybe "-" instead of "" ?????
                .replace("'", "")
        }

        fun emptyCard(): Card {
            return Card("Polyraptor", CardType.Token)
        }
    }

    private val uuid: UUID = UUID.randomUUID()
    private var cardName: String
    private var cardImageUrl: String?
    private var cardCount: Int = 1

    init {
        // Remove after "//" in name, example : "Amethyst Dragon // Explosive Crystal" -> only keep Amethyst Dragon
        val indexOfSlashes = cardName.indexOf(" //")
        this.cardName = cardName.substring(if (indexOfSlashes >= 0) indexOfSlashes + 4 else 0).trim()
        if (cardImageUrl == null) {
            if (cardId != null) {
                this.cardImageUrl = getScryfallImageUrl(cardId!!)
            } else {
                this.cardImageUrl = getScryfallImageUrl(cardName, specificSet)
            }
        } else {
            this.cardImageUrl = cardImageUrl
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
        tmpCard.cardCount = this.cardCount
        return tmpCard
    }
}

class CardFromCardSearch(
    cardName: String,
    cardType: CardType,
    cardImageUrl: String? = null,
    cardUIImage: Drawable? = null,
    hasFlashback: Boolean = false,
    specificSet: String? = null,
    cardOracleId: String? = null,
    cardId: String? = null,
    private var manaCost: String?
) : Card(
    cardName,
    cardType,
    cardImageUrl,
    cardUIImage,
    hasFlashback,
    specificSet,
    cardOracleId,
    cardId
) {

    // From {3}{R}{W} to ["3", "R", "W"]
    fun getManaCostArray(): List<String> {
        if (this.manaCost == null) {
            return emptyList()
        }

        val matches = "\\{(.*?)}".toRegex().findAll(this.manaCost!!)
        return matches.map { it.groupValues[1] }.toList()
    }
}