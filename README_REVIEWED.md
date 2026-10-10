# MediLife — Reviewed Project Bundle

This is a cleaned copy of the project supplied in `fgsgsf.zip`, with generated build/cache folders removed and machine-specific settings removed. It preserves the provided app screens and backend source rather than replacing them with invented versions.

## Changes made
- Removed generated `build/`, `.gradle/`, and IDE cache artifacts from the deliverable.
- Removed `org.gradle.java.home=C:/Program Files/Android/Android Studio/jbr` from `gradle.properties`; that hard-coded path only works on one Windows installation. Android Studio/Gradle can use the configured JDK.
- Excluded the backend `.env` file so a real API key/private key is not redistributed. A placeholder `.env.example` is included.
- Kept the uploaded `google-services.json` because the Android app uses Firebase. Treat this file as project configuration; review Firebase console restrictions and security rules.

## What the uploaded project indicates
- `MainActivity.kt` calls `enableEdgeToEdge()` and then `MediLifeApp()`.
- `MediLifeApp()` builds the theme, `Scaffold`, and navigation host.
- `Navigation.kt` starts at route `auth`.
- The supplied Logcat excerpt shows Firebase initialization and slow initial frames, but no `FATAL EXCEPTION`. Those lines alone do not prove why the screen is blank.
- The supplied archive contains prebuilt build output, but a previous successful build artifact is not proof that the current clean source builds on another machine.

## Open in Android Studio
1. Extract this ZIP to a new folder, e.g. `D:\MediLife_Reviewed`. Do not overwrite your existing folder yet.
2. Open the extracted folder (the folder containing `settings.gradle.kts`) in Android Studio.
3. Wait for Gradle sync to finish.
4. If Android Studio asks for a JDK, select the embedded JDK bundled with Android Studio (typically JBR 17 or the version required by your installed Gradle/AGP).
5. Use **Build → Make Project**. Review the first actual build error, if any.
6. Run the `app` configuration on your emulator.

## Backend (Windows)
1. Open `backend` in File Explorer and copy `.env.example` to `.env`.
2. Enter your own credentials locally in `.env`. Never send API keys or private keys in chat or commit `.env`.
3. In a terminal opened in `backend`:
   - `py -m venv .venv`
   - `.\.venv\Scripts\activate`
   - `pip install -r requirements.txt`
   - `uvicorn main:app --reload --host 0.0.0.0 --port 8000`
4. Test `http://127.0.0.1:8000/health` and `http://127.0.0.1:8000/docs`.
5. Android emulator reaches the host machine via `http://10.0.2.2:8000`. Ensure the Android app's API client uses this base URL for emulator builds.

## Honest verification status
This environment does not have your Android Studio emulator/Firebase project, so it cannot reproduce the blank screen or certify an Android build. The archive has been cleaned and the machine-specific Gradle JDK path removed, but **the blank-screen root cause is not yet confirmed**. Do not treat this bundle as production-ready for real patient data. The backend still needs server-side Firebase token verification, authorization, secure data storage, consent and audit controls before handling real patient information.
