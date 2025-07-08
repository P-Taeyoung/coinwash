#!/bin/bash
set -e

echo "🚀 Starting deployment..."

# 현재 브랜치 확인
CURRENT_BRANCH=$(git branch --show-current)
echo "📍 Current branch: $CURRENT_BRANCH"

# 최신 코드 가져오기
echo "📥 Pulling latest code..."
git pull origin main

# 빌드
echo "🔨 Building application..."
./gradlew clean build -x test

# 빌드 결과 확인
if [ ! -f build/libs/coinwash-*.jar ]; then
    echo "❌ Build failed - JAR file not found!"
    exit 1
fi

echo "✅ Build successful!"

# Docker 배포
echo "🐳 Deploying with Docker..."
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d --build

# 배포 후 대기
echo "⏳ Waiting for application to start..."
sleep 15

# 상태 확인
echo "📊 Checking status..."
docker-compose -f docker-compose.prod.yml ps

echo "🎉 Deployment finished!"
