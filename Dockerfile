# استخدم صورة جافا الرسمية المناسبة (Java 17 هنا كمثال)
FROM eclipse-temurin:17-jdk-alpine

# اضبط مجلد العمل داخل الحاوية
WORKDIR /app

# انسخ ملف jar من مجلد target إلى مجلد العمل داخل الحاوية
COPY target/al_mohannad_backend-0.0.1-SNAPSHOT.jar app.jar

# فتح البورت 8080 (أو البورت الذي يستخدمه تطبيقك)
EXPOSE 8080

# الأمر لتشغيل تطبيق Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
