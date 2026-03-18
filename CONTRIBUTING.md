# Contributing to CastFlow

Thanks for your interest in contributing! Below are quick guidelines to make collaboration smooth.

1. Fork the repo and open a feature branch with a descriptive name: `feat/<short-desc>` or `fix/<short-desc>`.
2. Open an issue to discuss large changes before implementing them.
3. Keep commits small and semantic. Use prefixes: `feat:`, `fix:`, `docs:`, `chore:`, `refactor:`.
4. Run the local build before opening a PR:

```bash
./gradlew :app:assembleDebug
```

5. Include a short description of the change and rationale in the PR. Link the issue if one exists.
6. Respect code style: Kotlin + Jetpack Compose idioms; keep UI strings in `strings.xml` for i18n.
7. For breaking changes or API modifications, document them in the PR and update README.

If you need help, open an issue and we can discuss the approach.
