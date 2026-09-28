package info.bigwizardmedia.wizardcut.ui.editor

import info.bigwizardmedia.wizardcut.engine.CaptionTranslationEngine
import info.bigwizardmedia.wizardcut.engine.whisper.SherpaAsrEngine
import info.bigwizardmedia.wizardcut.model.Caption

internal fun captionsToTranslationSegments(
    captions: List<Caption>
): List<SherpaAsrEngine.TranscriptionSegment> = captions
    .filter { it.text.isNotBlank() && it.endTimeMs >= it.startTimeMs }
    .sortedBy { it.startTimeMs }
    .map { caption ->
        SherpaAsrEngine.TranscriptionSegment(
            text = caption.text,
            startTimeMs = caption.startTimeMs,
            endTimeMs = caption.endTimeMs,
            words = caption.words.map { word ->
                SherpaAsrEngine.WordTimestamp(
                    word = word.text,
                    startTimeMs = word.startTimeMs,
                    endTimeMs = word.endTimeMs,
                    confidence = word.confidence,
                )
            },
        )
    }

internal fun translatedSegmentToTranscriptionSegment(
    segment: CaptionTranslationEngine.TranslatedSegment
): SherpaAsrEngine.TranscriptionSegment = SherpaAsrEngine.TranscriptionSegment(
    text = segment.sourceText,
    startTimeMs = segment.startTimeMs,
    endTimeMs = segment.endTimeMs,
    words = segment.words,
)
