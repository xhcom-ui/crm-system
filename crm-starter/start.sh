#!/bin/bash
# ============================================
# CRM 微服务一键启动脚本
# 用法: ./start.sh [选项]
#   --build    启动前先编译
#   --check    仅检查前置条件
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # 无颜色

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$PROJECT_ROOT/logs"
PID_DIR="$PROJECT_ROOT/.pids"
DEFAULT_JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
NACOS_SERVER_ADDR="127.0.0.1:8848"
NACOS_NAMESPACE="dev"
SENTINEL_DASHBOARD_PORT=8858
SENTINEL_DASHBOARD_JAR="$PROJECT_ROOT/crm-starter/src/main/resources/sentinel-dashboard-1.8.9.jar"
SENTINEL_DASHBOARD_USERNAME="${SENTINEL_DASHBOARD_USERNAME:-sentinel}"
SENTINEL_DASHBOARD_PASSWORD="${SENTINEL_DASHBOARD_PASSWORD:-sentinel}"

if [ -n "${CRM_STARTER_JAVA_HOME:-}" ] && [ -d "$CRM_STARTER_JAVA_HOME" ]; then
    export JAVA_HOME="$CRM_STARTER_JAVA_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"
elif [ -d "$DEFAULT_JAVA_HOME" ]; then
    export JAVA_HOME="$DEFAULT_JAVA_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# 服务定义: (模块名 端口 主类名)
SERVICES=(
    "sentinel-dashboard:${SENTINEL_DASHBOARD_PORT}:SentinelDashboard"
    "crm-auth:8087:AuthApplication"
    "crm-customer:8081:CustomerApplication"
    "crm-sales:8082:SalesApplication"
    "crm-marketing:8083:MarketingApplication"
    "crm-service:8084:ServiceApplication"
    "crm-leads:8085:LeadsApplication"
    "crm-workflow:8086:WorkflowApplication"
    "crm-gateway:8090:GatewayApplication"
)

# 日志函数
info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }
title() { echo -e "\n${CYAN}════════════════════════════════════════════${NC}"; 
          echo -e "${CYAN}  $1${NC}"; 
          echo -e "${CYAN}════════════════════════════════════════════${NC}"; }

# 检查端口是否开放
check_port() {
    (echo >/dev/tcp/$1/$2) &>/dev/null && return 0 || return 1
}

resolve_sentinel_dashboard_jar() {
    local source_jar="$SENTINEL_DASHBOARD_JAR"
    local packaged_jar="$PROJECT_ROOT/crm-starter/target/classes/sentinel-dashboard-1.8.9.jar"
    if [ -s "$source_jar" ]; then
        echo "$source_jar"
        return 0
    fi
    if [ -s "$packaged_jar" ]; then
        echo "$packaged_jar"
        return 0
    fi
    error "Sentinel 控制台 jar 不存在: $source_jar"
    return 1
}

start_sentinel_dashboard() {
    local module=$1
    local port=$2
    local log_file="$LOG_DIR/${module}.log"
    local pid_file="$PID_DIR/${module}.pid"

    local jar
    jar=$(resolve_sentinel_dashboard_jar) || return 1

    info "▶ 启动 $module (端口 $port)..."
    cd "$PROJECT_ROOT"
    nohup "$JAVA_HOME/bin/java" \
        -Dserver.port=$port \
        -Dcsp.sentinel.dashboard.server=127.0.0.1:$port \
        -Dproject.name=sentinel-dashboard \
        -Dauth.username="$SENTINEL_DASHBOARD_USERNAME" \
        -Dauth.password="$SENTINEL_DASHBOARD_PASSWORD" \
        -jar "$jar" \
        > "$log_file" 2>&1 &

    local pid=$!
    echo $pid > "$pid_file"
    info "  Sentinel 控制台登录账号: $SENTINEL_DASHBOARD_USERNAME"

    local waited=0
    while [ $waited -lt 60 ]; do
        if grep -q "Started .*in" "$log_file" 2>/dev/null; then
            info "  ✓ $module 启动成功 (${waited}s)"
            return 0
        fi
        if ! kill -0 "$pid" 2>/dev/null; then
            error "  ✗ $module 启动进程已退出，请查看错误："
            tail -n 80 "$log_file" 2>/dev/null || true
            return 1
        fi
        sleep 2
        waited=$((waited + 2))
    done

    warn "  ⚠ $module 启动超时，请检查 $log_file"
    tail -n 80 "$log_file" 2>/dev/null || true
    return 1
}

kill_port() {
    local port=$1
    local pids
    pids=$(lsof -tiTCP:$port -sTCP:LISTEN 2>/dev/null || true)
    if [ -z "$pids" ]; then
        warn "端口 $port 未找到占用进程"
        return 0
    fi

    for pid in $pids; do
        warn "kill 端口 $port 占用进程 PID $pid"
        kill "$pid" 2>/dev/null || true
    done
    sleep 1

    if check_port 127.0.0.1 $port; then
        pids=$(lsof -tiTCP:$port -sTCP:LISTEN 2>/dev/null || true)
        for pid in $pids; do
            warn "kill -9 端口 $port 残留进程 PID $pid"
            kill -9 "$pid" 2>/dev/null || true
        done
        sleep 1
    fi

    if check_port 127.0.0.1 $port; then
        error "端口 $port 清理失败，请手动检查: lsof -nP -iTCP:$port -sTCP:LISTEN"
        return 1
    fi
    info "端口 $port 已清理"
}

kill_backend_ports() {
    title "后端端口清理"
    for svc_def in "${SERVICES[@]}"; do
        IFS=':' read -r module port main <<< "$svc_def"
        if check_port 127.0.0.1 $port; then
            warn "$module 端口 $port 已占用，准备 kill"
            kill_port "$port"
        else
            info "$module 端口 $port 空闲"
        fi
    done
}

# 检查前置条件
check_prerequisites() {
    title "前置条件检查"
    
    # Java
    if java -version &>/dev/null 2>&1; then
        info "Java  ✓ ($(java -version 2>&1 | head -1))"
    else
        error "Java  ✗ 请安装 JDK 17+"
        return 1
    fi

    # Maven
    if mvn --version &>/dev/null 2>&1; then
        info "Maven ✓"
    else
        error "Maven ✗"
        return 1
    fi

    # MySQL
    if check_port 127.0.0.1 3306; then
        info "MySQL ✓ (127.0.0.1:3306)"
    else
        warn "MySQL ✗ (127.0.0.1:3306 不可达，服务可能无法启动)"
    fi

    # Redis
    if check_port 127.0.0.1 6379; then
        info "Redis ✓ (127.0.0.1:6379)"
    else
        warn "Redis ✗ (127.0.0.1:6379 不可达，缓存/会话不可用)"
    fi

    # Nacos
    if check_port 127.0.0.1 8848; then
        info "Nacos ✓ (127.0.0.1:8848)"
    else
        error "Nacos ✗ 请先启动 Nacos ($NACOS_SERVER_ADDR, namespace=$NACOS_NAMESPACE)"
        return 1
    fi
    info "Nacos 配置中心/注册中心 namespace=$NACOS_NAMESPACE"
}

# 启动单个服务
start_service() {
    local module=$1
    local port=$2
    local main=$3
    local log_file="$LOG_DIR/${module}.log"
    local pid_file="$PID_DIR/${module}.pid"

    if check_port 127.0.0.1 $port; then
        warn "$module 端口 $port 仍被占用，尝试清理后再启动"
        kill_port "$port"
    fi

    if [ "$module" = "sentinel-dashboard" ]; then
        start_sentinel_dashboard "$module" "$port"
        return $?
    fi

    info "▶ 启动 $module (端口 $port)..."
    info "  Nacos 配置读取: dataId=crm-common.yaml,${module}.yaml, namespace=$NACOS_NAMESPACE, server=$NACOS_SERVER_ADDR"
    info "  Nacos 注册中心: service=$module, namespace=$NACOS_NAMESPACE, server=$NACOS_SERVER_ADDR"

    cd "$PROJECT_ROOT"
    nohup mvn -pl $module \
        spring-boot:run \
        -Dspring-boot.run.jvmArguments="-Xmx256m -Xms128m" \
        -Dspring-boot.run.arguments="--spring.cloud.nacos.discovery.enabled=true --spring.cloud.nacos.discovery.server-addr=$NACOS_SERVER_ADDR --spring.cloud.nacos.discovery.namespace=$NACOS_NAMESPACE --spring.cloud.nacos.config.enabled=true --spring.cloud.nacos.config.server-addr=$NACOS_SERVER_ADDR --spring.cloud.nacos.config.namespace=$NACOS_NAMESPACE --spring.cloud.nacos.config.file-extension=yaml --spring.config.import=optional:classpath:application-common.yml,optional:nacos:crm-common.yaml,optional:nacos:${module}.yaml --logging.level.com.alibaba.nacos=INFO --logging.level.com.alibaba.cloud.nacos=INFO --logging.level.com.alibaba.cloud.nacos.registry=INFO --logging.level.com.alibaba.cloud.nacos.client=INFO --logging.level.org.springframework.cloud=INFO" \
        -q \
        > "$log_file" 2>&1 &
    
    local pid=$!
    echo $pid > "$pid_file"

    # 等待启动完成 (最多 60 秒)
    local waited=0
    while [ $waited -lt 60 ]; do
        if grep -q "Started .*in" "$log_file" 2>/dev/null; then
            info "  ✓ $module 启动成功 (${waited}s)"
            grep -Ei "nacos|configservice|namingservice|register|registered|registry|discovery|namespace|server-addr" "$log_file" | tail -n 30 || true
            return 0
        fi
        if ! kill -0 "$pid" 2>/dev/null; then
            error "  ✗ $module 启动进程已退出，请查看错误："
            tail -n 80 "$log_file" 2>/dev/null || true
            return 1
        fi
        sleep 2
        waited=$((waited + 2))
    done

    warn "  ⚠ $module 启动超时，请检查 $log_file"
    echo -e "${YELLOW}  最近日志:${NC}"
    tail -n 80 "$log_file" 2>/dev/null || true
    return 1
}

# 主流程
main() {
    mkdir -p "$LOG_DIR" "$PID_DIR"

    # 标题
    title "CRM 微服务一键启动器 v1.0"
    echo -e "  服务数量: ${#SERVICES[@]}"
    echo -e "  项目路径: $PROJECT_ROOT"
    echo ""

    # 前置检查
    check_prerequisites || exit 1

    # 清理后端端口
    kill_backend_ports

    # 编译
    if [[ "$1" == "--build" ]]; then
        title "编译项目"
        cd "$PROJECT_ROOT"
        mvn compile -q && info "编译完成 ✓" || { error "编译失败"; exit 1; }
    fi

    # 启动服务
    title "启动微服务"
    local total=${#SERVICES[@]}
    local success=0

    for svc_def in "${SERVICES[@]}"; do
        IFS=':' read -r module port main <<< "$svc_def"
        if start_service "$module" "$port" "$main"; then
            success=$((success + 1))
        fi
        sleep 2  # 间隔启动
    done

    # 结果仪表盘
    title "服务仪表盘"
    printf "  %-4s %-15s %-6s %s\n" "序号" "服务" "端口" "状态"
    printf "  %-4s %-15s %-6s %s\n" "----" "---------------" "------" "----"
    
    local i=1
    for svc_def in "${SERVICES[@]}"; do
        IFS=':' read -r module port main <<< "$svc_def"
        if check_port 127.0.0.1 $port; then
            printf "  ${GREEN}%-4d %-15s %-6s %s${NC}\n" $i "$module" "$port" "运行中 ✓"
        else
            printf "  ${RED}%-4d %-15s %-6s %s${NC}\n" $i "$module" "$port" "未启动 ✗"
        fi
        i=$((i + 1))
    done

    echo ""
    echo -e "  ${CYAN}Sentinel 控制台:${NC} http://localhost:${SENTINEL_DASHBOARD_PORT}"
    echo -e "  ${CYAN}Sentinel 账号:${NC} ${SENTINEL_DASHBOARD_USERNAME}"
    echo -e "  ${CYAN}网关地址:${NC} http://localhost:8090"
    echo -e "  ${CYAN}前端地址:${NC} http://localhost:3000 或 http://localhost:3007"
    echo ""
    info "全部完成 ($success/$total 服务运行中)"
    echo -e "  ${YELLOW}停止所有服务:${NC} ./stop.sh"
    echo -e "  ${YELLOW}查看日志:${NC}     tail -f logs/crm-*.log"
}

# 选项处理
case "${1:-}" in
    --check)
        check_prerequisites
        ;;
    *)
        main "$@"
        ;;
esac
