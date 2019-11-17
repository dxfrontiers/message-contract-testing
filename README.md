# Spring Cloud Contract Testing for 3rd-party Google Pub/Sub messaging


env GOOGLE_APPLICATION_CREDENTIALS=/Users/maverick/Development/google_hoechst_d.json STORAGE_NOTIFICATION_TOPIC=uploads_development_it STORAGE_BUCKET=nap-uploads_development_it ./gradlew clean message-contract-provider-proxy:build message-contract-provider-proxy:publishToMavenLocal

./gradlew clean message-contract-consumer:build
env GOOGLE_APPLICATION_CREDENTIALS=/Users/maverick/Development/google_hoechst_d.json STORAGE_NOTIFICATION_TOPIC=uploads_development_it STORAGE_BUCKET=nap-uploads_development_it ./gradlew clean message-contract-consumer:bootRun