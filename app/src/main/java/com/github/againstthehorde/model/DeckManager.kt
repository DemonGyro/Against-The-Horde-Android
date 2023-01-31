package com.github.againstthehorde.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.Collections.emptyList

class DeckDataPattern {
    companion object {
        const val deck: String = "## Horde Deck ##"
        const val tooStrong: String = "## Too Strong ##"
        const val availableTokens: String = "## Available Tokens ##"
        const val weakPermanents: String = "## Weak Permanents ##"
        const val powerfulPermanents: String = "## Powerful Permanents ##"
    }
}

class DeckId {
    companion object {
        const val Zombie = 0
        const val Human = 1
        const val Dinosaur = 2
        const val Nature = 3
        const val Phyrexian = 5
        const val Sliver = 4
        const val Eldrazi = 6
    }
}

class DeckManager {
    class CardDataIndices {
        companion object {
            const val Copies = 0
            const val Set = 1
            const val Type = 2
            const val FlashbackFlag = 3
            const val Name = 4
            const val OracleId = -2
            const val CardId = -1
        }
    }

    companion object {
        fun getDeckForId(context: Context, deckId: Int, difficulty: Int): HordeDeck {
            val deckData: String? = context.getSharedPreferences("Decks", Context.MODE_PRIVATE).getString("Deck_$deckId", "")
            return createDeckListFromDeckData(deckData, difficulty)
        }

        fun createDeckListFromDeckData(deckData: String?, difficulty: Int): HordeDeck {
            if (deckData == null || deckData.isEmpty()) {
                return HordeDeck(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
            }

            val deck = DeckEditorCardList(MainDeckList(emptyList(), emptyList(), emptyList(), emptyList()), emptyList(), emptyList(), emptyList(), emptyList())

            val allLines = deckData.split("\n")
            var selectedDeckList = ""
            for (line in allLines) {
                val trimLine = line.trim()
                when(trimLine) {
                    "" -> {}
                    DeckDataPattern.deck,
                        DeckDataPattern.availableTokens,
                        DeckDataPattern.tooStrong,
                        DeckDataPattern.weakPermanents,
                        DeckDataPattern.powerfulPermanents -> selectedDeckList = trimLine
                    else -> {
                        val cardData = trimLine.split(" ")
                        var cardName: String = cardData[CardDataIndices.Name]
                        for (i in 5 until cardData.size-2) {
                            cardName += " " + cardData[i]
                        }

                        val card = Card(
                            cardName,
                            cardType = getCardTypeFromTypeLine(cardData[CardDataIndices.Type]),
                            hasFlashback = cardData[CardDataIndices.FlashbackFlag].lowercase() == "yes",
                            specificSet = cardData[CardDataIndices.Set],
                            cardOracleId = cardData[cardData.size + CardDataIndices.OracleId],
                            cardId = cardData[cardData.size + CardDataIndices.CardId],
                            cardCount = try { cardData[CardDataIndices.Copies].toInt() } catch (e: NumberFormatException) { 0 }
                        )

                        addCardToSelectedDeck(card, selectedDeckList, deck)
                    }
                }
            }

            val hordeDeck: MutableList<Card> = mutableListOf()
            hordeDeck.addAll(deck.deckList.creatures)
            hordeDeck.addAll(deck.deckList.artifactsAndEnchantments)
            hordeDeck.addAll(deck.deckList.instantsAndSorceries)
            for (i in 0 until difficulty) {
                hordeDeck.addAll(deck.deckList.tokens)
            }
            hordeDeck.shuffle()

            return HordeDeck(hordeDeck, deck.availableTokensList, deck.tooStrongPermanentsList, deck.weakPermanentsList, deck.powerfulPermanentsList)
        }

        fun getCardTypeFromTypeLine(type: String): CardType {
            return when (type.trim().lowercase()) {
                "artifact" -> CardType.Artifact
                "creature" -> CardType.Creature
                "enchantment" -> CardType.Enchantment
                "instant" -> CardType.Instant
                "sorcery" -> CardType.Sorcery
                "token" -> CardType.Token
                else -> CardType.Unknown
            }
        }

        fun addCardToSelectedDeck(card: Card, selectedDeckList: String, deck: DeckEditorCardList) {
            when (selectedDeckList) {
                DeckDataPattern.deck -> {
                    when (card.cardType) {
                        CardType.Creature -> deck.deckList.creatures = addCardToDeck(card, deck.deckList.creatures)
                        CardType.Artifact, CardType.Enchantment -> deck.deckList.artifactsAndEnchantments = addCardToDeck(card, deck.deckList.artifactsAndEnchantments)
                        CardType.Instant, CardType.Sorcery -> deck.deckList.instantsAndSorceries = addCardToDeck(card, deck.deckList.instantsAndSorceries)
                        CardType.Token -> {
                            deck.deckList.tokens = addCardToDeck(card, deck.deckList.tokens)
                            deck.availableTokensList = addCardToDeck(card, deck.availableTokensList, unique = true)
                        }
                        CardType.Unknown -> {}
                    }
                }
                DeckDataPattern.tooStrong -> addCardToDeck(card, deck.tooStrongPermanentsList)
                DeckDataPattern.availableTokens -> addCardToDeck(card, deck.availableTokensList)
                DeckDataPattern.weakPermanents -> addCardToDeck(card, deck.weakPermanentsList)
                DeckDataPattern.powerfulPermanents -> addCardToDeck(card, deck.powerfulPermanentsList)
                else -> {}
            }
        }

        fun addCardToDeck(card: Card, deck: List<Card>, unique: Boolean = false): List<Card> {
            if (unique && deck.contains(card)) {
                return deck
            }

            val tmpDeck = deck.toMutableList()
            for (i in 0..(if (unique) 0 else card.cardCount - 1)) {
                tmpDeck.add(card.recreateCard(1))
            }
            return tmpDeck.toList()
        }

        fun createStarterDecks(context: Context) {
            createZombieDeck(context)
            createHumanDeck(context)
            createDinosaurDeck(context)
            createNatureDeck(context)
            createSliverDeck(context)
            createPhyrexianDeck(context)
            createEldraziDeck(context)
        }

        private fun createZombieDeck(context: Context) {
            createDeck(
                context,
                "Zombie",
                "The original horde deck by Peter Knudson\nArt by Grezegorz Rutkowski",
                "All creatures controlled by the Horde have haste",
                DeckId.Zombie
            )
        }

        private fun createHumanDeck(context: Context) {
            createDeck(
                context,
                "Human",
                "A modified version of the Armies of Men deck by TenkayCrit.\\nArt by Antonio José Manzanedo",
                "All creatures controlled by the Horde have haste and are Humans in addition to their other creature types. All tokens controlled by the Horde are white",
                DeckId.Human
            )
        }

        private fun createDinosaurDeck(context: Context) {
            createDeck(
                context,
                "Dinosaur",
                "A modified version of the Dinosaur Rage deck by TenkayCrit\nArt by Grzegorz Rutkowski",
                "All creatures controlled by the Horde have haste.",
                DeckId.Dinosaur
            )
        }

        private fun createPhyrexianDeck(context: Context) {
            createDeck(
                context,
                "Phyrexian",
                "A modified version of the Phyrexian Perfection deck by TenkayCrit\\nArt by Igor Kieryluk",
                "All creatures controlled by the Horde have haste. The Survivors share poison counters. They do not lose the game for having 10 or more poison counters. Every time the Survivors gain one or more poison counters, each Survivor exiles 1 card from the top of each of their libraries face down for each poison counter.",
                DeckId.Phyrexian
            )
        }

        private fun createSliverDeck(context: Context) {
            createDeck(
                context,
                "Sliver",
                "A modified version of the Sliver Hive deck by TenkayCrit\nArt by Aleksi Briclot",
                "All creatures controlled by the Horde have haste. All of the artifact slivers in the Horde deck are treated as tokens.",
                DeckId.Sliver
            )
        }

        private fun createNatureDeck(context: Context) {
            createDeck(
                context,
                "Nature",
                "Art by Grzegorz Rutkowski",
                "All tokens controlled by the Horde have haste.",
                DeckId.Nature
            )
        }

        private fun createEldraziDeck(context: Context) {
            createDeck(
                context,
                "Eldrazi",
                "A modified version of the Eldrazi Horror deck by TenkayCrit\nArt by Aleksi Briclot",
                "All tokens controlled by the Horde have haste. All eldrazi spawn the Horde controls cannot attack or block. If the Horde controls 10 eldrazi spawn at the start of its precombat main phase, they are sacrificed, and the Horde casts the three eldrazi titans from exile.",
                DeckId.Eldrazi
            )
        }

        private fun createDeck(context: Context, deckName: String, deckIntro: String, deckRules: String, deckId: Int) {
            val prefs = context.getSharedPreferences("Decks", Context.MODE_PRIVATE)

            if (prefs.getBoolean("Deck_" + deckId + "_Exists", false)) {
                return
            }

            val deckData = readDeckDataFromFile(deckName)
            val deckImage = ContextCompat.getDrawable(context, context.resources.getIdentifier("deck_picker_img__" + deckName.lowercase(), "drawable", context.packageName))
            assert(deckImage != null)

            val stream = ByteArrayOutputStream()
            deckImage!!.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, stream)

            prefs.edit()
                .putBoolean("Deck_" + deckId + "_Exists", true)
                .putString("Deck_" + deckId + "_DeckName", deckName)
                .putString("Deck_" + deckId + "_DeckIntro", deckIntro)
                .putString("Deck_" + deckId + "_DeckRules", deckRules)
                .putString("Deck_" + deckId + "_Image", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT))
                .putString("Deck_$deckId", deckData).apply()
        }

        private fun readDeckDataFromFile(deckName: String): String {
            val fs = DeckManager::class.java.getResourceAsStream("$deckName.txt") ?: return ""
            return fs.bufferedReader().use(BufferedReader::readText)
        }
    }
}