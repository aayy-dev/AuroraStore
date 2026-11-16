/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.navigation

import android.os.Parcel
import com.aurora.gplayapi.model.App
import com.aurora.gplayapi.model.AppDependencies
import com.aurora.gplayapi.model.StreamCluster
import kotlinx.parcelize.Parceler
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

/**
 * Round-trips a [StreamCluster] through [Screen]'s `Parcelable` backstack as JSON.
 *
 * gplayapi-v4 models are deliberately never `Parcelable` (`core` has no Android dependency
 * at all), but [Screen.StreamBrowse] still needs to carry a whole, already-fetched
 * [StreamCluster] - not just an id - so its apps show immediately without a second network
 * round trip, the same way v3's `Parcelable` [StreamCluster] did. Every [StreamCluster] field
 * is itself `@Serializable`, so JSON is a generic bridge here rather than a field-by-field
 * `Parceler` that would have to be kept in sync with the model by hand.
 */
internal object StreamClusterParceler : Parceler<StreamCluster> {
    override fun create(parcel: Parcel): StreamCluster =
        Json.decodeFromString(parcel.readString().orEmpty())

    override fun StreamCluster.write(parcel: Parcel, flags: Int) {
        parcel.writeString(Json.encodeToString(this))
    }
}

/**
 * Saves a [StreamCluster] in the saved back stack as one JSON string, the same way
 * [StreamClusterParceler] does for parcels, rather than as a tree of nested saved-state
 * entries, which is several times bigger.
 */
internal object StreamClusterJsonSerializer : KSerializer<StreamCluster> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.aurora.store.StreamClusterJson", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: StreamCluster) {
        encoder.encodeString(Json.encodeToString(value))
    }

    override fun deserialize(decoder: Decoder): StreamCluster =
        Json.decodeFromString(decoder.decodeString())
}

/**
 * This cluster with each app cut down to what the app lists show, so it fits in the saved
 * back stack. Full apps carry descriptions, screenshots, file lists and more; with a long row
 * that went over the system's limit for saved state, and the app crashed when it went to the
 * background. Opening an app fetches its details again by package name, so nothing is lost.
 */
internal fun StreamCluster.forBackStack(): StreamCluster = copy(apps = apps.map { it.forList() })

private fun App.forList() = App(
    packageName = packageName,
    id = id,
    displayName = displayName,
    developerName = developerName,
    iconArtwork = iconArtwork,
    coverArtwork = coverArtwork,
    labeledRating = labeledRating,
    rating = rating,
    downloadString = downloadString,
    installs = installs,
    size = size,
    isFree = isFree,
    price = price,
    containsAds = containsAds,
    // Only for whether the app needs Google Play services
    dependencies = AppDependencies(dependentPackages = dependencies.dependentPackages)
)
