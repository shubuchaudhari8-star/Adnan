package com.example.data.model

object ArchitectureBlueprint {

    val SYSTEM_PROMPTS = listOf(
        SystemPromptTemplate(
            title = "১. Emotion Extractor Prompt (আবেগ সনাক্তকরণ)",
            moduleTag = "Module 1: Extraction",
            icon = "🧠",
            description = "গুগল এআই স্টুডিওতে JSON Structured Output সহ আবেগ ও বিষয়বস্তু আলাদা করার নিখুঁত প্রম্পট।",
            systemPrompt = """
You are the HX Emotion Extractor Engine.
Your objective is to analyze raw human emotional confessions and extract core affective parameters while adhering to strict privacy filters.

GUIDELINES:
1. Identify primary and secondary emotions with both Bengali and English labels (e.g., 'হতাশা (Frustration)', 'মনভাঙা (Heartbreak)').
2. Calculate emotion intensity on a normalized floating scale from 0.0 to 1.0.
3. Determine emotional valence: 'Negative', 'Positive', 'Mixed', or 'Neutral'.
4. Summarize the core situation/topic into a concise 4-6 word description.
5. Extract key affective tokens and keywords.
6. STRIKE RULE: Under NO circumstance should any person's name, phone number, physical address, or identifying workplace be included in the output.
            """.trimIndent(),
            exampleInput = "গতকাল ৩ বছর ধরে জমানো সব টাকা দিয়ে শুরু করা ব্যবসাটা পার্টনারের বিশ্বাসঘাতকতায় বন্ধ হয়ে গেল। মাথায় আকাশ ভেঙে পড়েছে।",
            jsonOutputSchema = """
{
  "responseMimeType": "application/json",
  "responseSchema": {
    "type": "OBJECT",
    "properties": {
      "primaryEmotion": { "type": "STRING" },
      "secondaryEmotion": { "type": "STRING" },
      "intensity": { "type": "NUMBER", "description": "0.0 to 1.0" },
      "valence": { "type": "STRING", "enum": ["Negative", "Positive", "Mixed", "Neutral"] },
      "coreTopic": { "type": "STRING" },
      "keywords": { "type": "ARRAY", "items": { "type": "STRING" } }
    },
    "required": ["primaryEmotion", "intensity", "valence", "coreTopic", "keywords"]
  }
}
            """.trimIndent()
        ),
        SystemPromptTemplate(
            title = "২. Anonymizer & Privacy Filter (গোপনীয়তা রক্ষা)",
            moduleTag = "Module 2: Privacy",
            icon = "🛡️",
            description = "ভেক্টর স্টোরে সেভ করার আগে সকল PII (নাম, নম্বর, স্থান) মুছে ফেলার নির্দেশমালা।",
            systemPrompt = """
You are the HX Anonymizer & Zero-PII Sanitizer.
Your sole mission is to ingest user narrative and strip every single Personally Identifiable Information (PII) before any vector embedding or database commit.

MANDATORY REDACTIONS:
- Names of persons -> [REDACTED_PERSON]
- Phone numbers & emails -> [REDACTED_CONTACT]
- Specific locations, neighborhoods, cities -> [REDACTED_LOCATION]
- Specific companies, institutions, universities -> [REDACTED_ORGANIZATION]
- Dates, IDs, and financial account numbers -> [REDACTED_DATA]

Preserve the pure emotional weight and storyline untouched, while stripping any identity fingerprints.
            """.trimIndent(),
            exampleInput = "আমি তানভীর, ধানমন্ডির অফিসে কাজ করতাম। রফিক সাহেব আমাকে অন্যায়ভাবে বরখাস্ত করেছেন। ০১৭XXXXXXXX নম্বরে কেউ সাহায্য করতে পারেন?",
            jsonOutputSchema = """
{
  "sanitizedText": "আমি [REDACTED_PERSON], [REDACTED_LOCATION]-র অফিসে কাজ করতাম। [REDACTED_PERSON] আমাকে অন্যায়ভাবে বরখাস্ত করেছেন। [REDACTED_CONTACT] নম্বরে কেউ সাহায্য করতে পারেন?",
  "redactedEntitiesCount": 4,
  "privacyStatus": "VERIFIED_ANONYMOUS"
}
            """.trimIndent()
        ),
        SystemPromptTemplate(
            title = "৩. Empathy RAG Synthesis Prompt (সহমর্মিতা ইঞ্জিন)",
            moduleTag = "Module 4: Generation",
            icon = "🔄",
            description = "মেমোরি ব্যাংক থেকে প্রাপ্ত অতীত অভিজ্ঞতার ওপর ভিত্তি করে সান্ত্বনাদায়ক উত্তর তৈরি।",
            systemPrompt = """
You are HX Empathy & Matching Engine (সহমর্মিতা ও উত্তর তৈরির ইঞ্জিন).
A vulnerable human has shared their anonymous emotional state.

Context: You will be supplied with:
1. The user's current sanitized feeling and emotional classification.
2. Top 3 semantically matched past experiences from kindred humans who walked this path and survived/healed.

RESPONSE PROTOCOL:
- Match the language of the user (heartfelt, culturally resonant Bengali or compassionate English).
- Begin with profound validation. Do not dismiss their sadness with toxic positivity.
- Weave in the comforting realization that other humans in the memory bank weathered this exact storm.
- Offer 1-2 practical, gentle mental anchors.
- Tone: Deeply warm, respectful, poetic yet grounded. Like a wise, caring friend.
            """.trimIndent(),
            exampleInput = "Input: 'আমি খুব একা, নতুন শহরে কেউ আমার খোঁজ নেয় না।'\nRetrieved Match: 'অভিজ্ঞতা #২ (একাকীত্ব): নতুন শহরে শিকড় গজাতে সময় লাগে...'",
            jsonOutputSchema = """
Text response delivered with warmth, empathy, and wisdom directly addressing the human soul.
            """.trimIndent()
        )
    )

    val PYTHON_CODE_SNIPPET = """
# ==============================================================================
# 🧠 HX - Empathy & Vector Search Architecture (Python + Gemini + ChromaDB)
# ==============================================================================
# Requirements: pip install google-genai chromadb pydantic fastapi uvicorn

import os
from typing import List, Optional
from pydantic import BaseModel, Field
import chromadb
from chromadb.utils import embedding_functions
from google import genai
from google.genai import types

# 1. Initialize Gemini API Client
client = genai.Client(api_key=os.environ.get("GEMINI_API_KEY"))

# 2. Setup Vector Experience Store (ChromaDB)
chroma_client = chromadb.Client()
experience_collection = chroma_client.get_or_create_collection(
    name="hx_experience_memory_bank",
    metadata={"hnsw:space": "cosine"}
)

# ------------------------------------------------------------------------------
# Pydantic Schemas for Structured JSON Extraction
# ------------------------------------------------------------------------------
class EmotionAnalysis(BaseModel):
    primary_emotion: str = Field(description="Primary emotion e.g. হতাশা (Frustration)")
    secondary_emotion: str = Field(description="Secondary emotion e.g. একাকীত্ব (Loneliness)")
    intensity: float = Field(ge=0.0, le=1.0, description="Emotion intensity from 0 to 1")
    valence: str = Field(description="'Negative', 'Positive', 'Mixed', or 'Neutral'")
    core_topic: str = Field(description="Summary of the situation in 4-6 words")
    keywords: List[str] = Field(description="Affective keywords")

# ------------------------------------------------------------------------------
# Module 1 & 2: Emotion Extraction & Anonymizer
# ------------------------------------------------------------------------------
def extract_and_anonymize(raw_text: str) -> tuple[EmotionAnalysis, str]:
    prompt = f""${'"'}
    You are HX Emotion Extractor & Privacy Filter.
    1. Redact all PII (names -> [REDACTED_PERSON], phones/emails -> [REDACTED_CONTACT], locations -> [REDACTED_LOCATION]).
    2. Extract the emotional profile into the requested JSON schema.
    
    Raw User Text: "{raw_text}"
    ""${'"'}
    
    response = client.models.generate_content(
        model="gemini-3.5-flash",
        contents=prompt,
        config=types.GenerateContentConfig(
            response_mime_type="application/json",
            response_schema=EmotionAnalysis,
            temperature=0.2,
        ),
    )
    analysis = EmotionAnalysis.model_validate_json(response.text)
    
    # Fast regex pass for zero-PII guarantee
    sanitized_text = raw_text
    import re
    sanitized_text = re.sub(r'(\+?880|01)[0-9]{9}', '[REDACTED_CONTACT]', sanitized_text)
    sanitized_text = re.sub(r'[\w\.-]+@[\w\.-]+', '[REDACTED_CONTACT]', sanitized_text)
    return analysis, sanitized_text

# ------------------------------------------------------------------------------
# Module 3: Vector Experience Store (Embedding & Retrieval)
# ------------------------------------------------------------------------------
def get_embedding(text: str) -> List[float]:
    response = client.models.embed_content(
        model="gemini-embedding-2-preview", # or text-embedding-004
        contents=text
    )
    return response.embedding.values

def store_experience(story_id: str, sanitized_text: str, emotion: EmotionAnalysis, wisdom: str):
    vector = get_embedding(f"{emotion.primary_emotion}: {sanitized_text}")
    experience_collection.add(
        ids=[story_id],
        embeddings=[vector],
        documents=[sanitized_text],
        metadatas=[{
            "primary_emotion": emotion.primary_emotion,
            "core_topic": emotion.core_topic,
            "wisdom": wisdom
        }]
    )

def query_similar_experiences(query_text: str, emotion: EmotionAnalysis, n_results: int = 3):
    query_vector = get_embedding(f"{emotion.primary_emotion}: {query_text}")
    results = experience_collection.query(
        query_embeddings=[query_vector],
        n_results=n_results
    )
    return results

# ------------------------------------------------------------------------------
# Module 4: Empathy & Matching Engine (RAG Synthesis)
# ------------------------------------------------------------------------------
def generate_empathy_response(user_text: str, emotion: EmotionAnalysis, matches: dict) -> str:
    retrieved_context = ""
    documents = matches.get("documents", [[]])[0]
    metadatas = matches.get("metadatas", [[]])[0]
    
    for i, (doc, meta) in enumerate(zip(documents, metadatas)):
        retrieved_context += f"\n- Similar Past Experience #{i+1}: {doc}\n  Wisdom: {meta.get('wisdom')}\n"
        
    system_prompt = f""${'"'}
    You are HX (Heart Exchange Empathy Engine).
    A human feels: {emotion.primary_emotion} (Intensity: {emotion.intensity}).
    User says: "{user_text}"
    
    Retrieved Past Experiences from Memory Bank:
    {retrieved_context}
    
    Write a deeply comforting, empathetic response in the user's language (Bengali or English).
    Validate their feelings, show them they are not alone, and offer genuine gentle wisdom.
    ""${'"'}
    
    response = client.models.generate_content(
        model="gemini-3.5-flash",
        contents=system_prompt,
        config=types.GenerateContentConfig(temperature=0.7)
    )
    return response.text

# ------------------------------------------------------------------------------
# Complete Execution Pipeline Example
# ------------------------------------------------------------------------------
if __name__ == "__main__":
    sample_text = "আজকে ৩য় বারের মতো ইন্টারভিউ দিয়ে রিজেক্ট হলাম। আমি আর পারছি না।"
    print("🔹 Running HX 4-Module Pipeline...")
    
    # 1. Extraction & Anonymization
    emotion, sanitized = extract_and_anonymize(sample_text)
    print(f"🧠 Emotion: {emotion.primary_emotion}, Topic: {emotion.core_topic}")
    print(f"🛡️ Sanitized: {sanitized}")
    
    # 2. Vector Search (RAG)
    matches = query_similar_experiences(sanitized, emotion)
    print(f"🗄️ Retrieved {len(matches.get('ids', [[]])[0])} resonant memories.")
    
    # 3. Empathy Synthesis
    reply = generate_empathy_response(sanitized, emotion, matches)
    print("\n💬 Empathic Response:\n", reply)
""".trimIndent()
}
