package me.konyaco.collinsdictionary.service

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.konyaco.collinsdictionary.store.LocalStorage

class LocalCacheDictionary(private val localStorage: LocalStorage) : CollinsDictionary {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    override fun search(word: String): SearchResult {
        return when (val text = localStorage.getSearch(word)) {
            null -> SearchResult.NotFound(emptyList())
            word -> SearchResult.PreciseWord(word)
            else -> SearchResult.Redirect(text)
        }
    }

    override fun getDefinition(word: String): Word? {
        val text = localStorage.getDefinition(word) ?: return null
        return json.decodeFromString<Word>(text)
    }

    fun cacheSearchResult(word: String, searchResult: SearchResult) {
        when (searchResult) {
            is SearchResult.PreciseWord -> localStorage.saveSearch(word, word)
            is SearchResult.Redirect -> {
                localStorage.saveSearch(word, searchResult.redirectTo)
                localStorage.saveSearch(searchResult.redirectTo, searchResult.redirectTo)
            }
            is SearchResult.NotFound -> {}
        }
    }

    fun cacheDefinition(word: String, definition: Word) {
        localStorage.saveDefinition(word, json.encodeToString(definition))
    }
}