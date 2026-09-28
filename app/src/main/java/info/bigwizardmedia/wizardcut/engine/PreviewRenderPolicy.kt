package info.bigwizardmedia.wizardcut.engine

import info.bigwizardmedia.wizardcut.model.EffectType

/** Runtime rules shared by the preview composition builder and its tests. */
internal object PreviewRenderPolicy {
    /** Single-input transitions are an export effect, not a live-preview effect. */
    fun includesTransitions(previewMode: Boolean): Boolean = !previewMode

    /** Background removal is intentionally omitted from the interactive preview. */
    fun includesEffect(effectType: EffectType, previewMode: Boolean): Boolean =
        !previewMode || effectType != EffectType.BG_REMOVAL
}
