package com.example.videolibrary

import java.util.HashMap
import java.util.LinkedHashMap

class UUDataSource {

    var currentUrlIndex: Int = 0
    var urlsMap = LinkedHashMap(HashMap<String,Any>())
    var title = ""
    var headerMap = HashMap<String,Any>()

    var looping = false
    var objects: Array<Any>? = null

    val currentUrl: Any?
        get() = getValueFromLinkedMap(currentUrlIndex)

    val currentKey: Any?
        get() = getKeyFromDataSource(currentUrlIndex)

    constructor(url: String) {
        urlsMap.put(URL_KEY_DEFAULT, url)
        currentUrlIndex = 0
    }

    constructor(url: String, title: String) {
        urlsMap.put(URL_KEY_DEFAULT, url)
        this.title = title
        currentUrlIndex = 0
    }

    constructor(url: Any) {
        urlsMap.put(URL_KEY_DEFAULT, url)
        currentUrlIndex = 0
    }

    constructor(urlsMap: LinkedHashMap<String, *>) {
        this.urlsMap.clear()
        this.urlsMap.putAll(urlsMap)
        currentUrlIndex = 0
    }

    constructor(urlsMap: LinkedHashMap<String, *>, title: String) {
        this.urlsMap.clear()
        this.urlsMap.putAll(urlsMap)
        this.title = title
        currentUrlIndex = 0
    }

    fun getKeyFromDataSource(index: Int): String? {
        var currentIndex = 0
        for (key in urlsMap.keys) {
            if (currentIndex == index) {
                return key.toString()
            }
            currentIndex++
        }
        return null
    }

    fun getValueFromLinkedMap(index: Int): Any? {
        var currentIndex = 0
        for (key in urlsMap.keys) {
            if (currentIndex == index) {
                return urlsMap.get(key)
            }
            currentIndex++
        }
        return null
    }

    fun containsTheUrl(`object`: Any?): Boolean {
        return if (`object` != null) {
            urlsMap.containsValue(`object`)
        } else false
    }

    fun cloneMe(): UUDataSource {
        return UUDataSource(urlsMap, title)
    }

    companion object {

        val URL_KEY_DEFAULT = "URL_KEY_DEFAULT"
    }
}
