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

import com.google.adk.kt.types.MultiSpeakerVoiceConfig as KtMultiSpeakerVoiceConfig
import com.google.adk.kt.types.PrebuiltVoiceConfig as KtPrebuiltVoiceConfig
import com.google.adk.kt.types.ReplicatedVoiceConfig as KtReplicatedVoiceConfig
import com.google.adk.kt.types.SpeakerVoiceConfig as KtSpeakerVoiceConfig
import com.google.adk.kt.types.SpeechConfig as KtSpeechConfig
import com.google.adk.kt.types.VoiceConfig as KtVoiceConfig
import com.google.adk.kt.types.VoiceConsentSignature as KtVoiceConsentSignature
import com.google.genai.types.MultiSpeakerVoiceConfig as GenaiMultiSpeakerVoiceConfig
import com.google.genai.types.PrebuiltVoiceConfig as GenaiPrebuiltVoiceConfig
import com.google.genai.types.ReplicatedVoiceConfig as GenaiReplicatedVoiceConfig
import com.google.genai.types.SpeakerVoiceConfig as GenaiSpeakerVoiceConfig
import com.google.genai.types.SpeechConfig as GenaiSpeechConfig
import com.google.genai.types.VoiceConfig as GenaiVoiceConfig
import com.google.genai.types.VoiceConsentSignature as GenaiVoiceConsentSignature
import kotlin.jvm.optionals.getOrNull

/**
 * Converts a live voice setting, with its voices and speakers, between the ADK Kotlin
 * [KtSpeechConfig] and the genai [GenaiSpeechConfig] that the ADK Java `RunConfig` carries. Every
 * field exists on both sides, so nothing is lost in either direction.
 */
internal object SpeechConfigCodec {

  /** Returns the genai [GenaiSpeechConfig] view of the Kotlin [config]. */
  fun toJava(config: KtSpeechConfig): GenaiSpeechConfig =
    with(config) {
      val builder = GenaiSpeechConfig.builder()
      voiceConfig?.let { builder.voiceConfig(voiceToJava(it)) }
      languageCode?.let { builder.languageCode(it) }
      multiSpeakerVoiceConfig?.let { builder.multiSpeakerVoiceConfig(multiSpeakerToJava(it)) }
      builder.build()
    }

  /** Returns the Kotlin [KtSpeechConfig] view of the genai [config]. */
  fun fromJava(config: GenaiSpeechConfig): KtSpeechConfig =
    KtSpeechConfig(
      voiceConfig = config.voiceConfig().getOrNull()?.let { voiceFromJava(it) },
      languageCode = config.languageCode().getOrNull(),
      multiSpeakerVoiceConfig =
        config.multiSpeakerVoiceConfig().getOrNull()?.let { multi ->
          KtMultiSpeakerVoiceConfig(
            speakerVoiceConfigs =
              multi.speakerVoiceConfigs().getOrNull()?.map { speakerFromJava(it) }
          )
        },
    )

  private fun voiceToJava(voiceConfig: KtVoiceConfig): GenaiVoiceConfig =
    with(voiceConfig) {
      val builder = GenaiVoiceConfig.builder()
      prebuiltVoiceConfig?.let { builder.prebuiltVoiceConfig(prebuiltToJava(it)) }
      replicatedVoiceConfig?.let { builder.replicatedVoiceConfig(replicatedToJava(it)) }
      voice?.let { builder.voice(it) }
      builder.build()
    }

  private fun replicatedToJava(replicated: KtReplicatedVoiceConfig): GenaiReplicatedVoiceConfig =
    with(replicated) {
      val builder = GenaiReplicatedVoiceConfig.builder()
      mimeType?.let { builder.mimeType(it) }
      voiceSampleAudio?.let { builder.voiceSampleAudio(it) }
      consentAudio?.let { builder.consentAudio(it) }
      voiceConsentSignature?.let { builder.voiceConsentSignature(consentToJava(it)) }
      builder.build()
    }

  private fun multiSpeakerToJava(multi: KtMultiSpeakerVoiceConfig): GenaiMultiSpeakerVoiceConfig {
    val builder = GenaiMultiSpeakerVoiceConfig.builder()
    multi.speakerVoiceConfigs?.let { configs ->
      builder.speakerVoiceConfigs(configs.map { speakerToJava(it) })
    }
    return builder.build()
  }

  private fun prebuiltToJava(prebuilt: KtPrebuiltVoiceConfig): GenaiPrebuiltVoiceConfig {
    val builder = GenaiPrebuiltVoiceConfig.builder()
    prebuilt.voiceName?.let { builder.voiceName(it) }
    return builder.build()
  }

  private fun consentToJava(consent: KtVoiceConsentSignature): GenaiVoiceConsentSignature {
    val builder = GenaiVoiceConsentSignature.builder()
    consent.signature?.let { builder.signature(it) }
    return builder.build()
  }

  private fun speakerToJava(speakerConfig: KtSpeakerVoiceConfig): GenaiSpeakerVoiceConfig =
    with(speakerConfig) {
      val builder = GenaiSpeakerVoiceConfig.builder()
      speaker?.let { builder.speaker(it) }
      voiceConfig?.let { builder.voiceConfig(voiceToJava(it)) }
      builder.build()
    }

  private fun voiceFromJava(voice: GenaiVoiceConfig): KtVoiceConfig =
    KtVoiceConfig(
      prebuiltVoiceConfig =
        voice.prebuiltVoiceConfig().getOrNull()?.let {
          KtPrebuiltVoiceConfig(voiceName = it.voiceName().getOrNull())
        },
      replicatedVoiceConfig =
        voice.replicatedVoiceConfig().getOrNull()?.let { replicatedFromJava(it) },
      voice = voice.voice().getOrNull(),
    )

  private fun replicatedFromJava(replicated: GenaiReplicatedVoiceConfig): KtReplicatedVoiceConfig =
    KtReplicatedVoiceConfig(
      mimeType = replicated.mimeType().getOrNull(),
      voiceSampleAudio = replicated.voiceSampleAudio().getOrNull(),
      consentAudio = replicated.consentAudio().getOrNull(),
      voiceConsentSignature =
        replicated.voiceConsentSignature().getOrNull()?.let {
          KtVoiceConsentSignature(signature = it.signature().getOrNull())
        },
    )

  private fun speakerFromJava(speaker: GenaiSpeakerVoiceConfig): KtSpeakerVoiceConfig =
    KtSpeakerVoiceConfig(
      speaker = speaker.speaker().getOrNull(),
      voiceConfig = speaker.voiceConfig().getOrNull()?.let { voiceFromJava(it) },
    )
}
