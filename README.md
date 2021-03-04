# Shake Shack Android
Shake Shack's flagship Android app

## Getting started
The following sections will get you up and running with building and installing the app. See [confluence](https://shakeshack.atlassian.net/wiki/spaces/DPM/pages/344621057/Mobile) for additional documentation.

### Clone the project
`git clone git@github.com:Shake-Shack-Engineering/shake-shack-ios.git`

### Download dependencies
1. Install Java 8 and set your `$JAVA_HOME` environment variable
1. Ensure you have the version of Android Studio installed that matches the value of `com.android.tools.build:gradle` specified in `build.gradle` in the repo root

### Run the app
1. Open the project in Android Studio
1. Open the Build Variants pane
1. Change the active build variant to stagingDebug
1. Run the app

## Git strategy and conventions
See [confluence](https://shakeshack.atlassian.net/wiki/spaces/DPM/pages/344784923/Git+Strategy+and+Conventions+-+Mobile) for more in-depth details on Git strategy and conventions.

The following are the main branches:

- `dev` - The development branch for the production Java codebase. Feature branches are merged here for changes to the production app
- `dev-rewrite` - The development branch for modernized Kotlin codebase. Feature branches are merged here for changes to the rewritten app. When we're done, this branch will merge to `dev` and we'll be back to three main branches
- `staging` - Used for UAT builds
- `master` - Production branch. Master always represents the code for what's currently in the App Store 

## Placing test orders
**Important:** _Orders should only be placed to location named Demo Vendor_. See [confluence](https://shakeshack.atlassian.net/wiki/spaces/DPM/pages/374734849/Placing+Test+Orders+-+Mobile) for more details.

At this time, Demo Vendor can only be in either staging or production. If you don't see it available staging, check production and vice versa.

## Back-end environments and Android build variants
There are two backend environments: staging and production. Run `stagingDebug` to point at the staging backend, and `prodDebug` to point at the staging backend. Debug builds have private locations available (e.g. Demo Vendor), and release builds do not.

| Build Variant          | Backend Environments                                                                            | Notes                                               |
|------------------------|-------------------------------------------------------------------------------------------------|-----------------------------------------------------|
| stagingDebug           | - SSMA Staging with private locations (e.g. Demo Vendor) on<br>- Olo Sandbox<br>- Kount Staging | You will hear this referred to as simply "Staging"  |
| stagingRelease         | - SSMA Staging with private locations off<br>- Olo Sandbox<br>- Kount Staging                   | You will probably never use this variant            |
| prodDebug              | - SSMA Production with private locations on<br>- Olo Production<br>- Kount Production           |                                                     |
| prodRelease            | - SSMA Production with private locations off<br>- Olo Production<br>- Kount Production          | This is the variant used for Play Store releases    |

## Releases
- Run `tools/deployment/release_uat.sh` or `tools/deployment/release_prod.sh` to kick off a UAT or Production release. These scripts will force you to set a proper version number
