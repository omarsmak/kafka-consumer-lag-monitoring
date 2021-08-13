# How to publish this project to Maven Nexus repository

This project has moved from JCenter to Maven Central (Nexus repository), therefore publishing this project has now different workflow. To publish, make sure to follow these steps based on this [blog](https://proandroiddev.com/publishing-a-maven-artifact-3-3-step-by-step-instructions-to-mavencentral-publishing-bd661081645d):

1. Configure the the library version in `build.gradle`.

1. Create a GPG key:
    ```
    > gpg --full-generate-key
    > gpg --export-secret-keys 1D9BB9FE > secert-keys.gpg

    # upload your public key to a public server so that sonatype can find it. If that doesn't work, you can also upload manually at http://keys.gnupg.net:11371/
    > gpg --keyserver hkp://pool.sks-keyservers.net --send-keys 1D9BB9FE
    ```

1. Configure your credentials in your `~/.gradle/gradle.properties` file:

    ```
    signing.keyId=[gpg key id]
    signing.password=[gpg key password]
    signing.secretKeyRingFile=[gpg key file location to `secert-keys.gpg`]

    ossrhUsername=[sonatype username e.g: JIRA username]
    ossrhPassword=[sonatype password e.g: JIRA password]
    ```

1. Upload the artifacts:
    ```
    ./gradlew uploadArchives
    ```