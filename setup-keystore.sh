#!/bin/bash
# ASTUBE TWA Setup Script
# Run this in Termux to generate your keystore

echo ""
echo "╔══════════════════════════════════════╗"
echo "║   ASTUBE TWA Setup                   ║"
echo "╚══════════════════════════════════════╝"
echo ""

# Check if keytool is available
if ! command -v keytool &> /dev/null; then
    echo "Installing JDK..."
    pkg install openjdk-17 -y
fi

echo "Generating keystore..."
echo "You will be asked for:"
echo "  - Keystore password (remember this!)"
echo "  - Key alias: astube"
echo "  - Key password (can be same as keystore)"
echo "  - Your name/organization (anything)"
echo ""

keytool -genkeypair \
    -v \
    -keystore astube-release.jks \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias astube \
    -dname "CN=ASTUBE, OU=ASDeveloper, O=ASTUBE, L=Pakistan, ST=Pakistan, C=PK"

if [ ! -f astube-release.jks ]; then
    echo "❌ Keystore generation failed"
    exit 1
fi

echo ""
echo "✅ Keystore generated: astube-release.jks"
echo ""
echo "Now getting SHA256 fingerprint for assetlinks.json..."
echo ""

# Get SHA256 fingerprint
keytool -list -v -keystore astube-release.jks -alias astube 2>/dev/null | grep "SHA256:" | awk '{print $2}'

echo ""
echo "Getting base64 for GitHub secret..."
BASE64=$(base64 -w 0 astube-release.jks)
echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║  COPY THESE TO GITHUB SECRETS                            ║"
echo "║  (Settings → Secrets → Actions → New repository secret) ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
echo "Secret name: KEYSTORE_BASE64"
echo "Secret value:"
echo "$BASE64"
echo ""
echo "Secret name: KEYSTORE_PASSWORD"
echo "Secret value: (the password you entered above)"
echo ""
echo "Secret name: KEY_ALIAS"
echo "Secret value: astube"
echo ""
echo "Secret name: KEY_PASSWORD"
echo "Secret value: (the key password you entered above)"
echo ""
echo "════════════════════════════════════════════════════════════"
echo ""
echo "IMPORTANT: Save astube-release.jks somewhere safe!"
echo "You need it for every future update."
echo ""
