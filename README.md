# ASTUBE TWA — Android App

Full native Android wrapper for ASTUBE PWA.
Creates `Downloads/ASTUBE/` folder, handles storage permissions.
Built automatically via GitHub Actions — no PC needed.

---

## Step 1 — Create a new GitHub repository

1. Go to github.com → New repository
2. Name it: `ASTUBE-Android`
3. Set to **Public** (required for free GitHub Actions minutes)
4. Do NOT initialize with README
5. Click **Create repository**

---

## Step 2 — Upload this project to GitHub

In Termux:
```bash
pkg install git -y

cd ~
# Copy the ASTUBE-TWA folder to somewhere accessible
# Then:
git init
git add .
git commit -m "Initial ASTUBE TWA"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/ASTUBE-Android.git
git push -u origin main
```
Replace `YOUR_USERNAME` with your actual GitHub username.

---

## Step 3 — Generate keystore in Termux

```bash
pkg install openjdk-17 -y
bash setup-keystore.sh
```

This will:
- Generate `astube-release.jks`
- Show you the SHA256 fingerprint (needed for Step 5)
- Show you the base64 string (needed for Step 4)

**SAVE the keystore file somewhere safe. You need it forever.**

---

## Step 4 — Add secrets to GitHub

Go to your GitHub repo → Settings → Secrets and variables → Actions → New repository secret

Add these 4 secrets:

| Secret Name | Value |
|-------------|-------|
| `KEYSTORE_BASE64` | (base64 output from setup script) |
| `KEYSTORE_PASSWORD` | (password you chose) |
| `KEY_ALIAS` | `astube` |
| `KEY_PASSWORD` | (key password you chose) |

---

## Step 5 — Update assetlinks.json with your SHA256

1. Open `assetlinks.json` in this project
2. Replace `REPLACE_WITH_YOUR_SHA256_FINGERPRINT` with the SHA256 from Step 3
   - Format: `AA:BB:CC:DD:...` (colon-separated hex)
3. Commit and push

---

## Step 6 — Host assetlinks.json on GitHub Pages

This is CRITICAL — without it the TWA shows a browser bar.

1. Go to your **ASTUBE1** GitHub Pages repository (asdeveloperszone/ASTUBE1)
2. Create folder `.well-known`
3. Inside it, create file `assetlinks.json`
4. Paste the content from `assetlinks.json` in this project (with your real SHA256)
5. Commit and push

The file must be accessible at:
```
https://asdeveloperszone.github.io/.well-known/assetlinks.json
```

---

## Step 7 — Trigger the build

Push any change to the `main` branch OR:
1. Go to your ASTUBE-Android repo on GitHub
2. Click **Actions** tab
3. Click **Build ASTUBE APK**
4. Click **Run workflow** → **Run workflow**

Wait ~5 minutes for the build.

---

## Step 8 — Download and install APK

After build completes:
1. Go to **Actions** → click the completed workflow run
2. Scroll down to **Artifacts**
3. Download **ASTUBE-APK**
4. Extract the zip → get `ASTUBE-v1.0.apk`
5. Send to your phone (via Telegram, WhatsApp, email, etc.)
6. On phone: enable **Install from unknown sources**
7. Install APK
8. Done! ✅

OR — the build also creates a **GitHub Release** automatically.
Go to your repo → Releases → download APK directly.

---

## What the app does

- ✅ Opens ASTUBE fullscreen (no browser bar)
- ✅ Creates `Downloads/ASTUBE/` folder on first launch
- ✅ Requests storage permission automatically
- ✅ Handles Android 10, 11, 12, 13, 14
- ✅ Library page can read from Downloads/ASTUBE/
- ✅ Works offline (PWA cached)

---

## Updating the app

For PWA changes (HTML/CSS/JS) — just push to ASTUBE1 repo as normal.
The TWA automatically loads the latest version (it's just a wrapper).

For APK changes (permissions, package name, etc.) — push to ASTUBE-Android repo.
GitHub Actions rebuilds automatically. Bump `versionCode` in `app/build.gradle`.

---

## Troubleshooting

**Build fails — "keystore not found"**
→ Check KEYSTORE_BASE64 secret is correct (no extra spaces)

**App opens with browser bar**
→ assetlinks.json is missing or SHA256 is wrong
→ Check: `https://asdeveloperszone.github.io/.well-known/assetlinks.json`
→ Verify SHA256 matches exactly

**Storage permission not working**
→ Go to phone Settings → Apps → ASTUBE → Permissions → Storage → Allow

**Downloads/ASTUBE folder not created**
→ Grant storage permission first, then restart app
