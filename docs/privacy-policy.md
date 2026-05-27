# Privacy Policy for Lock View

**Last Updated:** May 27, 2026

## Introduction

This Privacy Policy describes how Lock View ("we", "our", or "the app") handles user information. Lock View is an open-source Android application developed by zac15987 that provides a lockable image viewer with advanced gesture controls.

We are committed to protecting your privacy. Lock View is designed with privacy in mind and collects no personal information whatsoever.

## Information We Do NOT Collect

Lock View does not collect, store, or transmit any personal information. Specifically, we do not collect:

- Personal identifiers (name, email, phone number, etc.)
- Device identifiers or advertising IDs
- Location data
- Usage analytics or statistics
- Crash reports or diagnostic data
- Photos or media metadata
- Any data through third-party services

## Local Data Storage

Lock View stores the following data locally on your device only:

- **Theme Preference**: Your choice of light, dark, or system theme
- **Language Preference**: Your selected display language (English, Traditional Chinese, or System default)

This data is stored using Android's DataStore and SharedPreferences, remains on your device, and is never transmitted to any external servers.

## Permissions

Lock View does not declare or request any runtime permissions.

### How Image Selection Works

To open an image, Lock View uses Android's Storage Access Framework (SAF). When you tap the image picker button, the Android system shows its built-in document picker. You manually choose a single image, and the system grants Lock View temporary read access to that image only — scoped to the current app session.

This means Lock View:
- Holds no permission to read your photo library
- Cannot scan, index, or enumerate any files on your device
- Can only read the specific image you actively choose, for the duration of the session
- Does not access image metadata or EXIF data beyond what is needed to display the image
- Does not modify or delete any files
- Loses access to the selected image when the app is closed (you must re-select it on next launch)

## Data Sharing

Lock View does not share any data with third parties. We do not use:
- Analytics services
- Advertising networks
- Social media integrations
- Cloud storage services
- Any external APIs or services

## Security

Your privacy and security are our top priorities:
- All app data remains locally on your device
- No network connections are established by the app
- No data is transmitted over the internet
- The app's source code is publicly available on GitHub for transparency and security auditing

## Children's Privacy

Lock View does not knowingly collect any personal information from children under the age of 13. The app does not collect any personal information from users of any age.

## Open Source Transparency

Lock View is an open-source application. Our complete source code is available on GitHub, allowing anyone to verify our privacy practices and ensure that the app functions exactly as described in this privacy policy.

## Future Updates

While Lock View currently collects no data, we may consider adding optional crash reporting functionality in future updates to improve app stability. If we implement such features:
- They will be completely optional and require explicit user consent
- We will update this privacy policy accordingly
- Users will be notified of any significant changes

## Changes to This Privacy Policy

We may update this Privacy Policy from time to time. Any changes will be reflected in the "Last Updated" date at the top of this policy. For significant changes, we will provide a more prominent notice within the app.

## Contact Information

If you have any questions or concerns about this Privacy Policy or Lock View's privacy practices, please contact us at:

**Email:** zac15987@gmail.com

## Consent

By using Lock View, you agree to this Privacy Policy. If you do not agree with this policy, please do not use the app.

---

*Lock View is developed with respect for user privacy. We believe in transparency and minimal data collection.*