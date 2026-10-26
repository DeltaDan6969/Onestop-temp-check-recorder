# TempTracker - Daily Food Temperature Check Sheet & Compliance

A responsive web application and Android app to track daily food core and T1 holding temperatures for food safety and HACCP compliance.

## ✨ Features

- **Preloaded 35+ Products & Fry Food**: Exact items from physical temperature check sheets.
- **Whole-Number Temperature Recording**: Formatted and recorded as whole numbers (e.g., 205°C, 88°C).
- **⚡ 1-Tap Autofill**:
  - Core temperatures randomized between **200°C and 210°C** (whole numbers).
  - T1 holding temperatures randomized between **81°C and 100°C** (whole numbers).
  - Individual product randomize dice buttons.
- **✏️ Edit & Add Products**: Rename, change category, add new products, or delete items directly from the sheet.
- **📅 Historical Date Search**: Search back any day to view or edit previous temperature logs.
- **📥 PDF & CSV Reports**:
  - Export printable A4 audit sheet with sign-off line.
  - Export CSV spreadsheets for Excel and Google Sheets.
- **💾 Auto-Sync**: Persistent storage in browser LocalStorage.

---

## 🐙 How to Fix the GitHub Pages Error & Host

The error `Error: No such file or directory @ dir_chdir0 - /github/workspace/docs` happened because GitHub Pages was configured to look for a `/docs` folder.

We have now added:
1. `/docs/index.html` (for `/docs` configuration)
2. `/index.html` (for `/ (root)` configuration)
3. `.nojekyll` (disables Jekyll static site processing errors)

### Steps to Deploy on GitHub Pages:

1. **Push the updated code to GitHub**:
   ```bash
   git add .
   git commit -m "Add docs folder and nojekyll for GitHub Pages"
   git push origin main
   ```

2. **Check your GitHub Pages Settings**:
   - Go to your repository on **GitHub.com**.
   - Click on the **Settings** tab.
   - In the left sidebar, click **Pages** (under "Code and automation").
   - Under **Build and deployment > Source**: choose **Deploy from a branch**.
   - Under **Branch**:
     - Choose `main` (or `master`).
     - In the folder dropdown, you can select **`/docs`** OR **`/ (root)`** — **both now work!**
   - Click **Save**.

3. **Verify the Build**:
   - Go to the **Actions** tab in your repository.
   - You will see the **pages-build-deployment** workflow run and turn **green (Success)**.
   - Your live site URL will be:
     `https://<your-username>.github.io/<repo-name>/`
