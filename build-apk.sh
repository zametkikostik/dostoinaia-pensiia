#!/bin/bash

# ============================================
# Скрипт сборки подписанного APK
# Пенсионный портфель
# ============================================

set -e

echo "🚀 Сборка Пенсионного портфеля..."

# Переход в директорию проекта
cd "$(dirname "$0")"

# Проверка наличия keystore.properties
if [ ! -f "android-app/keystore.properties" ]; then
    echo "❌ Файл keystore.properties не найден!"
    echo "📝 Создайте файл android-app/keystore.properties и заполните его данными."
    exit 1
fi

# Чтение свойств из файла
STORE_FILE=""
STORE_PASSWORD=""
KEY_ALIAS=""
KEY_PASSWORD=""

while IFS='=' read -r key value; do
    case "$key" in
        storeFile) STORE_FILE="$value" ;;
        storePassword) STORE_PASSWORD="$value" ;;
        keyAlias) KEY_ALIAS="$value" ;;
        keyPassword) KEY_PASSWORD="$value" ;;
    esac
done < <(grep -v '^#' android-app/keystore.properties | grep -v '^$')

# Проверка наличия ключа подписи
if [ ! -f "android-app/release-key.jks" ]; then
    echo "⚠️  Ключ подписи не найден. Генерирую новый..."
    
    keytool -genkey -v \
        -keystore android-app/release-key.jks \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -alias "$KEY_ALIAS" \
        -storepass "$STORE_PASSWORD" \
        -keypass "$KEY_PASSWORD" \
        -dname "CN=Pension Portfolio, OU=Mobile, O=PensionPortfolio, L=Moscow, ST=Moscow, C=RU"
    
    echo "✅ Ключ подписи создан: android-app/release-key.jks"
    echo "⚠️  СОХРАНИТЕ ЭТОТ КЛЮЧ!"
else
    echo "✅ Ключ подписи найден"
fi

# Переход в директорию Android приложения
cd android-app

# Проверка Gradle wrapper
if [ -f "gradlew" ]; then
    GRADLE_CMD="./gradlew"
elif command -v gradle &> /dev/null; then
    GRADLE_CMD="gradle"
else
    echo "❌ Gradle не найден. Установите Android Studio."
    exit 1
fi

# Очистка
echo "🧹 Очистка..."
$GRADLE_CMD clean || true

# Сборка debug APK
echo "📦 Сборка debug APK..."
$GRADLE_CMD assembleDebug

# Сборка release APK
echo "📦 Сборка release APK..."
$GRADLE_CMD assembleRelease

# Создание папки releases
cd ..
mkdir -p releases

# Копирование APK
echo "📋 Копирование APK..."
if [ -f "android-app/app/build/outputs/apk/debug/app-debug.apk" ]; then
    cp android-app/app/build/outputs/apk/debug/app-debug.apk releases/
    echo "✅ Debug APK: releases/app-debug.apk"
fi

if [ -f "android-app/app/build/outputs/apk/release/app-release.apk" ]; then
    cp android-app/app/build/outputs/apk/release/app-release.apk releases/
    echo "✅ Release APK: releases/app-release.apk"
fi

echo ""
echo "✅ Сборка завершена!"
echo "🔑 Ключ: android-app/release-key.jks"
echo ""
