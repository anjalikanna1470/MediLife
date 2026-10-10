# MediLife updated source bundle

This bundle is based on the uploaded `app.zip`, `.zip` backend archive, and `settings.gradle.zip`.

## Changes made in this source
- Profile screen: added a visible Settings entry that navigates to the existing `settings` route.
- Records: the Download PDF button now opens Android's system Save dialog and writes the selected record details to a PDF.
- CareMate and Doctor Clinical AI: attachment submissions now call `DocumentAnalysisClient.analyze(...)` instead of returning the old hard-coded “analysis is not connected” text.
- `AppViewModel` is now an `AndroidViewModel` so the attachment analyzer can access Android's content resolver.
- Excluded `.env` so private API keys are not redistributed.

## Important physical-phone backend configuration
`DocumentAnalysisClient.kt` currently uses `http://10.0.2.2:8000`, which is only correct for an Android Emulator reaching a backend on the host computer. For a real phone:
1. Connect phone and PC to the same Wi-Fi.
2. Find the PC's local IPv4 address in Windows Network settings.
3. Change `BASE_URL` in `DocumentAnalysisClient.kt` to `http://<PC-LAN-IP>:8000`.
4. Start the backend listening on `0.0.0.0:8000` and allow Python through Windows Firewall on the private network.
5. Keep both devices on the same trusted network. Do not expose the development server publicly.

## Backend setup (using project-root `local.properties`)
- Install Python 3.10+.
- You may keep `GROQ_API_KEY=...` in the project-root `local.properties` alongside `sdk.dir`. The backend now reads this value as a local-development fallback, so the key does not need to be placed in `backend/.env`.
- Alternatively, set `GROQ_API_KEY` as an environment variable or in `backend/.env`; environment variables take precedence.
- Do not commit or share `local.properties`, `.env`, or any real API key. The key is used by the Python backend and is **not** embedded in the Android APK.
- Install requirements and start the API using the commands in a terminal:
  `py -m venv .venv`
  `.\.venv\Scripts\activate`
  `pip install -r requirements.txt`
  `uvicorn main:app --reload --host 0.0.0.0 --port 8000`

## Android Studio setup
- This bundle includes source/resources and the Gradle files found in the supplied archives, but those archives did not provide a complete self-contained Gradle wrapper/version catalog and Firebase `google-services.json` in a reliable way.
- Open the existing project in Android Studio, back it up first, and merge/replace the matching `app/src/main` source files plus `app/build.gradle.kts`.
- Keep your own `google-services.json` in `app/` (do not share it publicly).
- Sync Gradle, then Build > Make Project.
- I could not run an Android Gradle build or test the physical phone from this environment; therefore this is a source-level update, not a verified release build.

## Still not guaranteed by this source-level patch
- Medicine Taken/Snooze/Skip statuses are now saved locally in SharedPreferences across app restarts. Snooze is recorded as a status; scheduling an actual OS reminder notification still needs additional implementation and device testing.
- PDF export saves the record summary and metadata currently available in the app; it does not download an original uploaded report unless the app has that file URL/content attached to the record.
- Medical attachments are sent to the backend for analysis, but the phone's BASE_URL must be configured as described above. Backend must be running.
- Medical data is sensitive. Do not use this prototype for real patient data until authentication/authorization, storage, consent, and security are reviewed.


## Fix for AI attachment connection on a physical phone
The previous hardcoded `http://10.0.2.2:8000` works for the Android Emulator only. The updated app reads its backend URL from Settings > Backend Connection.

1. Connect the phone and Windows PC to the same Wi-Fi network.
2. On Windows, run the backend bound to all interfaces: `uvicorn main:app --reload --host 0.0.0.0 --port 8000`.
3. Find the PC's IPv4 address using Windows Settings > Network & Internet > Wi-Fi > Hardware properties (or `ipconfig` in Command Prompt). Do not use `127.0.0.1` or `10.0.2.2` on the physical phone.
4. In the app, open Settings using the new bottom-right gear button. In Backend Connection, enter `http://YOUR_PC_IPV4:8000` (example only: `http://192.168.1.5:8000`) and tap Save backend address.
5. If it still fails, allow Python/port 8000 through Windows Defender Firewall on Private networks. Test `http://YOUR_PC_IPV4:8000/health` in the phone browser first. Keep the backend process running while using AI attachments.

The bottom-right Settings floating button is available on patient and doctor primary pages, including when a doctor switches between Patient Mode and Doctor Mode. It is hidden on the Settings screen itself.

## Verification status
XML and source edits were reviewed, but an Android build could not be completed in this environment because the original uploaded wrapper JAR was missing from the archive; the wrapper JAR was restored in this ZIP. Run **File > Sync Project with Gradle Files** and **Build > Make Project** in Android Studio to confirm against your local Android SDK and dependency cache. Physical-device connectivity also depends on the PC's actual LAN IP, Wi-Fi and firewall settings.


## API key configuration change in this bundle
`backend/main.py` checks `GROQ_API_KEY` in the process environment first, then checks the project-root `local.properties` file. Keep the real key only on your own computer. If a key was pasted into a chat or committed to source control, revoke it and create a replacement.
