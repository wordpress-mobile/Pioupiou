<img src="https://github.com/wordpress-mobile/Pioupiou/raw/develop/app/src/main/ic_launcher-web.png" alt="Pioupiou Title" style="width: 200px !important;" width="200px"/>

# Pioupiou

Micro blogging app using your WordPress site as backend.

## Build Instructions ##

You first need to create the `gradle.properties` file:

```shell
$ cp app/gradle.properties-example app/gradle.properties
```

Note: this is the default `./gradle.properties` file. If you
want to use a WordPress.com account, you'll have to get a WordPress.com
OAuth2 ID and secret. Please read the
[OAuth2 Authentication](#oauth2-authentication) section.

For WordPress.com accounts, you should also fill in `wp.SITE_DOMAIN`
with the site in your account you wish to use with PiouPiou. If this
is left blank, the default site will be used.

You can now build the project:

```shell
$ ./gradlew build
```

You can use [Android Studio][1] by importing the project as a Gradle project.

## OAuth2 Authentication ##

In order to use WordPress.com functions you will need a client ID and
a client secret key. These details will be used to authenticate your
application and verify that the API calls being made are valid. You can
create an application or view details for your existing applications with
our [WordPress.com applications manager][2].

When creating your application, you should select "Native client" for the
application type. The applications manager currently requires a "redirect URL",
but this isn't used for mobile apps. Just use "https://localhost".

Once you've created your application in the [applications manager][2], you'll
need to edit the `./gradle.properties` file and change the
`WP.OAUTH.APP.ID` and `WP.OAUTH.APP.SECRET` fields. Then you can compile and
run the app on a device or an emulator and try to login with a WordPress.com
account.

Read more about [OAuth2][3] and the [WordPress.com REST endpoint][4].

[1]: http://developer.android.com/sdk/installing/studio.html
[2]: https://developer.wordpress.com/apps/
[3]: https://developer.wordpress.com/docs/oauth2/
[4]: https://developer.wordpress.com/docs/api/
