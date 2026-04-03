# Release Checklist

Steps for publishing a new Kodemirror release to Maven Central.

## Pre-Release

### 1. All CI jobs are green

Check that **every** workflow on the `main` branch is passing:

```bash
gh run list --repo Monkopedia/kodemirror --branch main --limit 10
```

Required green:
- **CI** (JVM + wasmJs + Android) — runs on push
- **CI (Apple)** (macOS + iOS) — trigger manually if not recently run:
  ```bash
  gh workflow run ci-apple.yml --repo Monkopedia/kodemirror --ref main
  ```
- **Docs** (showcase build + API docs + site) — runs on push

Do not proceed until all three are green on the latest commit.

### 2. Version bump

Two files contain the version:

```bash
grep -r "SNAPSHOT" --include="*.gradle.kts" .
```

- `convention-plugins/src/main/kotlin/kodemirror.library.gradle.kts` — `version = "X.Y.Z-SNAPSHOT"`
- `kodemirror-bom/build.gradle.kts` — `version = "X.Y.Z-SNAPSHOT"`

Remove `-SNAPSHOT` from both. Also verify docs reference the release version:

```bash
grep -r "SNAPSHOT" docs-site/docs/ README.md CHANGELOG.md
```

### 3. Changelog

Ensure `CHANGELOG.md` has a dated `[X.Y.Z] - YYYY-MM-DD` section (not `[Unreleased]`) with all notable changes.

### 4. Commit the version bump

```bash
git add convention-plugins/src/main/kotlin/kodemirror.library.gradle.kts kodemirror-bom/build.gradle.kts
git commit -m "Bump version to X.Y.Z for release"
git push
```

Wait for CI + Docs to pass on this commit before proceeding.

## Release

### 5. Create GitHub Release with tag

```bash
gh release create vX.Y.Z \
  --repo Monkopedia/kodemirror \
  --title "vX.Y.Z" \
  --notes "$(cat <<'EOF'
## Highlights

- (copy key points from CHANGELOG)

See [CHANGELOG.md](CHANGELOG.md) for the full list of changes.
EOF
)" \
  --target main
```

This creates the git tag and the GitHub release in one step.

### 6. Deploy to Maven Central

Trigger the deploy workflow (manual dispatch):

```bash
gh workflow run deploy.yml --repo Monkopedia/kodemirror --ref vX.Y.Z
```

This runs CI first, then publishes all platform artifacts to Sonatype and closes the staging repository.

Monitor progress:
```bash
gh run list --repo Monkopedia/kodemirror --workflow deploy.yml --limit 3
```

### 7. Verify on Maven Central

After the deploy workflow succeeds, artifacts appear on Sonatype within minutes but may take up to 2 hours to sync to Maven Central:

- https://central.sonatype.com/namespace/com.monkopedia.kodemirror

Verify at least one artifact (e.g., `view`) shows the new version.

## Post-Release

### 8. Bump to next SNAPSHOT

```bash
# In both files, change version to next development version:
# convention-plugins/src/main/kotlin/kodemirror.library.gradle.kts
# kodemirror-bom/build.gradle.kts
# X.Y.Z → X.Y.(Z+1)-SNAPSHOT

git add convention-plugins/src/main/kotlin/kodemirror.library.gradle.kts kodemirror-bom/build.gradle.kts
git commit -m "Bump version to X.Y.(Z+1)-SNAPSHOT for development"
git push
```

### 9. Announce (optional)

- Reddit: r/Kotlin
