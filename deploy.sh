#!/bin/bash
set -e

echo "🚀 Starting memory-optimized deployment for low-spec EC2..."

# 메모리 상태 확인
echo "📊 Current memory status:"
free -h

# 현재 브랜치 확인
CURRENT_BRANCH=$(git branch --show-current)
echo "📍 Current branch: $CURRENT_BRANCH"

# 최신 코드 가져오기
echo "📥 Pulling latest code..."
git pull origin master

# 🛑 Docker 컨테이너 중지 (메모리 확보)
echo "🛑 Stopping containers to free memory..."
docker compose -f docker-compose.prod.yml down

# 🧹 적극적인 메모리 정리
echo "🧹 Aggressive memory cleanup..."
docker system prune -f --volumes
sync
echo 3 | sudo tee /proc/sys/vm/drop_caches > /dev/null 2>&1 || true

# 메모리 확인
AVAILABLE_MEM=$(free -m | awk 'NR==2{printf "%.0f", $7}')
echo "💾 Available memory: ${AVAILABLE_MEM}MB"

if [ $AVAILABLE_MEM -lt 300 ]; then
    echo "⚠️ Still low on memory. Consider adding swap or upgrading instance."
fi

# 🔨 빌드
echo "🔨 Building application..."
./gradlew clean build -x test --no-daemon

echo "✅ Build successful!"

# 🚀 Docker 컨테이너 시작
echo "🐳 Starting containers..."
docker compose -f docker-compose.prod.yml up -d --build

# 배포 후 대기
echo "⏳ Waiting for application to start..."
sleep 30

# 상태 확인
echo "📊 Final status check..."
docker compose -f docker-compose.prod.yml ps
free -h

echo "🎉 Memory-optimized deployment completed!"
