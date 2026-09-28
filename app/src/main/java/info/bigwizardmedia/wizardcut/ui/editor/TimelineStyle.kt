package info.bigwizardmedia.wizardcut.ui.editor

import info.bigwizardmedia.wizardcut.ui.theme.ClearCutAccents
import androidx.compose.ui.graphics.Color
import info.bigwizardmedia.wizardcut.model.TrackType

internal fun trackAccentColor(trackType: TrackType): Color = when (trackType) {
    TrackType.VIDEO -> ClearCutAccents.Blue
    TrackType.AUDIO -> ClearCutAccents.Green
    TrackType.OVERLAY -> ClearCutAccents.Peach
    TrackType.TEXT -> ClearCutAccents.Mauve
    TrackType.ADJUSTMENT -> ClearCutAccents.Yellow
}
