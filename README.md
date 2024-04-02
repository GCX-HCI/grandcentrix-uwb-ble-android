# grandcentrix-appscaffold-android
App Project Template for Android

Use this project as a scaffold, i.e., as a basis for your Android projects that
* are based on Kotlin
* use Koin as the dependency injection tool of choice
* use MVVM for your architecture needs
* use navigation components
* have an application-wide theming
* have a cloud connection
* use Auth0 for authentication
* have a login/register screen
* use Firebase Cloud Messaging and Crashlytics

## Usage

When using this template as a scaffold, there are a few things you need to do:

* [ ]  Create a repository based on this template using the “Use this template” button
* [ ]  Create an Azure AD group for your project team (if not already existing)
* [ ]  Ensure that your GitHub project is setup properly
  * [ ]  Branch protection preventing pushes to main branch
  * [ ]  Branch protection enforces PRs to be up-to-date before merging
  * [ ]  Branch protection sets "Build all variants", "Run Unit Tests & Lint" as mandatory checks
  * [ ]  Ensure user permissions (admin permissions to the project Azure AD group)
  * [ ]  Activate Dependabot
* [ ]  Change the package name to suit your project’s needs [#change-the-app-package-name]
* [ ]  Change the theming names to match your project
* [ ]  Exchange the App icon according to the project
* [ ]  Create debug and release keystores for your project

### Change the App Package Name
unfortunately Android Studio/IntelliJ does not provide a reliable rename action for the App root
package including the Application Id.
So this repository contains the `./changePackagename.sh` bash script to eliminate some work.

Usage: `./changePackagename.sh your.new.root.package.name`

It will also remove the old package structure for you. But it will not eliminate itself and will
stop working after you have a different package name set. So please take care to remove the script
afterwards and commit this change.

### Auth0

* [ ]  Create tenant on Auth0.com
* [ ]  Change /res/values/auth0.xml to fit your tenant’s settings
* [ ]  When using okhttp, use the `AuthTokenInterceptor` to add the access token to every request

### Firebase

Firebase has already been added to this project and the following have been enabled:

* Crashlytics
* Analytics
* Cloud Messaging

The current setup uses a test project specific to this template.

When setting up your project, you need to [set up Firebase for your project](https://firebase.google.com/docs/android/setup?hl=en#console).

* [ ] Create a Firebase project on [console.firebase.google.com](https://console.firebase.google.com)
* [ ] Activate all modules you’d like to use

      The ones currently added to this project can be seen above, so to get the same functionality, activate those by opening the panels on the left-hand side of the Firebase project console.
* [ ] Add `google-services.json` found in Firebase’s project settings to the `/app` directory, thereby replacing the current config.
* [ ] Change default notification icon and colour in `AndroidManifest.xml`

### How to handle network requests
This scaffold uses a [generator](https://openapi-generator.tech/) to generate the client code according to a [specs file](/api-backend/specs/api.yml). If you want to change the client code in the app, just replace the specs file with your exported Swagger docs.

If you want to know how the current implementation works, you can test it using a local server. This project includes a stub server implementation, which is basically a Docker container with a dump server instance. The server returns predefined values for [predefined requests](/stub-server/v1). When the server is running local, requests can be made in the app in the home fragment.

* [ ] The app defaults the requests to http://10.0.2.2:8081 This is so that the emulator can access the local server on the host. This can (and should for production) be changed [here](/api-backend/specs/api.yml).
* [ ] Since the app uses your local server, we had to enable `android:usesCleartextTraffic="true"` in [AndroidManifest](/app/src/main/AndroidManifest.xml). If you are using a suitable backend, remove this line

[Here's how to set up the stub server.](/stub-server)
