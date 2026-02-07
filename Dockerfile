FROM thyrlian/android-sdk:latest AS builder

WORKDIR /app
COPY . .

# Accept licenses
RUN yes | sdkmanager --licenses > /dev/null 2>&1 || true

# Build APK
RUN chmod +x gradlew && ./gradlew assembleDebug --no-daemon

# Extract APK to a clean stage
FROM alpine:latest
COPY --from=builder /app/app/build/outputs/apk/debug/app-debug.apk /out/bloodwork-tracker.apk
CMD ["cp", "/out/bloodwork-tracker.apk", "/output/bloodwork-tracker.apk"]
