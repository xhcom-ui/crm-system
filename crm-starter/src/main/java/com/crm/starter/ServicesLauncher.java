package com.crm.starter;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CRM 全服务启动器
 * 一键启动所有微服务，支持顺序启动、健康检查、优雅停止
 *
 * 使用方式: java -jar crm-starter.jar
 */
@Slf4j
public class ServicesLauncher {

    private static final String DEFAULT_JAVA_HOME = "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home";
    private static final String NACOS_SERVER_ADDR = "127.0.0.1:8848";
    private static final String NACOS_NAMESPACE = "dev";
    private static final int SENTINEL_DASHBOARD_PORT = 8858;
    private static final String SENTINEL_DASHBOARD_JAR_NAME = "sentinel-dashboard-1.8.9.jar";
    private static final String DEFAULT_SENTINEL_DASHBOARD_USERNAME = "sentinel";
    private static final String DEFAULT_SENTINEL_DASHBOARD_PASSWORD = "sentinel";
    private static final String PROJECT_ROOT = resolveProjectRoot();

    // 服务定义：(模块名, 主类, 端口, 启动顺序)
    private static final List<ServiceDef> SERVICES = List.of(
        new ServiceDef("sentinel-dashboard", "",                  SENTINEL_DASHBOARD_PORT, 0, true),
        new ServiceDef("crm-auth",      "com.crm.auth.AuthApplication",           8087, 1),
        new ServiceDef("crm-customer",  "com.crm.customer.CustomerApplication",   8081, 2),
        new ServiceDef("crm-sales",     "com.crm.sales.SalesApplication",         8082, 3),
        new ServiceDef("crm-marketing", "com.crm.marketing.MarketingApplication", 8083, 4),
        new ServiceDef("crm-service",   "com.crm.service.ServiceApplication",     8084, 5),
        new ServiceDef("crm-leads",     "com.crm.leads.LeadsApplication",         8085, 6),
        new ServiceDef("crm-workflow",  "com.crm.workflow.WorkflowApplication",   8086, 7),
        new ServiceDef("crm-gateway",   "com.crm.gateway.GatewayApplication",     8090, 8)
    );

    private static final Map<String, Process> RUNNING = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        log.info("╔══════════════════════════════════════════╗");
        log.info("║     CRM 微服务一键启动器 v1.0            ║");
        log.info("╠══════════════════════════════════════════╣");
        log.info("║  服务数量: {}                            ║", SERVICES.size());
        log.info("║  启动模式: 顺序启动                      ║");
        log.info("║  Nacos: {} namespace={}        ║", NACOS_SERVER_ADDR, NACOS_NAMESPACE);
        log.info("╚══════════════════════════════════════════╝");

        // 1. 检查前置条件
        checkPrerequisites();

        // 2. 清理已占用的后端端口
        killBackendPorts();

        // 3. 按顺序启动所有服务
        for (ServiceDef svc : SERVICES) {
            startService(svc);
        }

        // 4. 注册 JVM 关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("\n正在停止所有服务...");
            stopAllServices();
        }));

        // 5. 打印状态仪表盘
        printDashboard();

        // 6. 保持运行，等待用户输入
        log.info("按 Enter 键停止所有服务...");
        System.in.read();
        stopAllServices();
    }

    private static void checkPrerequisites() {
        log.info("┌─ 前置条件检查 ──────────────────────────┐");
        
        // 检查 Java
        String javaVersion = System.getProperty("java.version");
        log.info("│ Java:  {} ✓", javaVersion);

        // 检查 MySQL (尝试连接 3306)
        boolean mysqlOk = checkPort("127.0.0.1", 3306);
        log.info("│ MySQL: {} {}", mysqlOk ? "127.0.0.1:3306" : "不可达", mysqlOk ? "✓" : "✗ (服务可能无法启动)");

        // 检查 Redis
        boolean redisOk = checkPort("127.0.0.1", 6379);
        log.info("│ Redis: {} {}", redisOk ? "127.0.0.1:6379" : "不可达", redisOk ? "✓" : "✗ (缓存/会话不可用)");

        // 检查 Nacos
        boolean nacosOk = checkPort("127.0.0.1", 8848);
        log.info("│ Nacos: {} namespace={} {}", nacosOk ? NACOS_SERVER_ADDR : "不可达", NACOS_NAMESPACE, nacosOk ? "✓" : "✗ (服务注册失败)");

        log.info("└──────────────────────────────────────────┘");
    }

    private static void startService(ServiceDef svc) {
        try {
            log.info("▶ 启动 {} (端口 {})...", svc.module, svc.port);
            if (checkPort("127.0.0.1", svc.port)) {
                log.warn("⚠ {} 端口 {} 仍被占用，尝试清理后再启动。", svc.module, svc.port);
                killPort(svc.port);
            }

            if (svc.sentinelDashboard) {
                startSentinelDashboard(svc);
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(
                "mvn", "-pl", svc.module,
                "spring-boot:run",
                "-Dspring-boot.run.jvmArguments=-Xmx256m -Xms128m",
                "-Dspring-boot.run.arguments=" + buildSpringRunArguments(svc),
                "-q"
            );
            pb.directory(new File(PROJECT_ROOT));
            Map<String, String> env = pb.environment();
            env.put("JAVA_HOME", resolveJavaHome(env));
            env.put("PATH", env.get("JAVA_HOME") + "/bin:" + env.getOrDefault("PATH", ""));
            pb.redirectErrorStream(true);
            log.info("  Nacos 配置读取: dataId=crm-common.yaml,{}.yaml, namespace={}, server={}", svc.module, NACOS_NAMESPACE, NACOS_SERVER_ADDR);
            log.info("  Nacos 注册中心: service={}, namespace={}, server={}", svc.module, NACOS_NAMESPACE, NACOS_SERVER_ADDR);
            Process p = pb.start();

            RUNNING.put(svc.module, p);
            Deque<String> recentLines = new ArrayDeque<>();
            AtomicBoolean started = new AtomicBoolean(false);

            Thread reader = new Thread(() -> readProcessOutput(svc, p, recentLines, started), svc.module + "-reader");
            reader.setDaemon(true);
            reader.start();

            waitForStartup(svc, p, started, recentLines);

        } catch (Exception e) {
            log.error("✗ {} 启动失败: {}", svc.module, e.getMessage());
        }
    }

    private static void startSentinelDashboard(ServiceDef svc) {
        try {
            File jar = ensureSentinelDashboardJar();
            if (jar == null || !jar.isFile()) {
                log.error("✗ Sentinel 控制台 jar 不存在，跳过启动。期望路径: crm-starter/src/main/resources/{}", SENTINEL_DASHBOARD_JAR_NAME);
                return;
            }

            String javaHome = resolveJavaHome(System.getenv());
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String username = getEnvOrDefault("SENTINEL_DASHBOARD_USERNAME", DEFAULT_SENTINEL_DASHBOARD_USERNAME);
            String password = getEnvOrDefault("SENTINEL_DASHBOARD_PASSWORD", DEFAULT_SENTINEL_DASHBOARD_PASSWORD);
            ProcessBuilder pb = new ProcessBuilder(
                    javaBin,
                    "-Dserver.port=" + svc.port,
                    "-Dcsp.sentinel.dashboard.server=127.0.0.1:" + svc.port,
                    "-Dproject.name=sentinel-dashboard",
                    "-Dauth.username=" + username,
                    "-Dauth.password=" + password,
                    "-jar",
                    jar.getAbsolutePath()
            );
            pb.directory(new File(PROJECT_ROOT));
            pb.redirectErrorStream(true);
            log.info("  Sentinel 控制台登录账号: {}", username);
            Process p = pb.start();

            RUNNING.put(svc.module, p);
            Deque<String> recentLines = new ArrayDeque<>();
            AtomicBoolean started = new AtomicBoolean(false);
            Thread reader = new Thread(() -> readProcessOutput(svc, p, recentLines, started), svc.module + "-reader");
            reader.setDaemon(true);
            reader.start();

            waitForStartup(svc, p, started, recentLines);
        } catch (Exception e) {
            log.error("✗ Sentinel 控制台启动失败: {}", e.getMessage());
        }
    }

    private static File ensureSentinelDashboardJar() {
        List<File> candidates = List.of(
                new File(PROJECT_ROOT, "crm-starter/src/main/resources/" + SENTINEL_DASHBOARD_JAR_NAME),
                new File(PROJECT_ROOT, "crm-starter/target/classes/" + SENTINEL_DASHBOARD_JAR_NAME),
                new File(PROJECT_ROOT, "target/classes/" + SENTINEL_DASHBOARD_JAR_NAME)
        );
        for (File candidate : candidates) {
            if (candidate.isFile() && candidate.length() > 0) {
                log.info("  使用 Sentinel 控制台 jar: {}", candidate.getAbsolutePath());
                return candidate;
            }
        }
        log.error("  未找到 Sentinel 控制台 jar: {}", SENTINEL_DASHBOARD_JAR_NAME);
        return null;
    }

    private static void readProcessOutput(ServiceDef svc, Process p, Deque<String> recentLines, AtomicBoolean started) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                rememberLine(recentLines, line);
                if (line.contains("Started") && line.contains("in")) {
                    started.set(true);
                    log.info("✓ {} 启动成功 (端口 {})", svc.module, svc.port);
                } else if (isImportantLogLine(line)) {
                    if (isErrorLogLine(line)) {
                        log.error("[{}] {}", svc.module, line);
                    } else if (isWarnLogLine(line)) {
                        log.warn("[{}] {}", svc.module, line);
                    } else {
                        log.info("[{}] {}", svc.module, line);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[{}] 读取启动日志失败: {}", svc.module, e.getMessage());
        }
    }

    private static void waitForStartup(ServiceDef svc, Process process, AtomicBoolean started, Deque<String> recentLines) {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(90);
        boolean portListenLogged = false;
        while (System.currentTimeMillis() < deadline) {
            if (!portListenLogged && checkPort("127.0.0.1", svc.port)) {
                portListenLogged = true;
                log.info("  {} 端口已监听 (端口 {})，继续等待 Spring Boot 完成启动...", svc.module, svc.port);
            }

            if (started.get()) {
                sleep(2000);
                if (process.isAlive()) {
                    log.info("✓ {} 已完成启动并保持运行 (端口 {})", svc.module, svc.port);
                    return;
                }
                int exitCode = process.exitValue();
                log.error("✗ {} 输出启动成功后进程退出，退出码: {}", svc.module, exitCode);
                printRecentLines(svc.module, recentLines);
                return;
            }

            if (!process.isAlive()) {
                int exitCode = process.exitValue();
                log.error("✗ {} 启动进程已退出，退出码: {}", svc.module, exitCode);
                printRecentLines(svc.module, recentLines);
                return;
            }

            sleep(1000);
        }

        if (!started.get()) {
            log.error("✗ {} 启动超时，90 秒内未输出 Spring Boot Started 日志", svc.module);
            printRecentLines(svc.module, recentLines);
        }
    }

    private static void rememberLine(Deque<String> recentLines, String line) {
        synchronized (recentLines) {
            if (recentLines.size() >= 80) {
                recentLines.removeFirst();
            }
            recentLines.addLast(line);
        }
    }

    private static void printRecentLines(String module, Deque<String> recentLines) {
        List<String> lines;
        synchronized (recentLines) {
            lines = new ArrayList<>(recentLines);
        }
        if (lines.isEmpty()) {
            log.error("[{}] 没有捕获到启动日志。请确认 Maven、模块路径和项目根目录是否正确。", module);
            return;
        }

        log.error("──── {} 最近启动日志 BEGIN ────", module);
        int from = Math.max(0, lines.size() - 40);
        for (int i = from; i < lines.size(); i++) {
            log.error("[{}] {}", module, lines.get(i));
        }
        log.error("──── {} 最近启动日志 END ────", module);
    }

    private static boolean isImportantLogLine(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return isErrorLogLine(line)
                || isWarnLogLine(line)
                || lower.contains("nacos")
                || lower.contains("configservice")
                || lower.contains("config service")
                || lower.contains("namingservice")
                || lower.contains("naming service")
                || lower.contains("register")
                || lower.contains("registered")
                || lower.contains("registry")
                || lower.contains("discovery")
                || lower.contains("namespace")
                || lower.contains("server-addr");
    }

    private static boolean isErrorLogLine(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        if (lower.contains("unable to load io.netty.resolver.dns.macos.macosdnsserveraddressstreamprovider")) {
            return false;
        }
        return line.contains("[ERROR]")
                || line.contains(" ERROR ")
                || line.contains("|-ERROR")
                || lower.contains("application failed to start")
                || lower.contains("failed to execute goal")
                || lower.contains("failed to start bean")
                || lower.contains("web server failed to start")
                || lower.contains("address already in use")
                || lower.contains("unknown database")
                || lower.contains("access denied")
                || lower.contains("connection refused")
                || lower.contains("no plugin found")
                || lower.contains("could not resolve");
    }

    private static boolean isWarnLogLine(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return line.contains(" WARN ")
                || line.contains("|-WARN")
                || lower.contains("config is empty")
                || lower.contains("unable to load io.netty.resolver.dns.macos.macosdnsserveraddressstreamprovider");
    }

    private static String buildSpringRunArguments(ServiceDef svc) {
        return "--spring.cloud.nacos.discovery.enabled=true "
                + "--spring.cloud.nacos.discovery.server-addr=" + NACOS_SERVER_ADDR + " "
                + "--spring.cloud.nacos.discovery.namespace=" + NACOS_NAMESPACE + " "
                + "--spring.cloud.nacos.config.enabled=true "
                + "--spring.cloud.nacos.config.server-addr=" + NACOS_SERVER_ADDR + " "
                + "--spring.cloud.nacos.config.namespace=" + NACOS_NAMESPACE + " "
                + "--spring.cloud.nacos.config.file-extension=yaml "
                + "--spring.config.import=optional:classpath:application-common.yml,optional:nacos:crm-common.yaml,optional:nacos:" + svc.module + ".yaml "
                + "--logging.level.com.alibaba.nacos=INFO "
                + "--logging.level.com.alibaba.cloud.nacos=INFO "
                + "--logging.level.com.alibaba.cloud.nacos.registry=INFO "
                + "--logging.level.com.alibaba.cloud.nacos.client=INFO "
                + "--logging.level.org.springframework.cloud=INFO";
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void stopAllServices() {
        RUNNING.forEach((name, process) -> {
            try {
                log.info("◼ 停止 {}...", name);
                process.destroyForcibly().waitFor();
                log.info("  {} 已停止", name);
            } catch (Exception e) {
                log.warn("  停止 {} 失败: {}", name, e.getMessage());
            }
        });
        RUNNING.clear();
    }

    private static void killBackendPorts() {
        log.info("┌─ 后端端口清理 ──────────────────────────┐");
        for (ServiceDef svc : SERVICES) {
            if (checkPort("127.0.0.1", svc.port)) {
                log.warn("│ {} 端口 {} 已占用，准备 kill", svc.module, svc.port);
                killPort(svc.port);
            } else {
                log.info("│ {} 端口 {} 空闲", svc.module, svc.port);
            }
        }
        log.info("└──────────────────────────────────────────┘");
    }

    private static void killPort(int port) {
        List<String> pids = findPidsByPort(port);
        if (pids.isEmpty()) {
            log.warn("  端口 {} 未找到占用进程", port);
            return;
        }

        for (String pid : pids) {
            log.warn("  kill 端口 {} 占用进程 PID {}", port, pid);
            runCommand(List.of("kill", pid), 3000);
        }
        sleep(1200);

        if (checkPort("127.0.0.1", port)) {
            List<String> remaining = findPidsByPort(port);
            for (String pid : remaining) {
                log.warn("  kill -9 端口 {} 残留进程 PID {}", port, pid);
                runCommand(List.of("kill", "-9", pid), 3000);
            }
            sleep(800);
        }

        if (checkPort("127.0.0.1", port)) {
            log.error("  端口 {} 清理失败，请手动检查: lsof -nP -iTCP:{} -sTCP:LISTEN", port, port);
        } else {
            log.info("  端口 {} 已清理", port);
        }
    }

    private static List<String> findPidsByPort(int port) {
        String output = runCommand(List.of("lsof", "-tiTCP:" + port, "-sTCP:LISTEN"), 3000);
        if (output.isBlank()) {
            return List.of();
        }
        return Arrays.stream(output.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    private static String runCommand(List<String> command, long timeoutMillis) {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "";
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
                return sb.toString().trim();
            }
        } catch (Exception e) {
            log.warn("  执行命令失败 {}: {}", command, e.getMessage());
            return "";
        }
    }

    private static void printDashboard() {
        log.info("");
        log.info("╔════════════════════════════════════════════════╗");
        log.info("║         CRM 服务仪表盘                        ║");
        log.info("╠══════╤═══════════════╤══════╤══════════════════╣");
        log.info("║ 序号 │ 服务          │ 端口 │ 状态             ║");
        log.info("╟──────┼───────────────┼──────┼──────────────────╢");
        for (int i = 0; i < SERVICES.size(); i++) {
            ServiceDef svc = SERVICES.get(i);
            boolean running = checkPort("127.0.0.1", svc.port);
            String status = running ? "运行中 ✓" : "未启动 ✗";
            log.info(String.format("║ %-4d │ %-13s │ %-4d │ %-16s ║", i + 1, svc.module, svc.port, status));
        }
        log.info("╚══════╧═══════════════╧══════╧══════════════════╝");
        log.info("Sentinel 控制台: http://localhost:{}", SENTINEL_DASHBOARD_PORT);
        log.info("Sentinel 账号: {}", getEnvOrDefault("SENTINEL_DASHBOARD_USERNAME", DEFAULT_SENTINEL_DASHBOARD_USERNAME));
        log.info("网关地址: http://localhost:8090");
        log.info("前端地址: http://localhost:3000，端口占用时可用 http://localhost:3007");
    }

    private static String resolveJavaHome(Map<String, String> env) {
        String configured = env.get("CRM_STARTER_JAVA_HOME");
        if (configured != null && new File(configured).isDirectory()) {
            return configured;
        }
        if (new File(DEFAULT_JAVA_HOME).isDirectory()) {
            return DEFAULT_JAVA_HOME;
        }
        return env.getOrDefault("JAVA_HOME", System.getProperty("java.home"));
    }

    private static String resolveProjectRoot() {
        File current = new File("").getAbsoluteFile();
        if ("crm-starter".equals(current.getName())) {
            return current.getParentFile().getAbsolutePath();
        }
        return current.getAbsolutePath();
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static boolean checkPort(String host, int port) {
        try (java.net.Socket s = new java.net.Socket()) {
            s.connect(new java.net.InetSocketAddress(host, port), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 服务定义 */
    record ServiceDef(String module, String mainClass, int port, int order, boolean sentinelDashboard) {
        ServiceDef(String module, String mainClass, int port, int order) {
            this(module, mainClass, port, order, false);
        }
    }
}
