# 🤖 AI Chatbot Android Application

A modern, responsive AI Chatbot application built for Android using Kotlin and Material Design.

## ✨ Features
- 💬 **Interactive AI Chat**: Real-time AI response handling with smooth message lists.
- 🎙️ **Voice Search Support**: Built-in Speech-to-Text integration for voice message input.
- 🎨 **Modern UI Layout**: Custom rounded chat bubbles and background styling for user and AI messages.
- ⚡ **Asynchronous Coroutines**: Smooth UI interactions powered by Kotlin Coroutines and Lifecycle scopes.
- 🛡️ **Robust Fallback Engine**: Seamless AI response generation.

## 🛠️ Tech Stack & Libraries
- **Language**: Kotlin
- **UI Architecture**: XML Layouts, ConstraintLayout, RecyclerView, Material Design Components
- **Concurrency**: Kotlin Coroutines (`lifecycleScope`, `Dispatchers.IO`)
- **Networking & API**: Google Generative AI SDK, HttpURLConnection, Gson
- **Min SDK**: 24 (Android 7.0) | **Target SDK**: 37

## 🚀 How to Run the App
1. Clone or download this repository.
2. Open the project in **Android Studio**.
3. Let Gradle Sync complete.
4. (Optional) In `MainActivity.kt`, you can add your own Gemini API key:
   ```kotlin
   private val apiKey = "YOUR_GEMINI_API_KEY"
   ```
5. Click **Run (▶)** on an Android Emulator or connected Physical Device.

---
Developed as an Android Development Project.
