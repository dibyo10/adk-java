/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.adk.tokt.codecs

import com.google.adk.kt.types.AudioTranscriptionConfig as KtAudioTranscriptionConfig
import com.google.adk.kt.types.AudioTranscriptionConfigMode as KtAudioTranscriptionConfigMode
import com.google.genai.types.AudioTranscriptionConfig as GenaiAudioTranscriptionConfig
import com.google.genai.types.AudioTranscriptionConfigMode as GenaiAudioTranscriptionConfigMode
import kotlin.jvm.optionals.getOrNull

/**
 * Converts a live transcription setting between the ADK Kotlin [KtAudioTranscriptionConfig] and the
 * genai [GenaiAudioTranscriptionConfig] that the ADK Java `RunConfig` carries. The genai-only
 * fields, and a `mode` Kotlin has no constant for, cannot cross, so [unsupportedFields] names the
 * ones a Java caller set.
 */
internal object AudioTranscriptionConfigCodec {

  /** Returns the genai [GenaiAudioTranscriptionConfig] view of the Kotlin [config]. */
  fun toJava(config: KtAudioTranscriptionConfig): GenaiAudioTranscriptionConfig =
    with(config) {
      val builder = GenaiAudioTranscriptionConfig.builder()
      languageCodes?.let { builder.languageCodes(it) }
      customVocabulary?.let { builder.customVocabulary(it) }
      diarization?.let { builder.diarization(it) }
      wordTimestamp?.let { builder.wordTimestamp(it) }
      mode?.let { builder.mode(GenaiAudioTranscriptionConfigMode(it.name)) }
      builder.build()
    }

  /** Returns the Kotlin [KtAudioTranscriptionConfig] view of the genai [config]. */
  fun fromJava(config: GenaiAudioTranscriptionConfig): KtAudioTranscriptionConfig =
    KtAudioTranscriptionConfig(
      languageCodes = config.languageCodes().getOrNull(),
      customVocabulary = config.customVocabulary().getOrNull(),
      diarization = config.diarization().getOrNull(),
      wordTimestamp = config.wordTimestamp().getOrNull(),
      mode = config.mode().getOrNull()?.let { modeFromJava(it) },
    )

  /** Returns the fields set on [config] that [fromJava] cannot carry. */
  fun unsupportedFields(config: GenaiAudioTranscriptionConfig): List<String> = buildList {
    if (config.languageAuto().isPresent) add("languageAuto")
    if (config.languageHints().isPresent) add("languageHints")
    if (config.adaptationPhrases().isPresent) add("adaptationPhrases")
    val mode = config.mode().getOrNull()
    if (mode != null && modeFromJava(mode) == null) add("mode")
  }

  private fun modeFromJava(
    mode: GenaiAudioTranscriptionConfigMode
  ): KtAudioTranscriptionConfigMode? =
    enumByNameOrNull<KtAudioTranscriptionConfigMode>(mode.knownEnum().name)
}
