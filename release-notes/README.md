# Play Store release notes

One file per release, named `v<versionName>.txt`, holding the "What's new" text pasted into
Play Console when publishing that version.

## Format

Each language is wrapped in its Play Console language tag:

```
<en-US>
v1.5.0

• NEW: ...
• FIX: ...

</en-US>
<zh-TW>
v1.5.0

• 新增：...
• 修正：...
</zh-TW>
```

- Play Console limits each language to **500 characters**.
- Keep the two languages item-for-item in step.
- Write for users: describe what changed for them, not how it was implemented. Build and
  tooling changes (Gradle, AGP, Kotlin) are left out.
- The longer GitHub release notes live on the release page:
  https://github.com/zac15987/LockView/releases
