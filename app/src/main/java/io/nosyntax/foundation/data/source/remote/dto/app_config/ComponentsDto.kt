package io.nosyntax.foundation.data.source.remote.dto.app_config

import com.google.gson.annotations.SerializedName

data class ComponentsDto(
    @SerializedName("app_bar")
    val appBar: AppBar,
    @SerializedName("side_menu")
    val sideMenu: SideMenu,
    @SerializedName("loading_indicator")
    val loadingIndicator: LoadingIndicator
) {
    data class AppBar(
        @SerializedName("visible")
        val visible: Boolean,
        @SerializedName("background")
        val background: String,
        @SerializedName("title")
        val title: Title
    ) {
        data class Title(
            @SerializedName("visible")
            val visible: Boolean,
            @SerializedName("alignment")
            val alignment: String
        )
    }

    data class SideMenu(
        @SerializedName("visible")
        val visible: Boolean,
        @SerializedName("background")
        val background: String,
        @SerializedName("header")
        val header: Header,
        @SerializedName("items")
        val items: List<Item>
    ) {
        data class Header(
            @SerializedName("visible")
            val visible: Boolean,
            @SerializedName("image")
            val image: String
        )

        data class Item(
            @SerializedName("route")
            val route: String?,
            @SerializedName("type")
            val type: String,
            @SerializedName("label")
            val label: String?,
            @SerializedName("icon")
            val icon: String?,
            @SerializedName("action")
            val action: String?
        )
    }

    data class LoadingIndicator(
        @SerializedName("visible")
        val visible: Boolean,
        @SerializedName("animation")
        val animation: String,
        @SerializedName("background")
        val background: String,
        @SerializedName("color")
        val color: String
    )
}