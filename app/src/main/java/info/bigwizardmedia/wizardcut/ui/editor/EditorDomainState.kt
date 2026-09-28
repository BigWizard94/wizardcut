package info.bigwizardmedia.wizardcut.ui.editor

import info.bigwizardmedia.wizardcut.engine.AiToolRequirements
import info.bigwizardmedia.wizardcut.engine.AiUsageLedger
import info.bigwizardmedia.wizardcut.engine.CaptionTranslationEngine
import info.bigwizardmedia.wizardcut.engine.CaptionImportEngine
import info.bigwizardmedia.wizardcut.engine.CutAssistantEngine
import info.bigwizardmedia.wizardcut.engine.ExportHistoryEntry
import info.bigwizardmedia.wizardcut.engine.MediaRelinkProbe
import info.bigwizardmedia.wizardcut.engine.MediaHealthReport
import info.bigwizardmedia.wizardcut.engine.MediaDiagnostic
import info.bigwizardmedia.wizardcut.engine.Media3TrimOptimizationPolicy
import info.bigwizardmedia.wizardcut.engine.MetadataSidecarFormat
import info.bigwizardmedia.wizardcut.engine.SmartRenderEngine
import info.bigwizardmedia.wizardcut.engine.ExportState
import info.bigwizardmedia.wizardcut.engine.StabilizationEngine
import info.bigwizardmedia.wizardcut.engine.StabilizationProfileValidation
import info.bigwizardmedia.wizardcut.model.StabilizationProfile
import info.bigwizardmedia.wizardcut.ai.AutoEditResult
import info.bigwizardmedia.wizardcut.model.BatchExportItem
import info.bigwizardmedia.wizardcut.model.ExportConfig

/**
 * Typed projection of the large editor state bag into domain-owned slices.
 *
 * This is the first decomposition layer: each slice is sealed by
 * [EditorDomainState], can be tested independently, and gives later storage
 * migrations a stable target without forcing every `EditorState.copy(...)`
 * caller to move in one risky change.
 */
sealed interface EditorDomainState {
    val kind: Kind

    enum class Kind {
        PANEL,
        CAPTION,
        COMPOUND,
        EXPORT,
        AI,
        MEDIA
    }
}

data class EditorPanelState(
    val panels: PanelVisibility = PanelVisibility(),
    val selectedEffectId: String? = null,
    val editingTextOverlayId: String? = null
) : EditorDomainState {
    override val kind: EditorDomainState.Kind = EditorDomainState.Kind.PANEL
}

data class EditorCaptionState(
    val translationRows: List<CaptionTranslationEngine.EditorRow> = emptyList(),
    val sourceLang: String = "en",
    val targetLang: String? = null,
    val quality: CaptionTranslationEngine.LanguagePairQuality? = null,
    val variant: CaptionTranslationEngine.ModelVariant = CaptionTranslationEngine.ModelVariant.NLLB_600M,
    val captionImportPreview: CaptionImportEngine.Preview? = null,
    // True when the user requested a translation but no translation model is
    // installed, so the panel shows "model required" instead of untranslated
    // rows presented as a translation.
    val translationUnavailable: Boolean = false,
    // True when a translation action was blocked by the live connectivity gate.
    val translationOffline: Boolean = false,
) : EditorDomainState {
    override val kind: EditorDomainState.Kind = EditorDomainState.Kind.CAPTION
}

data class EditorCompoundState(
    val depth: Int = 0,
    val breadcrumbText: String = ""
) : EditorDomainState {
    override val kind: EditorDomainState.Kind = EditorDomainState.Kind.COMPOUND
}

data class EditorExportDomainState(
    val config: ExportConfig = ExportConfig(),
    val progress: Float = 0f,
    val state: ExportState = ExportState.IDLE,
    val lastExportedFilePath: String? = null,
    val errorMessage: String? = null,
    val warningMessage: String? = null,
    val trimOptimizationDisclosure: Media3TrimOptimizationPolicy.Disclosure? = null,
    val startTime: Long = 0L,
    val encoderName: String? = null,
    val etaMs: Long? = null,
    val stallWarning: Boolean = false,
    val renderSegments: List<SmartRenderEngine.RenderSegment> = emptyList(),
    val renderSummary: SmartRenderEngine.SmartRenderSummary? = null,
    val batchQueue: List<BatchExportItem> = emptyList(),
    val savedConfig: ExportConfig? = null,
    val history: List<ExportHistoryEntry> = emptyList(),
    /**
     * Set when preflight found warnings the user has not accepted yet. No export
     * work runs while this is non-null: the user either confirms (which records
     * the accepted fallbacks) or cancels.
     */
    val pendingConfirmation: ExportConfirmationRequest? = null,
    /**
     * The copyable failure report for the current failed export. It is cleared before
     * every attempt so an unreported failure cannot inherit another run's diagnosis.
     */
    val lastIncidentReport: String? = null
) : EditorDomainState {
    override val kind: EditorDomainState.Kind = EditorDomainState.Kind.EXPORT
}

/** Clear report state before preflight starts a new export attempt. */
internal fun EditorExportDomainState.prepareForExportAttempt(): EditorExportDomainState =
    copy(startTime = 0L, lastIncidentReport = null)

/** Attach a report only while the failed run it describes is still the current run. */
internal fun EditorExportDomainState.attachIncidentReport(
    runStartedAtMs: Long,
    report: String,
): EditorExportDomainState =
    if (state == ExportState.ERROR && startTime == runStartedAtMs) {
        copy(lastIncidentReport = report)
    } else {
        this
    }

data class StabilizationPreview(
    val clipId: String,
    val sourceName: String,
    val motionData: StabilizationEngine.MotionData,
    val config: StabilizationEngine.StabilizationConfig,
    val profile: StabilizationProfile = StabilizationProfile(),
)

data class EditorAiState(
    val requirementPrompt: AiRequirementPrompt? = null,
    val modelRequirement: AiToolRequirements.ToolRequirement? = null,
    val processingTool: String? = null,
    val processingProgress: Float = 0f,
    val stabilizationPreview: StabilizationPreview? = null,
    val stabilizationProfileImportPreview: StabilizationProfileValidation? = null,
    val suggestion: AiSuggestion? = null,
    val usageLedger: List<AiUsageLedger.Entry> = emptyList(),
    val cutAssistantReview: CutAssistantEngine.ReviewSet? = null,
    val autoEditProposal: AutoEditResult? = null,
    val isReframing: Boolean = false,
    val isAutoEditing: Boolean = false,
    val isSynthesizingTts: Boolean = false,
    val isTtsAvailable: Boolean = false,
    val isAnalyzingNoise: Boolean = false,
    val noiseAnalysisResult: String? = null,
    /** Whether the EU AI Act Article 50 pre-use disclosure has been shown this session. */
    val hasShownArticle50Disclosure: Boolean = false
) : EditorDomainState {
    override val kind: EditorDomainState.Kind = EditorDomainState.Kind.AI
}

data class PendingIngest(
    val workId: String,
    val displayName: String,
    val mediaType: String,
    val progress: Float = 0f
)

data class MetadataSidecarExportFile(
    val path: String,
    val fileName: String,
    val sizeBytes: Long,
    val format: MetadataSidecarFormat,
)

data class MetadataSidecarExportUiState(
    val isExporting: Boolean = false,
    val file: MetadataSidecarExportFile? = null,
    val message: String? = null,
    val errorMessage: String? = null,
)

data class EditorMediaState(
    val backupImportFeedback: BackupImportFeedback? = null,
    val timelineExchangeFeedback: TimelineExchangeFeedback? = null,
    val relinkReports: Map<String, MediaRelinkProbe.ClipRelinkReport> = emptyMap(),
    val diagnostics: Map<String, MediaDiagnostic> = emptyMap(),
    val healthReport: MediaHealthReport? = null,
    val metadataSidecarExport: MetadataSidecarExportUiState = MetadataSidecarExportUiState(),
    val pendingIngests: List<PendingIngest> = emptyList(),
    /**
     * The persisted local media-bin catalog, including assets no longer used by
     * the timeline so triage can distinguish unused from missing.
     */
    val mediaAssets: List<info.bigwizardmedia.wizardcut.engine.ProjectMediaAsset> = emptyList()
) : EditorDomainState {
    override val kind: EditorDomainState.Kind = EditorDomainState.Kind.MEDIA
}

data class EditorDomainStates(
    val panel: EditorPanelState,
    val caption: EditorCaptionState,
    val compound: EditorCompoundState,
    val export: EditorExportDomainState,
    val ai: EditorAiState,
    val media: EditorMediaState
) {
    fun asList(): List<EditorDomainState> = listOf(
        panel,
        caption,
        compound,
        export,
        ai,
        media
    )
}

val EditorState.domainStates: EditorDomainStates
    get() = EditorDomainStates(
        panel = panel,
        caption = caption,
        compound = compound,
        export = export,
        ai = ai,
        media = media
    )

inline fun EditorState.copyPanel(transform: (EditorPanelState) -> EditorPanelState): EditorState =
    copy(panel = transform(panel))

inline fun EditorState.copyAi(transform: (EditorAiState) -> EditorAiState): EditorState =
    copy(ai = transform(ai))

inline fun EditorState.copyExport(transform: (EditorExportDomainState) -> EditorExportDomainState): EditorState =
    copy(export = transform(export))

inline fun EditorState.copyMedia(transform: (EditorMediaState) -> EditorMediaState): EditorState =
    copy(media = transform(media))

inline fun EditorState.copyCompound(transform: (EditorCompoundState) -> EditorCompoundState): EditorState =
    copy(compound = transform(compound))

inline fun EditorState.copyCaption(transform: (EditorCaptionState) -> EditorCaptionState): EditorState =
    copy(caption = transform(caption))
