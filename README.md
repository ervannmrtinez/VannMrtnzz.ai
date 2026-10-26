# VannMrtnz.AI — Android + Flask backend

Starter project for an Android AI chat app. The Android client is Kotlin + Jetpack Compose.
The backend is Python Flask and calls the OpenAI Responses API. The API key stays on the server.

## Project layout
- `android/` — Android Studio project source
- `backend/app.py` — Flask API
- `backend/requirements.txt` — Python dependencies
- `backend/render.yaml` — example Render deployment blueprint

## 1) Configure the backend
Create an API key in the OpenAI Platform, then set it as an environment variable named `OPENAI_API_KEY`.
Do not put the key in Android source code or share it in public repositories.

In `backend/`, install dependencies and run locally:

```bash
python -m venv .venv
# Windows: .venv\Scripts\activate
# macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
# Set OPENAI_API_KEY in your environment first
python app.py
```

The local API will listen on `http://127.0.0.1:8000`.
Check health: `GET /health`.

## 2) Deploy backend
You can deploy the `backend/` folder to a Python host such as Render.
Set these environment variables in the host dashboard:
- `OPENAI_API_KEY` = your private API key
- `OPENAI_MODEL` = `gpt-6-astra` (change to a model available to your API project if needed)
- `APP_API_TOKEN` = a long random string (optional but recommended)

After deployment, copy the public HTTPS URL, e.g. `https://your-service.example.com`.

## 3) Connect Android app to backend
Open `android/` in Android Studio.
Edit `android/app/build.gradle.kts` and replace `https://YOUR-BACKEND-URL.example.com/` in `buildConfigField("String", "API_BASE_URL", ...)` with your deployed HTTPS base URL. Keep the trailing slash.

If `APP_API_TOKEN` is configured on the backend, set the same token in the Android app's `API_TOKEN` build config field. For production, consider a proper user login/token system rather than shipping one shared token inside the APK.

## 4) Build APK
In Android Studio:
1. Open the `android/` folder.
2. Wait for Gradle sync.
3. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Android Studio will show the APK output path.

You need Android Studio (or a compatible Gradle build environment) to compile the APK. This ZIP contains source code, not a precompiled APK.

## Features included
- Dark, neon-accented chat interface
- Send/receive messages from the AI backend
- Conversation context included in each request
- Loading indicator and visible error messages
- Clear-chat button
- Backend health endpoint and request validation

## Notes
- API usage may incur charges under your API account.
- Keep the backend key private; never commit `.env` or secrets.
- Use HTTPS for deployed backend URLs.
