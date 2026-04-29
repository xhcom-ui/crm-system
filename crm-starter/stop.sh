#!/bin/bash
# ============================================
# CRM 微服务停止脚本
# 用法: ./stop.sh
# ============================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PID_DIR="$PROJECT_ROOT/.pids"

info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }

# 端口列表 (按模块名)
declare -A SERVICE_PORTS=(
    ["sentinel-dashboard"]=8858
    ["crm-auth"]=8087
    ["crm-customer"]=8081
    ["crm-sales"]=8082
    ["crm-marketing"]=8083
    ["crm-service"]=8084
    ["crm-leads"]=8085
    ["crm-workflow"]=8086
    ["crm-gateway"]=8090
)

echo -e "${YELLOW}════════════════════════════════════════════${NC}"
echo -e "${YELLOW}  CRM 微服务 - 停止所有服务${NC}"
echo -e "${YELLOW}════════════════════════════════════════════${NC}"
echo ""

stopped=0

for module in "${!SERVICE_PORTS[@]}"; do
    port=${SERVICE_PORTS[$module]}
    pid_file="$PID_DIR/${module}.pid"

    # 方法1: 通过 PID 文件
    if [ -f "$pid_file" ]; then
        pid=$(cat "$pid_file")
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null && info "$module (PID $pid) 已停止"
            rm -f "$pid_file"
            stopped=$((stopped + 1))
            continue
        fi
        rm -f "$pid_file"
    fi

    # 方法2: 通过端口查找进程
    pids=$(lsof -ti :$port 2>/dev/null || true)
    if [ -n "$pids" ]; then
        for pid in $pids; do
            kill "$pid" 2>/dev/null && info "$module (端口 $port, PID $pid) 已停止"
            stopped=$((stopped + 1))
        done
    else
        info "$module (端口 $port) 未在运行"
    fi
done

echo ""
info "已停止 $stopped 个服务"

# 清理残留
rm -rf "$PID_DIR" 2>/dev/null || true
