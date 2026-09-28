package info.bigwizardmedia.wizardcut.engine

import info.bigwizardmedia.wizardcut.model.BlendMode
import info.bigwizardmedia.wizardcut.model.Track

/**
 * Single source of truth for track-level blend-mode support.
 *
 * Clip-level blend modes have a shader path. Media3's public compositor still
 * exposes only alpha and transform for track layers, so accepting a track
 * blend edit would create a saved value that the renderer cannot honor.
 */
object TrackBlendModeCapability {
    val supportedModes: Set<BlendMode> = setOf(BlendMode.NORMAL)

    fun isSupported(mode: BlendMode): Boolean = mode in supportedModes

    fun unsupportedTracks(tracks: List<Track>): List<Track> =
        tracks.filter { !isSupported(it.blendMode) }
}
